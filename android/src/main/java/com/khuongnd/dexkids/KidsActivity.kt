
package com.khuongnd.dexkids

import android.os.Bundle
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import com.khuongnd.dexkids.game.KidsGame

/**
 * Initial standalone launcher. M8 will add explicit DeX display routing
 * only after it is verified on actual Samsung hardware.
 */
class KidsActivity : AndroidApplication() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val configuration = AndroidApplicationConfiguration().apply {
            useImmersiveMode = true
            useWakelock = true
        }
        initialize(KidsGame(), configuration)
    }
}
