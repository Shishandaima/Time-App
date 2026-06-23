package com.timemaster.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.ui.home.HomeFocusTarget
import com.timemaster.ui.home.HomeScreen
import java.time.DayOfWeek
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyHomeShowsAddReminderPlusButtonWithReadableLabel() {
        composeRule.setContent {
            HomeScreen(
                reminders = emptyList(),
                onOpenSettings = {},
                onAddReminder = {},
                onEditReminder = {},
                onToggleReminder = { _, _ -> },
                onDeleteReminder = {}
            )
        }

        composeRule
            .onNodeWithText("+")
            .assertExists()

        composeRule
            .onNodeWithContentDescription("\u65b0\u5efa\u5468\u671f\u63d0\u9192")
            .assertHasClickAction()

        composeRule
            .onNodeWithContentDescription("\u8bbe\u7f6e")
            .assertHasClickAction()

        composeRule
            .onNodeWithText("\u65f6\u95f4\u7ba1\u7406\u5927\u5e08")
            .assert(hasHeading())
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
                onOpenSettings = {},
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

    @Test
    fun homeFocusTargetFocusesSettingsButton() {
        var consumed = false

        composeRule.setContent {
            HomeScreen(
                reminders = emptyList(),
                focusTarget = HomeFocusTarget.SettingsButton,
                onHomeFocusTargetConsumed = { consumed = true },
                onOpenSettings = {},
                onAddReminder = {},
                onEditReminder = {},
                onToggleReminder = { _, _ -> },
                onDeleteReminder = {}
            )
        }

        composeRule.onNodeWithContentDescription("\u8bbe\u7f6e").assertIsFocused()
        assertTrue(consumed)
    }

    @Test
    fun homeFocusTargetFocusesAddReminderButton() {
        var consumed = false

        composeRule.setContent {
            HomeScreen(
                reminders = emptyList(),
                focusTarget = HomeFocusTarget.AddReminderButton,
                onHomeFocusTargetConsumed = { consumed = true },
                onOpenSettings = {},
                onAddReminder = {},
                onEditReminder = {},
                onToggleReminder = { _, _ -> },
                onDeleteReminder = {}
            )
        }

        composeRule.onNodeWithContentDescription("\u65b0\u5efa\u5468\u671f\u63d0\u9192").assertIsFocused()
        assertTrue(consumed)
    }

    @Test
    fun homeFocusTargetFocusesMatchingReminderCard() {
        var consumed = false
        val targetReminder = reminder(id = 2, title = "\u76ee\u6807\u63d0\u9192")
        val otherReminder = reminder(id = 1, title = "\u5176\u4ed6\u63d0\u9192")

        composeRule.setContent {
            HomeScreen(
                reminders = listOf(otherReminder, targetReminder),
                focusTarget = HomeFocusTarget.ReminderCard(targetReminder.id),
                onHomeFocusTargetConsumed = { consumed = true },
                onOpenSettings = {},
                onAddReminder = {},
                onEditReminder = {},
                onToggleReminder = { _, _ -> },
                onDeleteReminder = {}
            )
        }

        composeRule.onNode(hasContentDescriptionParts("\u76ee\u6807\u63d0\u9192")).assertIsFocused()
        assertTrue(consumed)
    }

    private fun reminder(id: Long, title: String): Reminder {
        val now = System.currentTimeMillis()
        return Reminder(
            id = id,
            title = title,
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

    private fun hasHeading() =
        SemanticsMatcher("has heading") { node ->
            node.config.contains(SemanticsProperties.Heading)
        }
}
