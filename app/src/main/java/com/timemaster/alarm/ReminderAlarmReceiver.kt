package com.timemaster.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.timemaster.TimeMasterApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReminderAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        if (reminderId <= 0L) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val app = context.applicationContext as TimeMasterApplication
                app.reminderDueHandler.handleDueReminder(reminderId)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
