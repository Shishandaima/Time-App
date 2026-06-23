package com.timemaster.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build

const val ACTION_REMINDER_ALARM = "com.timemaster.ACTION_REMINDER_ALARM"
const val EXTRA_REMINDER_ID = "reminder_id"

interface AlarmScheduler {
    fun schedule(reminderId: Long, triggerAtMillis: Long): Boolean
    fun cancel(reminderId: Long)
}

class ReminderAlarmScheduler(
    private val context: Context
) : AlarmScheduler {
    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun schedule(reminderId: Long, triggerAtMillis: Long): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
            return false
        }

        val pendingIntent = pendingIntentFor(reminderId)
        try {
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
        } catch (_: SecurityException) {
            return false
        }
        return true
    }

    override fun cancel(reminderId: Long) {
        alarmManager.cancel(pendingIntentFor(reminderId))
    }

    private fun pendingIntentFor(reminderId: Long): PendingIntent {
        val intent = Intent(context, ReminderAlarmReceiver::class.java).apply {
            action = ACTION_REMINDER_ALARM
            data = Uri.parse("timemaster://reminder/$reminderId")
            putExtra(EXTRA_REMINDER_ID, reminderId)
        }
        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
