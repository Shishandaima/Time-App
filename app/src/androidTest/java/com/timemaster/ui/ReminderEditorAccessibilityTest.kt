package com.timemaster.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performSemanticsAction
import com.timemaster.ui.editor.ReminderEditorScreen
import com.timemaster.ui.theme.TimeMasterTheme
import org.junit.Rule
import org.junit.Test

class ReminderEditorAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun durationPickerExposesEachWheelAsAdjustableTalkBackSlider() {
        setEditorContent()

        composeRule.onNodeWithText(INTERVAL_LABEL).performClick()

        composeRule.onNode(hasContentDescription(hourSlider(0)))
            .assert(hasProgressInfo(0f, 0f..23f))
            .assert(hasSetProgressAction())
        composeRule.onNode(hasContentDescription(minuteSlider(30)))
            .assert(hasProgressInfo(30f, 0f..59f))
            .assert(hasSetProgressAction())
        composeRule.onNode(hasContentDescription(secondSlider(0)))
            .assert(hasProgressInfo(0f, 0f..59f))
            .assert(hasSetProgressAction())
    }

    @Test
    fun timePickerExposesHourAndMinuteWheelsWithFullSelectionState() {
        setEditorContent()

        composeRule.onNodeWithText(START_TIME_LABEL).performClick()

        composeRule.onNode(hasContentDescription(hourSlider(8)))
            .assert(hasStateDescription(timeState(8, 0)))
            .assert(hasSetProgressAction())
        composeRule.onNode(hasContentDescription(minuteSlider(0)))
            .assert(hasStateDescription(timeState(8, 0)))
            .assert(hasSetProgressAction())
    }

    @Test
    fun wheelDigitsAreHiddenFromTalkBackFocus() {
        setEditorContent()

        composeRule.onNodeWithText(START_TIME_LABEL).performClick()

        composeRule.onNode(
            hasText("08") and hasAnyAncestor(hasContentDescription(hourSlider(8))),
            useUnmergedTree = true
        ).assertDoesNotExist()
    }

    @Test
    fun durationPickerAdjustingMinuteAndSecondUpdatesTalkBackStateDescription() {
        setEditorContent()

        composeRule.onNodeWithText(INTERVAL_LABEL).performClick()

        composeRule.onNode(hasContentDescription(minuteSlider(30)))
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(31f) }
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(minuteSlider(31)))
            .assert(hasStateDescription(durationState(0, 31, 0)))

        composeRule.onNode(hasContentDescription(secondSlider(0)))
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(5f) }
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(secondSlider(5)))
            .assert(hasStateDescription(durationState(0, 31, 5)))
    }

    private fun setEditorContent() {
        composeRule.setContent {
            TimeMasterTheme {
                ReminderEditorScreen(
                    initialReminder = null,
                    onBack = {},
                    onSave = {},
                    onPreviewRingtone = {}
                )
            }
        }
    }

    private fun hasProgressInfo(current: Float, range: ClosedFloatingPointRange<Float>) =
        SemanticsMatcher("has progress info $current in $range") { node ->
            try {
                node.config[SemanticsProperties.ProgressBarRangeInfo] == ProgressBarRangeInfo(current, range)
            } catch (_: AssertionError) {
                false
            }
        }

    private fun hasStateDescription(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private fun hasSetProgressAction() =
        SemanticsMatcher("has set progress action") { node ->
            try {
                node.config[SemanticsActions.SetProgress]
                true
            } catch (_: AssertionError) {
                false
            }
        }

    private fun hourSlider(value: Int) = "$value\u5c0f\u65f6\u6ed1\u5757\uff0c\u53ef\u4e0a\u4e0b\u6ed1\u52a8\u8c03\u8282"

    private fun minuteSlider(value: Int) = "$value\u5206\u6ed1\u5757\uff0c\u53ef\u4e0a\u4e0b\u6ed1\u52a8\u8c03\u8282"

    private fun secondSlider(value: Int) = "$value\u79d2\u6ed1\u5757\uff0c\u53ef\u4e0a\u4e0b\u6ed1\u52a8\u8c03\u8282"

    private fun durationState(hours: Int, minutes: Int, seconds: Int) =
        "$hours\u5c0f\u65f6$minutes\u5206$seconds\u79d2"

    private fun timeState(hours: Int, minutes: Int) = "$hours\u70b9$minutes\u5206"

    companion object {
        private const val INTERVAL_LABEL = "\u6bcf\u9694\u591a\u5c11\u65f6\u95f4"
        private const val START_TIME_LABEL = "\u5f00\u59cb\u65f6\u95f4"
    }
}
