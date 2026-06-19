package com.timemaster.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.timemaster.ui.home.HomeScreen
import com.timemaster.ui.theme.TimeMasterTheme
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun emptyHomeShowsAddReminderButton() {
        composeRule.setContent {
            TimeMasterTheme {
                HomeScreen(
                    reminders = emptyList(),
                    onAddReminder = {},
                    onEditReminder = {},
                    onToggleReminder = { _, _ -> },
                    onDeleteReminder = {}
                )
            }
        }

        composeRule
            .onNodeWithText("\u65b0\u5efa\u5468\u671f\u63d0\u9192")
            .assertHasClickAction()
    }
}
