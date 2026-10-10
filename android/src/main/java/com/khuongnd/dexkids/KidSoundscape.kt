package com.khuongnd.dexkids

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin
import kotlin.math.max
import kotlin.math.min

/**
 * T-024: lightweight, synthesized, entirely offline cartoon audio.
 * Does not request audio focus, modify system volume, record audio or stream music.
 * Narration pauses ambient playback; external navigation-app mixing needs Fold3 QA.
 */
internal class KidSoundscape(context: Context) {
    private val settings = ParentSettings(context)
    private val main = Handler(Looper.getMainLooper())
    private val cache = context.cacheDir
    private val sampleFiles = mutableListOf<File>()
    private val pool = SoundPool.Builder().setMaxStreams(2)
        .setAudioAttributes(AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build())
        .build()
    private val ids = mutableMapOf<String, Int>()
    private val loaded = mutableSetOf<Int>()
    private var musicStream = 0
    private var speechActive = false
    private var paused = true
    @Volatile private var disposed = false

    init {
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            main.post {
                if (!disposed && status == 0) {
                    loaded.add(sampleId)
                    updateAmbient()
                }
            }
        }
        Thread({
            val types = listOf("chime", "bird", "cat", "rabbit", "bus", "ambient")
            val generated = mutableListOf<Pair<String, File>>()
            for (type in types) {
                if (disposed) break
                runCatching {
                    val file = File(cache, "dexkids-t024-v1-$type.wav")
                    file.writeBytes(synthesize(type))
                    generated.add(type to file)
                }
            }
            main.post {
                if (!disposed) {
                    for ((name, file) in generated) {
                        sampleFiles.add(file)
                        val id = pool.load(file.absolutePath, 1)
                        if (id > 0) ids[name] = id
                    }
                } else generated.forEach { it.second.delete() }
            }
        }, "dexkids-offline-soundscape").apply { isDaemon = true; start() }
    }

    /** Separate from OS audio focus; never ducks Maps/VietMap deliberately. */
    fun setPaused(value: Boolean) {
        paused = value
        if (value) pool.autoPause() else pool.autoResume()
        updateAmbient()
    }

    fun setSpeechActive(value: Boolean) {
        speechActive = value
        updateAmbient()
    }

    fun playForBeat(id: String) {
        if (disposed || paused || speechActive || !settings.audioEffects) return
        val type = when {
            id.contains("bird") -> "bird"
            id.contains("cat") -> "cat"
            id.contains("rabbit") -> "rabbit"
            id.contains("bus") || id.contains("beep") || id.contains("wheels") -> "bus"
            else -> "chime"
        }
        val soundId = ids[type] ?: return
        if (soundId in loaded) pool.play(soundId, 0.14f, 0.14f, 1, 0, 1f)
    }

    private fun updateAmbient() {
        if (disposed) return
        val shouldPlay = settings.ambientMusic && !paused && !speechActive
        if (!shouldPlay) {
            if (musicStream != 0) pool.stop(musicStream)
            musicStream = 0
            return
        }
        if (musicStream != 0) return
        val ambient = ids["ambient"] ?: return
        if (ambient in loaded)
            musicStream = pool.play(ambient, 0.035f, 0.035f, 0, -1, 1f)
    }

    fun shutdown() {
        if (disposed) return
        disposed = true
        if (musicStream != 0) pool.stop(musicStream)
        musicStream = 0
        pool.release()
        sampleFiles.forEach { it.delete() }
        sampleFiles.clear()
        ids.clear()
        loaded.clear()
    }

    private fun synthesize(type: String): ByteArray {
        val rate = 16_000
        val duration = if (type == "ambient") 4.0 else 0.42
        val samples = (rate * duration).toInt()
        val bodyBytes = samples * 2
        val out = ByteBuffer.allocate(44 + bodyBytes).order(ByteOrder.LITTLE_ENDIAN)
        out.put("RIFF".toByteArray(Charsets.US_ASCII))
        out.putInt(bodyBytes + 36)
        out.put("WAVE".toByteArray(Charsets.US_ASCII))
        out.put("fmt ".toByteArray(Charsets.US_ASCII))
        out.putInt(16)
        out.putShort(1)
        out.putShort(1)
        out.putInt(rate)
        out.putInt(rate * 2)
        out.putShort(2)
        out.putShort(16)
        out.put("data".toByteArray(Charsets.US_ASCII))
        out.putInt(bodyBytes)
        val melody = doubleArrayOf(523.25, 659.25, 783.99, 659.25, 587.33, 698.46, 523.25, 392.0)
        for (i in 0 until samples) {
            val t = i.toDouble() / rate
            val fade = min(1.0, min(t / 0.025, (duration - t) / 0.05)).coerceAtLeast(0.0)
            val signal = when (type) {
                "ambient" -> {
                    val note = (t / 0.5).toInt().coerceIn(0, melody.size - 1)
                    val phase = t % 0.5
                    val envelope = min(1.0, phase / 0.08) * min(1.0, (0.5 - phase) / 0.1)
                    (sin(2 * PI * melody[note] * t) * 0.35 +
                        sin(2 * PI * melody[note] * 0.5 * t) * 0.12) * envelope
                }
                "bird" -> sin(2 * PI * (1200 * t + 500 * t * t)) * 0.35
                "cat" -> sin(2 * PI * (620 * t - 170 * t * t)) * (0.24 + 0.2 * sin(2 * PI * 8 * t))
                "rabbit" -> sin(2 * PI * (350 * t + 600 * t * t)) * 0.4
                "bus" -> sin(2 * PI * 430 * t) * (if (t < 0.17 || t in 0.23..0.40) 0.34 else 0.0)
                else -> (sin(2 * PI * 880 * t) + 0.5 * sin(2 * PI * 1175 * t)) * 0.22
            }
            out.putShort((max(-1.0, min(1.0, signal * fade)) * 32767).toInt().toShort())
        }
        return out.array()
    }
}
