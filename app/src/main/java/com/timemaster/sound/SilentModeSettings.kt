package com.timemaster.sound

import android.content.Context

private const val SETTINGS_PREFS = "app_settings"
private const val SILENT_MODE_ENABLED_KEY = "silent_mode_enabled"

fun defaultSilentModeEnabled(): Boolean = false

fun readSilentModeEnabled(context: Context): Boolean =
    context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .getBoolean(SILENT_MODE_ENABLED_KEY, defaultSilentModeEnabled())

fun saveSilentModeEnabled(context: Context, enabled: Boolean) {
    context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putBoolean(SILENT_MODE_ENABLED_KEY, enabled)
        .apply()
}

fun shouldPlayAlertSound(audibleAllowed: Boolean, silentModeEnabled: Boolean): Boolean =
    audibleAllowed && !silentModeEnabled
