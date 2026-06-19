package com.timemaster.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.timemaster.data.ReminderRepository
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.ui.editor.ReminderEditorScreen
import com.timemaster.ui.home.HomeScreen
import java.time.DayOfWeek
import kotlinx.coroutines.launch

@Composable
fun TimeMasterApp(
    repository: ReminderRepository,
    onPreviewRingtone: (String) -> Unit = {}
) {
    val reminders by repository.observeReminders().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var showingEditor by remember { mutableStateOf(false) }

    if (showingEditor) {
        ReminderEditorScreen(
            initialReminder = editingReminder,
            onBack = {
                showingEditor = false
                editingReminder = null
            },
            onSave = { reminder ->
                scope.launch {
                    repository.saveReminder(reminder)
                    showingEditor = false
                    editingReminder = null
                }
            },
            onPreviewRingtone = onPreviewRingtone
        )
    } else {
        HomeScreen(
            reminders = reminders,
            onAddReminder = {
                editingReminder = null
                showingEditor = true
            },
            onEditReminder = { reminder ->
                editingReminder = reminder
                showingEditor = true
            },
            onToggleReminder = { reminder, enabled ->
                scope.launch {
                    repository.saveReminder(reminder.copy(isEnabled = enabled))
                }
            },
            onDeleteReminder = { reminder ->
                scope.launch {
                    repository.deleteReminder(reminder.id)
                }
            }
        )
    }
}

fun newReminder(
    title: String,
    intervalMinutes: Int,
    startMinuteOfDay: Int,
    endMinuteOfDay: Int,
    enabledDays: Set<DayOfWeek>,
    alertMode: AlertMode,
    ringtoneId: String
): Reminder = Reminder(
    id = 0,
    title = title,
    rule = ReminderRule(
        intervalMinutes = intervalMinutes,
        startMinuteOfDay = startMinuteOfDay,
        endMinuteOfDay = endMinuteOfDay,
        enabledDays = enabledDays
    ),
    alertMode = alertMode,
    ringtoneId = ringtoneId,
    isEnabled = true,
    nextTriggerAtMillis = null
)
