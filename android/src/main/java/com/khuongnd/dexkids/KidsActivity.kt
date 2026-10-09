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
    private var narrator: OfflineVietnameseNarrator? = null
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

    private fun narrateIfApproved(text: String) {
        if (parentMenuOpen || isFinishing || isDestroyed ||
            !ParentSettings(this).allowOfflineSpeech) return
        val voice = narrator ?: OfflineVietnameseNarrator(this).also {
            it.setParentApproved(true)
            narrator = it
        }
        voice.speakReviewed(text.take(240),
            onStarted = {
                speechStarted = true
                runningGame?.setNarrationActive(true)
            },
            onFinished = {
                speechStarted = false
                runningGame?.setNarrationActive(false)
            })
    }

    private fun showConversationLine(prefix: String, message: String) {
        if (parentMenuOpen || isFinishing || isDestroyed) return
        subtitleText = "$prefix · $message"
        subtitleUntilMs = android.os.SystemClock.elapsedRealtime() + 16_000L
        narrateIfApproved(message)
    }

    private fun clearTalkQueue() {
        delayedTalk.forEach(handler::removeCallbacks)
        delayedTalk.clear()
    }

    private fun queueLiveConversation(card: PoiDialogueCatalog.Dialogue) {
        clearTalkQueue()
        fun later(delayMs: Long, prefix: String, text: String) {
            val job = Runnable { showConversationLine(prefix, text) }
            delayedTalk.add(job)
            handler.postDelayed(job, delayMs)
        }
        later(16_000L, "ĐỐ VUI · Con thử trả lời", card.quiz())
        later(32_000L, "ĐÁP ÁN", card.answer())
        later(48_000L, "HỎI CHUYỆN · Con có thể kể", card.chat())
    }

    private val updatePoiNativeStatus = object : Runnable {
        override fun run() {
            // Live GPS narration is produced ONLY after real accepted GPS fixes
            // trigger the sourced OSM proximity engine. No timer fakes an arrival.
            val cue = runningGame?.pollNarrationCue()
            val nowElapsed = android.os.SystemClock.elapsedRealtime()
            if (cue != null && !parentMenuOpen) {
                val live = liveFeed != null
                lastNearbyStoryAt = nowElapsed
                nextGeneralQuestionAt = nowElapsed + 300_000L
                val intro = if (live)
                    "Có thể xe đang ở gần một địa danh trên bản đồ. " + cue.textVi()
                else "Giới thiệu POI mô phỏng. " + cue.textVi()
                showConversationLine(if (live) "GPS GẦN ĐỊA DANH · ƯỚC TÍNH" else "MẪU THUYẾT MINH", intro)
                runningGame?.pollPoiDialogue()?.let { if (live) queueLiveConversation(it) }
            }
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
                showConversationLine("CÂU HỎI VUI · KHÔNG THEO GPS",
                    questions[generalQuestionIndex++ % questions.size])
                nextGeneralQuestionAt = nowElapsed + 300_000L
            }
            // Time-based knowledge cards are allowed in explicit DEMO preview only;
            // real GPS journeys must NEVER call it an arrival announcement.
            if (liveFeed == null && cue == null && !parentMenuOpen &&
                routeCards.isNotEmpty() && nowElapsed >= routeNextAtElapsed) {
                val card = routeCards[routeCardIndex % routeCards.size]
                routeCardIndex++
                routeNextAtElapsed = nowElapsed + 90_000L
                showConversationLine("DEMO · CHỦ ĐỀ KHÔNG ĐỊNH VỊ",
                    card.title() + ". " + card.textVi())
            }
            val poiText = runningGame?.poiStatusText().orEmpty()
            val activeSubtitle = subtitleText?.takeIf {
                speechStarted || nowElapsed < subtitleUntilMs
            }.orEmpty()
            val routeWarning = routeTitle?.let {
                if (liveFeed != null) "GPS THẬT · TUYẾN $it · POI GẦN VỊ TRÍ XE CHỈ LÀ ƯỚC TÍNH"
                else "TUYẾN $it · DEMO KHÔNG THEO GPS"
            }.orEmpty()
            poiNativeStatus?.text = listOf(routeWarning,poiText,activeSubtitle)
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
        if (selectedRouteId != null) {
            if (!RouteKnowledgeCatalog.isSupportedRoute(selectedRouteId)) {
                finish(); return
            }
            routeCards = try {
                assets.open("routes/knowledge.tsv").use { RouteKnowledgeCatalog.parse(it) }
                    .cardsFor(selectedRouteId, ParentSettings(this).ageGroup)
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
            intent.getBooleanExtra(EXTRA_HCM_SAMPLE, false), ParentSettings(this).ageGroup)
        runningGame = game
        game.setAudioOnly(ParentSettings(this).audioOnly)
        initialize(game, config)
        installParentControls()
        installPoiNativeOverlay()
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
        handler.removeCallbacks(refreshReplayStatus)
        handler.removeCallbacks(updatePoiNativeStatus)
        gps?.stop()
        clearTalkQueue()
        narrator?.stop()
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
        gps?.stop()
        clearTalkQueue()
        narrator?.stop()
        speechStarted = false
        runningGame?.setNarrationActive(false)
        val settings = ParentSettings(this)
        val choices = arrayOf(
            if (settings.audioOnly) "Show animated journey" else "Audio-only (minimal visuals)"
        )
        val dialog = AlertDialog.Builder(this)
            .setTitle("Parent controls")
            .setMessage("Adventure paused here. No screen lock or PIN.")
            .setItems(choices) { _, selection ->
                when (selection) {
                    0 -> {
                        settings.audioOnly = !settings.audioOnly
                        runningGame?.setAudioOnly(settings.audioOnly)
                    }
                }
            }
            .setNegativeButton("Continue") { _, _ -> }
            .setPositiveButton("End adventure") { _, _ -> finish() }
            .setOnDismissListener {
                parentDialog = null
                parentMenuOpen = false
                runningGame?.setParentMenuOpen(false)
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
        gpxLoadThread?.interrupt()
        parentDialog?.setOnDismissListener(null)
        parentDialog?.dismiss()
        parentDialog = null
        narrator?.shutdown()
        narrator = null
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
