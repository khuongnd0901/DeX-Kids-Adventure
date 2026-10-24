package com.khuongnd.dexkids.ai

import android.content.Context

/** Legacy feature preferences retained; subscription consent is separate and defaults OFF. Child cloud consent remains OFF. */
class KidsAiSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kids_ai_settings_v1", Context.MODE_PRIVATE)
    var enabled: Boolean
        get() = prefs.getBoolean("ai_quizzes", true)
        set(v) { prefs.edit().putBoolean("ai_quizzes", v).apply() }
    /** Separate opt-in; existing POI AI preference cannot silently enable per-trip generation. */
    var automaticPacks: Boolean
        get() = prefs.getBoolean("automatic_trip_packs", false)
        set(v) { prefs.edit().putBoolean("automatic_trip_packs", v).commit() }
    /** Allow original AI-authored questions/stories instead of selecting curated IDs.
     * Independent parent acknowledgment, default OFF; no child data or GPS.
     */
    var creativePacks: Boolean
        get() = prefs.getBoolean("creative_trip_packs", false)
        set(v) { prefs.edit().putBoolean("creative_trip_packs",v).commit() }
    var cloudChildReply: Boolean
        get() = prefs.getBoolean("child_text_cloud_explicit", false)
        set(v) { prefs.edit().putBoolean("child_text_cloud_explicit", v).apply() }
    /** T-029: separate offline-only trip companion opt-in; child-cloud consent remains independent. */
    var tripCompanion: Boolean
        get() = prefs.getBoolean("ai_trip_companion_optin", false)
        set(value) { prefs.edit().putBoolean("ai_trip_companion_optin", value).commit() }
}
