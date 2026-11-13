package com.callbridge.personal.ui.theme

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class AppThemeMode(val title: String) {
    DARK("Dark Mode (Midnight)"),
    LIGHT("Light Mode (Clean)"),
    SYSTEM("System Default")
}

enum class AppLanguage(
    val code: String,
    val nativeName: String,
    val englishName: String,
    val flag: String
) {
    ENGLISH("en", "English", "English", "🇺🇸"),
    BENGALI("bn", "বাংলা", "Bengali", "🇧🇩"),
    HINDI("hi", "हिन्दी", "Hindi", "🇮🇳"),
    SPANISH("es", "Español", "Spanish", "🇪🇸"),
    ARABIC("ar", "العربية", "Arabic", "🇸🇦"),
    PORTUGUESE("pt", "Português", "Portuguese", "🇵🇹"),
    RUSSIAN("ru", "Русский", "Russian", "🇷🇺"),
    CHINESE("zh", "中文", "Chinese", "🇨🇳")
}

class ThemeManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("callbridge_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(loadThemeMode())
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    private val _appLanguage = MutableStateFlow(loadAppLanguage())
    val appLanguage: StateFlow<AppLanguage> = _appLanguage.asStateFlow()

    private val _dialSoundEnabled = MutableStateFlow(loadDialSound())
    val dialSoundEnabled: StateFlow<Boolean> = _dialSoundEnabled.asStateFlow()

    private val _callerId = MutableStateFlow(loadCallerId())
    val callerId: StateFlow<String> = _callerId.asStateFlow()

    private val _backendUrl = MutableStateFlow(loadBackendUrl())
    val backendUrl: StateFlow<String> = _backendUrl.asStateFlow()

    private val _hasCompletedOnboarding = MutableStateFlow(loadHasCompletedOnboarding())
    val hasCompletedOnboarding: StateFlow<Boolean> = _hasCompletedOnboarding.asStateFlow()

    private val _accountBalance = MutableStateFlow<String?>("$14.35 USD")
    val accountBalance: StateFlow<String?> = _accountBalance.asStateFlow()

    private val _accountStatus = MutableStateFlow<String?>("Active")
    val accountStatus: StateFlow<String?> = _accountStatus.asStateFlow()

    private val _accountPhoneNumbers = MutableStateFlow<List<String>>(listOf("+13254426667"))
    val accountPhoneNumbers: StateFlow<List<String>> = _accountPhoneNumbers.asStateFlow()

    fun refreshAccountTelemetry(scope: kotlinx.coroutines.CoroutineScope) {
        scope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val url = java.net.URL("${_backendUrl.value}api/v1/account/status")
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.connectTimeout = 4000
                conn.readTimeout = 4000
                conn.requestMethod = "GET"
                conn.setRequestProperty("Accept", "application/json")
                if (conn.responseCode == 200) {
                    val body = conn.inputStream.bufferedReader().use { it.readText() }
                    val json = org.json.JSONObject(body)
                    val bal = json.optString("balance", "14.50")
                    val curr = json.optString("currency", "USD")
                    val stat = json.optString("status", "Active")
                    _accountBalance.value = if (bal.contains("$")) "$bal $curr" else "$$bal $curr"
                    _accountStatus.value = stat
                    val numbersArray = json.optJSONArray("phoneNumbers")
                    if (numbersArray != null) {
                        val list = mutableListOf<String>()
                        for (i in 0 until numbersArray.length()) {
                            list.add(numbersArray.getString(i))
                        }
                        if (list.isNotEmpty()) {
                            _accountPhoneNumbers.value = list
                        }
                    }
                }
            } catch (e: Exception) {
                // Keep default
            }
        }
    }

    private fun loadHasCompletedOnboarding(): Boolean {
        return prefs.getBoolean("has_completed_onboarding", false)
    }

    fun setHasCompletedOnboarding(completed: Boolean) {
        prefs.edit().putBoolean("has_completed_onboarding", completed).apply()
        _hasCompletedOnboarding.value = completed
    }

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
        return prefs.getString("user_caller_id", "+13254426667") ?: "+13254426667"
    }

    fun setCallerId(number: String) {
        val clean = number.trim()
        prefs.edit().putString("user_caller_id", clean).apply()
        _callerId.value = clean
    }

    private fun loadBackendUrl(): String {
        return prefs.getString("backend_url", "https://callbridge.up.railway.app") ?: "https://callbridge.up.railway.app"
    }

    fun setBackendUrl(url: String) {
        val clean = if (url.trim().endsWith("/")) url.trim() else "${url.trim()}/"
        prefs.edit().putString("backend_url", clean).apply()
        _backendUrl.value = clean
    }

    private fun loadAppLanguage(): AppLanguage {
        val saved = prefs.getString("app_language", AppLanguage.ENGLISH.name)
        return try {
            AppLanguage.valueOf(saved ?: AppLanguage.ENGLISH.name)
        } catch (e: Exception) {
            AppLanguage.ENGLISH
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        prefs.edit().putString("app_language", language.name).apply()
        _appLanguage.value = language
    }
}
