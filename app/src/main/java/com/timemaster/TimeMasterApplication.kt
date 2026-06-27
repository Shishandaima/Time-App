package com.timemaster

import android.app.Application
import androidx.room.Room
import com.timemaster.alarm.AlarmScheduler
import com.timemaster.alarm.ReminderAlertDispatcher
import com.timemaster.alarm.ReminderDueHandler
import com.timemaster.alarm.ReminderAlarmScheduler
import com.timemaster.data.AppDatabase
import com.timemaster.data.ReminderRepository
import com.timemaster.domain.AlertMode
import com.timemaster.notification.ReminderNotifier
import com.timemaster.sound.RingtonePlayer

class TimeMasterApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "time_master.db")
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
    }

    val reminderRepository: ReminderRepository by lazy {
        ReminderRepository(database.reminderDao())
    }

    val alarmScheduler: AlarmScheduler by lazy {
        ReminderAlarmScheduler(this)
    }

    val ringtonePlayer: RingtonePlayer by lazy {
        RingtonePlayer(this)
    }

    val reminderNotifier: ReminderNotifier by lazy {
        ReminderNotifier(this)
    }

    val reminderAlertDispatcher: ReminderAlertDispatcher by lazy {
        ReminderAlertDispatcher { reminder ->
            if (reminder.alertMode == AlertMode.Strong) {
                ringtonePlayer.playLooping(reminder.ringtoneId)
            } else {
                ringtonePlayer.preview(reminder.ringtoneId)
            }
            reminderNotifier.showReminder(reminder)
        }
    }

    val reminderDueHandler: ReminderDueHandler by lazy {
        ReminderDueHandler(
            repository = reminderRepository,
            alarmScheduler = alarmScheduler,
            alertDispatcher = reminderAlertDispatcher
        )
    }
}
