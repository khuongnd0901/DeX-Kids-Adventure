package com.khuongnd.dexkids.media

import android.content.Context
import android.content.pm.PackageManager
import java.net.URL
import java.net.URLEncoder
import java.security.MessageDigest
import javax.net.ssl.HttpsURLConnection

/** Called only on a worker after embedded consent/readiness; no child profile is sent. */
object YouTubeMetadata {
    fun signingSha1(context: Context): String {
        val info = context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
        val signature = info.signingInfo?.apkContentsSigners?.firstOrNull() ?: error("Signing identity unavailable")
        return MessageDigest.getInstance("SHA-1").digest(signature.toByteArray()).joinToString("") { "%02X".format(it) }
    }
    fun validate(context: Context, video: YouTubeVideo, key: String, country: String): Int {
        require(video.approved() && video.enabled())
        val query = "part=status,contentDetails,snippet&id=${video.videoId()}&key=${URLEncoder.encode(key, "UTF-8") }"
        val connection = URL("https://www.googleapis.com/youtube/v3/videos?$query").openConnection() as HttpsURLConnection
        connection.connectTimeout = 8000; connection.readTimeout = 8000
        connection.instanceFollowRedirects = false
        connection.setRequestProperty("X-Android-Package", context.packageName)
        connection.setRequestProperty("X-Android-Cert", signingSha1(context))
        try {
            check(connection.responseCode == 200) { "Metadata request rejected" }
            val bytes = connection.inputStream.use { input ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    require(output.size() + count <= 131072)
                    output.write(buffer, 0, count)
                }
                output.toByteArray()
            }
            return YouTubeEligibility.validate(String(bytes, Charsets.UTF_8), video.videoId(), country, video.plannedSeconds())
        } finally { connection.disconnect() }
    }
}
