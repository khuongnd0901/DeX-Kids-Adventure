package com.khuongnd.dexkids.ai

import android.content.Context
import org.json.JSONArray

/** New opt-in only. Never reads, deletes or rewrites legacy credentials or saved packs. */
class ChatGptSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kids_chatgpt_subscription_v1", Context.MODE_PRIVATE)
    var enabled: Boolean
        get() = prefs.getBoolean("consent", false)
        set(value) { check(prefs.edit().putBoolean("consent", value).commit()) }
    fun model() = prefs.getString("model", "").orEmpty()
    fun models(connection: String?): List<ChatGptModelRepository.Model> {
        if (connection == null || prefs.getString("connection", null) != connection) return emptyList()
        return runCatching {
            val rows = JSONArray(prefs.getString("catalog", "[]"))
            (0 until rows.length()).map { i -> rows.getJSONObject(i).let {
                ChatGptModelRepository.Model(it.getString("slug"), it.getString("display_name"))
            } }
        }.getOrDefault(emptyList())
    }
    fun saveCatalog(connection: String, models: List<ChatGptModelRepository.Model>) {
        val rows = JSONArray()
        models.forEach { rows.put(org.json.JSONObject().put("slug", it.slug).put("display_name", it.displayName)) }
        check(prefs.edit().putString("connection", connection).putString("catalog", rows.toString()).commit())
    }
    fun select(connection: String?, model: String) {
        require(models(connection).any { it.slug == model })
        check(prefs.edit().putString("model", model).commit())
    }
}
