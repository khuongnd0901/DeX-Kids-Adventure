package com.khuongnd.dexkids

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.os.Handler
import android.os.Looper

/**
 * Parent-enabled on-device Vietnamese TTS only. Focus is transient/may duck,
 * not permanent media focus. Cancelled utterances never start child listening.
 */
class OfflineVietnameseNarrator(context: Context) {
    private val audio = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val narrationAge = ParentSettings(context).activeAge
    @Volatile private var ready = false
    @Volatile private var parentApproved = false
    private var focusRequest: AudioFocusRequest? = null
    private var currentUtteranceId: String? = null
    private var currentFinish: (() -> Unit)? = null
    private var utteranceSequence = 0L

    private val engine: TextToSpeech = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) ready = true
    }

    fun setParentApproved(approved: Boolean) {
        parentApproved = approved
        if (!approved) stop()
    }

    fun speakReviewed(textVi: String): Boolean = speakReviewed(textVi, {}, {})

    /** Completion callback runs only after genuine matching TTS onDone/onError. */
    fun speakReviewed(textVi: String, onStarted: () -> Unit, onFinished: () -> Unit): Boolean {
        if (!parentApproved || !ready || textVi.isBlank() || textVi.length > 240) return false
        // Never select a network TTS voice even when connectivity is available.
        val voice = engine.voices?.filter {
            it.locale.language == "vi" && !it.isNetworkConnectionRequired
        }?.maxByOrNull { it.quality } ?: return false
        if (engine.setVoice(voice) != TextToSpeech.SUCCESS) return false
        engine.setSpeechRate(if (narrationAge <= 3) 0.88f else 0.94f)
        engine.setPitch(1.02f)

        stop() // Invalidate any previous completion before a new utterance.
        val id = "dexkids_reviewed_" + (++utteranceSequence)
        val attributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        val request = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
            .setAudioAttributes(attributes)
            .setOnAudioFocusChangeListener { change ->
                if (change == AudioManager.AUDIOFOCUS_LOSS ||
                    change == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                    mainHandler.post { if (currentUtteranceId == id) stop() }
                }
            }.build()
        if (audio.requestAudioFocus(request) != AudioManager.AUDIOFOCUS_REQUEST_GRANTED) return false
        focusRequest = request
        currentUtteranceId = id
        currentFinish = onFinished
        engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                mainHandler.post {
                    if (currentUtteranceId == id && utteranceId == id) onStarted()
                }
            }
            override fun onDone(utteranceId: String?) {
                mainHandler.post { completeIfCurrent(utteranceId) }
            }
            override fun onError(utteranceId: String?) {
                mainHandler.post { completeIfCurrent(utteranceId) }
            }
        })
        if (engine.speak(textVi, TextToSpeech.QUEUE_FLUSH, null, id) != TextToSpeech.SUCCESS) {
            stop()
            return false
        }
        return true
    }

    private fun completeIfCurrent(id: String?) {
        if (id == null || currentUtteranceId != id) return
        val done = currentFinish
        currentFinish = null
        currentUtteranceId = null
        releaseFocus()
        done?.invoke()
    }

    private fun releaseFocus() {
        val previous = focusRequest
        focusRequest = null
        previous?.let { audio.abandonAudioFocusRequest(it) }
    }

    /** Never invoke normal completion after cancellation: prevents stale follow-ups. */
    fun stop() {
        currentUtteranceId = null
        currentFinish = null
        if (ready) engine.stop()
        releaseFocus()
    }

    fun shutdown() {
        stop()
        ready = false
        engine.shutdown()
    }
}
