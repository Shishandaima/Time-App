package com.timemaster.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek

class ReminderScheduleRepairTest {
    private val rule = ReminderRule(
        intervalSeconds = 30 * 60,
        startMinuteOfDay = 8 * 60,
        endMinuteOfDay = 22 * 60,
        enabledDays = DayOfWeek.entries.toSet()
    )

    @Test
    fun enabledReminderWithMissingNextTriggerNeedsRepair() {
        val reminder = reminder(isEnabled = true, nextTriggerAtMillis = null)

        assertTrue(needsScheduleRepair(reminder, nowMillis = 1_000L))
    }

    @Test
    fun enabledReminderWithPastNextTriggerNeedsRepair() {
        val reminder = reminder(isEnabled = true, nextTriggerAtMillis = 999L)

        assertTrue(needsScheduleRepair(reminder, nowMillis = 1_000L))
    }

    @Test
    fun enabledReminderWithCurrentNextTriggerNeedsRepair() {
        val reminder = reminder(isEnabled = true, nextTriggerAtMillis = 1_000L)

        assertTrue(needsScheduleRepair(reminder, nowMillis = 1_000L))
    }

    @Test
    fun enabledReminderWithFutureNextTriggerDoesNotNeedRepair() {
        val reminder = reminder(isEnabled = true, nextTriggerAtMillis = 1_001L)

        assertFalse(needsScheduleRepair(reminder, nowMillis = 1_000L))
    }

    @Test
    fun disabledReminderDoesNotNeedRepair() {
        val reminder = reminder(isEnabled = false, nextTriggerAtMillis = null)

        assertFalse(needsScheduleRepair(reminder, nowMillis = 1_000L))
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
