package com.timemaster.domain

fun needsScheduleRepair(reminder: Reminder, nowMillis: Long): Boolean =
    reminder.isEnabled &&
        reminder.nextTriggerAtMillis?.let { it <= nowMillis } != false
