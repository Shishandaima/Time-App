package com.timemaster.domain

import java.time.DayOfWeek

enum class AlertMode {
    Strong,
    Normal
}

data class ReminderRule(
    val intervalMinutes: Int,
    val startMinuteOfDay: Int,
    val endMinuteOfDay: Int,
    val enabledDays: Set<DayOfWeek>
) {
    init {
        require(intervalMinutes > 0) { "intervalMinutes must be greater than 0" }
        require(startMinuteOfDay in 0..1439) { "startMinuteOfDay must be in 0..1439" }
        require(endMinuteOfDay in 1..1440) { "endMinuteOfDay must be in 1..1440" }
        require(startMinuteOfDay < endMinuteOfDay) { "startMinuteOfDay must be before endMinuteOfDay" }
        require(enabledDays.isNotEmpty()) { "enabledDays must not be empty" }
    }
}

data class Reminder(
    val id: Long,
    val title: String,
    val rule: ReminderRule,
    val alertMode: AlertMode,
    val ringtoneId: String,
    val isEnabled: Boolean,
    val nextTriggerAtMillis: Long?
)
