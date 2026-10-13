package com.khuongnd.dexkids

import android.os.Build
import android.os.Debug
import android.os.SystemClock
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

/** Coalesced budget check outside GL, at most every 30s; purges only free pages. */
internal object NativeHeapTrim {
    private val pending = AtomicBoolean()
    private val nextCheck = AtomicLong()
    private val executor = Executors.newSingleThreadExecutor { task ->
        Thread(task,"dexkids-native-heap").apply { isDaemon = true }
    }
    private var available: Boolean? = null // confined to the one worker

    @JvmStatic external fun nativePurge(deep: Boolean): Boolean
    @JvmStatic fun isPending(): Boolean = pending.get()

    fun request() {
        val now=SystemClock.elapsedRealtime()
        if (now < nextCheck.get() || !pending.compareAndSet(false,true)) return
        nextCheck.set(now+30_000L)
        executor.execute {
            try {
                val free=Debug.getNativeHeapFreeSize()
                if (free < 32L*1024*1024) {
                    android.util.Log.i("NativeHeapTrim","skip free_kib=${free/1024}")
                    return@execute
                }
                if (available==null) available=runCatching {
                    System.loadLibrary("dexkids_memory")
                }.onFailure { android.util.Log.w("NativeHeapTrim","adapter_unavailable="+it.javaClass.simpleName) }.isSuccess
                if (available!=true) return@execute
                val started=SystemClock.elapsedRealtime()
                val ok=nativePurge(Build.VERSION.SDK_INT>=34)
                android.util.Log.i("NativeHeapTrim","purge=$ok free_kib_before=${free/1024} elapsed_ms=${SystemClock.elapsedRealtime()-started}")
            } catch (_: LinkageError) {
                available=false // unsupported native adapter cannot crash a journey
            } finally { pending.set(false) }
        }
    }
}
