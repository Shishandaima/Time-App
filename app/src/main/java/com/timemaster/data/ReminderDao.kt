package com.timemaster.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders ORDER BY createdAtMillis DESC, id DESC")
    fun observeAll(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE id = :id")
    suspend fun getById(id: Long): ReminderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReminderEntity): Long

    @Update
    suspend fun update(entity: ReminderEntity): Int

    @Transaction
    suspend fun upsert(entity: ReminderEntity): Long {
        return if (entity.id == 0L) {
            insert(entity)
        } else {
            val updatedRows = update(entity)
            if (updatedRows == 0) {
                throw IllegalStateException("Reminder ${entity.id} does not exist")
            }
            entity.id
        }
    }

    @Delete
    suspend fun delete(entity: ReminderEntity)

    @Query("DELETE FROM reminders WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY createdAtMillis DESC, id DESC")
    fun observeEnabled(): Flow<List<ReminderEntity>>

    @Query("SELECT * FROM reminders WHERE isEnabled = 1 ORDER BY createdAtMillis DESC, id DESC")
    suspend fun enabledReminders(): List<ReminderEntity>

    @Query("UPDATE reminders SET nextTriggerAtMillis = :nextTriggerAtMillis, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateNextTrigger(
        id: Long,
        nextTriggerAtMillis: Long?,
        updatedAtMillis: Long = System.currentTimeMillis()
    )
}
