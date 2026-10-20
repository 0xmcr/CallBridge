package com.callbridge.personal.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val title: String) {
    DARK("Dark Mode (Midnight)"),
    LIGHT("Light Mode (Clean)"),
    SYSTEM("System Default")
}

class ThemeManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("callbridge_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _dialSoundEnabled = MutableStateFlow(loadDialSound())
    val dialSoundEnabled: StateFlow<Boolean> = _dialSoundEnabled.asStateFlow()

    private val _callerId = MutableStateFlow(loadCallerId())
    val callerId: StateFlow<String> = _callerId.asStateFlow()

    private fun loadThemeMode(): AppThemeMode {
        val saved = prefs.getString("app_theme_mode", AppThemeMode.DARK.name)
        return try {
            AppThemeMode.valueOf(saved ?: AppThemeMode.DARK.name)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
    }

    fun setThemeMode(mode: AppThemeMode) {
        prefs.edit().putString("app_theme_mode", mode.name).apply()
        _themeMode.value = mode
    }

    private fun loadDialSound(): Boolean {
        return prefs.getBoolean("dial_sound_enabled", true)
    }

    fun setDialSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("dial_sound_enabled", enabled).apply()
        _dialSoundEnabled.value = enabled
    }

    private fun loadCallerId(): String {
        return prefs.getString("user_caller_id", "+1 (202) 555-0199") ?: "+1 (202) 555-0199"
    }

    fun setCallerId(number: String) {
        val clean = number.trim()
        prefs.edit().putString("user_caller_id", clean).apply()
        _callerId.value = clean
    }
}
