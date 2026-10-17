package com.khuongnd.dexkids

import android.content.Context
import android.os.SystemClock
import android.util.AtomicFile
import com.khuongnd.dexkids.story.OfflineLearningCatalog
import com.khuongnd.dexkids.story.adventure.StorySourceClient
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.net.HttpURLConnection

/** Parent-authored public content only. Sessions hold immutable snapshots until restarted. */
internal object LearningSource {
    private var running = false
    private val callbacks = mutableListOf<() -> Unit>()
    private var generation = 0L
    private var lastAttempt = Long.MIN_VALUE
    private fun prefs(c: Context) = c.getSharedPreferences("learning_source_v1", 0)
    private fun file(c: Context) = AtomicFile(java.io.File(c.filesDir, "learning-source.json"))
    @Synchronized fun mode(c: Context): String = prefs(c).getString("mode", "builtin")!!
    @Synchronized fun url(c: Context): String = prefs(c).getString("url", "")!!
    @Synchronized fun status(c: Context): String = prefs(c).getString("status", "Đang dùng bộ có sẵn.")!!
    private fun cached(c: Context): JSONObject? = runCatching {
        JSONObject(file(c).openRead().bufferedReader(Charsets.UTF_8).use { it.readText() })
    }.getOrNull()
    @Synchronized fun load(c: Context): OfflineLearningCatalog {
        if (mode(c) != "builtin") {
            val data = cached(c)?.optString("data")
            if (data != null) runCatching {
                return OfflineLearningCatalog.parse(ByteArrayInputStream(data.toByteArray(Charsets.UTF_8)))
            }
        }
        return c.assets.open("learning/offline-learning.tsv").use { OfflineLearningCatalog.parse(it) }
    }
    private fun save(c: Context, data: ByteArray, origin: String) {
        val pack = OfflineLearningCatalog.parse(ByteArrayInputStream(data))
        val payload = JSONObject().put("origin", origin).put("data", String(data, Charsets.UTF_8))
            .toString().toByteArray(Charsets.UTF_8)
        val target = file(c)
        val stream = target.startWrite()
        try { stream.write(payload); target.finishWrite(stream) }
        catch (e: Exception) { target.failWrite(stream); throw e }
        prefs(c).edit().putString("status", "Đã lưu ${pack.size()} thẻ. Áp dụng khi bắt đầu hành trình mới.").apply()
    }
    @Synchronized fun useBuiltin(c: Context) {
        generation++
        prefs(c).edit().putString("mode", "builtin").putString("status", "Đang dùng bộ có sẵn.").apply()
    }
    @Synchronized fun configure(c: Context, source: String) {
        val valid = StorySourceClient.validateUrl(source)
        generation++; lastAttempt = Long.MIN_VALUE
        prefs(c).edit().putString("url", valid).putString("mode", "https")
            .putString("status", "Đã bật nguồn HTTPS. Đang giữ bộ cũ cho đến khi tải thành công.").apply()
    }
    fun importFile(c: Context, input: InputStream) {
        // Parse directly to enforce the byte bound before retaining any payload.
        val out = java.io.ByteArrayOutputStream()
        val buffer = ByteArray(4096)
        while (true) {
            val count = input.read(buffer)
            if (count < 0) break
            require(out.size() + count <= OfflineLearningCatalog.MAX_BYTES)
            out.write(buffer, 0, count)
        }
        val bounded = out.toByteArray()
        synchronized(this) {
            save(c, bounded, "file")
            generation++
            prefs(c).edit().putString("mode", "file").apply()
        }
    }
    /** Foreground request, throttled five minutes; GET has no child/session information. */
    fun request(c: Context, force: Boolean = false, done: (() -> Unit)? = null) {
        val app = c.applicationContext
        val source: String
        val ticket: Long
        synchronized(this) {
            if (mode(app) != "https") { done?.invoke(); return }
            if (running) { done?.let { callbacks.add(it) }; return }
            val now = SystemClock.elapsedRealtime()
            if (!force && lastAttempt != Long.MIN_VALUE && now-lastAttempt < 300_000L) { done?.invoke(); return }
            running = true; lastAttempt = now; ticket = generation; source = url(app)
            done?.let { callbacks.add(it) }
        }
        Thread({
            try {
                val client = StorySourceClient({ it.openConnection() as HttpURLConnection },
                    OfflineLearningCatalog.MAX_BYTES, "text/tab-separated-values, text/plain")
                val result = client.fetch(source, null, null)
                require(!result.unchanged)
                synchronized(this) {
                    if (ticket == generation && mode(app) == "https" && url(app) == source)
                        save(app, result.body(), source)
                }
            } catch (_: Exception) {
                synchronized(this) {
                    if (ticket == generation) prefs(app).edit().putString("status",
                        "Không tải được hoặc file không hợp lệ. Giữ nguyên bộ đã lưu.").apply()
                }
            } finally {
                val pending: List<() -> Unit>
                val changed: Boolean
                synchronized(this) {
                    running = false
                    changed = ticket != generation && mode(app) == "https"
                    pending = if (changed) emptyList() else callbacks.toList()
                    if (!changed) callbacks.clear()
                }
                pending.forEach { runCatching { it() } }
                if (changed) request(app, true)
            }
        }, "dexkids-learning-source").apply { isDaemon = true; start() }
    }
}
