package com.example.coolcore

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class WoodTone(val label: String, val baseFreq: Double, val overtoneRatio: Double, val decay: Double) {
    SANDALWOOD("深山檀木", 390.0, 2.15, 26.0),
    CRISP("清脆空灵", 520.0, 2.02, 30.0),
    JADE("冰润冷玉", 690.0, 1.85, 22.0)
}

class WoodenFishAudio {
    private val sampleRate = 44100
    private val poolSize = 4
    private var tracks = Array<AudioTrack?>(poolSize) { null }
    private var nextIndex = 0

    init { loadTone(WoodTone.SANDALWOOD) }

    fun loadTone(tone: WoodTone) {
        release()
        val pcm = synthesizeWoodHit(tone)
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        val fmt = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
        for (i in 0 until poolSize) {
            try {
                val t = AudioTrack.Builder().setAudioAttributes(attrs).setAudioFormat(fmt)
                    .setBufferSizeInBytes(pcm.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
                t.write(pcm, 0, pcm.size)
                tracks[i] = t
            } catch (_: Exception) { tracks[i] = null }
        }
    }

    fun play() {
        val track = tracks[nextIndex]
        nextIndex = (nextIndex + 1) % poolSize
        if (track == null || track.state != AudioTrack.STATE_INITIALIZED) return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {}
    }

    fun release() {
        for (i in tracks.indices) {
            try { tracks[i]?.stop(); tracks[i]?.release() } catch (_: Exception) {}
            tracks[i] = null
        }
    }

    private fun synthesizeWoodHit(tone: WoodTone): ShortArray {
        val n = (sampleRate * 0.22).toInt()
        val buf = ShortArray(n)
        val rnd = Random(42)
        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            val strike = if (t < 0.006) (rnd.nextDouble() * 2.0 - 1.0) * exp(-t * 500.0) * 0.45 else 0.0
            val f1 = tone.baseFreq * (1.0 + 0.06 * exp(-t * 85.0))
            val body = sin(2.0 * PI * f1 * t) * exp(-t * tone.decay)
            val over = 0.42 * sin(2.0 * PI * tone.baseFreq * tone.overtoneRatio * t) * exp(-t * tone.decay * 1.9)
            val env = (t / 0.0025).coerceAtMost(1.0)
            buf[i] = (((strike + body + over) * env * 0.85).coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }
}
