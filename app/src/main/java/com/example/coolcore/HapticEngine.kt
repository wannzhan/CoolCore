package com.example.coolcore

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticEngine(context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun ok() = vibrator.hasVibrator()

    fun bubblePop() {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else vibrator.vibrate(VibrationEffect.createOneShot(22, 200))
    }

    fun magneticDetent() {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else vibrator.vibrate(VibrationEffect.createOneShot(10, 140))
    }

    fun woodenFishKnock() {
        if (!ok()) return
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 16, 18, 22), intArrayOf(0, 185, 0, 75), -1))
    }

    fun bilateralPulse() {
        if (!ok()) return
        vibrator.vibrate(VibrationEffect.createOneShot(30, 85))
    }

    fun breathCue(isExhale: Boolean) {
        if (!ok()) return
        if (isExhale) vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 80), intArrayOf(0, 110, 0, 50), -1))
        else vibrator.vibrate(VibrationEffect.createOneShot(45, 95))
    }

    fun shatterRelease() {
        if (!ok()) return
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 30, 50, 40, 90), intArrayOf(0, 140, 0, 210, 0, 90), -1))
    }

    fun holdTick(intensity: Float) {
        if (!ok()) return
        vibrator.vibrate(VibrationEffect.createOneShot(18, (40 + intensity * 160).toInt().coerceIn(1, 255)))
    }
}
