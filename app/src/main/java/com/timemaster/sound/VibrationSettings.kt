package com.timemaster.sound

import android.content.Context

private const val SETTINGS_PREFS = "app_settings"
private const val VIBRATION_ENABLED_KEY = "vibration_enabled"

fun defaultVibrationEnabled(): Boolean = true

fun readVibrationEnabled(context: Context): Boolean =
    context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(VIBRATION_ENABLED_KEY, defaultVibrationEnabled())

fun saveVibrationEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(VIBRATION_ENABLED_KEY, enabled)
        .apply()
}

fun shouldStartVibration(loop: Boolean, vibrationEnabled: Boolean): Boolean =
    loop && vibrationEnabled
