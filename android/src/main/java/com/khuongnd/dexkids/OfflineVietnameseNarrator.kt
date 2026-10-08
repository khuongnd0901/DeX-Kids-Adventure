package com.khuongnd.dexkids

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import java.util.Locale

/**
 * Parent opt-in is mandatory. Never silently use a network Vietnamese voice.
 * The adapter is not instantiated by the child activity until parent controls exist.
 */
class OfflineVietnameseNarrator(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    @Volatile private var ready = false
    @Volatile private var parentApproved = false
    private var focusRequest: AudioFocusRequest? = null
    private val engine: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            // Callback is asynchronous; availability must be checked again on use.
            ready = true
        }
    }

    fun setParentApproved(approved: Boolean) {
        parentApproved = approved
        if (!approved) stop()
    }

    fun speakReviewed(textVi: String): Boolean {
        if (!parentApproved || !ready || textVi.isBlank() || textVi.length > 240) return false
        // Never call setLanguage alone; it may select a network voice.
        val offlineVoice = engine.voices?.firstOrNull {
            it.locale.language == Locale("vi").language && !it.isNetworkConnectionRequired
        } ?: return false
        if (engine.setVoice(offlineVoice) != TextToSpeech.SUCCESS) return false
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT)
                    engine.stop()
            }.build()
        if (audio.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return false
        focusRequest = request
        return engine.speak(textVi, TextToSpeech.QUEUE_FLUSH, null, "dex_kids_reviewed") == TextToSpeech.SUCCESS
    }

    fun stop() {
        if (ready) engine.stop()
        focusRequest?.let { audio.abandonAudioFocusRequest(it) }
        focusRequest = null
    }

    fun shutdown() { stop(); engine.shutdown(); ready = false }
}
