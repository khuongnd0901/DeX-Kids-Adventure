package com.khuongnd.dexkids

import android.content.Context

/** All settings stored on the phone, with no network or analytics. */
class ParentSettings(context: Context) {
    private val prefs = context.getSharedPreferences("parent_settings", Context.MODE_PRIVATE)
    var sessionMinutes: Int
        get() = prefs.getInt("session_minutes", 30).coerceIn(10, 60)
        set(value) { prefs.edit().putInt("session_minutes", value.coerceIn(10, 60)).apply() }
    var quiet: Boolean
        get() = prefs.getBoolean("quiet", true)
        set(value) { prefs.edit().putBoolean("quiet", value).apply() }
    var ageGroup: Int
        get() = prefs.getInt("age", 4).coerceIn(2, 6)
        set(value) { prefs.edit().putInt("age", value.coerceIn(2, 6)).apply() }
    var allowOfflineSpeech: Boolean
        get() = prefs.getBoolean("offline_tts", false)
        set(value) { prefs.edit().putBoolean("offline_tts", value).apply() }
    /** Minimal graphics; does not lock or turn off the external display. */
    var audioOnly: Boolean
        get() = prefs.getBoolean("audio_only", false)
        set(value) { prefs.edit().putBoolean("audio_only", value).apply() }

    fun resetLocalOptions() { prefs.edit().clear().apply() }
}
