package com.timemaster.sound

import android.content.Context

enum class RingDurationMode(
    val label: String,
    val durationMillis: Long
) {
    FiveSeconds("5\u79d2", 5_000L),
    TenSeconds("10\u79d2", 10_000L),
    FifteenSeconds("15\u79d2", 15_000L)
}

private const val SETTINGS_PREFS = "app_settings"
private const val RING_DURATION_MODE_KEY = "ring_duration_mode"

fun defaultRingDurationMode(): RingDurationMode =
    RingDurationMode.TenSeconds

fun readRingDurationMode(context: Context): RingDurationMode =
    runCatching {
        RingDurationMode.valueOf(
            context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
                .getString(RING_DURATION_MODE_KEY, defaultRingDurationMode().name)!!
        )
    }.getOrDefault(defaultRingDurationMode())

fun saveRingDurationMode(context: Context, ringDurationMode: RingDurationMode) {
    context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(RING_DURATION_MODE_KEY, ringDurationMode.name)
        .apply()
}
