package com.khuongnd.dexkids

import java.lang.ref.WeakReference

/** Process-local active-session control; no location/history is persisted. */
internal object KidsSessionControl {
    @Volatile private var activity: WeakReference<KidsActivity>? = null
    fun register(child: KidsActivity) { activity = WeakReference(child) }
    fun clear(child: KidsActivity) {
        if (activity?.get() === child) activity = null
    }
    fun stop() = dispatch { finish() }
    fun pause() = dispatch { pauseFromTrustedController() }
    fun resume() = dispatch { resumeFromTrustedController() }
    private fun dispatch(action: KidsActivity.() -> Unit) {
        val child = activity?.get() ?: return
        child.runOnUiThread { if (!child.isFinishing && !child.isDestroyed) child.action() }
    }
    fun active(): Boolean = activity?.get()?.let { !it.isFinishing && !it.isDestroyed } ?: false
}
