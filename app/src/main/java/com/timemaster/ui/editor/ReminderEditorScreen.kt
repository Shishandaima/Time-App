package com.timemaster.ui.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.sound.RingtoneCatalog
import com.timemaster.ui.newReminder
import java.time.DayOfWeek

@Composable
fun ReminderEditorScreen(
    initialReminder: Reminder?,
    onBack: () -> Unit,
    onSave: (Reminder) -> Unit,
    onPreviewRingtone: (String) -> Unit = {}
) {
    var title by remember { mutableStateOf(initialReminder?.title.orEmpty()) }
    var intervalText by remember {
        mutableStateOf((initialReminder?.rule?.intervalMinutes ?: 30).toString())
    }
    var startText by remember {
        mutableStateOf(formatMinute(initialReminder?.rule?.startMinuteOfDay ?: 8 * 60))
    }
    var endText by remember {
        mutableStateOf(formatMinute(initialReminder?.rule?.endMinuteOfDay ?: 22 * 60))
    }
    var selectedDays by remember {
        mutableStateOf(initialReminder?.rule?.enabledDays ?: DayOfWeek.entries.toSet())
    }
    var alertMode by remember { mutableStateOf(initialReminder?.alertMode ?: AlertMode.Strong) }
    var ringtoneId by remember { mutableStateOf(initialReminder?.ringtoneId ?: RingtoneCatalog.all.first().id) }
    var errorText by remember { mutableStateOf<String?>(null) }
    var pickingStartTime by remember { mutableStateOf(false) }
    var pickingEndTime by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Text("\u2039", style = MaterialTheme.typography.displaySmall)
            }
            Text(
                text = if (initialReminder == null) "\u65b0\u5efa\u5468\u671f\u63d0\u9192" else "\u7f16\u8f91\u63d0\u9192",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("\u63d0\u9192\u5185\u5bb9") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true
        )
        OutlinedTextField(
            value = intervalText,
            onValueChange = { intervalText = it.filter(Char::isDigit).take(4) },
            label = { Text("\u6bcf\u9694\u591a\u5c11\u5206\u949f") },
            modifier = Modifier.fillMaxWidth(),
            textStyle = MaterialTheme.typography.bodyLarge,
            singleLine = true
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            TimePickerButton(
                label = "\u5f00\u59cb\u65f6\u95f4",
                value = startText,
                onClick = { pickingStartTime = true },
                modifier = Modifier.weight(1f),
            )
            TimePickerButton(
                label = "\u7ed3\u675f\u65f6\u95f4",
                value = endText,
                onClick = { pickingEndTime = true },
                modifier = Modifier.weight(1f),
            )
        }

        Text("\u6bcf\u5468\u542f\u7528", style = MaterialTheme.typography.titleLarge)
        WeekdaySelector(selectedDays = selectedDays, onSelectedDaysChange = { selectedDays = it })

        Text("\u63d0\u9192\u65b9\u5f0f", style = MaterialTheme.typography.titleLarge)
        AlertModeSelector(alertMode = alertMode, onAlertModeChange = { alertMode = it })

        Text("\u94c3\u58f0", style = MaterialTheme.typography.titleLarge)
        RingtoneSelector(
            selectedId = ringtoneId,
            onSelected = { ringtoneId = it },
            onPreview = onPreviewRingtone
        )

        errorText?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Button(
            onClick = {
                val parsed = buildReminder(
                    initialReminder = initialReminder,
                    title = title,
                    intervalText = intervalText,
                    startText = startText,
                    endText = endText,
                    selectedDays = selectedDays,
                    alertMode = alertMode,
                    ringtoneId = ringtoneId
                )
                if (parsed == null) {
                    errorText = "\u8bf7\u68c0\u67e5\u65f6\u95f4\u3001\u95f4\u9694\u548c\u661f\u671f\u8bbe\u7f6e"
                } else {
                    onSave(parsed)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
        ) {
            Text("\u4fdd\u5b58\u63d0\u9192")
        }
    }

    if (pickingStartTime) {
        TimePickerDialog(
            title = "\u9009\u62e9\u5f00\u59cb\u65f6\u95f4",
            initialMinuteOfDay = parseMinute(startText) ?: 8 * 60,
            onDismiss = { pickingStartTime = false },
            onConfirm = {
                startText = formatMinute(it)
                pickingStartTime = false
            }
        )
    }

    if (pickingEndTime) {
        TimePickerDialog(
            title = "\u9009\u62e9\u7ed3\u675f\u65f6\u95f4",
            initialMinuteOfDay = parseMinute(endText) ?: 22 * 60,
            onDismiss = { pickingEndTime = false },
            onConfirm = {
                endText = formatMinute(it)
                pickingEndTime = false
            }
        )
    }
}

@Composable
private fun WeekdaySelector(
    selectedDays: Set<DayOfWeek>,
    onSelectedDaysChange: (Set<DayOfWeek>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        DayOfWeek.entries.chunked(4).forEach { rowDays ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowDays.forEach { day ->
                    FilterChip(
                        selected = day in selectedDays,
                        onClick = {
                            val next = if (day in selectedDays) selectedDays - day else selectedDays + day
                            onSelectedDaysChange(next)
                        },
                        label = { Text(day.value.toString()) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF006A60),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun AlertModeSelector(
    alertMode: AlertMode,
    onAlertModeChange: (AlertMode) -> Unit
) {
    Column {
        AlertMode.entries.forEach { mode ->
            Row(modifier = Modifier.fillMaxWidth()) {
                RadioButton(selected = alertMode == mode, onClick = { onAlertModeChange(mode) })
                Text(
                    text = if (mode == AlertMode.Strong) "\u5f3a\u63d0\u9192" else "\u666e\u901a\u63d0\u9192",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun RingtoneSelector(
    selectedId: String,
    onSelected: (String) -> Unit,
    onPreview: (String) -> Unit
) {
    Column {
        RingtoneCatalog.all.forEach { option ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                RadioButton(selected = selectedId == option.id, onClick = { onSelected(option.id) })
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .weight(1f)
                        .padding(top = 12.dp)
                )
                OutlinedButton(onClick = { onPreview(option.id) }) {
                    Text("\u8bd5\u542c")
                }
            }
        }
    }
}

private fun buildReminder(
    initialReminder: Reminder?,
    title: String,
    intervalText: String,
    startText: String,
    endText: String,
    selectedDays: Set<DayOfWeek>,
    alertMode: AlertMode,
    ringtoneId: String
): Reminder? {
    val interval = intervalText.toIntOrNull()?.takeIf { it > 0 } ?: return null
    val start = parseMinute(startText) ?: return null
    val end = parseMinute(endText) ?: return null
    if (selectedDays.isEmpty() || start >= end) return null

    return if (initialReminder == null) {
        newReminder(
            title = title.ifBlank { "\u672a\u547d\u540d\u63d0\u9192" },
            intervalMinutes = interval,
            startMinuteOfDay = start,
            endMinuteOfDay = end,
            enabledDays = selectedDays,
            alertMode = alertMode,
            ringtoneId = ringtoneId
        )
    } else {
        initialReminder.copy(
            title = title.ifBlank { "\u672a\u547d\u540d\u63d0\u9192" },
            rule = ReminderRule(interval, start, end, selectedDays),
            alertMode = alertMode,
            ringtoneId = ringtoneId
        )
    }
}

private fun parseMinute(value: String): Int? {
    val parts = value.split(":")
    if (parts.size != 2) return null
    val hour = parts[0].toIntOrNull() ?: return null
    val minute = parts[1].toIntOrNull() ?: return null
    if (hour !in 0..23 || minute !in 0..59) return null
    return hour * 60 + minute
}

private fun formatMinute(minuteOfDay: Int): String =
    "%02d:%02d".format(minuteOfDay / 60, minuteOfDay % 60)

@Composable
private fun TimePickerButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 96.dp),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun TimePickerDialog(
    title: String,
    initialMinuteOfDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialMinuteOfDay / 60) }
    var selectedMinute by remember { mutableStateOf(initialMinuteOfDay % 60) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TimeColumn(
                    values = (0..23).toList(),
                    selected = selectedHour,
                    onSelected = { selectedHour = it }
                )
                TimeColumn(
                    values = (0..59).toList(),
                    selected = selectedMinute,
                    onSelected = { selectedMinute = it }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedHour * 60 + selectedMinute) }) {
                Text("\u786e\u5b9a")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("\u53d6\u6d88")
            }
        }
    )
}

@Composable
private fun TimeColumn(
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .height(220.dp)
            .width(104.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(values) { value ->
            val isSelected = value == selected
            if (isSelected) {
                Button(
                    onClick = { onSelected(value) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RectangleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("%02d".format(value))
                }
            } else {
                OutlinedButton(
                    onClick = { onSelected(value) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 56.dp),
                    shape = RectangleShape,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text("%02d".format(value))
                }
            }
        }
    }
}
