package com.example.coolcore

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

enum class AppThemeMode(val label: String, val subtitle: String) {
    FOLLOW_SYSTEM("跟随系统", "根据手机系统深浅色自动切换"),
    MACARON_LIGHT("马卡龙暖杏", "柔和清爽低饱和浅色模式，白天首选"),
    OBSIDIAN_DARK("深邃石墨夜", "低刺激暗黑静音模式，夜间更护眼")
}

class SettingsStorage(context: Context) {
    private val prefs = context.getSharedPreferences("coolcore_settings_v12", Context.MODE_PRIVATE)

    var themeMode by mutableStateOf(
        try {
            AppThemeMode.valueOf(prefs.getString("theme_mode", AppThemeMode.FOLLOW_SYSTEM.name) ?: AppThemeMode.FOLLOW_SYSTEM.name)
        } catch (_: Exception) {
            AppThemeMode.FOLLOW_SYSTEM
        }
    )

    var soundVolume by mutableFloatStateOf(prefs.getFloat("sound_volume", 0.90f))
    var soundEnabled by mutableStateOf(prefs.getBoolean("sound_enabled", true))
    var hapticEnabled by mutableStateOf(prefs.getBoolean("haptic_enabled", true))
    var instrumentMode by mutableStateOf(prefs.getString("instrument_mode", "WOODEN_FISH") ?: "WOODEN_FISH") // "WOODEN_FISH" or "STEEL_DRUM"

    fun updateTheme(mode: AppThemeMode) {
        themeMode = mode
        prefs.edit().putString("theme_mode", mode.name).apply()
    }

    fun updateVolume(vol: Float) {
        soundVolume = vol.coerceIn(0f, 1f)
        prefs.edit().putFloat("sound_volume", soundVolume).apply()
    }

    fun toggleSound(enabled: Boolean) {
        soundEnabled = enabled
        prefs.edit().putBoolean("sound_enabled", enabled).apply()
    }

    fun toggleHaptic(enabled: Boolean) {
        hapticEnabled = enabled
        prefs.edit().putBoolean("haptic_enabled", enabled).apply()
    }

    fun updateInstrument(mode: String) {
        instrumentMode = mode
        prefs.edit().putString("instrument_mode", mode).apply()
    }
}
