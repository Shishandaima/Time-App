package com.timemaster.ui

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

        composeRule.onNode(hasContentDescription(hourPrompt()))
            .assert(hasStateDescription(durationState(0, 30, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasNoProgressInfo())
        composeRule.onNode(hasContentDescription(minutePrompt()))
            .assert(hasStateDescription(durationState(0, 30, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasNoProgressInfo())
        composeRule.onNode(hasContentDescription(secondPrompt()))
            .assert(hasStateDescription(durationState(0, 30, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasNoProgressInfo())
    }

    @Test
    fun timePickerExposesHourAndMinuteWheelsWithFullSelectionState() {
        setEditorContent()

        composeRule.onNodeWithText(START_TIME_LABEL).performClick()

        composeRule.onNode(hasContentDescription(hourPrompt()))
            .assert(hasStateDescription(timeState(8, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasNoProgressInfo())
        composeRule.onNode(hasContentDescription(minutePrompt()))
            .assert(hasStateDescription(timeState(8, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasNoProgressInfo())
    }

    @Test
    fun wheelDigitsAreHiddenFromTalkBackFocus() {
        setEditorContent()

        composeRule.onNodeWithText(START_TIME_LABEL).performClick()

        composeRule.onNode(
            hasText("08") and hasAnyAncestor(hasContentDescription(hourPrompt())),
            useUnmergedTree = true
        ).assertDoesNotExist()
    }

    @Test
    fun durationPickerAdjustingMinuteAndSecondUpdatesTalkBackStateDescription() {
        setEditorContent()

        composeRule.onNodeWithText(INTERVAL_LABEL).performClick()

        composeRule.onNode(hasContentDescription(minutePrompt()))
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(31f) }
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(minutePrompt()))
            .assert(hasStateDescription(durationState(0, 31, 0)))

        repeat(5) {
            composeRule.onNode(hasContentDescription(secondPrompt()))
                .performSemanticsAction(SemanticsActions.SetProgress) { action -> action((it + 1).toFloat()) }
            composeRule.waitForIdle()
        }

        composeRule.onNode(hasContentDescription(secondPrompt()))
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

    private fun hasNoProgressInfo() =
        SemanticsMatcher("has no progress info") { node ->
            try {
                node.config[SemanticsProperties.ProgressBarRangeInfo]
                false
            } catch (_: AssertionError) {
                true
            }
        }

    private fun hasStateDescription(value: String) =
        SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private fun hasScrollAction() =
        SemanticsMatcher("has scroll action") { node ->
            try {
                node.config[SemanticsActions.ScrollBy]
                true
            } catch (_: AssertionError) {
                false
            }
        }

    private fun hasSetProgressAction() =
        SemanticsMatcher("has set progress action") { node ->
            try {
                node.config[SemanticsActions.SetProgress]
                true
            } catch (_: AssertionError) {
                false
            }
        }

    private fun hasVerticalScrollRange() =
        SemanticsMatcher("has vertical scroll range") { node ->
            try {
                node.config[SemanticsProperties.VerticalScrollAxisRange]
                true
            } catch (_: AssertionError) {
                false
            }
        }

    private fun hourPrompt() = "\u5c0f\u65f6\uff0c\u53ef\u4e0a\u4e0b\u6ed1\u52a8\u8c03\u8282"

    private fun minutePrompt() = "\u5206\uff0c\u53ef\u4e0a\u4e0b\u6ed1\u52a8\u8c03\u8282"

    private fun secondPrompt() = "\u79d2\uff0c\u53ef\u4e0a\u4e0b\u6ed1\u52a8\u8c03\u8282"

    private fun durationState(hours: Int, minutes: Int, seconds: Int) =
        "$hours\u5c0f\u65f6$minutes\u5206$seconds\u79d2"

    private fun timeState(hours: Int, minutes: Int) = "$hours\u70b9$minutes\u5206"

    companion object {
        private const val INTERVAL_LABEL = "\u6bcf\u9694\u591a\u5c11\u65f6\u95f4"
        private const val START_TIME_LABEL = "\u5f00\u59cb\u65f6\u95f4"
    }
}
