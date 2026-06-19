package com.timemaster.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timemaster.TimeMasterApplication
import com.timemaster.domain.nextTrigger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneId

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as TimeMasterApplication
                val nowMillis = System.currentTimeMillis()
                val now = LocalDateTime.now()
                app.reminderRepository.enabledReminders().forEach { reminder ->
                    val existingNextTrigger = reminder.nextTriggerAtMillis
                    val nextTriggerAtMillis = if (
                        existingNextTrigger != null &&
                        existingNextTrigger > nowMillis
                    ) {
                        existingNextTrigger
                    } else {
                        nextTrigger(now, reminder.rule)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                            .also { app.reminderRepository.updateNextTrigger(reminder.id, it) }
                    }
                    app.alarmScheduler.schedule(reminder.id, nextTriggerAtMillis)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
