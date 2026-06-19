package com.timemaster.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.ui.alert.AlertActivity

private const val STRONG_CHANNEL_ID = "strong_reminders"
private const val NORMAL_CHANNEL_ID = "normal_reminders"

class ReminderNotifier(
    private val context: Context
) {
    private val notificationManager = NotificationManagerCompat.from(context)

    init {
        createChannels()
    }

    fun showReminder(reminder: Reminder) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val strong = reminder.alertMode == AlertMode.Strong
        val contentIntent = alertIntent(reminder)
        val builder = NotificationCompat.Builder(
            context,
            if (strong) STRONG_CHANNEL_ID else NORMAL_CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(reminder.title)
            .setContentText("\u65f6\u95f4\u5230\u4e86")
            .setAutoCancel(!strong)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)
            .setSilent(true)

        if (strong) {
            builder
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOngoing(true)
                .setFullScreenIntent(contentIntent, true)
        } else {
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT)
        }

        notificationManager.notify(notificationId(reminder.id), builder.build())
    }

    private fun alertIntent(reminder: Reminder): PendingIntent {
        val intent = Intent(context, AlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            data = Uri.parse("timemaster://alert/${reminder.id}")
            putExtra(AlertActivity.EXTRA_REMINDER_ID, reminder.id)
            putExtra(AlertActivity.EXTRA_TITLE, reminder.title)
            putExtra(AlertActivity.EXTRA_RINGTONE_ID, reminder.ringtoneId)
        }
        return PendingIntent.getActivity(
            context,
            notificationId(reminder.id),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createChannels() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val manager = context.getSystemService(NotificationManager::class.java)
        val strongChannel = NotificationChannel(
            STRONG_CHANNEL_ID,
            "\u5f3a\u63d0\u9192",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "\u5168\u5c4f\u663e\u793a\u7684\u91cd\u8981\u63d0\u9192"
            lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            setSound(null, null)
        }
        val normalChannel = NotificationChannel(
            NORMAL_CHANNEL_ID,
            "\u666e\u901a\u63d0\u9192",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "\u666e\u901a\u65f6\u95f4\u63d0\u9192"
            setSound(null, null)
        }

        manager.createNotificationChannel(strongChannel)
        manager.createNotificationChannel(normalChannel)
    }

    private fun notificationId(reminderId: Long): Int =
        reminderId.hashCode()
}
