package com.timemaster.ui

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.timemaster.ui.settings.SettingsScreen
import com.timemaster.ui.theme.ThemeMode
import org.junit.Assert.assertTrue
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
                onCheckUpdate = {},
                onThemeModeChange = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithText("\u8bbe\u7f6e").assertExists()
        composeRule.onNodeWithText("\u8bbe\u7f6e").assert(hasHeading())
        composeRule.onNodeWithContentDescription("\u8fd4\u56de").assertHasClickAction()
        composeRule.onNodeWithText("\u901a\u7528").assertExists()
        composeRule.onNodeWithText("\u4e3b\u9898").assertExists()
        composeRule.onNodeWithText("\u6d45\u8272").assertExists()
        composeRule.onNodeWithText("\u6df1\u8272").assertExists()
        composeRule.onNodeWithText("\u8ddf\u968f\u7cfb\u7edf").assertExists()
        composeRule.onNodeWithText("\u5b57\u4f53\u5927\u5c0f").assertExists()
        composeRule.onNodeWithText("\u6807\u51c6").assertExists()
        composeRule.onNodeWithText("\u54cd\u94c3\u65f6\u957f").assertExists()
        composeRule.onNodeWithText("10\u79d2").assertExists()
        composeRule.onNodeWithText("\u9707\u52a8\u5f00\u5173").assertExists()
        composeRule.onNodeWithText("\u5173\u4e8e").assertExists()
        composeRule.onNodeWithText("\u68c0\u67e5\u66f4\u65b0").assertHasClickAction()
        composeRule.onNodeWithText("APP\u7248\u672c\u53f7").assertExists()
        composeRule.onNodeWithText("0.4.5").assertExists()
    }

    @Test
    fun generalSettingsShowNewRowsInOrderAndTemporaryVibrationSwitchToggles() {
        composeRule.setContent {
            SettingsScreen(
                themeMode = ThemeMode.System,
                appVersion = "0.4.5",
                onCheckUpdate = {},
                onThemeModeChange = {},
                onBack = {}
            )
        }

        val fontSizeTop = composeRule
            .onNodeWithText("\u5b57\u4f53\u5927\u5c0f")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val ringDurationTop = composeRule
            .onNodeWithText("\u54cd\u94c3\u65f6\u957f")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val vibrationTop = composeRule
            .onNodeWithText("\u9707\u52a8\u5f00\u5173")
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        assertTrue(fontSizeTop < ringDurationTop)
        assertTrue(ringDurationTop < vibrationTop)

        composeRule.onNode(isToggleable()).assertIsOn().performClick().assertIsOff()
    }

    private fun hasHeading() =
        SemanticsMatcher("has heading") { node ->
            node.config.contains(SemanticsProperties.Heading)
        }
}
