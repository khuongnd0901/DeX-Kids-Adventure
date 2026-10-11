package com.khuongnd.dexkids

import android.content.Context

/** All settings stored on the phone, with no network or analytics. */
class ParentSettings(context: Context) {
    private val prefs = context.getSharedPreferences("parent_settings", Context.MODE_PRIVATE)
    var sessionMinutes: Int
        get() = prefs.getInt("session_minutes", 30).coerceIn(10, 60)
        set(value) { prefs.edit().putInt("session_minutes", value.coerceIn(10, 60)).apply() }
    var ageGroup: Int
        get() = prefs.getInt("age", 4).coerceIn(2, 6)
        set(value) { prefs.edit().putInt("age", value.coerceIn(2, 6)).apply() }
    /** T-022: local audience choice, used for LIVE, DEMO and GPX. */
    var audienceMode: String
        get() = prefs.getString("audience_mode", "BOTH")
            ?.takeIf { it == "SAU" || it == "ONG" || it == "BOTH" } ?: "BOTH"
        set(value) {
            require(value == "SAU" || value == "ONG" || value == "BOTH")
            prefs.edit().putString("audience_mode", value).apply()
        }
    val activeAge: Int get() = if (audienceMode == "SAU") 5 else 4
    val audienceLabel: String get() = when (audienceMode) {
        "SAU" -> "Sâu · 5 tuổi"
        "ONG" -> "Ong · 4 tuổi"
        else -> "Sâu và Ong · 4–5 tuổi"
    }
    /** T-024: small foreground-only cartoon effects; no system volume changes. */
    var audioEffects: Boolean
        get() = prefs.getBoolean("audio_effects", true)
        set(value) { prefs.edit().putBoolean("audio_effects", value).apply() }
    /** Background melody defaults OFF to avoid competing with navigation instructions. */
    var ambientMusic: Boolean
        get() = prefs.getBoolean("ambient_music", false)
        set(value) { prefs.edit().putBoolean("ambient_music", value).apply() }
    var allowOfflineSpeech: Boolean
        get() = prefs.getBoolean("offline_tts", true)
        set(value) { prefs.edit().putBoolean("offline_tts", value).apply() }
    /** Enabled preference by default; Android RECORD_AUDIO grant and on-device recognizer still gate listening. */
    var allowChildMicrophone: Boolean
        get() = prefs.getBoolean("child_mic_optin", true)
        set(value) { prefs.edit().putBoolean("child_mic_optin", value).apply() }
    /** Minimal graphics; does not lock or turn off the external display. */
    var audioOnly: Boolean
        get() = prefs.getBoolean("audio_only", false)
        set(value) { prefs.edit().putBoolean("audio_only", value).apply() }

    /** Last parent-selected knowledge corridor. Stories are not GPS road-matched. */
    var selectedRouteId: String
        get() = prefs.getString("route_id", "dong-nai-vung-tau")
            ?.takeIf { com.khuongnd.dexkids.story.RouteKnowledgeCatalog.isSupportedRoute(it) }
            ?: "dong-nai-vung-tau"
        set(value) {
            require(com.khuongnd.dexkids.story.RouteKnowledgeCatalog.isSupportedRoute(value))
            prefs.edit().putString("route_id", value).apply()
        }

    /** Only a local content slot, no response/history or child behavior data. */
    fun takeEntertainmentOpeningOffset(): Int {
        val key = "entertainment_opening_$audienceMode"
        val offset = Math.floorMod(prefs.getInt(key, 1), 18)
        check(prefs.edit().putInt(key, (offset + 1) % 18).commit())
        return offset
    }

    fun resetLocalOptions() { prefs.edit().clear().apply() }
}
