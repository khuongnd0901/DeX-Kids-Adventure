package com.khuongnd.dexkids

import android.app.Activity
import android.app.ActivityOptions
import android.content.Intent
import android.hardware.display.DisplayManager
import android.view.Display

/** External-screen launch only, no guessed displayId and no phone fallback. */
class DisplayRouter(private val parent: Activity) {
    fun externalDisplays(): List<Display> {
        val manager = parent.getSystemService(DisplayManager::class.java)
        return manager.displays.filter { display ->
            display.displayId != Display.DEFAULT_DISPLAY && display.state == Display.STATE_ON
        }
    }
    fun launchOnExternalDisplay(): String {
        val screen = externalDisplays().firstOrNull()
            ?: return "No external presentation screen detected; child launch aborted."
        return try {
            val options = ActivityOptions.makeBasic().apply { launchDisplayId = screen.displayId }
            parent.startActivity(Intent(parent, KidsActivity::class.java), options.toBundle())
            "External display launch requested. Hardware confirmation still required."
        } catch (_: SecurityException) {
            "Samsung/Android restricted secondary display launch; no phone fallback."
        } catch (_: IllegalArgumentException) {
            "External display is not eligible for Activity launch; no phone fallback."
        } catch (_: android.content.ActivityNotFoundException) {
            "Child activity unavailable; no phone fallback."
        }
    }
}
