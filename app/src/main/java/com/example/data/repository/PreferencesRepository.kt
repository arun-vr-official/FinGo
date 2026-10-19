package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UserPreferences(
    val userName: String = "",
    val currencyCode: String = "USD",
    val currencySymbol: String = "$",
    val dateFormatPattern: String = "MMM dd, yyyy",
    val is24HourTime: Boolean = false,
    val themeMode: String = "SYSTEM", // "SYSTEM", "LIGHT", "DARK"
    val themeStyle: String = "PLAIN", // "PLAIN", "GRID", "DOTS", "MESH", "WAVES"
    val isAppLocked: Boolean = false,
    val pinCode: String = "",
    val hideBalances: Boolean = false,
    val onboardingCompleted: Boolean = false
)

class PreferencesRepository(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("fintrack_prefs", Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private fun loadPreferences(): UserPreferences {
        return UserPreferences(
            userName = prefs.getString("user_name", "") ?: "",
            currencyCode = prefs.getString("currency_code", "USD") ?: "USD",
            currencySymbol = prefs.getString("currency_symbol", "$") ?: "$",
            dateFormatPattern = prefs.getString("date_format", "MMM dd, yyyy") ?: "MMM dd, yyyy",
            is24HourTime = prefs.getBoolean("is_24_hour", false),
            themeMode = prefs.getString("theme_mode", "SYSTEM") ?: "SYSTEM",
            themeStyle = prefs.getString("theme_style", "PLAIN") ?: "PLAIN",
            isAppLocked = prefs.getBoolean("is_app_locked", false),
            pinCode = prefs.getString("pin_code", "") ?: "",
            hideBalances = prefs.getBoolean("hide_balances", false),
            onboardingCompleted = prefs.getBoolean("onboarding_completed", false)
        )
    }

    fun setUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _preferences.value = _preferences.value.copy(userName = name)
    }

    fun setThemeStyle(style: String) {
        prefs.edit().putString("theme_style", style).apply()
        _preferences.value = _preferences.value.copy(themeStyle = style)
    }

    fun setCurrency(code: String, symbol: String) {
        prefs.edit().putString("currency_code", code).putString("currency_symbol", symbol).apply()
        _preferences.value = _preferences.value.copy(currencyCode = code, currencySymbol = symbol)
    }

    fun setDateFormat(pattern: String) {
        prefs.edit().putString("date_format", pattern).apply()
        _preferences.value = _preferences.value.copy(dateFormatPattern = pattern)
    }

    fun setTimeFormat24h(is24h: Boolean) {
        prefs.edit().putBoolean("is_24_hour", is24h).apply()
        _preferences.value = _preferences.value.copy(is24HourTime = is24h)
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString("theme_mode", mode).apply()
        _preferences.value = _preferences.value.copy(themeMode = mode)
    }

    fun setAppLocked(locked: Boolean, pin: String = "") {
        prefs.edit().putBoolean("is_app_locked", locked).putString("pin_code", pin).apply()
        _preferences.value = _preferences.value.copy(isAppLocked = locked, pinCode = pin)
    }

    fun setHideBalances(hide: Boolean) {
        prefs.edit().putBoolean("hide_balances", hide).apply()
        _preferences.value = _preferences.value.copy(hideBalances = hide)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean("onboarding_completed", completed).apply()
        _preferences.value = _preferences.value.copy(onboardingCompleted = completed)
    }
}
