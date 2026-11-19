package com.example.coolcore

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class HapticEngine(context: Context) {
    var enabled: Boolean = true

    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun ok() = enabled && vibrator.hasVibrator()

    fun bubblePop() {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VibrationEffect.createOneShot(22, 200))
        }
    }

    fun magneticDetent() {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(VibrationEffect.createOneShot(10, 140))
        }
    }

    fun woodenFishKnock() {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 14, 18, 20), intArrayOf(0, 190, 0, 70), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(25)
        }
    }

    fun bilateralPulse() {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(28, 90))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(28)
        }
    }

    fun breathCue(isExhale: Boolean) {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (isExhale) vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 60, 40, 80), intArrayOf(0, 110, 0, 50), -1))
            else vibrator.vibrate(VibrationEffect.createOneShot(45, 95))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }

    fun shatterRelease() {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 35, 30, 50, 40, 90), intArrayOf(0, 140, 0, 210, 0, 90), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    fun holdTick(intensity: Float) {
        if (!ok()) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(18, (40 + intensity * 160).toInt().coerceIn(1, 255)))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(18)
        }
    }
}
