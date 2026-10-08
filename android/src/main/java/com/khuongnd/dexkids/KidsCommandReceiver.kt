package com.khuongnd.dexkids

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Exported endpoint is protected by a signature-level permission in Manifest.
 * Independent APKs must share signing certificate or this command is denied.
 * No user-supplied coordinates, media or backend token accepted.
 */
class KidsCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_STOP) return
        KidsSessionControl.stop()
    }

    companion object {
        const val ACTION_STOP = "com.khuongnd.dexkids.action.KIDS_STOP"
    }
}
