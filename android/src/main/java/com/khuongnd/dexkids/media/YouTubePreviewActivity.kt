package com.khuongnd.dexkids.media

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import com.khuongnd.dexkids.KidsSessionControl

/** Parent-only visible review, independent of scheduled approval and playback switches. */
class YouTubePreviewActivity : Activity() {
    private var host: EmbeddedYouTubeHost? = null
    private var video: YouTubeVideo? = null
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        val settings = YouTubeEmbedSettings(this)
        val store = YouTubeStore(this)
        if (KidsSessionControl.active() || store.pendingId.isNotEmpty() || !settings.selected || !settings.ready() ||
            OfficialYouTube.externalDisplay(this) == null) {
            notice("Chưa thể xem thử. Kiểm tra cấu hình, màn hình DeX và kết thúc hành trình đang chạy.")
            finish(); return
        }
        video = runCatching { store.videos().single { it.videoId() == intent.getStringExtra("video_id") } }.getOrNull()
        val candidate = video ?: run { finish(); return }
        host = EmbeddedYouTubeHost(this, candidate, completed = { reason ->
            when (reason) {
                "ended" -> notice("Đã xem thử xong. Thư viện giữ nguyên; bạn tự duyệt và bật link khi phù hợp.")
                "error", "blocked" -> notice("Không phát được video. Kiểm tra mạng, API, thời lượng đã nhập và quyền phát nhúng.")
            }
            finish()
        }, external = ::openExternal, parentReview = true)
        host?.start()
    }
    private fun openExternal(uri: Uri) {
        val store = YouTubeStore(this)
        var held = false
        try {
            val display = OfficialYouTube.externalDisplay(this) ?: error("Display unavailable")
            val target = Intent(Intent.ACTION_VIEW, uri)
            check(getSystemService(ActivityManager::class.java).isActivityStartAllowedOnDisplay(this, display, target))
            store.begin(video ?: error("Video unavailable")); held = true
            startActivity(target, ActivityOptions.makeBasic().apply { launchDisplayId = display }.toBundle())
        } catch (_: Exception) {
            if (held) runCatching { store.clearPending() }
            notice("Không mở được liên kết trên màn hình này.")
        }
        finish()
    }
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (event.action == KeyEvent.ACTION_UP && event.keyCode in setOf(KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_F10, KeyEvent.KEYCODE_MENU)) {
            host?.finish("cancelled"); finish(); return true
        }
        return super.dispatchKeyEvent(event)
    }
    override fun onPause() {
        host?.finish("cancelled"); host = null
        super.onPause()
        if (!isFinishing) finish()
    }
    override fun onDestroy() { host?.dispose(); host = null; super.onDestroy() }
    private fun notice(text: String) { Toast.makeText(this, text, Toast.LENGTH_LONG).show() }
}
