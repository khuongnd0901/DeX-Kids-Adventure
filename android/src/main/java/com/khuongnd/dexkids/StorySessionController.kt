package com.khuongnd.dexkids

import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import com.khuongnd.dexkids.story.StoryRepository
import com.khuongnd.dexkids.story.StoryRemoteSync
import com.khuongnd.dexkids.story.adventure.*
import java.util.concurrent.Executors

/** One UI-owned speech clock; private storage is serialized on a worker. No AI dependency. */
internal class StorySessionController(
    private val host: KidsActivity,
    private val show: (StoryPlaybackDirector.Speech) -> Unit,
    private val speak: (String, () -> Unit) -> Boolean,
    private val cancelAudio: () -> Unit,
    private val listen: (StoryEpisode.Question, (Boolean) -> Unit) -> Boolean,
    private val audioBusy: () -> Boolean,
    private val ended: () -> Unit,
    private val clearVisual: () -> Unit
) {
    val director = StoryPlaybackDirector(3, 5)
    private val main = Handler(Looper.getMainLooper())
    private val worker = Executors.newSingleThreadExecutor()
    private var generation = 0L
    private var disposed = false
    private var paused = false
    private var loading = false
    private var resumeChecking = false
    private var resumeAt = 0L
    private var waitStarted = 0L
    private var listenSequence = 0L
    private var activeSpeech: StoryPlaybackDirector.Speech? = null
    var selectedId: String? = null
        private set
    val active: Boolean get() = loading || director.state() !in setOf(
        StoryPlaybackDirector.State.IDLE, StoryPlaybackDirector.State.COMPLETE)

    @JvmOverloads
    fun load(id: String?, automatic: Boolean = false, interlude: Boolean = false) {
        if (disposed || loading || (automatic && active)) return
        stop()
        loading = true
        val epoch = generation
        val audience = StoryEpisode.Audience.valueOf(ParentSettings(host).audienceMode)
        worker.execute {
            val loaded = runCatching {
                StoryRemoteSync.acquire(this)
                StoryRepository(host).use { repo ->
                    val builtin = if (id != null) BuiltinInterludes.find(id, audience) else null
                    if (builtin != null) return@use Triple(builtin, repo.cursor(builtin.id(), builtin.revision()), builtin.id())
                    val entry = if (id != null) repo.get(id) else repo.next(audience)
                    if (entry == null && interlude) {
                        val prefs = host.getSharedPreferences("learning_source_v1", 0)
                        val index = prefs.getInt("builtin_story_index", 0)
                        val story = BuiltinInterludes.at(audience, index)
                        prefs.edit().putInt("builtin_story_index", (index+1)%3).apply()
                        return@use Triple(story, 0, story.id())
                    }
                    if (entry == null || !entry.approved || entry.deleted || entry.episode.audience() != audience || !repo.selectable(entry.episode.id()))
                        null
                    else {
                        repo.markPlayed(entry.episode.id())
                        Triple(entry.episode, repo.cursor(entry.episode.id(), entry.episode.revision()), entry.episode.id())
                    }
                }
            }.getOrNull()
            main.post {
                if (disposed || epoch != generation) return@post
                loading = false
                if (loaded == null) { worker.execute { StoryRemoteSync.release(host.applicationContext,this) }; ended(); return@post }
                selectedId = loaded.third
                val packed = loaded.second
                val cursor = StoryPlaybackDirector.Cursor(packed / 1000, packed % 1000 / 10, packed % 10)
                runCatching { director.restore(loaded.first, cursor) }.onFailure { director.start(loaded.first) }
                director.pause()
                if (!paused) { paused = true; resume() }
            }
        }
    }
    private fun saveCursor(completed: Boolean = false) {
        val episode = director.episode() ?: return
        val c = director.cursor()
        val packed = if (completed) 0 else c.scene()*1000+c.beat()*10+c.phase()
        worker.execute { runCatching { StoryRepository(host).use { repo ->
            repo.cursor(episode.id(), episode.revision(), packed)
            if (completed) repo.markPlayed(episode.id())
        } } }
    }
    private fun pump() {
        if (paused || disposed) return
        saveCursor()
        when (val event = director.next()) {
            is StoryPlaybackDirector.Speech -> {
                activeSpeech = event
                show(event)
                val epoch = generation
                val started = speak(event.text()) {
                    if (epoch == generation && !paused && director.speechCompleted(event.token())) {
                        activeSpeech = null
                        pump()
                    }
                }
                if (!started) {
                    // Silent read-along has its own dwell; this is not a guessed TTS completion.
                    main.postDelayed({
                        if (epoch == generation && !paused && director.speechFailed(event.token())) {
                            activeSpeech = null; pump()
                        }
                    }, 6500L)
                } else main.postDelayed({
                    // Missing engine callback pauses playback; never start mic on an invented completion.
                    if (epoch == generation && activeSpeech?.token() == event.token() && !paused) pause()
                }, 45000L)
            }
            is StoryPlaybackDirector.Listen -> {
                waitStarted = SystemClock.elapsedRealtime()
                val epoch = generation
                val ticket = ++listenSequence
                // A listening deadline must not depend on the one-second scene polling tick.
                // The ticket prevents an old timeout from answering a later question.
                main.postDelayed({
                    if (epoch == generation && ticket == listenSequence && !paused && !disposed &&
                        director.state() == StoryPlaybackDirector.State.LISTENING && director.answer(false)) {
                        cancelAudio()
                        pump()
                    }
                }, event.maxWaitSeconds() * 1000L)
                val q = director.episode().scenes()[director.cursor().scene()].question()
                listen(q) { recognized ->
                    if (epoch == generation && ticket == listenSequence && !paused && recognized && director.answer(true)) pump()
                    // Empty/unmatched recognition does not reveal the answer before the thinking window.
                }
            }
            is StoryPlaybackDirector.Finished -> {
                activeSpeech = null; clearVisual(); saveCursor(true); worker.execute { StoryRemoteSync.release(host.applicationContext,this) }; ended()
            }
            else -> Unit
        }
    }
    fun tick() {
        if (paused && resumeAt > 0 && SystemClock.elapsedRealtime() >= resumeAt && !audioBusy()) resume()
        if (!paused && director.state() == StoryPlaybackDirector.State.LISTENING) {
            val now = SystemClock.elapsedRealtime()
            val elapsed = (now-waitStarted).coerceAtLeast(0)/1000.0
            waitStarted = now
            if (director.tickListening(elapsed)) { cancelAudio(); pump() }
        }
    }
    fun pause(until: Long = 0L) {
        paused = true; resumeAt = until; resumeChecking = false; if (!loading) generation++; activeSpeech = null
        director.pause(); cancelAudio(); saveCursor()
    }
    fun resume() {
        if (disposed || !paused || resumeChecking) return
        resumeAt = 0L
        if (loading) { paused = false; return }
        val episode = director.episode()
        if (episode == null) { paused = false; return }
        val id = selectedId ?: return
        val revision = episode.revision()
        val audience = ParentSettings(host).audienceMode
        val epoch = ++generation
        resumeChecking = true
        worker.execute {
            val valid = runCatching { StoryRepository(host).use { repo ->
                val builtin = BuiltinInterludes.find(id, StoryEpisode.Audience.valueOf(audience))
                if (builtin != null) return@use builtin.revision() == revision
                val entry = repo.get(id)
                entry != null && entry.approved && !entry.deleted &&
                    entry.episode.revision() == revision && entry.episode.audience().name == audience
            } }.getOrDefault(false)
            main.post {
                if (disposed || epoch != generation) return@post
                resumeChecking = false
                if (!valid || selectedId != id || director.episode()?.revision() != revision) {
                    director.stop(); stop(); paused = false; ended()
                } else if (paused) {
                    paused = false; director.resume(); pump()
                }
            }
        }
    }
    fun stop() {
        generation++; loading = false; resumeChecking = false; activeSpeech = null; cancelAudio()
        if (active) saveCursor()
        director.stop(); selectedId = null; clearVisual()
        worker.execute { StoryRemoteSync.release(host.applicationContext,this) }
    }
    fun close() { if (disposed) return; stop(); disposed=true; worker.shutdown(); main.removeCallbacksAndMessages(null) }
}
