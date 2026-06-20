package com.timemaster.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import java.time.DayOfWeek

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val intervalSeconds: Int = 30 * 60,
    val intervalMinutes: Int = 30,
    val startMinuteOfDay: Int = 8 * 60,
    val endMinuteOfDay: Int = 22 * 60,
    val enabledDaysMask: Int = EVERY_DAY_MASK,
    val alertMode: String = AlertMode.Strong.name,
    val ringtoneId: String = "gentle_chime",
    val isEnabled: Boolean = true,
    val nextTriggerAtMillis: Long? = null,
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    fun toDomain(): Reminder = Reminder(
        id = id,
        title = title,
        rule = ReminderRule(
            intervalSeconds = intervalSeconds,
            startMinuteOfDay = startMinuteOfDay,
            endMinuteOfDay = endMinuteOfDay,
            enabledDays = daysFromMask(enabledDaysMask)
        ),
        alertMode = runCatching { AlertMode.valueOf(alertMode) }.getOrDefault(AlertMode.Strong),
        ringtoneId = ringtoneId,
        isEnabled = isEnabled,
        nextTriggerAtMillis = nextTriggerAtMillis
    )

    companion object {
        fun fromDomain(
            reminder: Reminder,
            createdAtMillis: Long = System.currentTimeMillis(),
            updatedAtMillis: Long = System.currentTimeMillis()
        ): ReminderEntity = ReminderEntity(
            id = reminder.id,
            title = reminder.title,
            intervalSeconds = reminder.rule.intervalSeconds,
            intervalMinutes = reminder.rule.intervalSeconds / 60,
            startMinuteOfDay = reminder.rule.startMinuteOfDay,
            endMinuteOfDay = reminder.rule.endMinuteOfDay,
            enabledDaysMask = maskFromDays(reminder.rule.enabledDays),
            alertMode = reminder.alertMode.name,
            ringtoneId = reminder.ringtoneId,
            isEnabled = reminder.isEnabled,
            nextTriggerAtMillis = reminder.nextTriggerAtMillis,
            createdAtMillis = createdAtMillis,
            updatedAtMillis = updatedAtMillis
        )
    }
}

const val EVERY_DAY_MASK = 0b1111111

fun maskFromDays(days: Set<DayOfWeek>): Int =
    days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

fun daysFromMask(mask: Int): Set<DayOfWeek> {
    // Treat empty or unknown-only masks as every day so legacy/corrupt rows still create a valid rule.
    val normalizedMask = mask and EVERY_DAY_MASK
    val safeMask = if (normalizedMask == 0) EVERY_DAY_MASK else normalizedMask
    return DayOfWeek.entries
        .filter { day -> safeMask and (1 shl (day.value - 1)) != 0 }
        .toSet()
}
