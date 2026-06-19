package com.timemaster

import android.app.Application
import androidx.room.Room
import com.timemaster.alarm.AlarmScheduler
import com.timemaster.alarm.ReminderAlarmScheduler
import com.timemaster.data.AppDatabase
import com.timemaster.data.ReminderRepository

class TimeMasterApplication : Application() {
    val database: AppDatabase by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "time_master.db").build()
    }

    val reminderRepository: ReminderRepository by lazy {
        ReminderRepository(database.reminderDao())
    }

    val alarmScheduler: AlarmScheduler by lazy {
        ReminderAlarmScheduler(this)
    }
}
