package com.timemaster.ui

import android.Manifest
import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.timemaster.alarm.AlarmScheduler
import com.timemaster.data.ReminderRepository
import com.timemaster.domain.AlertMode
import com.timemaster.domain.Reminder
import com.timemaster.domain.ReminderRule
import com.timemaster.domain.nextTrigger
import com.timemaster.sound.RingDurationMode
import com.timemaster.permissions.canPostNotifications
import com.timemaster.permissions.canScheduleExactAlarms
import com.timemaster.permissions.openExactAlarmSettings
import com.timemaster.ui.editor.ReminderEditorScreen
import com.timemaster.ui.home.HomeFocusTarget
import com.timemaster.ui.home.HomeScreen
import com.timemaster.ui.settings.SettingsScreen
import com.timemaster.ui.theme.FontSizeMode
import com.timemaster.ui.theme.ThemeMode
import com.timemaster.update.DownloadProgress
import com.timemaster.update.GitHubReleaseClient
import com.timemaster.update.ReleaseInfo
import com.timemaster.update.UpdateCheckResult
import com.timemaster.update.canInstallDownloadedApk
import com.timemaster.update.installDownloadedApk
import com.timemaster.update.openInstallPermissionSettings
import com.timemaster.update.updateApkDestination
import java.io.File
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.ZoneId
import kotlinx.coroutines.launch

@Composable
fun TimeMasterApp(
    repository: ReminderRepository,
    alarmScheduler: AlarmScheduler,
    onPreviewRingtone: (String) -> Unit = {},
    themeMode: ThemeMode = ThemeMode.System,
    fontSizeMode: FontSizeMode = FontSizeMode.Standard,
    ringDurationMode: RingDurationMode = RingDurationMode.TenSeconds,
    vibrationEnabled: Boolean = true,
    silentModeEnabled: Boolean = false,
    onThemeModeChange: (ThemeMode) -> Unit = {},
    onFontSizeModeChange: (FontSizeMode) -> Unit = {},
    onRingDurationModeChange: (RingDurationMode) -> Unit = {},
    onVibrationEnabledChange: (Boolean) -> Unit = {},
    onSilentModeEnabledChange: (Boolean) -> Unit = {},
    appVersion: String = ""
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionPrefs = remember {
        context.getSharedPreferences("permission_prompts", Context.MODE_PRIVATE)
    }
    val reminders by repository.observeReminders().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    val releaseClient = remember { GitHubReleaseClient() }
    var editingReminder by remember { mutableStateOf<Reminder?>(null) }
    var showingEditor by remember { mutableStateOf(false) }
    var showingSettings by remember { mutableStateOf(false) }
    var homeFocusTarget by remember { mutableStateOf<HomeFocusTarget?>(null) }
    var updateDialog by remember { mutableStateOf<UpdateDialogState?>(null) }
    var downloadedUpdateApk by remember { mutableStateOf<File?>(null) }
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
                val apk = downloadedUpdateApk
                if (apk != null && canInstallDownloadedApk(context)) {
                    installDownloadedApk(context, apk)
                    downloadedUpdateApk = null
                }
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

    fun editorReturnFocusTarget(): HomeFocusTarget =
        editingReminder?.let { HomeFocusTarget.ReminderCard(it.id) }
            ?: HomeFocusTarget.AddReminderButton

    fun returnHomeFromEditor() {
        homeFocusTarget = editorReturnFocusTarget()
        showingEditor = false
        editingReminder = null
    }

    fun returnHomeFromSettings() {
        homeFocusTarget = HomeFocusTarget.SettingsButton
        showingSettings = false
    }

    if (showingEditor) {
        BackHandler {
            returnHomeFromEditor()
        }
        ReminderEditorScreen(
            initialReminder = editingReminder,
            onBack = {
                returnHomeFromEditor()
            },
            onSave = { reminder ->
                val returnFocusTarget = editorReturnFocusTarget()
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
                    homeFocusTarget = returnFocusTarget
                    showingEditor = false
                    editingReminder = null
                }
            },
            onPreviewRingtone = onPreviewRingtone
        )
    } else if (showingSettings) {
        BackHandler {
            returnHomeFromSettings()
        }
        SettingsScreen(
            themeMode = themeMode,
            fontSizeMode = fontSizeMode,
            ringDurationMode = ringDurationMode,
            vibrationEnabled = vibrationEnabled,
            silentModeEnabled = silentModeEnabled,
            appVersion = appVersion,
            onCheckUpdate = {
                updateDialog = UpdateDialogState.Checking
                scope.launch {
                    updateDialog = runCatching {
                        when (val result = releaseClient.checkForUpdate(appVersion)) {
                            UpdateCheckResult.Latest -> UpdateDialogState.Latest
                            is UpdateCheckResult.UpdateAvailable ->
                                UpdateDialogState.Available(result.release, appVersion)
                        }
                    }.getOrElse { error ->
                        UpdateDialogState.Error(error.userMessage())
                    }
                }
            },
            onThemeModeChange = onThemeModeChange,
            onFontSizeModeChange = onFontSizeModeChange,
            onRingDurationModeChange = onRingDurationModeChange,
            onVibrationEnabledChange = onVibrationEnabledChange,
            onSilentModeEnabledChange = onSilentModeEnabledChange,
            onBack = { returnHomeFromSettings() }
        )
    } else {
        HomeScreen(
            reminders = reminders,
            focusTarget = homeFocusTarget,
            onOpenSettings = { showingSettings = true },
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

    UpdateDialog(
        state = updateDialog,
        onDismiss = { updateDialog = null },
        onRetryCheck = {
            updateDialog = UpdateDialogState.Checking
            scope.launch {
                updateDialog = runCatching {
                    when (val result = releaseClient.checkForUpdate(appVersion)) {
                        UpdateCheckResult.Latest -> UpdateDialogState.Latest
                        is UpdateCheckResult.UpdateAvailable ->
                            UpdateDialogState.Available(result.release, appVersion)
                    }
                }.getOrElse { error ->
                    UpdateDialogState.Error(error.userMessage())
                }
            }
        },
        onInstall = { release ->
            startUpdateDownload(
                context = context,
                scope = scope,
                releaseClient = releaseClient,
                release = release,
                setState = { updateDialog = it },
                onDownloaded = { apk ->
                    if (canInstallDownloadedApk(context)) {
                        installDownloadedApk(context, apk)
                        updateDialog = null
                    } else {
                        downloadedUpdateApk = apk
                        updateDialog = UpdateDialogState.InstallPermissionRequired
                        openInstallPermissionSettings(context)
                    }
                }
            )
        },
        onRetryDownload = { release ->
            startUpdateDownload(
                context = context,
                scope = scope,
                releaseClient = releaseClient,
                release = release,
                setState = { updateDialog = it },
                onDownloaded = { apk ->
                    if (canInstallDownloadedApk(context)) {
                        installDownloadedApk(context, apk)
                        updateDialog = null
                    } else {
                        downloadedUpdateApk = apk
                        updateDialog = UpdateDialogState.InstallPermissionRequired
                        openInstallPermissionSettings(context)
                    }
                }
            )
        }
    )
}

private sealed interface UpdateDialogState {
    data object Checking : UpdateDialogState
    data object Latest : UpdateDialogState
    data class Available(val release: ReleaseInfo, val currentVersion: String) : UpdateDialogState
    data class Downloading(val release: ReleaseInfo, val progress: DownloadProgress) : UpdateDialogState
    data class DownloadFailed(val release: ReleaseInfo, val message: String) : UpdateDialogState
    data object InstallPermissionRequired : UpdateDialogState
    data class Error(val message: String) : UpdateDialogState
}

@Composable
private fun UpdateDialog(
    state: UpdateDialogState?,
    onDismiss: () -> Unit,
    onRetryCheck: () -> Unit,
    onInstall: (ReleaseInfo) -> Unit,
    onRetryDownload: (ReleaseInfo) -> Unit
) {
    when (state) {
        null -> Unit
        UpdateDialogState.Checking -> AlertDialog(
            onDismissRequest = {},
            title = { Text("\u68c0\u67e5\u66f4\u65b0") },
            text = { Text("\u6b63\u5728\u83b7\u53d6\u6700\u65b0\u7248\u672c\u4fe1\u606f\u2026") },
            confirmButton = {}
        )
        UpdateDialogState.Latest -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("\u68c0\u67e5\u66f4\u65b0") },
            text = { Text("\u5f53\u524d\u5df2\u662f\u6700\u65b0\u7248\u672c") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("\u786e\u5b9a") }
            }
        )
        is UpdateDialogState.Available -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("\u53d1\u73b0\u65b0\u7248\u672c") },
            text = { ReleaseInfoText(state.release, state.currentVersion) },
            confirmButton = {
                TextButton(onClick = { onInstall(state.release) }) { Text("\u7acb\u5373\u66f4\u65b0") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("\u53d6\u6d88") }
            }
        )
        is UpdateDialogState.Downloading -> AlertDialog(
            onDismissRequest = {},
            title = { Text("\u6b63\u5728\u4e0b\u8f7d") },
            text = { DownloadProgressText(state.progress) },
            confirmButton = {}
        )
        is UpdateDialogState.DownloadFailed -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("\u4e0b\u8f7d\u5931\u8d25") },
            text = { Text(state.message) },
            confirmButton = {
                TextButton(onClick = { onRetryDownload(state.release) }) { Text("\u91cd\u8bd5") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("\u53d6\u6d88") }
            }
        )
        UpdateDialogState.InstallPermissionRequired -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("\u9700\u8981\u5b89\u88c5\u6743\u9650") },
            text = { Text("\u8bf7\u5141\u8bb8\u5b89\u88c5\u672a\u77e5\u6765\u6e90\u5e94\u7528\uff0c\u6388\u6743\u540e\u5c06\u7ee7\u7eed\u5b89\u88c5\u3002") },
            confirmButton = {
                TextButton(onClick = onDismiss) { Text("\u786e\u5b9a") }
            }
        )
        is UpdateDialogState.Error -> AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("\u68c0\u67e5\u5931\u8d25") },
            text = { Text(state.message) },
            confirmButton = {
                TextButton(onClick = onRetryCheck) { Text("\u91cd\u8bd5") }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("\u53d6\u6d88") }
            }
        )
    }
}

@Composable
private fun ReleaseInfoText(release: ReleaseInfo, currentVersion: String) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
    ) {
        Text("\u6700\u65b0\u7248\u672c\u53f7\uff1a${release.version}")
        Text("\u53d1\u5e03\u65f6\u95f4\uff1a${release.publishedAt.ifBlank { "\u672a\u63d0\u4f9b" }}")
        Text("\u5f53\u524d\u7248\u672c\u53f7\uff1a$currentVersion")
        Text(
            text = "\u66f4\u65b0\u65e5\u5fd7\uff1a\n${release.releaseNotes.ifBlank { "\u672a\u63d0\u4f9b" }}",
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun DownloadProgressText(progress: DownloadProgress) {
    Column {
        LinearProgressIndicator(progress = { progress.percent / 100f })
        Text("${progress.percent}%")
        Text(
            "\u5df2\u4e0b\u8f7d\uff1a${formatBytes(progress.downloadedBytes)} / " +
                "\u603b\u5927\u5c0f\uff1a${formatBytes(progress.totalBytes)}"
        )
    }
}

private fun startUpdateDownload(
    context: Context,
    scope: kotlinx.coroutines.CoroutineScope,
    releaseClient: GitHubReleaseClient,
    release: ReleaseInfo,
    setState: (UpdateDialogState) -> Unit,
    onDownloaded: (File) -> Unit
) {
    val apkAsset = release.apkAsset ?: run {
        setState(UpdateDialogState.DownloadFailed(release, "\u6700\u65b0 Release \u4e2d\u672a\u627e\u5230 APK \u6587\u4ef6"))
        return
    }
    setState(UpdateDialogState.Downloading(release, DownloadProgress(0L, apkAsset.sizeBytes)))
    scope.launch {
        runCatching {
            releaseClient.downloadApk(
                asset = apkAsset,
                destination = updateApkDestination(context)
            ) { progress ->
                setState(UpdateDialogState.Downloading(release, progress))
            }
        }.onSuccess(onDownloaded)
            .onFailure { error ->
                setState(UpdateDialogState.DownloadFailed(release, error.userMessage()))
            }
    }
}

private fun Throwable.userMessage(): String =
    message?.takeIf { it.isNotBlank() } ?: "\u7f51\u7edc\u6216\u6587\u4ef6\u5904\u7406\u5931\u8d25"

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "\u672a\u77e5"
    val units = listOf("B", "KB", "MB", "GB")
    var value = bytes.toDouble()
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    return if (unitIndex == 0) {
        "${bytes}${units[unitIndex]}"
    } else {
        String.format("%.1f%s", value, units[unitIndex])
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
