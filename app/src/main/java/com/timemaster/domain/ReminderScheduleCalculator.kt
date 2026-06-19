package com.timemaster.domain

import java.time.LocalDate
import java.time.LocalDateTime

fun nextTrigger(now: LocalDateTime, rule: ReminderRule): LocalDateTime {
    for (daysAhead in 0..13) {
        val date = now.toLocalDate().plusDays(daysAhead.toLong())
        if (date.dayOfWeek !in rule.enabledDays) continue

        val start = date.atMinuteOfDay(rule.startMinuteOfDay)
        val end = date.atMinuteOfDay(rule.endMinuteOfDay)
        val candidate = when {
            daysAhead > 0 -> start
            now.isBefore(start) -> start
            now.isBefore(end) -> now.plusMinutes(rule.intervalMinutes.toLong())
            else -> null
        }

        if (candidate != null && !candidate.isAfter(end)) {
            return candidate
        }
    }

    error("Unable to find next trigger within two weeks")
}

private fun LocalDate.atMinuteOfDay(minuteOfDay: Int): LocalDateTime =
    atStartOfDay().plusMinutes(minuteOfDay.toLong())
