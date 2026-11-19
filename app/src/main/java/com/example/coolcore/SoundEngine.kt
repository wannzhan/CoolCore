package com.example.coolcore

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin
import kotlin.random.Random

enum class FishTone(val label: String, val baseFreq: Double, val overtoneFreq: Double, val decay: Double) {
    CRISP_TEAK("清脆柚木", 640.0, 1420.0, 32.0),
    CRISP_JADE("冰润冷玉", 800.0, 1850.0, 28.0),
    WARM_CEDAR("温润雪松", 480.0, 1080.0, 24.0)
}

class SoundEngine {
    private val sampleRate = 44100
    private var volumeFactor = 0.90f

    // Wooden Fish Audio Tracks (pool)
    private val fishTracks = Array<AudioTrack?>(4) { null }
    private var fishIdx = 0

    // Steel Tongue Drum Tracks (6 pentatonic notes)
    private val drumTracks = Array<AudioTrack?>(6) { null }
    private var drumCycleIdx = 0

    // Bubble Pop Tracks (pool)
    private val popTracks = Array<AudioTrack?>(4) { null }
    private var popIdx = 0

    private val pentatonicFreqs = doubleArrayOf(288.0, 324.0, 384.0, 432.0, 512.0, 576.0)

    init {
        reloadFishTone(FishTone.CRISP_TEAK)
        reloadSteelDrum()
        reloadBubblePop()
    }

    fun setVolume(vol: Float) {
        volumeFactor = vol.coerceIn(0f, 1f)
        val v = volumeFactor
        fishTracks.forEach { try { it?.setVolume(v) } catch (_: Exception) {} }
        drumTracks.forEach { try { it?.setVolume(v) } catch (_: Exception) {} }
        popTracks.forEach { try { it?.setVolume(v) } catch (_: Exception) {} }
    }

    fun reloadFishTone(tone: FishTone) {
        releaseTracks(fishTracks)
        val pcm = synthesizeCrispFish(tone)
        initStaticTracks(fishTracks, pcm)
    }

    fun reloadSteelDrum() {
        releaseTracks(drumTracks)
        for (i in pentatonicFreqs.indices) {
            val pcm = synthesizeSteelDrum(pentatonicFreqs[i])
            val t = buildStaticTrack(pcm)
            drumTracks[i] = t
        }
    }

    fun reloadBubblePop() {
        releaseTracks(popTracks)
        val pcm = synthesizeBubblePop()
        initStaticTracks(popTracks, pcm)
    }

    fun playFish() {
        playFromPool(fishTracks, fishIdx).also { fishIdx = (fishIdx + 1) % fishTracks.size }
    }

    fun playDrum(noteIdx: Int = -1) {
        val idx = if (noteIdx in drumTracks.indices) noteIdx else {
            val i = drumCycleIdx
            drumCycleIdx = (drumCycleIdx + 1) % drumTracks.size
            i
        }
        val track = drumTracks[idx] ?: return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
            track.reloadStaticData()
            track.setVolume(volumeFactor)
            track.play()
        } catch (_: Exception) {}
    }

    fun playPop() {
        playFromPool(popTracks, popIdx).also { popIdx = (popIdx + 1) % popTracks.size }
    }

    private fun playFromPool(pool: Array<AudioTrack?>, idx: Int) {
        val track = pool[idx] ?: return
        try {
            if (track.playState == AudioTrack.PLAYSTATE_PLAYING) track.stop()
            track.reloadStaticData()
            track.setVolume(volumeFactor)
            track.play()
        } catch (_: Exception) {}
    }

    fun release() {
        releaseTracks(fishTracks)
        releaseTracks(drumTracks)
        releaseTracks(popTracks)
    }

    private fun releaseTracks(pool: Array<AudioTrack?>) {
        for (i in pool.indices) {
            try { pool[i]?.stop(); pool[i]?.release() } catch (_: Exception) {}
            pool[i] = null
        }
    }

    private fun initStaticTracks(pool: Array<AudioTrack?>, pcm: ShortArray) {
        for (i in pool.indices) {
            pool[i] = buildStaticTrack(pcm)
        }
    }

    private fun buildStaticTrack(pcm: ShortArray): AudioTrack? {
        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
        val fmt = AudioFormat.Builder()
            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(sampleRate)
            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
        return try {
            val t = AudioTrack.Builder().setAudioAttributes(attrs).setAudioFormat(fmt)
                .setBufferSizeInBytes(pcm.size * 2).setTransferMode(AudioTrack.MODE_STATIC).build()
            t.write(pcm, 0, pcm.size)
            t.setVolume(volumeFactor)
            t
        } catch (_: Exception) { null }
    }

    private fun synthesizeCrispFish(tone: FishTone): ShortArray {
        val durationSec = 0.24
        val n = (sampleRate * durationSec).toInt()
        val buf = ShortArray(n)
        val rnd = Random(101)
        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            // Crisp transient strike (high-pass wooden click in first 8ms)
            val strike = if (t < 0.008) {
                (rnd.nextDouble() * 2.0 - 1.0) * exp(-t * 600.0) * 0.48 +
                sin(2.0 * PI * 1850.0 * t) * exp(-t * 500.0) * 0.40
            } else 0.0
            // Resonant hollow body pitch with gentle pitch-bend envelope
            val f = tone.baseFreq * (1.0 + 0.08 * exp(-t * 120.0))
            val body = sin(2.0 * PI * f * t) * exp(-t * tone.decay)
            // Hollow wood overtone
            val over = 0.40 * sin(2.0 * PI * tone.overtoneFreq * t) * exp(-t * tone.decay * 1.8)
            val env = (t / 0.0018).coerceAtMost(1.0)
            val sample = ((strike + body + over) * env * 0.88).coerceIn(-1.0, 1.0)
            buf[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeSteelDrum(baseFreq: Double): ShortArray {
        val durationSec = 1.3
        val n = (sampleRate * durationSec).toInt()
        val buf = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            // Pure sinusoidal fundamental
            val fund = sin(2.0 * PI * baseFreq * t) * exp(-t * 3.1)
            // Octave & fifth bell-like harmonics
            val h2 = 0.38 * sin(2.0 * PI * (baseFreq * 2.0) * t) * exp(-t * 4.8)
            val h3 = 0.16 * sin(2.0 * PI * (baseFreq * 3.01) * t) * exp(-t * 7.2)
            // Delicate ethereal metallic shimmer
            val shimmer = 1.0 + 0.008 * sin(2.0 * PI * 5.2 * t)
            val attack = (t / 0.004).coerceAtMost(1.0)
            val sample = ((fund + h2 + h3) * shimmer * attack * 0.82).coerceIn(-1.0, 1.0)
            buf[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }

    private fun synthesizeBubblePop(): ShortArray {
        val durationSec = 0.048
        val n = (sampleRate * durationSec).toInt()
        val buf = ShortArray(n)
        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            // Rapid pitch drop simulating air bubble bursting under pressure
            val freq = 260.0 + 1150.0 * exp(-t * 88.0)
            val attack = (t / 0.0015).coerceAtMost(1.0)
            val decay = exp(-t * 58.0)
            val sample = (sin(2.0 * PI * freq * t) * attack * decay * 0.92).coerceIn(-1.0, 1.0)
            buf[i] = (sample * Short.MAX_VALUE).toInt().toShort()
        }
        return buf
    }
}
