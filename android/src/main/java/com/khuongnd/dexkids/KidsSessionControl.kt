package com.khuongnd.dexkids

import java.lang.ref.WeakReference

/** Process-local stop, never an exported unprotected command or system-wide action. */
internal object KidsSessionControl {
    @Volatile private var activity: WeakReference<KidsActivity>? = null
    fun register(child: KidsActivity) { activity = WeakReference(child) }
    fun clear(child: KidsActivity) {
        if (activity?.get() === child) activity = null
    }
    fun stop() { activity?.get()?.runOnUiThread { activity?.get()?.finish() } }
    fun active(): Boolean = activity?.get() != null
}
