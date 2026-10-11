package com.khuongnd.dexkids.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Handler
import android.os.Looper
import com.khuongnd.dexkids.story.EntertainmentDirector
import com.khuongnd.dexkids.story.OfflineLearningCatalog
import com.khuongnd.dexkids.story.TripQuestionPack
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

/**
 * Foreground-only orchestration. Never blocks GL/UI. The current pack continues playing while
 * the next pack is fetched; interrupted/stale callbacks cannot replace it.
 */
internal class TripPackCoordinator(
    context: Context, private val catalog: OfflineLearningCatalog,
    private val audience: EntertainmentDirector.Audience,
    private val isSessionActive: () -> Boolean
) {
    private val app = context.applicationContext
    private val store = TripPackStore(app,catalog,audience)
    private val main = Handler(Looper.getMainLooper())
    private val epoch = AtomicInteger(0)
    private var thread: Thread? = null
    private var loaded = false
    private var firstStart = true
    private var initialIssued = false
    private var requestOnRefill = false
    // All fields below are confined to the Android main thread.
    private var active: TripPackStore.Snapshot? = null
    private var deck = emptyList<TripQuestionPack.Item>()
    private var cursor = 0
    private var round = 0
    private var pending: TripQuestionPack.Item? = null
    private var pendingId: String? = null

    /** Called only from KidsActivity.onResume; a recreation is never a NEW trip. */
    fun resume(isNewTrip: Boolean) {
        if (!isSessionActive()) return
        if (firstStart) {
            firstStart = false
            val fetch = isNewTrip && !initialIssued
            initialIssued = initialIssued || fetch
            runWorker(true,fetch)
        } else if (!loaded && thread == null) {
            runWorker(true,false)
        }
    }

    fun pause() {
        epoch.incrementAndGet()
        thread?.interrupt()
        thread = null
        pending = null
        pendingId = null
        // Keep the loaded, last-good pack for offline playback on resumed Activity.
    }

    fun clearSavedPacks() = store.clear()

    private fun online(): Boolean {
        val cm = app.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val network = cm.activeNetwork ?: return false
        return cm.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun eligible(): Boolean {
        val settings=KidsAiSettings(app)
        return isSessionActive() && !Thread.currentThread().isInterrupted &&
            settings.enabled && settings.automaticPacks && online()
    }

    private fun runWorker(loadFirst: Boolean, fetchNew: Boolean) {
        if (thread?.isAlive == true) return
        val ticket=epoch.incrementAndGet()
        thread=Thread({
            try {
                if (loadFirst) {
                    val previous=store.load()
                    main.post {
                        if (ticket == epoch.get() && isSessionActive()) {
                            loaded=true
                            if (previous!=null) install(previous)
                        }
                    }
                }
                if (!fetchNew || !eligible() || catalog.count(OfflineLearningCatalog.Kind.QUIZ)<20)
                    return@Thread
                val gateway=KidsAiGateway(app)
                if (!gateway.ready()) return@Thread
                // Initial + 5 retries. All *physical* HTTP attempts pass through shared limiter.
                val backoffs=longArrayOf(5_000,15_000,35_000,75_000,150_000)
                for (attempt in 0..5) {
                    if (ticket!=epoch.get() || !eligible()) break
                    try {
                        val proposed=gateway.generateTripPack(catalog,audience,store.recent())
                        if (ticket!=epoch.get() || !eligible()) break
                        val saved=store.swap(proposed)
                        main.post {
                            if (ticket==epoch.get() && isSessionActive() &&
                                KidsAiSettings(app).automaticPacks &&
                                KidsAiSettings(app).enabled)
                                install(saved)
                        }
                        break
                    } catch (failure: Exception) {
                        if (ticket!=epoch.get() || !eligible() || !retryable(failure) || attempt==5)
                            break
                        val limitDelay=(failure as? KidsAiRetryAfterException)?.retryAfterMs ?: 0L
                        Thread.sleep(maxOf(backoffs[attempt],limitDelay))
                    }
                }
            } catch (_: InterruptedException) {
                // Exit promptly without modifying the last good pack.
            } finally {
                // Avoid clearing the reference of a newer worker after pause/resume.
                val me=Thread.currentThread()
                main.post { if (thread===me) thread=null }
            }
        }, "dexkids-trip-pack").also { it.isDaemon=true; it.start() }
    }

    private fun retryable(error: Exception): Boolean {
        if (error is KidsAiRetryAfterException) return true
        if (error is IOException) {
            return error.message !in listOf("AI_PROVIDER_REFUSED","AI_DISABLED",
                "NO_PROVIDER","AI_RATE_PERSIST_FAILED")
        }
        // A malformed / non-curated pack may be retried within the same cycle.
        return error is IllegalArgumentException || error is org.json.JSONException
    }

    private fun install(value: TripPackStore.Snapshot) {
        active=value
        deck=value.pack.items()
        cursor=value.cursor
        round=0
        pending=null
        pendingId=null
    }

    /** Call on main thread at an actual learning turn, not on every rendered frame. */
    fun nextBeat(): EntertainmentDirector.Beat? {
        if (!isSessionActive()) return null
        val snapshot=active ?: return null
        if (pending != null) return null
        if (deck.isEmpty()) return null
        if (cursor>=deck.size) {
            round++
            // A refill trigger occurs only on the completed-pack transition.
            if (!requestOnRefill && thread?.isAlive != true) {
                requestOnRefill=true
                runWorker(false,true)
            }
            deck=replayOrder(snapshot.pack.items(),store.recent().takeLast(5))
            cursor=0
        }
        val item=deck[cursor]
        val question=catalog.cards().firstOrNull { it.id()==item.quizId() } ?: return null
        val word=catalog.cards().firstOrNull { it.id()==item.wordId() } ?: return null
        val beatId="trip-${round}-${cursor}-${item.quizId()}"
        pending=item
        pendingId=beatId
        // Trusted question, answer and translated word come from the offline reviewed TSV.
        return EntertainmentDirector.Beat(beatId,item.focus(),
            question.promptVi(),question.answerVi()+" "+word.answerVi(),
            EntertainmentDirector.Reaction.WAVE,word.englishWord())
    }

    /** Advance only AFTER both the spoken question and its approved answer have been delivered. */
    fun complete(beatId: String) {
        val current=active ?: return
        if (pendingId!=beatId) return
        store.remember(pending!!.quizId())
        pending=null
        pendingId=null
        cursor++
        if (round==0) store.markConsumed(current.fingerprint,cursor)
        if (cursor>=deck.size) {
            // Refill once per exhausted full round; no new calls on every beat/frame.
            requestOnRefill=false
        }
    }

    fun cancelPending() {
        pending=null
        pendingId=null
    }

    private fun replayOrder(items: List<TripQuestionPack.Item>, last: List<String>):
        List<TripQuestionPack.Item> {
        // Keep an incomplete trailing BOTH unit at the end. Otherwise a replay of
        // 16/17/19/20 cards can start with ONG/BOTH instead of age-5 SAU.
        val chunks=if(audience==EntertainmentDirector.Audience.BOTH)
            items.chunked(3) else items.map { listOf(it) }
        val complete=if(audience==EntertainmentDirector.Audience.BOTH)
            chunks.filter { it.size==3 } else chunks
        val tail=if(audience==EntertainmentDirector.Audience.BOTH)
            chunks.filter { it.size<3 }.flatten() else emptyList()
        // Avoid the most recently heard questions when enough alternatives exist.
        return complete.shuffled().sortedBy { group ->
            group.count { it.quizId() in last }
        }.flatMap { it } + tail
    }
}
