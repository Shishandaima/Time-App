package com.timemaster.data

import com.timemaster.domain.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ReminderRepository(
    private val reminderDao: ReminderDao
) {
    fun observeReminders(): Flow<List<Reminder>> =
        reminderDao.observeAll().map { reminders -> reminders.map { it.toDomain() } }

    suspend fun getReminder(id: Long): Reminder? =
        reminderDao.getById(id)?.toDomain()

    suspend fun saveReminder(reminder: Reminder): Long {
        val now = System.currentTimeMillis()
        val existing = if (reminder.id == 0L) null else reminderDao.getById(reminder.id)
        val entity = ReminderEntity.fromDomain(
            reminder = reminder,
            createdAtMillis = existing?.createdAtMillis ?: now,
            updatedAtMillis = now
        )
        return reminderDao.upsert(entity)
    }

    suspend fun saveEntity(entity: ReminderEntity): Long =
        reminderDao.upsert(entity.copy(updatedAtMillis = System.currentTimeMillis()))

    suspend fun deleteReminder(id: Long) {
        reminderDao.deleteById(id)
    }

    suspend fun enabledReminders(): List<Reminder> =
        reminderDao.enabledReminders().map { it.toDomain() }

    suspend fun updateNextTrigger(id: Long, nextTriggerAtMillis: Long?) {
        reminderDao.updateNextTrigger(id, nextTriggerAtMillis)
    }
}
