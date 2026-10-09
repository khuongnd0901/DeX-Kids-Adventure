package com.khuongnd.dexkids.ai

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.MessageDigest

/**
 * Offline cache of generated question wording only. Never stores GPS positions,
 * child utterances, conversation history or API keys. Invalidates when source changes.
 */
class KidsAiQuizCache(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kids_ai_quiz_cache_v1",Context.MODE_PRIVATE)
    private val ttlMillis = 14L * 24 * 3600 * 1000
    private fun key(id: String, age: Int): String {
        require(id.matches(Regex("osm:(node|way|relation):[1-9][0-9]{0,15}")))
        require(age in 2..6)
        return id.replace(":", "_") + "_age" + age
    }
    private fun digest(fact: String, base: KidsQuiz): String {
        val hash = MessageDigest.getInstance("SHA-256")
            .digest((fact + "|" + base.question + "|" + base.answer).toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }
    fun get(id: String, age: Int, fact: String, base: KidsQuiz): List<KidsQuiz> {
        val raw = prefs.getString(key(id,age),null) ?: return emptyList()
        return runCatching {
            val json = JSONObject(raw)
            check(json.getString("source") == digest(fact,base))
            check(System.currentTimeMillis() - json.getLong("created") in 0..ttlMillis)
            val entries = json.getJSONArray("entries")
            check(entries.length() in 10..20)
            List(entries.length()) { i ->
                val row = entries.getJSONObject(i)
                val q = row.getString("q")
                val a = row.getString("a")
                val follow = row.getString("f")
                check(q.endsWith("?") && KidsAiSafety.safe(q,150) &&
                    a.length in 12..150 && KidsAiSafety.safe(follow,150))
                KidsQuiz(q,a,follow)
            }
        }.getOrDefault(emptyList())
    }
    fun put(id: String, age: Int, fact: String, base: KidsQuiz, entries: List<KidsQuiz>) {
        require(entries.size in 10..20)
        val allowed = (listOf(base.answer) +
            fact.split(Regex("(?<=[.!?])\\s+")).map { it.trim() })
            .filter { it.length in 12..150 }.toSet()
        require(entries.all {
            it.answer in allowed && it.question.endsWith("?") &&
                KidsAiSafety.safe(it.question,150) && KidsAiSafety.safe(it.followUp,150)
        })
        require(entries.map { it.question.lowercase() }.toSet().size == entries.size)
        val arr = JSONArray()
        entries.forEach { arr.put(JSONObject().put("q",it.question)
            .put("a",it.answer).put("f",it.followUp)) }
        val json = JSONObject().put("version",1).put("source",digest(fact,base))
            .put("created",System.currentTimeMillis()).put("entries",arr).toString()
        require(json.length < 16000)
        check(prefs.edit().putString(key(id,age),json).commit())
    }
    fun next(id: String, age: Int, fact: String, base: KidsQuiz): KidsQuiz? {
        val questions = get(id,age,fact,base)
        if (questions.isEmpty()) return null
        val counter = key(id,age) + "_cursor"
        val idx = prefs.getInt(counter,0).coerceAtLeast(0)
        prefs.edit().putInt(counter,(idx+1)%questions.size).apply()
        return questions[idx%questions.size]
    }
    fun clear() { prefs.edit().clear().apply() }
}
