package com.timemaster.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ReminderDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var dao: ReminderDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        dao = database.reminderDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun observeAllReturnsInsertedReminder() = runBlocking {
        dao.insert(ReminderEntity(title = "喝水", intervalMinutes = 30))

        val reminders = dao.observeAll().first()

        assertEquals("喝水", reminders.single().title)
        assertEquals(30, reminders.single().intervalMinutes)
    }

    @Test
    fun weekdaysMaskRoundTripsSelectedDays() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY)

        val restoredDays = daysFromMask(maskFromDays(days))

        assertEquals(days, restoredDays)
    }
}
