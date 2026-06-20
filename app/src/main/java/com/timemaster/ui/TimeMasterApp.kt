package com.timemaster.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.timemaster.alarm.AlarmScheduler
import com.timemaster.data.ReminderRepository
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.domain.nextTrigger
import com.timemaster.permissions.canPostNotifications
import com.timemaster.permissions.canScheduleExactAlarms
import com.timemaster.permissions.openExactAlarmSettings
import com.timemaster.ui.editor.ReminderEditorScreen
import com.timemaster.ui.home.HomeScreen
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.launch

@Composable
fun TimeMasterApp(
    repository: ReminderRepository,
    alarmScheduler: AlarmScheduler,
    onPreviewRingtone: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionPrefs = remember {
        context.getSharedPreferences("permission_prompts", Context.MODE_PRIVATE)
    }
    val reminders by repository.observeReminders().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var showingEditor by remember { mutableStateOf(false) }
    var permissionRefresh by remember { mutableStateOf(0) }
    var requestedInitialNotificationPermission by rememberSaveable {
        mutableStateOf(permissionPrefs.getBoolean("initial_notification_requested", false))
    }
    var requestedInitialExactAlarmPermission by rememberSaveable {
        mutableStateOf(permissionPrefs.getBoolean("initial_exact_alarm_requested", false))
    }
    var requestExactAfterNotification by rememberSaveable { mutableStateOf(false) }
    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        permissionRefresh++
        requestExactAfterNotification = true
    }
    val canNotify = remember(permissionRefresh) { canPostNotifications(context) }
    val canScheduleExact = remember(permissionRefresh) { canScheduleExactAlarms(context) }
    val permissionWarnings = buildList {
        if (!canNotify) add("\u9700\u8981\u901a\u77e5\u6743\u9650\uff0c\u5426\u5219\u63d0\u9192\u53ef\u80fd\u65e0\u6cd5\u663e\u793a\u3002")
        if (!canScheduleExact) add("\u9700\u8981\u201c\u95f9\u949f\u548c\u63d0\u9192\u201d\u6743\u9650\uff0c\u5426\u5219\u4e0d\u80fd\u51c6\u65f6\u54cd\u94c3\u3002")
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionRefresh++
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(Unit) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !canPostNotifications(context) &&
            !requestedInitialNotificationPermission
        ) {
            requestedInitialNotificationPermission = true
            permissionPrefs.edit().putBoolean("initial_notification_requested", true).apply()
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            requestExactAfterNotification = true
        }
    }

    LaunchedEffect(permissionRefresh, requestExactAfterNotification) {
        if (
            requestExactAfterNotification &&
            !canScheduleExactAlarms(context) &&
            !requestedInitialExactAlarmPermission
        ) {
            requestedInitialExactAlarmPermission = true
            permissionPrefs.edit().putBoolean("initial_exact_alarm_requested", true).apply()
            requestExactAfterNotification = false
            openExactAlarmSettings(context)
        }
    }

    if (showingEditor) {
        BackHandler {
            showingEditor = false
            editingReminder = null
        }
        ReminderEditorScreen(
            initialReminder = editingReminder,
            onBack = {
                showingEditor = false
                editingReminder = null
            },
            onSave = { reminder ->
                scope.launch {
                    val savedId = repository.saveReminder(reminder)
                    val savedReminder = reminder.copy(id = savedId)
                    scheduleIfEnabled(
                        reminder = savedReminder,
                        repository = repository,
                        alarmScheduler = alarmScheduler,
                        onMissingExactAlarmPermission = { openExactAlarmSettings(context) }
                    )
                    if (
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        !canPostNotifications(context)
                    ) {
                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
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
                    val updated = reminder.copy(isEnabled = enabled)
                    repository.saveReminder(updated)
                    if (enabled) {
                        scheduleIfEnabled(
                            reminder = updated,
                            repository = repository,
                            alarmScheduler = alarmScheduler,
                            onMissingExactAlarmPermission = { openExactAlarmSettings(context) }
                        )
                    } else {
                        alarmScheduler.cancel(reminder.id)
                        repository.updateNextTrigger(reminder.id, null)
                    }
                }
            },
            onDeleteReminder = { reminder ->
                scope.launch {
                    repository.deleteReminder(reminder.id)
                }
            },
            permissionWarnings = permissionWarnings
        )
    }
}

private suspend fun scheduleIfEnabled(
    reminder: Reminder,
    repository: ReminderRepository,
    alarmScheduler: AlarmScheduler,
    onMissingExactAlarmPermission: () -> Unit
) {
    if (!reminder.isEnabled) return

    val nextTriggerAtMillis = nextTrigger(LocalDateTime.now(), reminder.rule)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    if (alarmScheduler.schedule(reminder.id, nextTriggerAtMillis)) {
        repository.updateNextTrigger(reminder.id, nextTriggerAtMillis)
    } else {
        repository.updateNextTrigger(reminder.id, null)
        onMissingExactAlarmPermission()
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
