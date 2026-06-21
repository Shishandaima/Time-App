package com.timemaster.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.ui.home.HomeScreen
import java.time.DayOfWeek
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyHomeShowsAddReminderButton() {
        composeRule.setContent {
            HomeScreen(
                reminders = emptyList(),
                onAddReminder = {},
                onEditReminder = {},
                onToggleReminder = { _, _ -> },
                onDeleteReminder = {}
            )
        }

        composeRule
            .onNodeWithText("\u65b0\u5efa\u5468\u671f\u63d0\u9192")
            .assertHasClickAction()
    }

    @Test
    fun reminderCardShowsLabeledRowsAndReadableDurations() {
        val now = System.currentTimeMillis()
        val reminder = Reminder(
            id = 1,
            title = "\u6d4b\u8bd5\u63d0\u9192",
            rule = ReminderRule(
                intervalSeconds = 60 * 60,
                startMinuteOfDay = 9 * 60,
                endMinuteOfDay = 21 * 60,
                enabledDays = DayOfWeek.entries.toSet()
            ),
            alertMode = AlertMode.Strong,
            ringtoneId = "gentle_chime",
            isEnabled = true,
            nextTriggerAtMillis = now + 4_558_000L
        )

        composeRule.setContent {
            HomeScreen(
                reminders = listOf(reminder),
                onAddReminder = {},
                onEditReminder = {},
                onToggleReminder = { _, _ -> },
                onDeleteReminder = {}
            )
        }

        composeRule.onNodeWithText("\u95f4\u9694\u65f6\u95f4\uff1a01:00:00\uff0c\u6bcf\u5929").assertExists()
        composeRule.onNodeWithText("\u65f6\u95f4\u6bb5\uff1a09:00 \u5230 21:00").assertExists()
        composeRule.onNodeWithText("\u63d0\u9192\u65b9\u5f0f\uff1a\u5f3a\u63d0\u9192").assertExists()
        composeRule.onNode(
            hasContentDescriptionParts(
                "\u95f4\u9694\u65f6\u95f4\uff1a1\u5c0f\u65f6",
                "\u4e0b\u6b21\u63d0\u9192\uff1a1\u5c0f\u65f615\u5206"
            )
        ).assertExists()
    }

    private fun hasContentDescriptionParts(vararg parts: String) =
        SemanticsMatcher("has content description parts") { node ->
            try {
                val description = node.config[SemanticsProperties.ContentDescription]
                    .joinToString(separator = "\uff0c")
                parts.all { description.contains(it) }
            } catch (_: AssertionError) {
                false
            }
        }
}
