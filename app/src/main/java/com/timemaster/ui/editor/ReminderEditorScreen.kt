package com.timemaster.ui.editor

import android.media.AudioManager
import android.media.ToneGenerator
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.ScrollAxisRange
import androidx.compose.ui.semantics.scrollBy
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.setProgress
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.verticalScrollAxisRange
import kotlin.math.roundToInt
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.sound.RingtoneCatalog
import com.timemaster.ui.accessibility.pageEntryTitleFocus
import java.time.DayOfWeek
import kotlin.math.abs
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun ReminderEditorScreen(
    initialReminder: Reminder?,
    onBack: () -> Unit,
    onSave: (Reminder) -> Unit,
    onPreviewRingtone: (String) -> Unit = {}
) {
    var title by remember { mutableStateOf(initialReminder?.title.orEmpty()) }
    var intervalSeconds by remember {
        mutableStateOf(initialReminder?.rule?.intervalSeconds ?: 30 * 60)
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
    var pickingInterval by remember { mutableStateOf(false) }
    var pickingStartTime by remember { mutableStateOf(false) }
    var pickingEndTime by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = (-20).dp)
                    .size(56.dp)
                    .semantics {
                        contentDescription = "\u8fd4\u56de"
                    }
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "\u2039",
                        style = MaterialTheme.typography.displaySmall.copy(lineHeight = 44.sp),
                        modifier = Modifier.offset(y = (-1).dp)
                    )
                }
            }
            Text(
                text = if (initialReminder == null) "\u65b0\u5efa\u5468\u671f\u63d0\u9192" else "\u7f16\u8f91\u63d0\u9192",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 64.dp)
                    .pageEntryTitleFocus()
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
        TimePickerButton(
            label = "\u6bcf\u9694\u591a\u5c11\u65f6\u95f4",
            value = formatDuration(intervalSeconds),
            accessibilityValue = formatDurationForTalkBack(intervalSeconds),
            onClick = { pickingInterval = true },
            modifier = Modifier.fillMaxWidth()
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
                    intervalSeconds = intervalSeconds,
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
                .height(72.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("\u4fdd\u5b58\u63d0\u9192")
        }
    }

    if (pickingInterval) {
        DurationPickerDialog(
            title = "\u9009\u62e9\u95f4\u9694\u65f6\u95f4",
            initialSeconds = intervalSeconds,
            onDismiss = { pickingInterval = false },
            onConfirm = {
                intervalSeconds = it
                pickingInterval = false
            }
        )
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = alertMode == mode, onClick = { onAlertModeChange(mode) })
                Text(
                    text = if (mode == AlertMode.Strong) "\u5f3a\u63d0\u9192" else "\u666e\u901a\u63d0\u9192",
                    style = MaterialTheme.typography.bodyLarge
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
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = selectedId == option.id, onClick = { onSelected(option.id) })
                Text(
                    text = option.label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                OutlinedButton(
                    onClick = { onPreview(option.id) },
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text("\u8bd5\u542c")
                }
            }
        }
    }
}

private fun buildReminder(
    initialReminder: Reminder?,
    title: String,
    intervalSeconds: Int,
    startText: String,
    endText: String,
    selectedDays: Set<DayOfWeek>,
    alertMode: AlertMode,
    ringtoneId: String
): Reminder? {
    val interval = intervalSeconds.takeIf { it > 0 } ?: return null
    val start = parseMinute(startText) ?: return null
    val end = parseMinute(endText) ?: return null
    if (selectedDays.isEmpty() || start >= end) return null

    return if (initialReminder == null) {
        createNewReminder(
            title = title.ifBlank { "\u672a\u547d\u540d\u63d0\u9192" },
            intervalSeconds = interval,
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

private fun createNewReminder(
    title: String,
    intervalSeconds: Int,
    startMinuteOfDay: Int,
    endMinuteOfDay: Int,
    enabledDays: Set<DayOfWeek>,
    alertMode: AlertMode,
    ringtoneId: String
): Reminder = Reminder(
    id = 0,
    title = title,
    rule = ReminderRule(
        intervalSeconds = intervalSeconds,
        startMinuteOfDay = startMinuteOfDay,
        endMinuteOfDay = endMinuteOfDay,
        enabledDays = enabledDays
    ),
    alertMode = alertMode,
    ringtoneId = ringtoneId,
    isEnabled = true,
    nextTriggerAtMillis = null
)

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

private fun formatDuration(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d:%02d".format(hours, minutes, seconds)
}

private fun formatDurationForTalkBack(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val parts = buildList {
        if (hours > 0) add("${hours}\u5c0f\u65f6")
        if (minutes > 0) {
            val unit = if (hours == 0 && seconds == 0) "\u5206\u949f" else "\u5206"
            add("${minutes}$unit")
        }
        if (seconds > 0) add("${seconds}\u79d2")
    }
    return parts.takeIf { it.isNotEmpty() }?.joinToString(separator = "") ?: "0\u79d2"
}

@Composable
private fun TimePickerButton(
    label: String,
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accessibilityValue: String? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .heightIn(min = 96.dp)
            .then(
                if (accessibilityValue == null) {
                    Modifier
                } else {
                    Modifier.semantics {
                        contentDescription = "$label\uff0c$accessibilityValue"
                    }
                }
            ),
        shape = RectangleShape,
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (accessibilityValue == null) {
                        Modifier
                    } else {
                        Modifier.clearAndSetSemantics { }
                    }
                ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text(value, style = MaterialTheme.typography.headlineMedium)
        }
    }
}

@Composable
private fun DurationPickerDialog(
    title: String,
    initialSeconds: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf((initialSeconds / 3600).coerceIn(0, 23)) }
    var selectedMinute by remember { mutableStateOf(((initialSeconds % 3600) / 60).coerceIn(0, 59)) }
    var selectedSecond by remember { mutableStateOf((initialSeconds % 60).coerceIn(0, 59)) }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_SYSTEM, 35) }

    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }

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
                    onSelected = { selectedHour = it },
                    toneGenerator = toneGenerator,
                    columnWidth = 82.dp,
                    accessibilityUnit = "\u5c0f\u65f6",
                    selectionDescription = durationSelectionDescription(
                        selectedHour,
                        selectedMinute,
                        selectedSecond
                    )
                )
                TimeColumn(
                    values = (0..59).toList(),
                    selected = selectedMinute,
                    onSelected = { selectedMinute = it },
                    toneGenerator = toneGenerator,
                    columnWidth = 82.dp,
                    accessibilityUnit = "\u5206",
                    selectionDescription = durationSelectionDescription(
                        selectedHour,
                        selectedMinute,
                        selectedSecond
                    )
                )
                TimeColumn(
                    values = (0..59).toList(),
                    selected = selectedSecond,
                    onSelected = { selectedSecond = it },
                    toneGenerator = toneGenerator,
                    columnWidth = 82.dp,
                    accessibilityUnit = "\u79d2",
                    selectionDescription = durationSelectionDescription(
                        selectedHour,
                        selectedMinute,
                        selectedSecond
                    )
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val totalSeconds = selectedHour * 3600 + selectedMinute * 60 + selectedSecond
                    onConfirm(totalSeconds.coerceAtLeast(1))
                }
            ) {
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
private fun TimePickerDialog(
    title: String,
    initialMinuteOfDay: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var selectedHour by remember { mutableStateOf(initialMinuteOfDay / 60) }
    var selectedMinute by remember { mutableStateOf(initialMinuteOfDay % 60) }
    val toneGenerator = remember { ToneGenerator(AudioManager.STREAM_SYSTEM, 35) }

    DisposableEffect(Unit) {
        onDispose { toneGenerator.release() }
    }

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
                    onSelected = { selectedHour = it },
                    toneGenerator = toneGenerator,
                    accessibilityUnit = "\u5c0f\u65f6",
                    selectionDescription = timeSelectionDescription(selectedHour, selectedMinute)
                )
                TimeColumn(
                    values = (0..59).toList(),
                    selected = selectedMinute,
                    onSelected = { selectedMinute = it },
                    toneGenerator = toneGenerator,
                    accessibilityUnit = "\u5206",
                    selectionDescription = timeSelectionDescription(selectedHour, selectedMinute)
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
    onSelected: (Int) -> Unit,
    toneGenerator: ToneGenerator,
    columnWidth: Dp = 104.dp,
    accessibilityUnit: String,
    selectionDescription: String
) {
    val itemHeight = 56.dp
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = values.indexOf(selected).coerceAtLeast(0))
    val hapticFeedback = LocalHapticFeedback.current
    val flingBehavior = rememberSnapFlingBehavior(lazyListState = listState)
    val halfItemHeightPx = with(LocalDensity.current) { (itemHeight / 2).roundToPx() }

    LaunchedEffect(selected, values, listState.isScrollInProgress) {
        val selectedIndex = values.indexOf(selected)
        if (
            selectedIndex >= 0 &&
            !listState.isScrollInProgress &&
            (listState.firstVisibleItemIndex != selectedIndex || listState.firstVisibleItemScrollOffset != 0)
        ) {
            listState.scrollToItem(selectedIndex)
        }
    }

    LaunchedEffect(listState, values) {
        var lastSelected = selected
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val center = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2
            val centeredItem = layoutInfo.visibleItemsInfo.minByOrNull { item ->
                abs(item.offset + item.size / 2 - center)
            }
            val index = centeredItem?.index
                ?: (listState.firstVisibleItemIndex +
                    if (listState.firstVisibleItemScrollOffset > halfItemHeightPx) 1 else 0)
            index.coerceIn(values.indices)
        }
            .distinctUntilChanged()
            .collect { index ->
                val value = values[index]
                if (value != lastSelected) {
                    lastSelected = value
                    onSelected(value)
                    performPickerFeedback(hapticFeedback, toneGenerator)
                }
            }
    }

    Box(
        modifier = Modifier
            .height(216.dp)
            .width(columnWidth)
            .semantics {
                contentDescription = "${selected}${accessibilityUnit}"
                stateDescription = selectionDescription
                verticalScrollAxisRange = ScrollAxisRange(
                    value = { selected.toFloat() },
                    maxValue = { values.last().toFloat() },
                    reverseScrolling = false
                )
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = selected.toFloat(),
                    range = values.first().toFloat()..values.last().toFloat(),
                    steps = (values.size - 2).coerceAtLeast(0)
                )
                setProgress { targetValue ->
                    val nextValue = targetValue
                        .roundToInt()
                        .coerceIn(values.first(), values.last())
                    updateTimeColumnSelection(
                        nextValue = nextValue,
                        selected = selected,
                        onSelected = onSelected,
                        hapticFeedback = hapticFeedback,
                        toneGenerator = toneGenerator
                    )
                    true
                }
                scrollBy { _, y ->
                    val direction = when {
                        y > 0f -> 1
                        y < 0f -> -1
                        else -> 0
                    }
                    val currentIndex = values.indexOf(selected)
                    val nextValue = values
                        .getOrNull((currentIndex + direction).coerceIn(values.indices))
                        ?: selected
                    updateTimeColumnSelection(
                        nextValue = nextValue,
                        selected = selected,
                        onSelected = onSelected,
                        hapticFeedback = hapticFeedback,
                        toneGenerator = toneGenerator
                    )
                    true
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
        )
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            modifier = Modifier
                .fillMaxSize()
                .clearAndSetSemantics { },
            contentPadding = PaddingValues(vertical = 80.dp)
        ) {
            items(values) { value ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "%02d".format(value),
                        style = if (value == selected) {
                            MaterialTheme.typography.headlineMedium
                        } else {
                            MaterialTheme.typography.titleLarge
                        },
                        color = if (value == selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
        }
    }
}

private fun durationSelectionDescription(hours: Int, minutes: Int, seconds: Int): String =
    "${hours}\u5c0f\u65f6${minutes}\u5206${seconds}\u79d2"

private fun timeSelectionDescription(hours: Int, minutes: Int): String =
    "${hours}\u70b9${minutes}\u5206"

private fun updateTimeColumnSelection(
    nextValue: Int,
    selected: Int,
    onSelected: (Int) -> Unit,
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback,
    toneGenerator: ToneGenerator
) {
    if (nextValue != selected) {
        onSelected(nextValue)
        performPickerFeedback(hapticFeedback, toneGenerator)
    }
}

private fun performPickerFeedback(
    hapticFeedback: androidx.compose.ui.hapticfeedback.HapticFeedback,
    toneGenerator: ToneGenerator
) {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
    toneGenerator.startTone(ToneGenerator.TONE_PROP_BEEP, 25)
}
