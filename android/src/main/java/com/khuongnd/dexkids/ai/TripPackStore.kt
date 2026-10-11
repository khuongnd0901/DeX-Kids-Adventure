package com.khuongnd.dexkids.ai

import android.content.Context
import android.util.AtomicFile
import com.khuongnd.dexkids.story.EntertainmentDirector
import com.khuongnd.dexkids.story.OfflineLearningCatalog
import com.khuongnd.dexkids.story.TripQuestionPack
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * Atomic, per-audience last-good-pack. Store only local approved IDs, never child data.
 * AtomicFile preserves the former file if the write fails/power is lost mid-commit.
 */
internal class TripPackStore(context: Context, private val catalog: OfflineLearningCatalog,
                             private val audience: EntertainmentDirector.Audience) {
    private val app = context.applicationContext
    private val file = AtomicFile(File(app.filesDir,"trip-packs-v1-${audience.name.lowercase()}.json"))
    private val prefs = app.getSharedPreferences("trip-pack-state-v1",Context.MODE_PRIVATE)
    private val prefix = audience.name + ":"

    data class Snapshot(val pack: TripQuestionPack, val fingerprint: String, val cursor: Int)

    private fun digest(text: String) = MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private fun entries(pack: TripQuestionPack): JSONArray = JSONArray().apply {
        pack.items().forEach { item ->
            put(JSONArray().put(item.quizId()).put(item.wordId()).put(item.focus().name))
        }
    }

    @Synchronized fun load(): Snapshot? = runCatching {
        val bytes=file.openRead().use { input ->
            val out=input.readBytes()
            require(out.size in 100..16_384)
            out
        }
        val root=JSONObject(String(bytes,Charsets.UTF_8))
        require(root.length()==4 && root.getInt("version")==1 &&
            root.getString("audience")==audience.name)
        val raw=root.getJSONArray("items")
        val checksum=digest("1:${audience.name}:${raw}")
        require(checksum==root.getString("sha256"))
        val items=(0 until raw.length()).map { index ->
            val row=raw.getJSONArray(index)
            require(row.length()==3)
            TripQuestionPack.Item(row.getString(0),row.getString(1),
                EntertainmentDirector.Audience.valueOf(row.getString(2)))
        }
        val validated=TripQuestionPack.validate(items,catalog,audience)
        val cursor=if(prefs.getString(prefix+"sha","")==checksum)
            prefs.getInt(prefix+"cursor",0).coerceIn(0,items.size) else 0
        Snapshot(validated,checksum,cursor)
    }.getOrNull()

    /** Never touch the last good file until ALL IDs pass validation. */
    @Synchronized fun swap(pack: TripQuestionPack): Snapshot {
        val validated=TripQuestionPack.validate(pack.items(),catalog,audience)
        val arr=entries(validated)
        val sha=digest("1:${audience.name}:${arr}")
        val root=JSONObject().put("version",1).put("audience",audience.name)
            .put("items",arr).put("sha256",sha)
        val bytes=root.toString().toByteArray(Charsets.UTF_8)
        require(bytes.size <= 16_384)
        val stream=file.startWrite()
        try {
            stream.write(bytes)
            file.finishWrite(stream)
        } catch (failure: Exception) {
            file.failWrite(stream)
            throw failure
        }
        // A commit failure leaves the newly durable pack readable with a reset cursor.
        prefs.edit().putString(prefix+"sha",sha).putInt(prefix+"cursor",0).commit()
        return Snapshot(validated,sha,0)
    }

    fun markConsumed(sha: String, count: Int) {
        if(prefs.getString(prefix+"sha","")==sha)
            prefs.edit().putInt(prefix+"cursor",count).apply()
    }
    @Synchronized fun recent(): List<String> = prefs.getString(prefix+"recent","")
        .orEmpty().split(",").filter { it.matches(Regex("quiz-[a-z0-9-]{3,60}")) }.takeLast(50)

    @Synchronized fun remember(quizId: String) {
        val values=(recent()+quizId).takeLast(50)
        prefs.edit().putString(prefix+"recent",values.joinToString(",")).apply()
    }

    @Synchronized fun clear() {
        file.delete()
        prefs.edit().remove(prefix+"sha").remove(prefix+"cursor").remove(prefix+"recent").commit()
    }
}
