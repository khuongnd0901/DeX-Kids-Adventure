package com.khuongnd.dexkids.media

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import java.io.File

/** Local catalog and durable single handoff journal. Never stores Google credentials. */
class YouTubeStore(context: Context) {
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
    val step get() = prefs.getInt("step", 0)
    val lastVideoId get() = prefs.getString("last", "").orEmpty()
    val pendingId get() = prefs.getString("pending", "").orEmpty()
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
        check(prefs.edit().putString("pending", video.videoId())
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
    /** Only failed launch or explicit parent acknowledgement releases a handoff. */
    fun clearPending() { check(prefs.edit().remove("pending").remove("pending_replay_running").commit()) }
    fun deleteLibrary() { save(emptyList()); enabled = false; automatic = false }
}
