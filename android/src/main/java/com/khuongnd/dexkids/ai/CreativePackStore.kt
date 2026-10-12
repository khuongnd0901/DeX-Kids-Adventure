package com.khuongnd.dexkids.ai

import android.content.Context
import android.util.AtomicFile
import com.khuongnd.dexkids.story.CreativeLessonPack
import com.khuongnd.dexkids.story.EntertainmentDirector
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.security.MessageDigest

/**
 * Separate last-good AI-authored pack (no child speech, GPS or API key).
 * An invalid response must never overwrite the previous locally playable pack.
 */
internal class CreativePackStore(context: Context,
    private val audience: EntertainmentDirector.Audience,
    private val sauAge: Int, private val ongAge: Int) {
    private val app=context.applicationContext
    private val file=AtomicFile(File(app.filesDir,"creative-packs-v1-${audience.name.lowercase()}.json"))
    private val prefs=app.getSharedPreferences("creative-pack-state-v1",Context.MODE_PRIVATE)
    private val prefix=audience.name+":"
    data class Snapshot(val pack: CreativeLessonPack,val fingerprint: String,val cursor: Int)
    private fun hash(text: String): String=MessageDigest.getInstance("SHA-256")
        .digest(text.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it) }

    private fun rows(pack: CreativeLessonPack) = JSONArray().apply {
        pack.lessons().forEach { x ->
            put(JSONArray().put(x.prompt()).put(x.answer()).put(x.englishWord())
                .put(x.meaningVi()).put(x.topic()).put(x.genre().name).put(x.focus().name)
                .put(x.targetAge()).put(x.fictional()))
        }
    }
    @Synchronized fun load(): Snapshot? = runCatching {
        val bytes=file.openRead().use { it.readBytes() }
        require(bytes.size in 100..65_536)
        val root=JSONObject(String(bytes,Charsets.UTF_8))
        val version=root.getInt("version")
        require(((version==1 && root.length()==6) || (version==2 && root.length()==7)) &&
            root.getString("audience")==audience.name &&
            root.getInt("sauAge")==sauAge && root.getInt("ongAge")==ongAge)
        val raw=root.getJSONArray("items")
        val sources=if(version==2) root.getJSONArray("reviewSources") else null
        val checksum=hash("$version:${audience.name}:$sauAge:$ongAge:$raw" +
            (sources?.let { ":$it" } ?: ""))
        require(checksum==root.getString("sha256"))
        val lessons=(0 until raw.length()).map { index ->
            val row=raw.getJSONArray(index)
            require(row.length()==9)
            CreativeLessonPack.Lesson(row.getString(0),row.getString(1),row.getString(2),
                row.getString(3),row.getString(4),
                CreativeLessonPack.Genre.valueOf(row.getString(5)),
                EntertainmentDirector.Audience.valueOf(row.getString(6)),
                row.getInt(7),row.getBoolean(8))
        }
        var pack=CreativeLessonPack.validate(lessons,audience,sauAge,ongAge,emptyList())
        if(sources!=null) pack=pack.withReviewSources((0 until sources.length()).map {
            val source=sources.getJSONArray(it)
            require(source.length()==3)
            CreativeLessonPack.ReviewSource(source.getString(0),source.getString(1),source.getString(2))
        })
        val cursor=if(prefs.getString(prefix+"sha","")==checksum)
            prefs.getInt(prefix+"cursor",0).coerceIn(0,lessons.size) else 0
        Snapshot(pack,checksum,cursor)
    }.getOrNull()

    @Synchronized fun swap(pack: CreativeLessonPack): Snapshot {
        val valid=CreativeLessonPack.validate(pack.lessons(),audience,sauAge,ongAge,recent())
        val arr=rows(valid)
        val sources=JSONArray(pack.reviewSources().map {
            JSONArray().put(it.questionId()).put(it.generationModel()).put(it.reviewModel())
        })
        val version=if(pack.reviewSources().isEmpty()) 1 else 2
        if(version==2)require(pack.reviewSources().size==valid.lessons().size)
        val checksum=hash("$version:${audience.name}:$sauAge:$ongAge:$arr" + if(version==2) ":$sources" else "")
        val root=JSONObject().put("version",version).put("audience",audience.name)
            .put("sauAge",sauAge).put("ongAge",ongAge)
            .put("items",arr).put("sha256",checksum)
        if(version==2)root.put("reviewSources",sources)
        val json=root.toString().toByteArray(Charsets.UTF_8)
        require(json.size<=65_536)
        val stream=file.startWrite()
        try { stream.write(json); file.finishWrite(stream) }
        catch (failure: Exception) { file.failWrite(stream); throw failure }
        check(prefs.edit().putString(prefix+"sha",checksum).putInt(prefix+"cursor",0).commit())
        return Snapshot(if(version==2)valid.withReviewSources(pack.reviewSources()) else valid,checksum,0)
    }
    @Synchronized fun recent(): List<String> = prefs.getString(prefix+"recent","")
        .orEmpty().split(",").filter { it.matches(Regex("[a-f0-9]{64}")) }.takeLast(100)
    @Synchronized fun remember(prompt: String) {
        val newHash=CreativeLessonPack.fingerprint(prompt)
        prefs.edit().putString(prefix+"recent",(recent()+newHash).takeLast(100).joinToString(",")).apply()
    }
    @Synchronized fun markConsumed(fingerprint: String, cursor: Int) {
        if(prefs.getString(prefix+"sha","")==fingerprint)
            prefs.edit().putInt(prefix+"cursor",cursor).apply()
    }
    @Synchronized fun clear() {
        file.delete()
        prefs.edit().remove(prefix+"sha").remove(prefix+"cursor").remove(prefix+"recent").commit()
    }
}
