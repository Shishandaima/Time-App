package com.timemaster.domain

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

fun nextTrigger(now: LocalDateTime, rule: ReminderRule): LocalDateTime {
    for (daysAhead in 0..13) {
        val date = now.toLocalDate().plusDays(daysAhead.toLong())
        if (date.dayOfWeek !in rule.enabledDays) continue

        val start = date.atMinuteOfDay(rule.startMinuteOfDay)
        val end = date.atMinuteOfDay(rule.endMinuteOfDay)
        val candidate = when {
            daysAhead > 0 -> start
            now.isBefore(start) -> start
            now.isBefore(end) -> now.plusSeconds(rule.intervalSeconds.toLong())
            else -> null
        }

        if (candidate != null && !candidate.isAfter(end)) {
            return candidate
        }
    }

    error("Unable to find next trigger within two weeks")
}

fun nextTriggerAfterScheduled(
    scheduledTrigger: LocalDateTime,
    now: LocalDateTime,
    rule: ReminderRule
): LocalDateTime {
    val intervalSeconds = rule.intervalSeconds.toLong()
    val scheduledDate = scheduledTrigger.toLocalDate()
    val scheduledEnd = scheduledDate.atMinuteOfDay(rule.endMinuteOfDay)
    val firstAfterScheduled = scheduledTrigger
        .plusSeconds(intervalSeconds)
        .coerceAtLeast(scheduledDate.atMinuteOfDay(rule.startMinuteOfDay))
    val sameDayCandidate = firstAfterScheduled.advancePast(now, intervalSeconds)

    if (
        scheduledDate.dayOfWeek in rule.enabledDays &&
        !sameDayCandidate.isAfter(scheduledEnd)
    ) {
        return sameDayCandidate
    }

    val firstSearchDate = maxOf(scheduledDate.plusDays(1), now.toLocalDate())
    for (daysAhead in 0..13) {
        val date = firstSearchDate.plusDays(daysAhead.toLong())
        if (date.dayOfWeek !in rule.enabledDays) continue

        val start = date.atMinuteOfDay(rule.startMinuteOfDay)
        val end = date.atMinuteOfDay(rule.endMinuteOfDay)
        val candidate = start.advancePast(now, intervalSeconds)

        if (!candidate.isAfter(end)) {
            return candidate
        }
    }

    error("Unable to find next trigger within two weeks")
}

private fun LocalDateTime.advancePast(
    reference: LocalDateTime,
    intervalSeconds: Long
): LocalDateTime {
    if (isAfter(reference)) return this

    val elapsedSeconds = ChronoUnit.SECONDS.between(this, reference)
    val intervalsToAdvance = elapsedSeconds / intervalSeconds + 1L
    return plusSeconds(intervalsToAdvance * intervalSeconds)
}

private fun LocalDateTime.coerceAtLeast(minimum: LocalDateTime): LocalDateTime =
    if (isBefore(minimum)) minimum else this

private fun LocalDate.atMinuteOfDay(minuteOfDay: Int): LocalDateTime =
    atStartOfDay().plusMinutes(minuteOfDay.toLong())
