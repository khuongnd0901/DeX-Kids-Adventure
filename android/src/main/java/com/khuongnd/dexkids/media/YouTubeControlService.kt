package com.khuongnd.dexkids.media

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.hardware.display.DisplayManager
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.service.notification.NotificationListenerService
import android.view.Display

/** No notification contents are read. Only official YouTube media sessions are controlled. */
class YouTubeControlService : NotificationListenerService() {
    private val handler = Handler(Looper.getMainLooper())
    private var attemptedDeadline = 0L
    private var connected = false
    private val tick = object : Runnable {
        override fun run() {
            if (!connected) return
            val store = YouTubeStore(this@YouTubeControlService)
            val deadline = store.deadline
            if (store.managedStop && !store.pendingEmbedded && store.pendingId.isNotEmpty() && deadline > 0 &&
                SystemClock.elapsedRealtime() >= deadline && attemptedDeadline != deadline) {
                attemptedDeadline = deadline
                val pending = store.pendingId
                pauseAndConfirm(this@YouTubeControlService) { stopped ->
                    // Never revive a finished/replaced journey or route it to the phone.
                    if (connected && stopped && store.managedStop && permitted(this@YouTubeControlService) && store.pendingId == pending && store.deadline == deadline) {
                        val display = getSystemService(DisplayManager::class.java).getDisplay(store.returnDisplay)
                        if (display != null && display.displayId != Display.DEFAULT_DISPLAY &&
                            display.isValid && display.state == Display.STATE_ON) {
                            if (!com.khuongnd.dexkids.KidsSessionControl.matchesExternalTask(store.returnTask, store.returnDisplay)) return@pauseAndConfirm
                            runCatching {
                                getSystemService(ActivityManager::class.java).appTasks
                                    .firstOrNull { it.taskInfo.taskId == store.returnTask &&
                                        it.taskInfo.topActivity?.className == "com.khuongnd.dexkids.KidsActivity" }?.moveToFront()
                            }
                        }
                    }
                }
            }
            handler.postDelayed(this, if (store.pendingId.isEmpty()) 10_000 else 1000)
        }
    }
    override fun onListenerConnected() {
        connected = true
        attemptedDeadline = 0L
        handler.removeCallbacks(tick)
        handler.post(tick)
    }
    override fun onListenerDisconnected() { connected = false; handler.removeCallbacksAndMessages(null) }
    override fun onDestroy() { connected = false; handler.removeCallbacksAndMessages(null); super.onDestroy() }

    companion object {
        fun component(context: Context) = ComponentName(context, YouTubeControlService::class.java)
        fun permitted(context: Context) = context.getSystemService(NotificationManager::class.java)
            .isNotificationListenerAccessGranted(component(context))
        private fun sessions(context: Context): List<MediaController>? = try {
            if (!permitted(context)) null else context.getSystemService(MediaSessionManager::class.java)
                .getActiveSessions(component(context)).filter { it.packageName == OfficialYouTube.PACKAGE }
        } catch (_: RuntimeException) { null }

        /** A command is not confirmation. Require the same nonempty token set and paused/stopped states. */
        fun pauseAndConfirm(context: Context, result: (Boolean) -> Unit) {
            val controllers = sessions(context)
            if (controllers.isNullOrEmpty()) { result(false); return }
            try { controllers.forEach { it.transportControls.pause() } }
            catch (_: RuntimeException) { result(false); return }
            val tokens = controllers.map { it.sessionToken }.toSet()
            val handler = Handler(Looper.getMainLooper())
            val end = SystemClock.elapsedRealtime() + 3000
            val poll = object : Runnable {
                override fun run() {
                    val current = sessions(context)
                    val confirmed = YouTubeStopGate.confirmed(tokens, current?.map {
                        YouTubeStopGate.Session(it.sessionToken, it.packageName,
                            it.playbackState?.state in setOf(PlaybackState.STATE_PAUSED, PlaybackState.STATE_STOPPED))
                    })
                    if (confirmed) result(true)
                    else if (SystemClock.elapsedRealtime() >= end) result(false)
                    else handler.postDelayed(this, 150)
                }
            }
            // Allow the remote process to apply the pause before checking state.
            handler.postDelayed(poll, 150)
        }
    }
}
