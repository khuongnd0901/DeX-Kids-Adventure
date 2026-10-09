package com.khuongnd.dexkids

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * AndroidManifest guards every exported command via signature permission.
 * Do not support untrusted senders, coordinates or background START.
 */
class KidsCommandReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP -> KidsSessionControl.stop()
            ACTION_PAUSE -> KidsSessionControl.pause()
            ACTION_RESUME -> KidsSessionControl.resume()
        }
    }
    companion object {
        const val ACTION_STOP = "com.khuongnd.dexkids.action.KIDS_STOP"
        const val ACTION_PAUSE = "com.khuongnd.dexkids.action.KIDS_PAUSE"
        const val ACTION_RESUME = "com.khuongnd.dexkids.action.KIDS_RESUME"
    }
}
