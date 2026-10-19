package com.khuongnd.dexkids.media

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import java.io.File

/** Local catalog and durable single handoff journal. Never stores Google credentials. */
class YouTubeStore(private val context: Context) {
    private val prefs = context.getSharedPreferences("youtube_handoff_v1", Context.MODE_PRIVATE)
    private val catalog = AtomicFile(File(context.filesDir, "youtube-library-v1.json"))
    val consented get() = prefs.getInt("notice", 0) == 1
    var enabled: Boolean
        get() = consented && prefs.getBoolean("enabled", false)
        set(value) { check(prefs.edit().putBoolean("enabled", value && consented).commit()) }
    var automatic: Boolean
        get() = enabled && prefs.getBoolean("automatic", false)
        set(value) { check(prefs.edit().putBoolean("automatic", value && enabled).commit()) }
    var every: Int
        get() = prefs.getInt("every", 2).takeIf { it == 2 || it == 4 } ?: 2
        set(value) {
            require(value == 2 || value == 4)
            check(prefs.edit().putInt("every", value).putInt("step", 0).commit())
        }
    var managedStop: Boolean
        get() = prefs.getBoolean("managed_stop", false)
        set(value) { check(prefs.edit().putBoolean("managed_stop", value).commit()) }
    private fun bootCount() = android.provider.Settings.Global.getInt(context.contentResolver, android.provider.Settings.Global.BOOT_COUNT, -1)
    val deadline: Long get() {
        val saved = prefs.getLong("deadline_elapsed", 0L)
        // elapsedRealtime is valid across process death, but resets on device reboot.
        return if (saved > 0 && prefs.getInt("deadline_boot", -1) != bootCount()) 1L else saved
    }
    val returnTask get() = prefs.getInt("return_task", -1)
    val returnDisplay get() = prefs.getInt("return_display", -1)
    fun armReturn(task: Int, display: Int, seconds: Int) {
        check(pendingId.isNotEmpty())
        require(task >= 0 && display > 0 && seconds in 60..1800)
        check(prefs.edit().putInt("deadline_boot", bootCount()).putLong("deadline_elapsed", android.os.SystemClock.elapsedRealtime() + seconds * 1000L)
            .putInt("return_task", task).putInt("return_display", display).commit())
    }
    val step get() = prefs.getInt("step", 0)
    val lastVideoId get() = prefs.getString("last", "").orEmpty()
    val pendingId get() = prefs.getString("pending", "").orEmpty()
    val pendingEmbedded get() = prefs.getString("pending_kind", "official") == "embedded"
    fun beginEmbedded(video: YouTubeVideo, replayRunning: Boolean) {
        check(pendingId.isEmpty())
        check(prefs.edit().putString("pending", video.videoId()).putString("pending_kind", "embedded")
            .putBoolean("pending_replay_running", replayRunning).commit())
    }
    fun markExternalPending() { check(prefs.edit().putString("pending_kind", "official").commit()) }
    val replayWasRunning get() = prefs.getBoolean("pending_replay_running", false)
    fun acceptNotice() { check(prefs.edit().putInt("notice", 1).commit()) }
    fun advance(next: Int) { check(prefs.edit().putInt("step", next).commit()) }
    fun videos(): List<YouTubeVideo> {
        return try {
            catalog.openRead().use { stream ->
                val output = java.io.ByteArrayOutputStream()
                val buffer = ByteArray(4096)
                while (true) {
                    val size = stream.read(buffer)
                    if (size < 0) break
                    require(output.size() + size <= YouTubeLibrary.MAX_BYTES)
                    output.write(buffer, 0, size)
                }
                YouTubeLibrary.decode(output.toString("UTF-8"))
            }
        } catch (_: java.io.FileNotFoundException) { emptyList() }
    }
    fun save(videos: List<YouTubeVideo>) {
        val bytes = YouTubeLibrary.encode(videos).toByteArray(Charsets.UTF_8)
        val stream = catalog.startWrite()
        try { stream.write(bytes); catalog.finishWrite(stream) }
        catch (error: Exception) { catalog.failWrite(stream); throw error }
    }
    fun launches(): List<YouTubeInterlude.Launch> {
        val rows = JSONArray(prefs.getString("launches", "[]"))
        require(rows.length() <= 2)
        return (0 until rows.length()).map {
            val row = rows.getJSONArray(it)
            YouTubeInterlude.Launch(row.getLong(0), row.getInt(1))
        }
    }
    /** Persist the hold before launching: recreation/process loss cannot start local audio. */
    @JvmOverloads fun begin(video: YouTubeVideo, replayRunning: Boolean = false) {
        check(pendingId.isEmpty())
        check(prefs.edit().putString("pending", video.videoId()).putString("pending_kind", "official")
            .putBoolean("pending_replay_running", replayRunning).commit())
    }
    fun requested(video: YouTubeVideo, now: Long) {
        val rows = JSONArray()
        launches().filter { it.atMillis() > now || now - it.atMillis() < 3_600_000L }.forEach {
            rows.put(JSONArray().put(it.atMillis()).put(it.plannedSeconds()))
        }
        check(rows.length() < 2)
        rows.put(JSONArray().put(now).put(video.plannedSeconds()))
        check(prefs.edit().putString("last", video.videoId()).putString("launches", rows.toString()).commit())
    }
    /** Failed launch, parent acknowledgement or confirmed managed pause releases a handoff. */
    fun clearPending() { check(prefs.edit().remove("pending").remove("pending_kind").remove("pending_replay_running").remove("deadline_elapsed").remove("deadline_boot").remove("return_task").remove("return_display").commit()) }
    fun deleteLibrary() { save(emptyList()); enabled = false; automatic = false }
}
