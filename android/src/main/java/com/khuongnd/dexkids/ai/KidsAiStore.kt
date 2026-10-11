package com.khuongnd.dexkids.ai

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

enum class KidsAiProvider(val title: String) { GEMINI("Gemini"), GROQ("Groq") }

/** Features preferred ON by default; no AI call without key, model and Free Tier acknowledgment. Child cloud consent remains OFF. */
class KidsAiSettings(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kids_ai_settings_v1", Context.MODE_PRIVATE)
    var enabled: Boolean
        get() = prefs.getBoolean("ai_quizzes", true)
        set(v) { prefs.edit().putBoolean("ai_quizzes", v).apply() }
    /** Separate opt-in; existing POI AI preference cannot silently enable per-trip generation. */
    var automaticPacks: Boolean
        get() = prefs.getBoolean("automatic_trip_packs", false)
        set(v) { prefs.edit().putBoolean("automatic_trip_packs", v).commit() }
    var cloudChildReply: Boolean
        get() = prefs.getBoolean("child_text_cloud_explicit", false)
        set(v) { prefs.edit().putBoolean("child_text_cloud_explicit", v).apply() }
    /** T-029: separate offline-only trip companion opt-in; child-cloud consent remains independent. */
    var tripCompanion: Boolean
        get() = prefs.getBoolean("ai_trip_companion_optin", false)
        set(value) { prefs.edit().putBoolean("ai_trip_companion_optin", value).commit() }
    var provider: KidsAiProvider
        get() = runCatching {
            KidsAiProvider.valueOf(prefs.getString("provider", "GEMINI") ?: "GEMINI")
        }.getOrDefault(KidsAiProvider.GEMINI)
        set(v) { prefs.edit().putString("provider", v.name).apply() }
    fun active(p: KidsAiProvider) = prefs.getBoolean(p.name + "_enabled", true)
    fun setActive(p: KidsAiProvider, v: Boolean) {
        prefs.edit().putBoolean(p.name + "_enabled", v).apply()
    }
    fun model(p: KidsAiProvider) = prefs.getString(p.name + "_model", "") ?: ""
    fun setModel(p: KidsAiProvider, value: String) {
        require(value.length in 1..100 && value.matches(Regex("[a-zA-Z0-9._/-]+")))
        prefs.edit().putString(p.name + "_model", value)
            .putBoolean(p.name + "_free_ack", false).apply()
    }
    fun freeTierAcknowledged(p: KidsAiProvider) = prefs.getBoolean(p.name + "_free_ack", false)
    fun setFreeTierAcknowledged(p: KidsAiProvider, v: Boolean) {
        prefs.edit().putBoolean(p.name + "_free_ack", v).apply()
    }
}

class KidsAiKeys(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("kids_ai_keys_v1", Context.MODE_PRIVATE)
    private fun alias(provider: KidsAiProvider) = "dexkids-ai-v1-" + provider.name
    private fun key(provider: KidsAiProvider): SecretKey {
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias(provider), null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").run {
            init(KeyGenParameterSpec.Builder(alias(provider),
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true).build())
            generateKey()
        }
    }
    @Synchronized fun save(provider: KidsAiProvider, secret: String) {
        val value = secret.trim()
        require(value.length in 8..1024 && !value.any { it.isWhitespace() })
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key(provider))
        val bytes = cipher.doFinal(value.toByteArray(Charsets.UTF_8))
        check(prefs.edit().putString(provider.name + "_cipher",
            Base64.encodeToString(bytes, Base64.NO_WRAP))
            .putString(provider.name + "_iv", Base64.encodeToString(cipher.iv, Base64.NO_WRAP))
            .commit())
    }
    @Synchronized fun load(provider: KidsAiProvider): String? {
        val cipherText = prefs.getString(provider.name + "_cipher", null) ?: return null
        val iv = prefs.getString(provider.name + "_iv", null) ?: return null
        return runCatching {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, key(provider),
                GCMParameterSpec(128, Base64.decode(iv, Base64.NO_WRAP)))
            String(cipher.doFinal(Base64.decode(cipherText, Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrNull()
    }
    @Synchronized fun delete(provider: KidsAiProvider) {
        check(prefs.edit().remove(provider.name + "_cipher")
            .remove(provider.name + "_iv").commit())
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        if (store.containsAlias(alias(provider))) store.deleteEntry(alias(provider))
    }
    fun configured(provider: KidsAiProvider) = load(provider) != null
}
