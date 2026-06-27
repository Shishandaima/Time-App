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
import com.timemaster.sound.RingDurationMode
import com.timemaster.ui.settings.SettingsScreen
import com.timemaster.ui.theme.FontSizeMode
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
                fontSizeMode = FontSizeMode.Standard,
                ringDurationMode = RingDurationMode.TenSeconds,
                vibrationEnabled = true,
                silentModeEnabled = false,
                appVersion = "0.4.5",
                onCheckUpdate = {},
                onThemeModeChange = {},
                onFontSizeModeChange = {},
                onRingDurationModeChange = {},
                onVibrationEnabledChange = {},
                onSilentModeEnabledChange = {},
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
        composeRule.onNodeWithText("\u9707\u52a8").assertExists()
        composeRule.onNodeWithText("\u9759\u97f3").assertExists()
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
                fontSizeMode = FontSizeMode.Standard,
                ringDurationMode = RingDurationMode.TenSeconds,
                vibrationEnabled = true,
                silentModeEnabled = false,
                appVersion = "0.4.5",
                onCheckUpdate = {},
                onThemeModeChange = {},
                onFontSizeModeChange = {},
                onRingDurationModeChange = {},
                onVibrationEnabledChange = {},
                onSilentModeEnabledChange = {},
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
            .onNodeWithText("\u9707\u52a8")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        val silentTop = composeRule
            .onNodeWithText("\u9759\u97f3")
            .fetchSemanticsNode()
            .boundsInRoot
            .top

        assertTrue(fontSizeTop < ringDurationTop)
        assertTrue(ringDurationTop < vibrationTop)
        assertTrue(vibrationTop < silentTop)

        composeRule.onNode(isToggleable()).assertIsOn().performClick().assertIsOff()
    }

    @Test
    fun fontSizeRowShowsOptionsAndReportsSelection() {
        var selectedMode = FontSizeMode.Standard
        composeRule.setContent {
            SettingsScreen(
                themeMode = ThemeMode.System,
                fontSizeMode = selectedMode,
                ringDurationMode = RingDurationMode.TenSeconds,
                vibrationEnabled = true,
                silentModeEnabled = false,
                appVersion = "0.4.5",
                onCheckUpdate = {},
                onThemeModeChange = {},
                onFontSizeModeChange = { selectedMode = it },
                onRingDurationModeChange = {},
                onVibrationEnabledChange = {},
                onSilentModeEnabledChange = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithText("\u5b57\u4f53\u5927\u5c0f").performClick()

        composeRule.onNodeWithText("\u9009\u62e9\u5b57\u4f53\u5927\u5c0f").assertExists()
        composeRule.onNodeWithText("\u6807\u51c6").assertExists()
        composeRule.onNodeWithText("\u5927").assertExists()
        composeRule.onNodeWithText("\u7279\u5927").assertExists().performClick()
        composeRule.waitForIdle()

        assertTrue(selectedMode == FontSizeMode.ExtraLarge)
    }

    @Test
    fun ringDurationRowShowsOptionsAndReportsSelection() {
        var selectedMode = RingDurationMode.TenSeconds
        composeRule.setContent {
            SettingsScreen(
                themeMode = ThemeMode.System,
                fontSizeMode = FontSizeMode.Standard,
                ringDurationMode = selectedMode,
                vibrationEnabled = true,
                silentModeEnabled = false,
                appVersion = "0.4.5",
                onCheckUpdate = {},
                onThemeModeChange = {},
                onFontSizeModeChange = {},
                onRingDurationModeChange = { selectedMode = it },
                onVibrationEnabledChange = {},
                onSilentModeEnabledChange = {},
                onBack = {}
            )
        }

        composeRule.onNodeWithText("\u54cd\u94c3\u65f6\u957f").performClick()

        composeRule.onNodeWithText("\u9009\u62e9\u54cd\u94c3\u65f6\u957f").assertExists()
        composeRule.onNodeWithText("5\u79d2").assertExists().performClick()
        composeRule.waitForIdle()

        assertTrue(selectedMode == RingDurationMode.FiveSeconds)
    }

    @Test
    fun vibrationSwitchReportsPersistedSettingChange() {
        var vibrationEnabled = true
        composeRule.setContent {
            SettingsScreen(
                themeMode = ThemeMode.System,
                fontSizeMode = FontSizeMode.Standard,
                ringDurationMode = RingDurationMode.TenSeconds,
                vibrationEnabled = vibrationEnabled,
                silentModeEnabled = false,
                appVersion = "0.4.5",
                onCheckUpdate = {},
                onThemeModeChange = {},
                onFontSizeModeChange = {},
                onRingDurationModeChange = {},
                onVibrationEnabledChange = { vibrationEnabled = it },
                onSilentModeEnabledChange = {},
                onBack = {}
            )
        }

        composeRule.onNode(isToggleable()).assertIsOn().performClick()
        composeRule.waitForIdle()

        assertTrue(!vibrationEnabled)
    }

    @Test
    fun silentSwitchDefaultsOffAndReportsPersistedSettingChange() {
        var silentModeEnabled = false
        composeRule.setContent {
            SettingsScreen(
                themeMode = ThemeMode.System,
                fontSizeMode = FontSizeMode.Standard,
                ringDurationMode = RingDurationMode.TenSeconds,
                vibrationEnabled = true,
                silentModeEnabled = silentModeEnabled,
                appVersion = "0.4.5",
                onCheckUpdate = {},
                onThemeModeChange = {},
                onFontSizeModeChange = {},
                onRingDurationModeChange = {},
                onVibrationEnabledChange = {},
                onSilentModeEnabledChange = { silentModeEnabled = it },
                onBack = {}
            )
        }

        composeRule.onNodeWithText("\u9759\u97f3").assertExists()
        composeRule.onAllNodes(isToggleable())[1].assertIsOff().performClick()
        composeRule.waitForIdle()

        assertTrue(silentModeEnabled)
    }

    private fun hasHeading() =
        SemanticsMatcher("has heading") { node ->
            node.config.contains(SemanticsProperties.Heading)
        }
}
