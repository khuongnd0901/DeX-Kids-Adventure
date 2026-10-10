package com.khuongnd.dexkids

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.app.AlertDialog
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.widget.LinearLayout
import android.widget.TextView
import android.view.View
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.os.Handler
import android.os.Looper
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.khuongnd.dexkids.game.KidsGame
import com.khuongnd.dexkids.story.RouteKnowledgeCatalog
import com.khuongnd.dexkids.story.PoiDialogueCatalog
import com.khuongnd.dexkids.story.ChildAnswerInterpreter
import com.khuongnd.dexkids.story.EntertainmentDirector
import com.khuongnd.dexkids.story.ChildEngagementMetrics
import com.khuongnd.dexkids.ai.KidsAiSettings
import com.khuongnd.dexkids.ai.KidsAiQuizCache
import com.khuongnd.dexkids.ai.KidsAiGateway
import com.khuongnd.dexkids.ai.KidsAiSafety
import com.khuongnd.dexkids.ai.KidsQuiz
import com.khuongnd.dexkids.journey.JourneyFeed
import com.khuongnd.dexkids.journey.DemoJourneyFeed
import com.khuongnd.dexkids.journey.LiveJourneyFeed
import com.khuongnd.dexkids.journey.DeferredGpxJourneyFeed
import com.khuongnd.dexkids.journey.GpxReplayFeed
import com.khuongnd.dexkids.journey.BoundedGpxInputStream
import java.io.IOException
import com.badlogic.gdx.Gdx

/** Game and protected parent controls share one display. Touchscreen not required. */
class KidsActivity : AndroidApplication() {
    private val handler = Handler(Looper.getMainLooper())
    private var runningGame: KidsGame? = null
    private var parentMenuOpen = false
    private var parentDialog: AlertDialog? = null
    private var sessionDeadlineElapsed = 0L
    private val sessionStop = Runnable {
        android.util.Log.i("KidsSession", "event=limit_reached")
        finish()
    }
    private var gps: AndroidGpsSource? = null
    private var liveFeed: LiveJourneyFeed? = null
    private var journeyFeed: JourneyFeed? = null
    private var gpxLoadThread: Thread? = null
    private var replayStatus: TextView? = null
    private var poiNativeStatus: TextView? = null
    private var entertainmentBanner: TextView? = null
    private var entertainmentBannerUntilMs = 0L
    private var narrator: OfflineVietnameseNarrator? = null
    private var soundscape: KidSoundscape? = null
    private val engagementMetrics = ChildEngagementMetrics()
    private var subtitleUntilMs = 0L
    private var subtitleText: String? = null
    private var speechStarted = false
    private var routeCards: List<RouteKnowledgeCatalog.Card> = emptyList()
    private var routeTitle: String? = null
    private var routeCardIndex = 0
    private var routeNextAtElapsed = 0L
    private val delayedTalk = mutableListOf<Runnable>()
    private var nextGeneralQuestionAt = android.os.SystemClock.elapsedRealtime() + 300_000L
    private var lastNearbyStoryAt = 0L
    private var generalQuestionIndex = 0
    private val entertainmentDirector = EntertainmentDirector()
    private var nextEntertainmentAtElapsed = android.os.SystemClock.elapsedRealtime() +
        EntertainmentDirector.FIRST_BEAT_DELAY_MS

    private var childSpeech: OnDeviceChildSpeech? = null
    private var talkEpoch = 0L
    private var microphoneStatus = ""
    private val aiCache by lazy { KidsAiQuizCache(this) }
    private val aiGateway by lazy { KidsAiGateway(this) }
    private var aiWarmThread: Thread? = null
    private var aiAnswerThread: Thread? = null
    private var currentSourcedFact = ""
    private var currentPoiId: String? = null

    private fun canListenToChild(): Boolean =
        liveFeed != null && !parentMenuOpen && !isFinishing && !isDestroyed &&
            ParentSettings(this).allowChildMicrophone &&
            checkSelfPermission(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED &&
            OnDeviceChildSpeech.available(this)

    /**
     * Speech and listening never overlap: the recognizer is opened only AFTER
     * the actual local TTS completion (or after a no-TTS visual question delay).
     */
    private fun narrateIfApproved(text: String, onFinished: (() -> Unit)? = null): Boolean {
        if (parentMenuOpen || isFinishing || isDestroyed ||
            !ParentSettings(this).allowOfflineSpeech) return false
        val voice = narrator ?: OfflineVietnameseNarrator(this).also {
            it.setParentApproved(true)
            narrator = it
        }
        val epoch = talkEpoch
        soundscape?.setSpeechActive(true)
        val started = voice.speakReviewed(text.take(240),
            onStarted = {
                if (epoch == talkEpoch && !parentMenuOpen) {
                    speechStarted = true
                    runningGame?.setNarrationActive(true)
                }
            },
            onFinished = {
                if (epoch == talkEpoch && !isFinishing && !isDestroyed) {
                    speechStarted = false
                    runningGame?.setNarrationActive(false)
                    soundscape?.setSpeechActive(false)
                    onFinished?.invoke()
                }
            },
            onInterrupted = {
                if (epoch == talkEpoch && !parentMenuOpen && !isFinishing && !isDestroyed) {
                    speechStarted = false
                    runningGame?.setNarrationActive(false)
                    soundscape?.setSpeechActive(false)
                    engagementMetrics.cancelBeat()
                    clearTalkQueue()
                    nextEntertainmentAtElapsed = maxOf(nextEntertainmentAtElapsed,
                        android.os.SystemClock.elapsedRealtime() + 30_000L)
                }
            })
        if (!started) soundscape?.setSpeechActive(false)
        return started
    }

    private fun showConversationLine(prefix: String, message: String) {
        if (parentMenuOpen || isFinishing || isDestroyed) return
        subtitleText = "$prefix · $message"
        subtitleUntilMs = android.os.SystemClock.elapsedRealtime() + 16_000L
        narrateIfApproved(message)
    }

    private fun clearTalkQueue() {
        talkEpoch++
        delayedTalk.forEach(handler::removeCallbacks)
        delayedTalk.clear()
        childSpeech?.cancel()
        aiAnswerThread?.interrupt()
        aiAnswerThread = null
        aiWarmThread?.interrupt()
        aiWarmThread = null
        microphoneStatus = ""
    }

    private fun runLater(ms: Long, task: () -> Unit) {
        val generation = talkEpoch
        val job = Runnable {
            if (talkEpoch == generation && !parentMenuOpen && !isFinishing && !isDestroyed)
                task()
        }
        delayedTalk.add(job)
        handler.postDelayed(job, ms)
    }

    private fun askAndListen(
        question: String,
        label: String,
        expectedAnswer: String? = null,
        followUp: String? = null
    ) {
        if (!canListenToChild()) {
            showConversationLine(label, question)
            if (expectedAnswer != null) runLater(16_000L) {
                showConversationLine("ĐÁP ÁN", expectedAnswer)
            }
            if (followUp != null) runLater(32_000L) {
                showConversationLine("HỎI CHUYỆN", followUp)
            }
            return
        }
        val generation = talkEpoch
        subtitleText = "$label · $question"
        subtitleUntilMs = android.os.SystemClock.elapsedRealtime() + 16_000L
        microphoneStatus = "CAPYBARA ĐANG HỎI · MICRO CHƯA BẬT"
        val afterSpeech = {
            if (generation == talkEpoch && canListenToChild()) {
                runLater(400L) {
                    if (!canListenToChild()) return@runLater
                    microphoneStatus = "MICRO ĐANG NGHE BÉ · chỉ xử lý offline, tối đa 9 giây"
                    val mic = childSpeech ?: OnDeviceChildSpeech(this).also { childSpeech = it }
                    if (!mic.listenOnce { heard ->
                        if (generation != talkEpoch || !canListenToChild()) return@listenOnce
                        microphoneStatus = ""
                        val reply = if (expectedAnswer != null)
                            ChildAnswerInterpreter.quiz(heard, expectedAnswer)
                        else ChildAnswerInterpreter.chat(heard)
                        val fallback = {
                            if (generation == talkEpoch && !parentMenuOpen && !isFinishing) {
                                showConversationLine("CAPYBARA PHẢN HỒI OFFLINE", reply.textVi())
                                if (followUp != null)
                                    runLater(13_000L) {
                                        askAndListen(followUp, "CAPYBARA HỎI TIẾP")
                                    }
                            }
                        }
                        val settings = KidsAiSettings(this)
                        val safeSpeech = KidsAiSafety.childCloudInput(heard)
                        val fact = currentSourcedFact
                        if (settings.enabled && settings.cloudChildReply &&
                            safeSpeech != null && fact.length in 20..240) {
                            val originalQuestion = question
                            aiAnswerThread = Thread({
                                val result = runCatching {
                                    // Recheck opt-in immediately before any HTTPS call.
                                    val current = KidsAiSettings(this)
                                    check(current.enabled && current.cloudChildReply &&
                                        !Thread.currentThread().isInterrupted)
                                    aiGateway.childReply(originalQuestion,fact,safeSpeech)
                                }.getOrNull()
                                runOnUiThread {
                                    if (generation != talkEpoch || isFinishing || isDestroyed ||
                                        parentMenuOpen) return@runOnUiThread
                                    if (result != null && KidsAiSettings(this).cloudChildReply) {
                                        showConversationLine("CAPYBARA AI", result.text)
                                        runLater(13_000L) {
                                            askAndListen(result.nextQuestion, "CAPYBARA HỎI TIẾP")
                                        }
                                    } else fallback()
                                }
                            }, "dexkids-ai-child-reply").also { it.start() }
                        } else fallback()
                    }) {
                        microphoneStatus = "Chưa có dịch vụ nhận dạng tiếng Việt trên thiết bị"
                        if (expectedAnswer != null) runLater(3_000L) {
                            showConversationLine("ĐÁP ÁN", expectedAnswer)
                        }
                    }
                }
            }
        }
        val speechStartedSuccessfully = narrateIfApproved(question, afterSpeech)
        if (!speechStartedSuccessfully) runLater(1_500L) { afterSpeech() }
    }

    private fun showEntertainmentBanner(line: String) {
        entertainmentBanner?.apply {
            text = line
            visibility = View.VISIBLE
        }
        entertainmentBannerUntilMs = android.os.SystemClock.elapsedRealtime() + 16_000L
    }

    /** Passive preschool episodes: no microphone, GPS or AI key required. */
    private fun playEntertainment(beat: EntertainmentDirector.Beat) {
        val selected = EntertainmentDirector.Audience.fromId(ParentSettings(this).audienceMode)
        // COMMON is neutral authored content, not a second child in SOLO mode.
        val focus = if (selected != EntertainmentDirector.Audience.BOTH &&
            beat.id().startsWith("common-")) selected else beat.focus()
        if (!engagementMetrics.recordBeatStart(focus)) return
        clearTalkQueue()
        val currentEpoch = talkEpoch
        val audienceName = when (focus) {
            EntertainmentDirector.Audience.SAU -> "DÀNH CHO SÂU"
            EntertainmentDirector.Audience.ONG -> "DÀNH CHO ONG"
            else -> "CÙNG CHƠI"
        }
        subtitleText = "CAPYBARA · $audienceName · ${beat.introduction()}"
        showEntertainmentBanner(beat.introduction())
        subtitleUntilMs = android.os.SystemClock.elapsedRealtime() + 16_000L
        val gestureThenAnswer = {
            if (talkEpoch == currentEpoch && !parentMenuOpen && !isFinishing && !isDestroyed) {
                runningGame?.showEntertainmentReaction(beat.reaction().name)
                soundscape?.playForBeat(beat.id())
                runLater(2_000L) {
                    engagementMetrics.completeBeat()
                    showEntertainmentBanner(beat.resolution())
                    showConversationLine("CAPYBARA · ĐẾN LƯỢT MÌNH", beat.resolution())
                }
            }
        }
        if (!narrateIfApproved(beat.introduction(), gestureThenAnswer)) {
            engagementMetrics.recordVoiceUnavailable()
            runLater(9_000L) { gestureThenAnswer() }
        } else {
            // Some device TTS engines never invoke onDone/onError: keep the game moving.
            runLater(25_000L) {
                if (engagementMetrics.hasPendingBeat()) {
                    narrator?.stop()
                    speechStarted = false
                    soundscape?.setSpeechActive(false)
                    runningGame?.setNarrationActive(false)
                    engagementMetrics.recordVoiceUnavailable()
                    gestureThenAnswer()
                }
            }
        }
    }

    private fun queueLiveConversation(card: PoiDialogueCatalog.Dialogue, fact: String) {
        clearTalkQueue()
        currentPoiId = card.poiId()
        currentSourcedFact = fact
        val base = KidsQuiz(card.quiz(),card.answer(),card.chat())
        val cfg = KidsAiSettings(this)
        val age = ParentSettings(this).activeAge
        val selected = if (cfg.enabled)
            aiCache.next(card.poiId(),age,fact,base) ?: base else base
        // Nonblocking prefetch for a later trip when parent enabled AI but no cached pack.
        if (cfg.enabled && selected === base && aiWarmThread?.isAlive != true) {
            val gateway = aiGateway
            if (gateway.ready()) {
                aiWarmThread = Thread({
                    runCatching {
                        val cards = gateway.generate(card.poiId(),fact,base,age)
                        if (!Thread.currentThread().isInterrupted)
                            aiCache.put(card.poiId(),age,fact,base,cards)
                    } // Offline scripted fallback if provider/quota/JSON fails.
                }, "dexkids-ai-poi-warm").also { it.start() }
            }
        }
        val tag = if (selected === base) "ĐỐ VUI" else "ĐỐ VUI AI · THAM KHẢO"
        if (canListenToChild()) {
            runLater(16_000L) {
                askAndListen(selected.question, tag + " · BÉ TRẢ LỜI",
                    selected.answer,selected.followUp)
            }
        } else {
            runLater(16_000L) { showConversationLine(tag + " · Con thử trả lời", selected.question) }
            runLater(32_000L) { showConversationLine("ĐÁP ÁN", selected.answer) }
            runLater(48_000L) { showConversationLine("HỎI CHUYỆN · Con có thể kể", selected.followUp) }
        }
    }

    private val updatePoiNativeStatus = object : Runnable {
        override fun run() {
            // Live GPS narration is produced ONLY after real accepted GPS fixes
            // trigger the sourced OSM proximity engine. No timer fakes an arrival.
            val cue = runningGame?.pollNarrationCue()
            val nowElapsed = android.os.SystemClock.elapsedRealtime()
            if (cue != null && !parentMenuOpen) {
                // Source-backed POIs outrank invented cartoon entertainment.
                engagementMetrics.interruptForPoi()
                entertainmentBanner?.visibility = View.GONE
                clearTalkQueue()
                narrator?.stop()
                soundscape?.setSpeechActive(false)
                speechStarted = false
                runningGame?.setNarrationActive(false)
                nextEntertainmentAtElapsed = nowElapsed + EntertainmentDirector.POI_PRIORITY_DELAY_MS
                val live = liveFeed != null
                lastNearbyStoryAt = nowElapsed
                nextGeneralQuestionAt = nowElapsed + 300_000L
                val intro = if (live)
                    "Có thể xe đang ở gần một địa danh trên bản đồ. " + cue.textVi()
                else "Giới thiệu POI mô phỏng. " + cue.textVi()
                showConversationLine(if (live) "GPS GẦN ĐỊA DANH · ƯỚC TÍNH" else "MẪU THUYẾT MINH", intro)
                runningGame?.pollPoiDialogue()?.let {
                    if (live) queueLiveConversation(it,cue.textVi())
                }
            }
            // Fictional fun runs independently of LIVE GPS and never asserts a nearby place.
            if (!parentMenuOpen && cue == null && !speechStarted &&
                !(liveFeed == null && routeCards.isNotEmpty() && nowElapsed >= routeNextAtElapsed) &&
                !engagementMetrics.hasPendingBeat() &&
                microphoneStatus.isEmpty() && nowElapsed >= nextEntertainmentAtElapsed) {
                val audience = EntertainmentDirector.Audience.fromId(
                    ParentSettings(this@KidsActivity).audienceMode)
                val beat = entertainmentDirector.next(audience)
                nextEntertainmentAtElapsed = nowElapsed + EntertainmentDirector.BETWEEN_BEATS_MS
                nextGeneralQuestionAt = nowElapsed + 300_000L
                playEntertainment(beat)
            }
            if (entertainmentBannerUntilMs > 0L && nowElapsed >= entertainmentBannerUntilMs)
                entertainmentBanner?.visibility = View.GONE
            // Generic questions keep children occupied during long gaps, but
            // never assert a landmark or location when nothing is nearby.
            if (liveFeed != null && !parentMenuOpen &&
                nowElapsed >= nextGeneralQuestionAt && cue == null) {
                val questions = arrayOf(
                    "Con kể tên ba loài vật mình biết nhé!",
                    "Nếu vẽ một chiếc xe buýt vui vẻ, con muốn tô màu gì?",
                    "Con có thể kể tên ba loại trái cây không?",
                    "Con thích nghe tiếng mưa hay tiếng suối chảy?",
                    "Con biết những việc gì giúp bảo vệ cây xanh?"
                )
                val prompt = questions[generalQuestionIndex++ % questions.size]
                if (canListenToChild()) {
                    clearTalkQueue()
                    currentPoiId = null
                    currentSourcedFact = "Không xác định địa danh từ GPS; đây chỉ là một câu hỏi vui chung."
                    askAndListen(prompt, "CÂU HỎI VUI · KHÔNG THEO GPS")
                } else showConversationLine("CÂU HỎI VUI · KHÔNG THEO GPS", prompt)
                nextGeneralQuestionAt = nowElapsed + 300_000L
            }
            // Time-based knowledge cards are allowed in explicit DEMO preview only;
            // real GPS journeys must NEVER call it an arrival announcement.
            if (liveFeed == null && cue == null && !parentMenuOpen &&
                routeCards.isNotEmpty() && nowElapsed >= routeNextAtElapsed) {
                engagementMetrics.cancelBeat()
                clearTalkQueue()
                narrator?.stop()
                soundscape?.setSpeechActive(false)
                speechStarted = false
                runningGame?.setNarrationActive(false)
                val card = routeCards[routeCardIndex % routeCards.size]
                routeCardIndex++
                routeNextAtElapsed = nowElapsed + 90_000L
                showConversationLine("DEMO · CHỦ ĐỀ KHÔNG ĐỊNH VỊ",
                    card.title() + ". " + card.textVi())
                nextEntertainmentAtElapsed = maxOf(nextEntertainmentAtElapsed,
                    nowElapsed + 38_000L)
            }
            val poiText = runningGame?.poiStatusText().orEmpty()
            val activeSubtitle = subtitleText?.takeIf {
                speechStarted || nowElapsed < subtitleUntilMs
            }.orEmpty()
            val routeWarning = routeTitle?.let {
                if (liveFeed != null) "GPS THẬT · TUYẾN $it · POI GẦN VỊ TRÍ XE CHỈ LÀ ƯỚC TÍNH"
                else "TUYẾN $it · DEMO KHÔNG THEO GPS"
            }.orEmpty()
            poiNativeStatus?.text = listOf(routeWarning,poiText,activeSubtitle,microphoneStatus)
                .filter { it.isNotEmpty() }.joinToString("\n") +
                if (poiText.contains("OSM") || poiText.contains("GPS") || poiText.contains("GPX"))
                    "\n© OpenStreetMap contributors · ODbL" else ""
            handler.postDelayed(this, 1000)
        }
    }
    private var replayPauseButton: Button? = null
    private val refreshReplayStatus = object : Runnable {
        override fun run() {
            val feed = (journeyFeed as? DeferredGpxJourneyFeed)?.replay()
            if (feed == null) {
                replayStatus?.text = "Loading GPX route…"
                replayPauseButton?.isEnabled = false
                handler.postDelayed(this, 500)
                return
            }
            replayPauseButton?.isEnabled = true
            replayStatus?.text = String.format(java.util.Locale.ROOT,
                "GPX %s · %.0f/%.0f s · %.1f km",
                if (feed.finished()) "FINISHED" else if (feed.isPaused()) "PAUSED" else "PLAYING",
                feed.replayTimeSeconds(), feed.totalTimeSeconds(), feed.distanceMeters()/1000.0)
            replayPauseButton?.text = if (feed.isPaused()) "Resume" else "Pause"
            handler.postDelayed(this, 500)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wantsLive = intent.getBooleanExtra(EXTRA_LIVE_GPS, false)
        val wantsHcmSample = intent.getBooleanExtra(EXTRA_HCM_SAMPLE, false)
        val wantsReplay = intent.getBooleanExtra(EXTRA_GPX_REPLAY, false)
        val selectedRouteId = intent.getStringExtra(EXTRA_ROUTE_ID)
        if ((wantsLive && wantsReplay) || (wantsHcmSample && (wantsReplay || wantsLive || selectedRouteId != null))) {
            finish(); return
        }
        if (wantsLive && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            finish() // Never silently show demo as live GPS.
            return
        }
        val now = android.os.SystemClock.elapsedRealtime()
        entertainmentDirector.restore(savedInstanceState?.getInt("entertainment.position", 0) ?: 0)
        nextEntertainmentAtElapsed = savedInstanceState?.getLong("entertainment.next.elapsed")
            ?.takeIf { it > 0L } ?: now + EntertainmentDirector.FIRST_BEAT_DELAY_MS
        if (selectedRouteId != null) {
            if (!RouteKnowledgeCatalog.isSupportedRoute(selectedRouteId)) {
                finish(); return
            }
            routeCards = try {
                assets.open("routes/knowledge.tsv").use { RouteKnowledgeCatalog.parse(it) }
                    .cardsFor(selectedRouteId, ParentSettings(this).activeAge)
            } catch (error: IllegalArgumentException) {
                android.util.Log.e("RouteKnowledge", "Offline route catalog invalid; route session aborted")
                finish(); return
            } catch (error: java.io.IOException) {
                android.util.Log.e("RouteKnowledge", "Offline route catalog missing; route session aborted")
                finish(); return
            }
            if (routeCards.isEmpty()) { finish(); return }
            routeTitle = RouteKnowledgeCatalog.routeTitle(selectedRouteId)
            routeCardIndex = savedInstanceState?.getInt("route.card.index", 0)
                ?.coerceAtLeast(0) ?: 0
            routeNextAtElapsed = savedInstanceState?.getLong("route.next.elapsed")
                ?.takeIf { it > 0L } ?: now + 8_000L
        }
        val limitMs = ParentSettings(this).sessionMinutes * 60_000L
        sessionDeadlineElapsed = savedInstanceState?.getLong(SESSION_DEADLINE, 0L)
            ?.takeIf { it > 0 } ?: (now + limitMs)
        val remainingMs = sessionDeadlineElapsed - now
        if (remainingMs <= 0) { sessionStop.run(); return }
        KidsSessionControl.register(this)
        android.util.Log.i("KidsSession", "event=started remaining_ms=$remainingMs")
        handler.postDelayed(sessionStop, remainingMs)
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useWakelock = true
            // 2D only: RGB565 framebuffer and no depth/stencil/MSAA to reduce
            // FullHD external display memory bandwidth when supported by EGL.
            r = 5
            g = 6
            b = 5
            a = 0
            depth = 0
            stencil = 0
            numSamples = 0
        }

        when {
            wantsHcmSample -> {
                val feed = DeferredGpxJourneyFeed()
                initializeJourney(feed, config)
                loadBundledHcmSample(feed)
            }
            wantsReplay -> {
                // After Android configuration recreation, keep progress and do not reparse XML.
                val feed = (lastNonConfigurationInstance as? DeferredGpxJourneyFeed)
                    ?: DeferredGpxJourneyFeed()
                // LibGDX MUST be initialized synchronously in onCreate before onResume.
                initializeJourney(feed, config)
                if (!feed.isLoaded()) parseReplayAsync(feed)
            }
            wantsLive -> {
                val feed = (lastNonConfigurationInstance as? LiveJourneyFeed) ?: LiveJourneyFeed()
                liveFeed = feed
                gps = AndroidGpsSource(this)
                if (gps?.start(feed::accept) != true) { finish(); return }
                initializeJourney(feed, config)
            }
            else -> {
                val feed = (lastNonConfigurationInstance as? DemoJourneyFeed) ?: DemoJourneyFeed()
                initializeJourney(feed, config)
            }
        }
    }

    /** Bundled synthetic location preview, never a route recommendation. */
    private fun loadBundledHcmSample(holder: DeferredGpxJourneyFeed) {
        Thread({
            val parsed = runCatching {
                assets.open("gps/hcm-preview.gpx").use { GpxReplayFeed.fromGpx(BoundedGpxInputStream(it)) }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                parsed.onSuccess { Gdx.app?.postRunnable { holder.install(it) } }
                    .onFailure { showImportError() }
            }
        }, "dexkids-hcm-preview").also { gpxLoadThread = it; it.start() }
    }

    private fun parseReplayAsync(holder: DeferredGpxJourneyFeed) {
        val uri = intent.data
        if (uri?.scheme != ContentResolver.SCHEME_CONTENT) {
            showImportError()
            return
        }
        // Game is already initialized with a stationary placeholder feed.
        // Large XML parsing never blocks the Android UI thread.
        gpxLoadThread = Thread({
            val outcome = runCatching {
                val stream = contentResolver.openInputStream(uri)
                    ?: throw IOException("Cannot read selected GPX")
                stream.use { GpxReplayFeed.fromGpx(BoundedGpxInputStream(it)) }
            }
            runOnUiThread {
                if (isFinishing || isDestroyed) return@runOnUiThread
                outcome.onSuccess { parsed ->
                    Gdx.app?.postRunnable { holder.install(parsed) }
                }.onFailure { showImportError() }
            }
        }, "dexkids-gpx-import").also { it.start() }
    }

    private fun showImportError() {
        if (isFinishing || isDestroyed) return
        AlertDialog.Builder(this)
            .setTitle("Unable to open GPX")
            .setMessage("Select a valid local GPX track (at least 2 timed points; 4 MB maximum).")
            .setCancelable(false)
            .setPositiveButton("Return to dashboard") { _, _ -> finish() }
            .show()
    }

    private fun initializeJourney(feed: JourneyFeed, config: AndroidApplicationConfiguration) {
        journeyFeed = feed
        val game = KidsGame(feed,
            intent.getBooleanExtra(EXTRA_HCM_SAMPLE, false), ParentSettings(this).activeAge)
        runningGame = game
        game.setAudioOnly(ParentSettings(this).audioOnly)
        initialize(game, config)
        installParentControls()
        installPoiNativeOverlay()
        soundscape = KidSoundscape(this).also { it.setPaused(false) }
        if (feed is DeferredGpxJourneyFeed) installReplayControls(feed)
    }

    /** Android TextView renders Vietnamese Unicode that LibGDX's default bitmap font cannot. */
    private fun installPoiNativeOverlay() {
        val view = TextView(this).apply {
            textSize = 15f
            setPadding(12, 12, 12, 12)
            setBackgroundColor(0xBA102E3FL.toInt())
            setTextColor(android.graphics.Color.WHITE)
            contentDescription = "Offline place information and OpenStreetMap attribution"
        }
        poiNativeStatus = view
        val dp = resources.displayMetrics.density
        addContentView(view, FrameLayout.LayoutParams(
            (390 * dp).toInt().coerceAtMost(resources.displayMetrics.widthPixels),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.END
        ).apply {
            setMargins((12*dp).toInt(), (12*dp).toInt(), (12*dp).toInt(), (84*dp).toInt())
        })
        val banner = TextView(this).apply {
            textSize = 28f
            setPadding((22*dp).toInt(), (14*dp).toInt(), (22*dp).toInt(), (14*dp).toInt())
            setBackgroundColor(0xF4FFF3D2.toInt())
            setTextColor(android.graphics.Color.rgb(36, 53, 68))
            gravity = Gravity.CENTER
            maxLines = 3
            visibility = View.GONE
        }
        entertainmentBanner = banner
        addContentView(banner, FrameLayout.LayoutParams(
            (840 * dp).toInt().coerceAtMost(resources.displayMetrics.widthPixels),
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
        ).apply { setMargins((18*dp).toInt(),0,(18*dp).toInt(),(24*dp).toInt()) })
        handler.post(updatePoiNativeStatus)
    }

    private fun installReplayControls(holder: DeferredGpxJourneyFeed) {
        val controls = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(10, 10, 10, 10)
        }
        val pause = Button(this).apply {
            text = "Pause"
            setOnClickListener {
                // GPX state and distance are owned by the LibGDX render thread.
                Gdx.app?.postRunnable {
                    holder.replay()?.let { it.setPaused(!it.isPaused()) }
                }
            }
        }
        replayPauseButton = pause
        controls.addView(pause)
        controls.addView(Button(this).apply {
            text = "Restart"
            setOnClickListener { Gdx.app?.postRunnable { holder.replay()?.reset() } }
        })
        controls.addView(TextView(this).apply {
            textSize = 15f
            setPadding(14, 22, 10, 10)
            text = "GPX loading"
            replayStatus = this
        })
        val margin = (16f * resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.BOTTOM or Gravity.START
        ).apply { setMargins(margin, margin, margin, margin) }
        addContentView(controls, params)
        handler.post(refreshReplayStatus)
    }

    override fun onPause() {
        soundscape?.setPaused(true)
        if (!parentMenuOpen) engagementMetrics.recordParentPause()
        handler.removeCallbacks(refreshReplayStatus)
        handler.removeCallbacks(updatePoiNativeStatus)
        gps?.stop()
        clearTalkQueue()
        entertainmentBanner?.visibility = View.GONE
        narrator?.stop()
        soundscape?.setSpeechActive(false)
        speechStarted = false
        runningGame?.setNarrationActive(false)
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (sessionDeadlineElapsed > 0 &&
            android.os.SystemClock.elapsedRealtime() >= sessionDeadlineElapsed) {
            sessionStop.run()
            return
        }
        val feed = liveFeed
        if (feed != null && !parentMenuOpen) gps?.start(feed::accept)
        if (runningGame != null) {
            if (!parentMenuOpen) soundscape?.setPaused(false)
            handler.removeCallbacks(updatePoiNativeStatus)
            handler.post(updatePoiNativeStatus)
        }
        if (journeyFeed is DeferredGpxJourneyFeed) {
            handler.removeCallbacks(refreshReplayStatus)
            handler.post(refreshReplayStatus)
        }
    }

    @Suppress("DEPRECATION")
    override fun onRetainNonConfigurationInstance(): Any? = journeyFeed

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(SESSION_DEADLINE, sessionDeadlineElapsed)
        outState.putInt("route.card.index", routeCardIndex)
        outState.putLong("route.next.elapsed", routeNextAtElapsed)
        outState.putInt("entertainment.position", entertainmentDirector.position())
        outState.putLong("entertainment.next.elapsed", nextEntertainmentAtElapsed)
        super.onSaveInstanceState(outState)
    }


    /** Native controls on the SAME DeX display: one mouse click or F10/Menu. */
    private fun installParentControls() {
        val control = Button(this).apply {
            text = "Parents · menu"
            isAllCaps = false
            contentDescription = "Click for parent controls on this display"
            setOnClickListener { showParentMenu() }
        }
        val margin = (16f * resources.displayMetrics.density).toInt()
        val params = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.END
        ).apply { setMargins(margin, margin, margin, margin) }
        addContentView(control, params)
    }

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP &&
            (event.keyCode == KeyEvent.KEYCODE_F10 || event.keyCode == KeyEvent.KEYCODE_MENU)) {
            showParentMenu()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        showParentMenu()
    }

    /** Only accessible to the signature-protected command receiver in this package. */
    fun pauseFromTrustedController() { showParentMenu() }
    fun resumeFromTrustedController() { parentDialog?.dismiss() }

    private fun showParentMenu() {
        if (isFinishing || isDestroyed || parentMenuOpen || runningGame == null) return
        parentMenuOpen = true
        // The absolute session timeout continues while the scene/GPS are paused.
        runningGame?.setParentMenuOpen(true)
        soundscape?.setPaused(true)
        engagementMetrics.recordParentPause()
        gps?.stop()
        clearTalkQueue()
        entertainmentBanner?.visibility = View.GONE
        narrator?.stop()
        soundscape?.setSpeechActive(false)
        speechStarted = false
        runningGame?.setNarrationActive(false)
        val settings = ParentSettings(this)
        val choices = buildList {
            add(if (settings.audioOnly) "Show animated journey" else "Audio-only (minimal visuals)")
            val ai = KidsAiSettings(this@KidsActivity)
            if (ai.cloudChildReply) add("Tắt gửi câu trả lời của bé lên AI ngay")
            if (ai.enabled) add("Tắt AI Kids ngay, dùng câu đố offline")
        }.toTypedArray()
        val counts = engagementMetrics.snapshot()
        val metricText = "Phiên này: ${counts.starts()} hoạt động · " +
            "Sâu ${counts.sauBeats()} / Ong ${counts.ongBeats()} / cả hai ${counts.togetherBeats()}\n" +
            "Đã kể kết thúc ${counts.resolutions()} · POI ngắt ${counts.poiPreemptions()} · " +
            "Bị hủy khác ${counts.otherCancellations()} · TTS chưa phát ${counts.voiceUnavailable()}\n" +
            "Đây là số liệu phát nội dung, không phải thước đo hai bé có thích hay không."
        // Android AlertController can hide list choices when setMessage + setItems
        // are combined. A custom title preserves the single-display action list.
        val metricsHeading = TextView(this).apply {
            text = "Parent controls · Adventure paused\n" + metricText
            textSize = 16f
            setPadding(26, 22, 26, 18)
        }
        val dialog = AlertDialog.Builder(this)
            .setCustomTitle(metricsHeading)
            .setItems(choices) { _, selection ->
                when (selection) {
                    0 -> {
                        settings.audioOnly = !settings.audioOnly
                        runningGame?.setAudioOnly(settings.audioOnly)
                    }
                    else -> {
                        val ai = KidsAiSettings(this)
                        if (choices[selection].startsWith("Tắt gửi")) {
                            ai.cloudChildReply = false
                            aiAnswerThread?.interrupt()
                        } else if (choices[selection].startsWith("Tắt AI Kids")) {
                            ai.enabled = false
                            ai.cloudChildReply = false
                            aiAnswerThread?.interrupt()
                            aiWarmThread?.interrupt()
                        }
                    }
                }
            }
            .setNegativeButton("Continue") { _, _ -> }
            .setPositiveButton("End adventure") { _, _ -> finish() }
            .setOnDismissListener {
                parentDialog = null
                parentMenuOpen = false
                runningGame?.setParentMenuOpen(false)
                if (!isFinishing && !isDestroyed) soundscape?.setPaused(false)
                nextEntertainmentAtElapsed = maxOf(nextEntertainmentAtElapsed,
                    android.os.SystemClock.elapsedRealtime() + 8_000L)
                val feed = liveFeed
                if (!isFinishing && !isDestroyed && feed != null &&
                    android.os.SystemClock.elapsedRealtime() < sessionDeadlineElapsed) {
                    gps?.start(feed::accept)
                }
            }
            .create()
        parentDialog = dialog
        dialog.show()
    }

    override fun onDestroy() {
        handler.removeCallbacks(updatePoiNativeStatus)
        handler.removeCallbacks(refreshReplayStatus)
        clearTalkQueue()
        childSpeech?.cancel()
        childSpeech = null
        aiWarmThread?.interrupt()
        aiAnswerThread?.interrupt()
        gpxLoadThread?.interrupt()
        parentDialog?.setOnDismissListener(null)
        parentDialog?.dismiss()
        parentDialog = null
        narrator?.shutdown()
        narrator = null
        soundscape?.shutdown()
        soundscape = null
        gps?.stop()
        runningGame = null
        handler.removeCallbacks(sessionStop)
        KidsSessionControl.clear(this)
        super.onDestroy()
    }

    companion object {
        private const val SESSION_DEADLINE = "kids.session.deadline.elapsed"
        const val EXTRA_LIVE_GPS = "com.khuongnd.dexkids.extra.LIVE_GPS"
        const val EXTRA_GPX_REPLAY = "com.khuongnd.dexkids.extra.GPX_REPLAY"
        const val EXTRA_HCM_SAMPLE = "com.khuongnd.dexkids.extra.HCM_SAMPLE_PREVIEW"
        const val EXTRA_ROUTE_ID = "com.khuongnd.dexkids.extra.ROUTE_KNOWLEDGE_ID"
    }
}
