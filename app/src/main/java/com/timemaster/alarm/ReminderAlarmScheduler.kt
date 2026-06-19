package com.timemaster.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build

const val ACTION_REMINDER_ALARM = "com.timemaster.ACTION_REMINDER_ALARM"
const val EXTRA_REMINDER_ID = "reminder_id"

interface AlarmScheduler {
    fun schedule(reminderId: Long, triggerAtMillis: Long)
    fun cancel(reminderId: Long)
}

class ReminderAlarmScheduler(
    private val context: Context
) : AlarmScheduler {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminderId: Long, triggerAtMillis: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return
        }

        val pendingIntent = pendingIntentFor(reminderId)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    override fun cancel(reminderId: Long) {
        alarmManager.cancel(pendingIntentFor(reminderId))
    }

    private fun pendingIntentFor(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER_ALARM
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            reminderId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
