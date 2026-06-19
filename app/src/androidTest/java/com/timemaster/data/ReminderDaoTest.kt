package com.timemaster.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import java.time.DayOfWeek
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.fail
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

    @Test
    fun daysFromMaskWithUnknownBitsFallsBackToEveryDay() {
        val restoredDays = daysFromMask(128)

        assertEquals(DayOfWeek.entries.toSet(), restoredDays)
    }

    @Test
    fun upsertWithExistingIdUpdatesSameRow() = runBlocking {
        val id = dao.upsert(
            ReminderEntity(
                title = "喝水",
                createdAtMillis = 100,
                updatedAtMillis = 100
            )
        )

        val returnedId = dao.upsert(
            ReminderEntity(
                id = id,
                title = "站立",
                createdAtMillis = 100,
                updatedAtMillis = 200
            )
        )

        val reminders = dao.observeAll().first()
        assertEquals(id, returnedId)
        assertEquals(1, reminders.size)
        assertEquals("站立", reminders.single().title)
    }

    @Test
    fun upsertWithMissingNonzeroIdThrows() = runBlocking {
        try {
            dao.upsert(ReminderEntity(id = 999, title = "站立"))
            fail("Expected IllegalStateException")
        } catch (exception: IllegalStateException) {
            assertEquals("Reminder 999 does not exist", exception.message)
        }
    }

    @Test
    fun repositorySaveReminderPreservesCreatedAtMillisForExistingReminder() = runBlocking {
        val id = dao.insert(
            ReminderEntity(
                title = "喝水",
                createdAtMillis = 100,
                updatedAtMillis = 100
            )
        )
        val repository = ReminderRepository(dao)

        repository.saveReminder(
            Reminder(
                id = id,
                title = "站立",
                rule = ReminderRule(
                    intervalMinutes = 30,
                    startMinuteOfDay = 8 * 60,
                    endMinuteOfDay = 22 * 60,
                    enabledDays = DayOfWeek.entries.toSet()
                ),
                alertMode = AlertMode.Strong,
                ringtoneId = "gentle_chime",
                isEnabled = true,
                nextTriggerAtMillis = null
            )
        )

        val saved = dao.getById(id)
        assertNotNull(saved)
        assertEquals("站立", saved?.title)
        assertEquals(100L, saved?.createdAtMillis)
    }
}
