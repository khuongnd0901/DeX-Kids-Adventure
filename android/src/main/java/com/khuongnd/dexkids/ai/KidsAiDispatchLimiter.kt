package com.khuongnd.dexkids.ai

import android.content.Context
import android.os.Looper
import java.io.IOException

/** A single process-wide AND persisted rolling-window gate for all subscription Responses HTTP dispatches. */
internal object KidsAiDispatchLimiter {
    private const val WINDOW_MS = 60_000L
    private const val MIN_GAP_MS = 6_100L
    private const val MAX_REQUESTS = 10
    private val lock = Any()

    /** Reserve one physical request start; interruption cancels before reservation. */
    @JvmOverloads
    @Throws(InterruptedException::class, IOException::class)
    fun acquire(context: Context, nowMillis: () -> Long = { System.currentTimeMillis() },
                waitMillis: (Long) -> Unit = { Thread.sleep(it) }) {
        check(Looper.myLooper() != Looper.getMainLooper()) { "Network limiter on UI thread" }
        val prefs = context.applicationContext.getSharedPreferences("kids_ai_rate_v1",Context.MODE_PRIVATE)
        synchronized(lock) {
            while (true) {
                if (Thread.currentThread().isInterrupted) throw InterruptedException("AI dispatch cancelled")
                val now = nowMillis()
                // On clock rollback, fail conservatively until older timestamps expire.
                val starts = prefs.getString("starts","")!!.split(",")
                    .mapNotNull(String::toLongOrNull)
                    .filter { it >= now || now - it < WINDOW_MS }
                    .sorted().takeLast(MAX_REQUESTS)
                val last = starts.lastOrNull()
                val spacing = if (last == null) 0L else last + MIN_GAP_MS - now
                val window = if (starts.size < MAX_REQUESTS) 0L
                    else starts.first() + WINDOW_MS + 100L - now
                val wait = maxOf(spacing,window)
                if (wait > 0L) {
                    waitMillis(wait.coerceAtMost(6_500L))
                    continue
                }
                // commit() prevents Activity recreation/process restart resetting the budget.
                if (!prefs.edit().putString("starts",(starts + now).joinToString(",")).commit())
                    throw IOException("AI_RATE_PERSIST_FAILED")
                return
            }
        }
    }
}
