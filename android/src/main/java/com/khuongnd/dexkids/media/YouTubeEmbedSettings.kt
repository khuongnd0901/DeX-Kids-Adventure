package com.khuongnd.dexkids.media

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/** No key is put in HTML, logs, source or backups (application backup is disabled). */
class YouTubeEmbedSettings(private val context: Context) {
    private val prefs = context.getSharedPreferences("youtube_embed_v1", Context.MODE_PRIVATE)
    init { importPrivateDebugSeed() }
    /** Only an explicitly provisioned debuggable APK may seed a key; no opt-in gates change. */
    private fun importPrivateDebugSeed() {
        if (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE == 0) return
        runCatching {
            val raw = try {
                context.assets.open("youtube-test-key.json").use { input ->
                    val bytes = ByteArray(1025)
                    val count = input.read(bytes)
                    require(count in 1..1024 && input.read() == -1)
                    String(bytes, 0, count, Charsets.UTF_8)
                }
            } catch (_: java.io.FileNotFoundException) { return }
            val key = org.json.JSONObject(raw).getString("key")
            require(key.matches(Regex("[A-Za-z0-9_-]{20,200}")))
            val fingerprint = java.security.MessageDigest.getInstance("SHA-256").digest(key.toByteArray())
                .joinToString("") { "%02x".format(it) }
            if (prefs.getString("debug_seed_handled", "") == fingerprint) return
            // Keep consent, reference, country, contact, selection and library switches unchanged.
            configure(key, notificationReference, contact, country, consented)
            check(prefs.edit().putString("debug_seed_handled", fingerprint).commit())
        }.onFailure { android.util.Log.w("KidsYouTube", "Private debug key import unavailable") }
    }
    var selected: Boolean
        get() = prefs.getBoolean("selected", true)
        set(value) { check(prefs.edit().putBoolean("selected", value).commit()) }
    val notificationReference get() = prefs.getString("notification", "").orEmpty()
    val contact get() = prefs.getString("contact", "").orEmpty()
    val country get() = prefs.getString("country", "VN").orEmpty()
    val consented get() = prefs.getBoolean("consent_v1", false)
    fun key(): String = runCatching {
        val raw = prefs.getString("key", null) ?: return ""
        val parts = raw.split(":")
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, secret(), GCMParameterSpec(128, Base64.decode(parts[0], Base64.NO_WRAP)))
        String(cipher.doFinal(Base64.decode(parts[1], Base64.NO_WRAP)), Charsets.UTF_8)
    }.getOrDefault("")
    // Personal sideload configuration; readiness is not Google designation/certification.
    fun ready() = consented &&
        country.matches(Regex("[A-Z]{2}")) && key().isNotBlank()
    fun configure(newKey: String, notification: String, operatorContact: String, region: String, consent: Boolean) {
        require(notification.length <= 500 && operatorContact.length <= 500 && region.matches(Regex("[A-Z]{2}")))
        val edit = prefs.edit().putString("notification", notification.trim()).putString("contact", operatorContact.trim())
            .putString("country", region).putBoolean("consent_v1", consent)
        if (newKey.isNotBlank()) {
            require(newKey.matches(Regex("[A-Za-z0-9_-]{20,200}")))
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, secret())
            edit.putString("key", Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
                Base64.encodeToString(cipher.doFinal(newKey.toByteArray(Charsets.UTF_8)), Base64.NO_WRAP))
        }
        check(edit.commit())
    }
    fun delete() {
        // Remember the handled seed so deleting credentials cannot silently reimport them.
        val handled = prefs.getString("debug_seed_handled", "")
        check(prefs.edit().clear().putBoolean("selected", true).putString("debug_seed_handled", handled).commit())
        runCatching { KeyStore.getInstance("AndroidKeyStore").apply { load(null) }.deleteEntry(context.packageName + ".youtube_embed_key_v1") }
    }
    private fun secret(): SecretKey {
        val alias = context.packageName + ".youtube_embed_key_v1"
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (store.getKey(alias, null) as? SecretKey)?.let { return it }
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore").apply {
            init(KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).build())
        }.generateKey()
    }
}
