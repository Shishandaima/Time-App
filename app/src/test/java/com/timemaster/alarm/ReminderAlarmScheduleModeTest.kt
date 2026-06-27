package com.timemaster.alarm

import org.junit.Assert.assertEquals
import org.junit.Test

class ReminderAlarmScheduleModeTest {
    @Test
    fun remindersUseAlarmClockModeForLockedScreenPunctuality() {
        assertEquals(
            ReminderAlarmScheduleMode.AlarmClock,
            reminderAlarmScheduleMode()
        )
    }
}
