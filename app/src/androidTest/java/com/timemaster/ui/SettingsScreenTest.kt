package com.timemaster.ui

import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import com.timemaster.ui.settings.SettingsScreen
import com.timemaster.ui.theme.ThemeMode
import org.junit.Rule
import org.junit.Test

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsScreenShowsSectionsAndReadableBackButton() {
        composeRule.setContent {
            SettingsScreen(
                themeMode = ThemeMode.System,
                appVersion = "0.4.5",
                onThemeModeChange = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithText("\u8bbe\u7f6e").assertExists()
        composeRule.onNodeWithContentDescription("\u8fd4\u56de").assertHasClickAction()
        composeRule.onNodeWithText("\u901a\u7528").assertExists()
        composeRule.onNodeWithText("\u4e3b\u9898").assertExists()
        composeRule.onNodeWithText("\u6d45\u8272").assertExists()
        composeRule.onNodeWithText("\u6df1\u8272").assertExists()
        composeRule.onNodeWithText("\u8ddf\u968f\u7cfb\u7edf").assertExists()
        composeRule.onNodeWithText("\u5173\u4e8e").assertExists()
        composeRule.onNodeWithText("\u68c0\u67e5\u66f4\u65b0").assertExists()
        composeRule.onNodeWithText("APP\u7248\u672c\u53f7").assertExists()
        composeRule.onNodeWithText("0.4.5").assertExists()
    }
}
