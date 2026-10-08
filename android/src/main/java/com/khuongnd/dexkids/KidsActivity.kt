package com.khuongnd.dexkids

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.khuongnd.dexkids.game.KidsGame
import com.khuongnd.dexkids.journey.LiveJourneyFeed

/** Child screen does not request location permission itself. */
class KidsActivity : AndroidApplication() {
    private val handler = Handler(Looper.getMainLooper())
    private val sessionStop = Runnable { finish() }
    private var gps: AndroidGpsSource? = null
    private var liveFeed: LiveJourneyFeed? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val wantsLive = intent.getBooleanExtra(EXTRA_LIVE_GPS, false)
        if (wantsLive && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            finish() // Never silently show demo as live GPS.
            return
        }
        KidsSessionControl.register(this)
        handler.postDelayed(sessionStop, ParentSettings(this).sessionMinutes * 60_000L)
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useWakelock = true
        }
        if (wantsLive) {
            val feed = LiveJourneyFeed()
            liveFeed = feed
            gps = AndroidGpsSource(this)
            if (gps?.start(feed::accept) != true) {
                finish()
                return
            }
            initialize(KidsGame(feed), config)
        } else initialize(KidsGame(), config)
    }

    override fun onPause() {
        gps?.stop()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        val feed = liveFeed
        if (feed != null) gps?.start(feed::accept)
    }

    override fun onDestroy() {
        gps?.stop()
        handler.removeCallbacks(sessionStop)
        KidsSessionControl.clear(this)
        super.onDestroy()
    }

    companion object {
        const val EXTRA_LIVE_GPS = "com.khuongnd.dexkids.extra.LIVE_GPS"
    }
}
