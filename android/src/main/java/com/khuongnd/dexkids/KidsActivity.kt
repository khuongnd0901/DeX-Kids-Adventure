package com.khuongnd.dexkids

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.khuongnd.dexkids.game.KidsGame
import com.khuongnd.dexkids.journey.JourneyFeed
import com.khuongnd.dexkids.journey.DemoJourneyFeed
import com.khuongnd.dexkids.journey.LiveJourneyFeed

/** Child screen does not request location permission itself. */
class KidsActivity : AndroidApplication() {
    private val handler = Handler(Looper.getMainLooper())
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
            initialize(KidsGame(feed), config)
        } else {
            val feed = (lastNonConfigurationInstance as? DemoJourneyFeed) ?: DemoJourneyFeed()
            journeyFeed = feed
            initialize(KidsGame(feed), config)
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
        if (feed != null) gps?.start(feed::accept)
    }

    @Suppress("DEPRECATION")
    override fun onRetainNonConfigurationInstance(): Any? = journeyFeed

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(SESSION_DEADLINE, sessionDeadlineElapsed)
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        gps?.stop()
        handler.removeCallbacks(sessionStop)
        KidsSessionControl.clear(this)
        super.onDestroy()
    }

    companion object {
        private const val SESSION_DEADLINE = "kids.session.deadline.elapsed"
        const val EXTRA_LIVE_GPS = "com.khuongnd.dexkids.extra.LIVE_GPS"
    }
}
