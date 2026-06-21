package com.timemaster.ui

import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

        composeRule.onNodeWithText("每隔多少时间").performClick()

        composeRule.onNode(hasContentDescription("0小时滑块，可上下滑动调节"))
            .assert(hasProgressInfo(0f, 0f..23f))
            .assert(hasSetProgressAction())
        composeRule.onNode(hasContentDescription("30分滑块，可上下滑动调节"))
            .assert(hasProgressInfo(30f, 0f..59f))
            .assert(hasSetProgressAction())
        composeRule.onNode(hasContentDescription("0秒滑块，可上下滑动调节"))
            .assert(hasProgressInfo(0f, 0f..59f))
            .assert(hasSetProgressAction())
    }

    @Test
    fun timePickerExposesHourAndMinuteWheelsWithFullSelectionState() {
        setEditorContent()

        composeRule.onNodeWithText("开始时间").performClick()

        composeRule.onNode(hasContentDescription("8小时滑块，可上下滑动调节"))
            .assert(hasStateDescription("8点0分"))
            .assert(hasSetProgressAction())
        composeRule.onNode(hasContentDescription("0分滑块，可上下滑动调节"))
            .assert(hasStateDescription("8点0分"))
            .assert(hasSetProgressAction())
    }

    @Test
    fun wheelDigitsAreHiddenFromTalkBackFocus() {
        setEditorContent()

        composeRule.onNodeWithText("开始时间").performClick()

        composeRule.onNode(
            hasText("08") and hasAnyAncestor(hasContentDescription("8小时滑块，可上下滑动调节")),
            useUnmergedTree = true
        ).assertDoesNotExist()
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
        androidx.compose.ui.test.SemanticsMatcher("has progress info $current in $range") { node ->
            try {
                node.config[SemanticsProperties.ProgressBarRangeInfo] == ProgressBarRangeInfo(current, range)
            } catch (_: AssertionError) {
                false
            }
        }

    private fun hasStateDescription(value: String) =
        androidx.compose.ui.test.SemanticsMatcher.expectValue(SemanticsProperties.StateDescription, value)

    private fun hasSetProgressAction() =
        androidx.compose.ui.test.SemanticsMatcher("has set progress action") { node ->
            try {
                node.config[SemanticsActions.SetProgress]
                true
            } catch (_: AssertionError) {
                false
            }
        }
}
