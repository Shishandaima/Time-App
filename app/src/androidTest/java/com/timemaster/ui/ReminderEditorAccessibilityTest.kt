package com.timemaster.ui

import androidx.compose.ui.test.assertHasClickAction
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
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.ui.editor.ReminderEditorScreen
import java.time.DayOfWeek
import org.junit.Rule
import org.junit.Test

class ReminderEditorAccessibilityTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun backButtonReadsReturnForTalkBack() {
        setEditorContent()

        composeRule.onNode(hasContentDescription("\u8fd4\u56de"))
            .assertHasClickAction()
    }

    @Test
    fun intervalButtonReadsDurationAsHoursMinutesAndSeconds() {
        setEditorContent(initialReminder = reminderWithInterval(1 * 3600 + 30 * 60))

        composeRule.onNodeWithText("01:30:00").assertDoesNotExist()
        composeRule.onNode(hasContentDescription("$INTERVAL_LABEL\uff0c1\u5c0f\u65f630\u5206"))
            .assertExists()

        setEditorContent(initialReminder = reminderWithInterval(30 * 60))

        composeRule.onNodeWithText("00:30:00").assertDoesNotExist()
        composeRule.onNode(hasContentDescription("$INTERVAL_LABEL\uff0c30\u5206\u949f"))
            .assertExists()

        setEditorContent(initialReminder = reminderWithInterval(2 * 3600 + 30 * 60 + 15))

        composeRule.onNodeWithText("02:30:15").assertDoesNotExist()
        composeRule.onNode(hasContentDescription("$INTERVAL_LABEL\uff0c2\u5c0f\u65f630\u520615\u79d2"))
            .assertExists()
    }

    @Test
    fun startTimeButtonKeepsClockStyleText() {
        setEditorContent(initialReminder = reminderWithInterval(30 * 60, startMinuteOfDay = 9 * 60))

        composeRule.onNodeWithText("09:00").assertExists()
    }

    @Test
    fun durationPickerExposesEachWheelAsAdjustableTalkBackSlider() {
        setEditorContent()

        composeRule.onNodeWithText(INTERVAL_LABEL).performClick()

        composeRule.onNode(hasContentDescription(hourLabel(0)))
            .assert(hasStateDescription(durationState(0, 30, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasProgressInfo(0f, 0f..23f, steps = 22))
        composeRule.onNode(hasContentDescription(minuteLabel(30)))
            .assert(hasStateDescription(durationState(0, 30, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasProgressInfo(30f, 0f..59f, steps = 58))
        composeRule.onNode(hasContentDescription(secondLabel(0)))
            .assert(hasStateDescription(durationState(0, 30, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasProgressInfo(0f, 0f..59f, steps = 58))
    }

    @Test
    fun timePickerExposesHourAndMinuteWheelsWithFullSelectionState() {
        setEditorContent()

        composeRule.onNodeWithText(START_TIME_LABEL).performClick()

        composeRule.onNode(hasContentDescription(hourLabel(8)))
            .assert(hasStateDescription(timeState(8, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasProgressInfo(8f, 0f..23f, steps = 22))
        composeRule.onNode(hasContentDescription(minuteLabel(0)))
            .assert(hasStateDescription(timeState(8, 0)))
            .assert(hasScrollAction())
            .assert(hasSetProgressAction())
            .assert(hasVerticalScrollRange())
            .assert(hasProgressInfo(0f, 0f..59f, steps = 58))
    }

    @Test
    fun wheelDigitsAreHiddenFromTalkBackFocus() {
        setEditorContent()

        composeRule.onNodeWithText(START_TIME_LABEL).performClick()

        composeRule.onNode(
            hasText("08") and hasAnyAncestor(hasContentDescription(hourLabel(8))),
            useUnmergedTree = true
        ).assertDoesNotExist()
    }

    @Test
    fun durationPickerAdjustingMinuteAndSecondUpdatesTalkBackStateDescription() {
        setEditorContent()

        composeRule.onNodeWithText(INTERVAL_LABEL).performClick()

        composeRule.onNode(hasContentDescription(minuteLabel(30)))
            .performSemanticsAction(SemanticsActions.SetProgress) { action -> action(31f) }
        composeRule.waitForIdle()

        composeRule.onNode(hasContentDescription(minuteLabel(31)))
            .assert(hasStateDescription(durationState(0, 31, 0)))

        repeat(5) {
            composeRule.onNode(hasContentDescription(secondLabel(it)))
                .performSemanticsAction(SemanticsActions.SetProgress) { action -> action((it + 1).toFloat()) }
            composeRule.waitForIdle()
        }

        composeRule.onNode(hasContentDescription(secondLabel(5)))
            .assert(hasStateDescription(durationState(0, 31, 5)))
    }

    private fun setEditorContent(initialReminder: Reminder? = null) {
        composeRule.setContent {
            ReminderEditorScreen(
                initialReminder = initialReminder,
                onBack = {},
                onSave = {},
                onPreviewRingtone = {}
            )
        }
    }

    private fun hasProgressInfo(
        current: Float,
        range: ClosedFloatingPointRange<Float>,
        steps: Int
    ) =
        SemanticsMatcher("has progress info $current in $range") { node ->
            try {
                node.config[SemanticsProperties.ProgressBarRangeInfo] ==
                    ProgressBarRangeInfo(current, range, steps)
            } catch (_: AssertionError) {
                false
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

    private fun hourLabel(value: Int) = "$value\u5c0f\u65f6"

    private fun minuteLabel(value: Int) = "$value\u5206"

    private fun secondLabel(value: Int) = "$value\u79d2"

    private fun durationState(hours: Int, minutes: Int, seconds: Int) =
        "$hours\u5c0f\u65f6$minutes\u5206$seconds\u79d2"

    private fun timeState(hours: Int, minutes: Int) = "$hours\u70b9$minutes\u5206"

    private fun reminderWithInterval(
        intervalSeconds: Int,
        startMinuteOfDay: Int = 8 * 60
    ) = Reminder(
        id = 1,
        title = "\u6d4b\u8bd5\u63d0\u9192",
        rule = ReminderRule(
            intervalSeconds = intervalSeconds,
            startMinuteOfDay = startMinuteOfDay,
            endMinuteOfDay = 22 * 60,
            enabledDays = DayOfWeek.entries.toSet()
        ),
        alertMode = AlertMode.Strong,
        ringtoneId = "gentle_chime",
        isEnabled = true,
        nextTriggerAtMillis = null
    )

    companion object {
        private const val INTERVAL_LABEL = "\u6bcf\u9694\u591a\u5c11\u65f6\u95f4"
        private const val START_TIME_LABEL = "\u5f00\u59cb\u65f6\u95f4"
    }
}
