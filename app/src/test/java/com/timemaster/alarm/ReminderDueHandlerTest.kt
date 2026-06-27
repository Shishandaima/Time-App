package com.timemaster.alarm

import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderDueHandlerTest {
    private val zoneId = ZoneId.of("UTC")
    private val rule = ReminderRule(
        intervalSeconds = 30 * 60,
        startMinuteOfDay = 9 * 60,
        endMinuteOfDay = 21 * 60,
        enabledDays = DayOfWeek.entries.toSet()
    )

    @Test
    fun dueReminderAlertsBeforeAdvancingNextTrigger() = runBlocking {
        val scheduledAt = LocalDateTime.of(2026, 6, 24, 10, 30).toMillis()
        val deliveredAt = LocalDateTime.of(2026, 6, 24, 10, 33).toMillis()
        val events = mutableListOf<String>()
        val repository = FakeDueRepository(reminder(nextTriggerAtMillis = scheduledAt), events)
        val scheduler = FakeAlarmScheduler(events)
        val handler = ReminderDueHandler(
            repository = repository,
            alarmScheduler = scheduler,
            alertDispatcher = ReminderAlertDispatcher { events += "alert:${it.id}" },
            currentTimeMillis = { deliveredAt },
            zoneId = { zoneId }
        )

        handler.handleDueReminder(1L)

        assertEquals(
            listOf(
                "alert:1",
                "schedule:${LocalDateTime.of(2026, 6, 24, 11, 0).toMillis()}",
                "update:${LocalDateTime.of(2026, 6, 24, 11, 0).toMillis()}"
            ),
            events
        )
    }

    @Test
    fun futureReminderDoesNotAlertOrAdvance() = runBlocking {
        val futureAt = LocalDateTime.of(2026, 6, 24, 10, 30).toMillis()
        val now = LocalDateTime.of(2026, 6, 24, 10, 25).toMillis()
        val events = mutableListOf<String>()
        val repository = FakeDueRepository(reminder(nextTriggerAtMillis = futureAt), events)
        val scheduler = FakeAlarmScheduler(events)
        val handler = ReminderDueHandler(
            repository = repository,
            alarmScheduler = scheduler,
            alertDispatcher = ReminderAlertDispatcher { events += "alert:${it.id}" },
            currentTimeMillis = { now },
            zoneId = { zoneId }
        )

        handler.handleDueReminder(1L)

        assertEquals(listOf("schedule:$futureAt"), events)
        assertFalse(repository.updated)
    }

    private fun reminder(nextTriggerAtMillis: Long?): Reminder = Reminder(
        id = 1L,
        title = "\u6d4b\u8bd5\u63d0\u9192",
        rule = rule,
        alertMode = AlertMode.Strong,
        ringtoneId = "gentle_chime",
        isEnabled = true,
        nextTriggerAtMillis = nextTriggerAtMillis
    )

    private fun LocalDateTime.toMillis(): Long =
        atZone(zoneId).toInstant().toEpochMilli()

    private class FakeDueRepository(
        private val reminder: Reminder?,
        private val events: MutableList<String>
    ) : ReminderDueRepository {
        var updated = false

        override suspend fun getReminder(id: Long): Reminder? = reminder

        override suspend fun updateNextTrigger(id: Long, nextTriggerAtMillis: Long?) {
            updated = true
            events += "update:$nextTriggerAtMillis"
        }
    }

    private class FakeAlarmScheduler(
        private val events: MutableList<String>
    ) : AlarmScheduler {
        override fun schedule(reminderId: Long, triggerAtMillis: Long): Boolean {
            events += "schedule:$triggerAtMillis"
            return true
        }

        override fun cancel(reminderId: Long) {
            events += "cancel:$reminderId"
        }
    }
}
