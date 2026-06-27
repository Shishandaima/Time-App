package com.timemaster.alarm

import com.timemaster.domain.Reminder
import com.timemaster.domain.nextTriggerAfterScheduled
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

interface ReminderDueRepository {
    suspend fun getReminder(id: Long): Reminder?
    suspend fun updateNextTrigger(id: Long, nextTriggerAtMillis: Long?)
}

fun interface ReminderAlertDispatcher {
    fun alert(reminder: Reminder)
}

class ReminderDueHandler(
    private val repository: ReminderDueRepository,
    private val alarmScheduler: AlarmScheduler,
    private val alertDispatcher: ReminderAlertDispatcher,
    private val currentTimeMillis: () -> Long = { System.currentTimeMillis() },
    private val zoneId: () -> ZoneId = { ZoneId.systemDefault() }
) {
    private val mutex = Mutex()

    suspend fun handleDueReminder(reminderId: Long) = mutex.withLock {
        val reminder = repository.getReminder(reminderId)
        if (reminder == null || !reminder.isEnabled) {
            alarmScheduler.cancel(reminderId)
            return@withLock
        }

        val nowMillis = currentTimeMillis()
        val scheduledMillis = reminder.nextTriggerAtMillis
        if (scheduledMillis != null && scheduledMillis > nowMillis) {
            alarmScheduler.schedule(reminderId, scheduledMillis)
            return@withLock
        }

        val zone = zoneId()
        val deliveredAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zone)
        val scheduledTrigger = scheduledMillis
            ?.let { LocalDateTime.ofInstant(Instant.ofEpochMilli(it), zone) }
            ?: deliveredAt

        alertDispatcher.alert(reminder)

        val nextTriggerAtMillis = nextTriggerAfterScheduled(
            scheduledTrigger = scheduledTrigger,
            now = deliveredAt,
            rule = reminder.rule
        )
            .atZone(zone)
            .toInstant()
            .toEpochMilli()

        if (alarmScheduler.schedule(reminderId, nextTriggerAtMillis)) {
            repository.updateNextTrigger(reminderId, nextTriggerAtMillis)
        } else {
            repository.updateNextTrigger(reminderId, null)
        }
    }
}
