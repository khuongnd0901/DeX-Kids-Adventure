package com.khuongnd.dexkids

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.app.AlertDialog
import android.view.Gravity
import android.view.KeyEvent
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import android.os.Handler
import android.os.Looper
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.khuongnd.dexkids.game.KidsGame
import com.khuongnd.dexkids.journey.JourneyFeed
import com.khuongnd.dexkids.journey.DemoJourneyFeed
import com.khuongnd.dexkids.journey.LiveJourneyFeed

/** Game and protected parent controls share one display. Touchscreen not required. */
class KidsActivity : AndroidApplication() {
    private val handler = Handler(Looper.getMainLooper())
    private var runningGame: KidsGame? = null
    private var parentMenuOpen = false
    private var sessionDeadlineElapsed = 0L
    private val sessionStop = Runnable {
        android.util.Log.i("KidsSession", "event=limit_reached")
        finish()
    }
    private var gps: AndroidGpsSource? = null
    private var liveFeed: LiveJourneyFeed? = null
    private var journeyFeed: JourneyFeed? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wantsLive = intent.getBooleanExtra(EXTRA_LIVE_GPS, false)
        if (wantsLive && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            finish() // Never silently show demo as live GPS.
            return
        }
        val now = android.os.SystemClock.elapsedRealtime()
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
        if (wantsLive) {
            val feed = (lastNonConfigurationInstance as? LiveJourneyFeed) ?: LiveJourneyFeed()
            journeyFeed = feed
            liveFeed = feed
            gps = AndroidGpsSource(this)
            if (gps?.start(feed::accept) != true) {
                finish()
                return
            }
            val game = KidsGame(feed)
            runningGame = game
            initialize(game, config)
            installParentControls()
        } else {
            val feed = (lastNonConfigurationInstance as? DemoJourneyFeed) ?: DemoJourneyFeed()
            journeyFeed = feed
            val game = KidsGame(feed)
            runningGame = game
            initialize(game, config)
            installParentControls()
        }
    }

    override fun onPause() {
        gps?.stop()
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
    }

    @Suppress("DEPRECATION")
    override fun onRetainNonConfigurationInstance(): Any? = journeyFeed

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(SESSION_DEADLINE, sessionDeadlineElapsed)
        super.onSaveInstanceState(outState)
    }


    /**
     * Controls are native Android views on top of the SAME LibGDX Activity.
     * Hold with a DeX mouse pointer or press F10/Menu on a keyboard.
     */
    private fun installParentControls() {
        val control = Button(this).apply {
            text = "Parents · hold"
            isAllCaps = false
            contentDescription = "Hold to open parental controls on this display"
            setOnClickListener {
                Toast.makeText(this@KidsActivity,
                    "Hold this button or press F10 to manage the adventure",
                    Toast.LENGTH_SHORT).show()
            }
            setOnLongClickListener {
                showParentMenu()
                true
            }
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

    private fun showParentMenu() {
        if (isFinishing || isDestroyed || parentMenuOpen || runningGame == null) return
        parentMenuOpen = true
        // Journey and sprite clocks freeze; wall-clock session deadline keeps running.
        runningGame?.setParentMenuOpen(true)
        gps?.stop()
        AlertDialog.Builder(this)
            .setTitle("Parent controls")
            .setMessage("Adventure paused on this monitor. Continue, or return to settings.")
            .setNegativeButton("Continue") { _, _ -> }
            .setPositiveButton("End adventure") { _, _ -> finish() }
            .setOnDismissListener {
                parentMenuOpen = false
                runningGame?.setParentMenuOpen(false)
                val feed = liveFeed
                if (!isFinishing && !isDestroyed && feed != null &&
                    android.os.SystemClock.elapsedRealtime() < sessionDeadlineElapsed) {
                    gps?.start(feed::accept)
                }
            }
            .show()
    }

    override fun onDestroy() {
        gps?.stop()
        runningGame = null
        handler.removeCallbacks(sessionStop)
        KidsSessionControl.clear(this)
        super.onDestroy()
    }

    companion object {
        private const val SESSION_DEADLINE = "kids.session.deadline.elapsed"
        const val EXTRA_LIVE_GPS = "com.khuongnd.dexkids.extra.LIVE_GPS"
    }
}
