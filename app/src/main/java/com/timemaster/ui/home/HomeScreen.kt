package com.timemaster.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.ui.accessibility.pageEntryTitleFocus
import com.timemaster.ui.accessibility.requestFocusOnEntry
import java.time.Instant
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max
import kotlinx.coroutines.delay

sealed interface HomeFocusTarget {
    data object SettingsButton : HomeFocusTarget
    data object AddReminderButton : HomeFocusTarget
    data class ReminderCard(val reminderId: Long) : HomeFocusTarget
}

@Composable
fun HomeScreen(
    reminders: List<Reminder>,
    focusTarget: HomeFocusTarget? = null,
    onOpenSettings: () -> Unit,
    onAddReminder: () -> Unit,
    onEditReminder: (Reminder) -> Unit,
    onToggleReminder: (Reminder, Boolean) -> Unit,
    onDeleteReminder: (Reminder) -> Unit,
    permissionWarnings: List<String> = emptyList()
) {
    var deleteCandidate by remember { mutableStateOf<Reminder?>(null) }
    var nowMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    val focusTitleOnEntry = remember { focusTarget == null }

    LaunchedEffect(Unit) {
        while (true) {
            nowMillis = System.currentTimeMillis()
            delay(1_000L)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "\u65f6\u95f4\u7ba1\u7406\u5927\u5e08",
                style = MaterialTheme.typography.displaySmall,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 64.dp)
                    .pageEntryTitleFocus(enabled = focusTitleOnEntry)
            )
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-20).dp)
                    .size(64.dp)
                    .requestFocusOnEntry(
                        focusKey = focusTarget.takeIf { it == HomeFocusTarget.SettingsButton }
                    )
                    .semantics {
                        contentDescription = "\u8bbe\u7f6e"
                    }
            ) {
                Text(
                    text = "\u2699",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
            }
            IconButton(
                onClick = onAddReminder,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp)
                    .size(64.dp)
                    .requestFocusOnEntry(
                        focusKey = focusTarget.takeIf { it == HomeFocusTarget.AddReminderButton }
                    )
                    .semantics {
                        contentDescription = "\u65b0\u5efa\u5468\u671f\u63d0\u9192"
                    }
            ) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.displaySmall,
                    textAlign = TextAlign.Center
                )
            }
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
                        focusKey = focusTarget
                            .takeIf {
                                it is HomeFocusTarget.ReminderCard &&
                                    it.reminderId == reminder.id
                            },
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
    focusKey: Any?,
    onEdit: () -> Unit,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val countdownText = cardCountdownText(reminder, nowMillis)
    val description = remember(reminder, countdownText) { reminder.accessibilitySummary(countdownText) }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .semantics { contentDescription = description }
            .requestFocusOnEntry(
                focusKey = focusKey
            )
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
            Text(
                text = reminder.intervalSummary(),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.semantics {
                    contentDescription = reminder.intervalAccessibilitySummary()
                }
            )
            Text(text = reminder.timeWindowSummary(), style = MaterialTheme.typography.bodyLarge)
            Text(
                text = countdownText,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.semantics {
                    contentDescription = countdownAccessibilityText(countdownText)
                }
            )
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

private fun Reminder.intervalSummary(): String =
    "\u95f4\u9694\u65f6\u95f4\uff1a${formatDuration(rule.intervalSeconds)}\uff0c${daySummary(rule.enabledDays)}"

private fun Reminder.intervalAccessibilitySummary(): String =
    "\u95f4\u9694\u65f6\u95f4\uff1a${formatDurationForTalkBack(rule.intervalSeconds.toLong())}\uff0c${daySummary(rule.enabledDays)}"

private fun Reminder.timeWindowSummary(): String =
    "\u65f6\u95f4\u6bb5\uff1a${formatMinute(rule.startMinuteOfDay)} \u5230 ${formatMinute(rule.endMinuteOfDay)}"

private fun Reminder.modeSummary(): String =
    "\u63d0\u9192\u65b9\u5f0f\uff1a${if (alertMode == AlertMode.Strong) "\u5f3a\u63d0\u9192" else "\u666e\u901a\u63d0\u9192"}"

private fun Reminder.accessibilitySummary(countdownText: String): String =
    "${title.ifBlank { "\u672a\u547d\u540d\u63d0\u9192" }}\uff0c${if (isEnabled) "\u5df2\u542f\u7528" else "\u5df2\u5173\u95ed"}\uff0c${intervalAccessibilitySummary()}\uff0c${timeWindowSummary()}\uff0c${countdownAccessibilityText(countdownText)}\uff0c${modeSummary()}"

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

private fun formatDurationForTalkBack(totalSeconds: Long): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val parts = buildList {
        if (hours > 0) add("${hours}\u5c0f\u65f6")
        if (minutes > 0) {
            val unit = if (hours == 0L && seconds == 0L) "\u5206\u949f" else "\u5206"
            add("${minutes}$unit")
        }
        if (seconds > 0) add("${seconds}\u79d2")
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = "") ?: "0\u79d2"
}

private fun countdownAccessibilityText(countdownText: String): String {
    val prefix = "\u4e0b\u6b21\u63d0\u9192\uff1a"
    val value = countdownText.removePrefix(prefix)
    if (value == "\u672a\u542f\u7528") return countdownText

    val daysAndClock = value.split(" ", limit = 2)
    val daysText = daysAndClock.firstOrNull()?.takeIf { it.endsWith("\u5929") }
    val clock = if (daysText == null) value else daysAndClock.getOrNull(1).orEmpty()
    val seconds = parseClockSeconds(clock) ?: return countdownText
    return buildString {
        append(prefix)
        if (daysText != null) append(daysText)
        append(formatDurationForTalkBack(seconds))
    }
}

private fun parseClockSeconds(clock: String): Long? {
    val parts = clock.split(":")
    if (parts.size != 3) return null
    val hours = parts[0].toLongOrNull() ?: return null
    val minutes = parts[1].toLongOrNull() ?: return null
    val seconds = parts[2].toLongOrNull() ?: return null
    return hours * 3600 + minutes * 60 + seconds
}

private fun cardCountdownText(
    reminder: Reminder,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    if (!reminder.isEnabled) return "\u4e0b\u6b21\u63d0\u9192\uff1a\u672a\u542f\u7528"

    val targetMillis = reminder.nextTriggerAtMillis
        ?.takeIf { it > nowMillis }
        ?: cardNextTrigger(
            now = LocalDateTime.ofInstant(Instant.ofEpochMilli(nowMillis), zoneId),
            rule = reminder.rule
        ).atZone(zoneId).toInstant().toEpochMilli()

    val totalSeconds = max(
        0L,
        ChronoUnit.SECONDS.between(
            Instant.ofEpochMilli(nowMillis),
            Instant.ofEpochMilli(targetMillis)
        )
    )
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    val clock = "%02d:%02d:%02d".format(hours, minutes, seconds)

    return if (days > 0) {
        "\u4e0b\u6b21\u63d0\u9192\uff1a${days}\u5929 $clock"
    } else {
        "\u4e0b\u6b21\u63d0\u9192\uff1a$clock"
    }
}

private fun cardNextTrigger(now: LocalDateTime, rule: ReminderRule): LocalDateTime {
    for (daysAhead in 0..13) {
        val date = now.toLocalDate().plusDays(daysAhead.toLong())
        if (date.dayOfWeek !in rule.enabledDays) continue

        val start = date.atMinuteOfDay(rule.startMinuteOfDay)
        val end = date.atMinuteOfDay(rule.endMinuteOfDay)
        val candidate = when {
            daysAhead > 0 -> start
            now.isBefore(start) -> start
            now.isBefore(end) -> now.plusSeconds(rule.intervalSeconds.toLong())
            else -> null
        }

        if (candidate != null && !candidate.isAfter(end)) {
            return candidate
        }
    }

    error("Unable to find next trigger within two weeks")
}

private fun LocalDate.atMinuteOfDay(minuteOfDay: Int): LocalDateTime =
    atStartOfDay().plusMinutes(minuteOfDay.toLong())

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
