package com.timemaster.domain

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime

class ReminderScheduleCalculatorTest {
    private val weekdayRule = ReminderRule(
        intervalSeconds = 30 * 60,
        startMinuteOfDay = 8 * 60,
        endMinuteOfDay = 22 * 60,
        enabledDays = setOf(
            DayOfWeek.MONDAY,
            DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY,
            DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY
        )
    )

    @Test
    fun beforeWindow_returnsTodayStart() {
        val now = LocalDateTime.of(2026, 6, 19, 7, 10)

        assertEquals(LocalDateTime.of(2026, 6, 19, 8, 0), nextTrigger(now, weekdayRule))
    }

    @Test
    fun insideWindow_advancesByInterval() {
        val now = LocalDateTime.of(2026, 6, 19, 9, 10)

        assertEquals(LocalDateTime.of(2026, 6, 19, 9, 40), nextTrigger(now, weekdayRule))
    }

    @Test
    fun insideWindow_advancesBySecondInterval() {
        val now = LocalDateTime.of(2026, 6, 19, 9, 10, 15)
        val rule = weekdayRule.copy(intervalSeconds = 90)

        assertEquals(LocalDateTime.of(2026, 6, 19, 9, 11, 45), nextTrigger(now, rule))
    }

    @Test
    fun afterWindow_skipsToNextEnabledDayStart() {
        val now = LocalDateTime.of(2026, 6, 19, 22, 30)

        assertEquals(LocalDateTime.of(2026, 6, 22, 8, 0), nextTrigger(now, weekdayRule))
    }

    @Test
    fun intervalCrossingEndWindow_skipsToNextEnabledDayStart() {
        val now = LocalDateTime.of(2026, 6, 19, 21, 45)

        assertEquals(LocalDateTime.of(2026, 6, 22, 8, 0), nextTrigger(now, weekdayRule))
    }
}
