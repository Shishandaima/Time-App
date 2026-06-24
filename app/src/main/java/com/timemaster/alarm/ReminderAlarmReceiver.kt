package com.timemaster.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timemaster.TimeMasterApplication
import com.timemaster.domain.AlertMode
import com.timemaster.domain.nextTriggerAfterScheduled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as TimeMasterApplication
                val reminder = app.reminderRepository.getReminder(reminderId)
                if (reminder == null || !reminder.isEnabled) {
                    app.alarmScheduler.cancel(reminderId)
                    return@launch
                }

                val zoneId = ZoneId.systemDefault()
                val deliveredAt = LocalDateTime.now()
                val scheduledTrigger = reminder.nextTriggerAtMillis
                    ?.let { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDateTime() }
                    ?: deliveredAt
                val nextTriggerAtMillis = nextTriggerAfterScheduled(
                    scheduledTrigger = scheduledTrigger,
                    now = deliveredAt,
                    rule = reminder.rule
                )
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli()
                if (app.alarmScheduler.schedule(reminderId, nextTriggerAtMillis)) {
                    app.reminderRepository.updateNextTrigger(reminderId, nextTriggerAtMillis)
                } else {
                    app.reminderRepository.updateNextTrigger(reminderId, null)
                }

                if (reminder.alertMode == AlertMode.Strong) {
                    app.ringtonePlayer.playLooping(reminder.ringtoneId)
                } else {
                    app.ringtonePlayer.preview(reminder.ringtoneId)
                }
                app.reminderNotifier.showReminder(reminder)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
