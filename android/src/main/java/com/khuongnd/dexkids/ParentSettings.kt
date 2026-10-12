package com.khuongnd.dexkids

import android.content.Context

/** All settings stored on the phone, with no network or analytics. */
class ParentSettings(context: Context) {
    private val prefs = context.getSharedPreferences("parent_settings", Context.MODE_PRIVATE)
    var pinCredential: String?
        get() = prefs.getString("pin_credential", null)
        set(value) { prefs.edit().putString("pin_credential", value).commit() }
    var pinRetryAfter: Long
        get() = prefs.getLong("pin_retry_after", 0)
        set(value) { prefs.edit().putLong("pin_retry_after", value).commit() }
    var pinFailures: Int
        get() = prefs.getInt("pin_failures", 0)
        set(value) { prefs.edit().putInt("pin_failures", value).commit() }
    var audioOnly: Boolean
        get() = prefs.getBoolean("audio_only", false)
        set(value) { prefs.edit().putBoolean("audio_only", value).apply() }
    var sessionMinutes: Int
        get() = prefs.getInt("session_minutes", 30).coerceIn(10, 60)
        set(value) { prefs.edit().putInt("session_minutes", value.coerceIn(10, 60)).apply() }
    var ageGroup: Int
        get() = prefs.getInt("age", 4).coerceIn(4, 6)
        set(value) { prefs.edit().putInt("age", value.coerceIn(4, 6)).apply() }
    /** T-022: local audience choice, used for LIVE, DEMO and GPX. */
    var audienceMode: String
        get() = prefs.getString("audience_mode", "BOTH")
            ?.takeIf { it == "SAU" || it == "ONG" || it == "BOTH" } ?: "BOTH"
        set(value) {
            require(value == "SAU" || value == "ONG" || value == "BOTH")
            prefs.edit().putString("audience_mode", value).apply()
        }
    /** Parent-selected learning ages (4–7); default keeps Sâu 5 / Ong 4. */
    var sauAge: Int
        get() = prefs.getInt("sau_age",5).coerceIn(4,7)
        set(value) { prefs.edit().putInt("sau_age",value.coerceIn(4,7)).apply() }
    var ongAge: Int
        get() = prefs.getInt("ong_age",4).coerceIn(4,7)
        set(value) { prefs.edit().putInt("ong_age",value.coerceIn(4,7)).apply() }
    fun ageForFocus(focus: com.khuongnd.dexkids.story.EntertainmentDirector.Audience): Int =
        when(focus) {
            com.khuongnd.dexkids.story.EntertainmentDirector.Audience.SAU -> sauAge
            com.khuongnd.dexkids.story.EntertainmentDirector.Audience.ONG -> ongAge
            com.khuongnd.dexkids.story.EntertainmentDirector.Audience.BOTH -> minOf(sauAge,ongAge)
        }
    /** Legacy authored catalog is age 4–6; AI creative lessons use the configured 4–7. */
    val activeAge: Int get() = minOf(6, if (audienceMode == "SAU") sauAge else
        if (audienceMode == "ONG") ongAge else minOf(sauAge,ongAge))
    val audienceLabel: String get() = when (audienceMode) {
        "SAU" -> "Sâu · ${sauAge} tuổi"
        "ONG" -> "Ong · ${ongAge} tuổi"
        else -> "Sâu và Ong · ${ongAge}–${sauAge} tuổi"
    }
    /** T-024: small foreground-only cartoon effects; no system volume changes. */
    var audioEffects: Boolean
        get() = prefs.getBoolean("audio_effects", true)
        set(value) { prefs.edit().putBoolean("audio_effects", value).apply() }
    /** Parent requested background melody ON for DEMO and LIVE; still manually switchable. */
    var ambientMusic: Boolean
        get() = prefs.getBoolean("ambient_music", true)
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
