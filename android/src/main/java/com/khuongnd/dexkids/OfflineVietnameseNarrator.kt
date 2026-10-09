package com.khuongnd.dexkids

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.os.Handler
import android.os.Looper
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
    private val mainHandler = Handler(Looper.getMainLooper())
    @Volatile private var currentFinish: (() -> Unit)? = null
    private var utteranceSequence = 0L
    private fun finishSpeaking() {
        val finished = currentFinish
        currentFinish = null
        focusRequest?.let { audio.abandonAudioFocusRequest(it) }
        focusRequest = null
        finished?.let { mainHandler.post { it.invoke() } }
    }
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

    fun speakReviewed(textVi: String): Boolean =
        speakReviewed(textVi, {}, {})

    /**
     * onStarted is called only by the offline TTS engine's *real* onStart;
     * onFinished releases narration animation even after errors/focus loss.
     */
    fun speakReviewed(textVi: String, onStarted: () -> Unit, onFinished: () -> Unit): Boolean {
        if (!parentApproved || !ready || textVi.isBlank() || textVi.length > 240) return false
        // Never call setLanguage alone; it may select a network voice.
        val offlineVoice = engine.voices?.firstOrNull {
            it.locale.language == "vi" && !it.isNetworkConnectionRequired
        } ?: return false
        if (engine.setVoice(offlineVoice) != TextToSpeech.SUCCESS) return false
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS || change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    engine.stop()
                    finishSpeaking()
                }
            }.build()
        if (audio.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED)
            return false
        stop()
        focusRequest = request
        currentFinish = onFinished
        val id = "dexkids_reviewed_" + (++utteranceSequence)
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                if (utteranceId == id) mainHandler.post { if (currentFinish != null) onStarted() }
            }
            override fun onDone(utteranceId: String?) {
                if (utteranceId == id) mainHandler.post { finishSpeaking() }
            }
            override fun onError(utteranceId: String?) {
                if (utteranceId == id) mainHandler.post { finishSpeaking() }
            }
        })
        val success = engine.speak(textVi, TextToSpeech.QUEUE_FLUSH, null, id) == TextToSpeech.SUCCESS
        if (!success) finishSpeaking()
        return success
    }

    fun stop() {
        if (ready) engine.stop()
        finishSpeaking()
    }

    fun shutdown() { stop(); engine.shutdown(); ready = false }
}
