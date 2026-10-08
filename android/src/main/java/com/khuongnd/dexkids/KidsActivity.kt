package com.khuongnd.dexkids

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.khuongnd.dexkids.game.KidsGame

/** Passive screen; launched only by an explicit parent action. */
class KidsActivity : AndroidApplication() {
    private val handler = Handler(Looper.getMainLooper())
    private val sessionStop = Runnable { finish() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        KidsSessionControl.register(this)
        handler.postDelayed(sessionStop, ParentSettings(this).sessionMinutes * 60_000L)
        val config = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useWakelock = true
        }
        initialize(KidsGame(), config)
    }

    override fun onDestroy() {
        handler.removeCallbacks(sessionStop)
        KidsSessionControl.clear(this)
        super.onDestroy()
    }
}
