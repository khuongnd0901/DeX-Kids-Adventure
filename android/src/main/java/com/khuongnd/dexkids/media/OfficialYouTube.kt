package com.khuongnd.dexkids.media

import android.app.Activity
import android.app.ActivityManager
import android.app.ActivityOptions
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.view.Display

/** Official app only. No browser, WebView or default-display fallback. */
object OfficialYouTube {
    const val PACKAGE = "com.google.android.youtube"
    fun online(activity: Activity): Boolean {
        val manager = activity.getSystemService(ConnectivityManager::class.java)
        val network = manager.activeNetwork ?: return false
        return manager.getNetworkCapabilities(network)?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
    }
    fun externalDisplay(activity: Activity): Int? {
        // In Samsung DeX the current desktop can be virtual and omitted from
        // default getDisplays(). Inherit this Activity's display; never pick HDMI by name.
        val display = activity.display ?: return null
        return display.displayId.takeIf { it != Display.DEFAULT_DISPLAY && display.isValid && display.state == Display.STATE_ON }
    }
    fun intent(video: YouTubeVideo) = Intent(Intent.ACTION_VIEW, Uri.parse(video.url())).apply {
        setPackage(PACKAGE)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    }
    fun preflight(activity: Activity, video: YouTubeVideo): String? {
        val displayId = externalDisplay(activity) ?: return "Cần màn hình DeX ngoài đang bật; không mở trên điện thoại."
        if (YouTubeStore(activity).managedStop && !YouTubeControlService.permitted(activity))
            return "Chưa cấp quyền điều khiển dừng video; tiếp tục nội dung offline."
        if (!online(activity)) return "Chưa có mạng; tiếp tục nội dung offline."
        val target = intent(video)
        if (target.resolveActivity(activity.packageManager) == null) return "Chưa có ứng dụng YouTube chính thức."
        if (!activity.getSystemService(ActivityManager::class.java).isActivityStartAllowedOnDisplay(activity, displayId, target))
            return "Android không cho phép mở YouTube trên màn hình DeX này."
        return null
    }
    fun launch(activity: Activity, video: YouTubeVideo) {
        val id = externalDisplay(activity) ?: throw IllegalStateException("External display unavailable")
        check(preflight(activity, video) == null)
        activity.startActivity(intent(video), ActivityOptions.makeBasic().apply { launchDisplayId = id }.toBundle())
    }
}
