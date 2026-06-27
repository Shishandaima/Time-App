package com.timemaster.ui.home

import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

class ReminderCountdownTextTest {
    private val rule = ReminderRule(
        intervalSeconds = 30 * 60,
        startMinuteOfDay = 8 * 60,
        endMinuteOfDay = 22 * 60,
        enabledDays = DayOfWeek.entries.toSet()
    )

    @Test
    fun disabledReminderShowsOffText() {
        val reminder = reminder(isEnabled = false, nextTriggerAtMillis = null)

        assertEquals(
            "\u4e0b\u6b21\u63d0\u9192\uff1a\u672a\u542f\u7528",
            reminderCountdownText(reminder, nowMillis = 1_000L, zoneId = ZoneId.of("UTC"))
        )
    }

    @Test
    fun futureTriggerShowsSecondPrecisionCountdown() {
        val now = Instant.parse("2026-06-20T08:00:00Z").toEpochMilli()
        val reminder = reminder(
            isEnabled = true,
            nextTriggerAtMillis = now + 3_723_000L
        )

        assertEquals(
            "\u4e0b\u6b21\u63d0\u9192\uff1a01:02:03",
            reminderCountdownText(reminder, nowMillis = now, zoneId = ZoneId.of("UTC"))
        )
    }

    @Test
    fun longCountdownShowsDaysAndTime() {
        val now = Instant.parse("2026-06-20T08:00:00Z").toEpochMilli()
        val reminder = reminder(
            isEnabled = true,
            nextTriggerAtMillis = now + 93_784_000L
        )

        assertEquals(
            "\u4e0b\u6b21\u63d0\u9192\uff1a1\u5929 02:03:04",
            reminderCountdownText(reminder, nowMillis = now, zoneId = ZoneId.of("UTC"))
        )
    }

    @Test
    fun missingTriggerShowsReschedulingTextInsteadOfFakeCountdown() {
        val now = Instant.parse("2026-06-20T08:00:00Z").toEpochMilli()
        val reminder = reminder(
            isEnabled = true,
            nextTriggerAtMillis = null
        )

        assertEquals(
            "\u4e0b\u6b21\u63d0\u9192\uff1a\u6b63\u5728\u91cd\u65b0\u8c03\u5ea6",
            reminderCountdownText(reminder, nowMillis = now, zoneId = ZoneId.of("UTC"))
        )
    }

    @Test
    fun pastTriggerShowsReschedulingTextInsteadOfFakeCountdown() {
        val now = Instant.parse("2026-06-20T08:00:00Z").toEpochMilli()
        val reminder = reminder(
            isEnabled = true,
            nextTriggerAtMillis = now
        )

        assertEquals(
            "\u4e0b\u6b21\u63d0\u9192\uff1a\u6b63\u5728\u91cd\u65b0\u8c03\u5ea6",
            reminderCountdownText(reminder, nowMillis = now, zoneId = ZoneId.of("UTC"))
        )
    }

    private fun reminder(
        isEnabled: Boolean,
        nextTriggerAtMillis: Long?
    ): Reminder = Reminder(
        id = 1,
        title = "\u6d4b\u8bd5",
        rule = rule,
        alertMode = AlertMode.Strong,
        ringtoneId = "gentle_chime",
        isEnabled = isEnabled,
        nextTriggerAtMillis = nextTriggerAtMillis
    )
}
