package com.timemaster.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import java.time.DayOfWeek
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(
    reminders: List<Reminder>,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder, Boolean) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    permissionWarnings: List<String> = emptyList()
) {
    var deleteCandidate by remember { mutableStateOf<Reminder?>(null) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Text(
            text = "\u65f6\u95f4\u7ba1\u7406\u5927\u5e08",
            style = MaterialTheme.typography.displaySmall
        )
        Button(
            onClick = onAddReminder,
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Text("\u65b0\u5efa\u5468\u671f\u63d0\u9192")
        }

        permissionWarnings.forEach { warning ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = warning,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        if (reminders.isEmpty()) {
            EmptyState(onAddReminder = onAddReminder)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(reminders, key = { it.id }) { reminder ->
                    ReminderCard(
                        reminder = reminder,
                        nowMillis = nowMillis,
                        onEdit = { onEditReminder(reminder) },
                        onToggle = { enabled -> onToggleReminder(reminder, enabled) },
                        onDelete = { deleteCandidate = reminder }
                    )
                }
            }
        }
    }

    deleteCandidate?.let { reminder ->
        AlertDialog(
            onDismissRequest = { deleteCandidate = null },
            title = { Text("\u5220\u9664\u63d0\u9192") },
            text = { Text("\u786e\u5b9a\u5220\u9664\u201c${reminder.title}\u201d\u5417\uff1f") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDeleteReminder(reminder)
                        deleteCandidate = null
                    }
                ) {
                    Text("\u5220\u9664")
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteCandidate = null }) {
                    Text("\u53d6\u6d88")
                }
            }
        )
    }
}

@Composable
private fun EmptyState(onAddReminder: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "\u8fd8\u6ca1\u6709\u63d0\u9192",
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = "\u6dfb\u52a0\u4e00\u4e2a\u5468\u671f\u63d0\u9192\uff0c\u6bd4\u5982\u559d\u6c34\u3001\u5403\u836f\u6216\u6d3b\u52a8\u4e00\u4e0b\u3002",
            style = MaterialTheme.typography.bodyLarge
        )
        OutlinedButton(
            onClick = onAddReminder,
            modifier = Modifier.height(64.dp)
        ) {
            Text("\u7acb\u5373\u65b0\u5efa")
        }
    }
}

@Composable
private fun ReminderCard(
    reminder: Reminder,
    nowMillis: Long,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val countdownText = reminderCountdownText(reminder, nowMillis)
    val description = remember(reminder, countdownText) { reminder.accessibilitySummary(countdownText) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description }
            .clickable(onClick = onEdit)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = reminder.title.ifBlank { "\u672a\u547d\u540d\u63d0\u9192" },
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = reminder.isEnabled,
                    onCheckedChange = onToggle
                )
            }
            Text(text = reminder.ruleSummary(), style = MaterialTheme.typography.bodyLarge)
            Text(text = countdownText, style = MaterialTheme.typography.headlineSmall)
            Text(text = reminder.modeSummary(), style = MaterialTheme.typography.bodyLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.height(56.dp)) {
                    Text("\u7f16\u8f91")
                }
                OutlinedButton(onClick = onDelete, modifier = Modifier.height(56.dp)) {
                    Text("\u5220\u9664")
                }
            }
        }
    }
}

private fun Reminder.ruleSummary(): String =
    "\u6bcf ${formatDuration(rule.intervalSeconds)}\uff0c${formatMinute(rule.startMinuteOfDay)} \u5230 ${formatMinute(rule.endMinuteOfDay)}\uff0c${daySummary(rule.enabledDays)}"

private fun Reminder.modeSummary(): String =
    if (alertMode == AlertMode.Strong) "\u5f3a\u63d0\u9192" else "\u666e\u901a\u63d0\u9192"

private fun Reminder.accessibilitySummary(countdownText: String): String =
    "${title.ifBlank { "\u672a\u547d\u540d\u63d0\u9192" }}\uff0c${if (isEnabled) "\u5df2\u542f\u7528" else "\u5df2\u5173\u95ed"}\uff0c${ruleSummary()}\uff0c$countdownText\uff0c${modeSummary()}"

private fun formatMinute(minuteOfDay: Int): String {
    val hour = minuteOfDay / 60
    val minute = minuteOfDay % 60
    return "%02d:%02d".format(hour, minute)
}

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun daySummary(days: Set<DayOfWeek>): String {
    if (days.size == 7) return "\u6bcf\u5929"
    return days.sortedBy { it.value }.joinToString(" ") { day ->
        when (day) {
            DayOfWeek.MONDAY -> "\u5468\u4e00"
            DayOfWeek.TUESDAY -> "\u5468\u4e8c"
            DayOfWeek.WEDNESDAY -> "\u5468\u4e09"
            DayOfWeek.THURSDAY -> "\u5468\u56db"
            DayOfWeek.FRIDAY -> "\u5468\u4e94"
            DayOfWeek.SATURDAY -> "\u5468\u516d"
            DayOfWeek.SUNDAY -> "\u5468\u65e5"
        }
    }
}
