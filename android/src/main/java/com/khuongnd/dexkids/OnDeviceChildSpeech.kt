package com.khuongnd.dexkids

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognitionSupport
import android.speech.RecognitionSupportCallback
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

/**
 * One-shot, main-thread, on-device ONLY Vietnamese ASR.
 * No default/network recognizer, audio file, raw transcript logging, service or hot microphone.
 */
class OnDeviceChildSpeech(private val context: Context) {
    private val handler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var response: ((String?) -> Unit)? = null
    private var epoch = 0L
    private var timeout: Runnable? = null

    companion object {
        fun available(context: Context): Boolean =
            Build.VERSION.SDK_INT >= 31 &&
                SpeechRecognizer.isOnDeviceRecognitionAvailable(context)
    }

    fun listenOnce(onResult: (String?) -> Unit): Boolean {
        check(Looper.myLooper() == Looper.getMainLooper()) { "SpeechRecognizer requires main thread" }
        if (response != null || context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED || !available(context)) return false
        val engine = try {
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        } catch (_: UnsupportedOperationException) { return false }
          catch (_: SecurityException) { return false }
        epoch++
        val generation = epoch
        recognizer = engine
        response = onResult
        val request = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // additional hint, not the security boundary
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        engine.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
            override fun onError(error: Int) { complete(generation, null) }
            override fun onResults(results: Bundle?) {
                val text = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()?.take(160)?.takeIf { it.isNotBlank() }
                complete(generation, text)
            }
        })
        val deadline = Runnable { complete(generation, null) }
        timeout = deadline
        handler.postDelayed(deadline, 9_000L)
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                // On-device recognizer alone does not prove a Vietnamese model is installed.
                // On supported Android versions verify the local pack before opening mic.
                engine.checkRecognitionSupport(request, context.mainExecutor,
                    object : RecognitionSupportCallback {
                        override fun onSupportResult(recognitionSupport: RecognitionSupport) {
                            if (generation != epoch || response == null) return
                            val installed = recognitionSupport.installedOnDeviceLanguages.any {
                                it.equals("vi", true) || it.startsWith("vi-", true) ||
                                    it.startsWith("vi_", true)
                            }
                            if (installed) startEngine(engine, request, generation)
                            else complete(generation, null)
                        }
                        override fun onError(error: Int) { complete(generation, null) }
                    })
            } else {
                // Android 12 may lack language support query, but the FACTORY is on-device only.
                // Language failure is handled by onError; never fall back to default recognizer.
                startEngine(engine, request, generation)
            }
        } catch (_: RuntimeException) {
            complete(generation, null)
        }
        return true
    }

    private fun startEngine(engine: SpeechRecognizer, request: Intent, generation: Long) {
        if (generation != epoch || response == null) return
        try { engine.startListening(request) }
        catch (_: RuntimeException) { complete(generation, null) }
    }

    private fun complete(generation: Long, transcript: String?) {
        if (generation != epoch || response == null) return
        val callback = response
        release()
        callback?.invoke(transcript) // memory-only; caller must not persist/log raw speech
    }

    private fun release() {
        epoch++
        timeout?.let(handler::removeCallbacks)
        timeout = null
        response = null
        recognizer?.let {
            try { it.cancel() } catch (_: RuntimeException) {}
            it.destroy()
        }
        recognizer = null
    }

    fun cancel() {
        check(Looper.myLooper() == Looper.getMainLooper())
        release()
    }
}