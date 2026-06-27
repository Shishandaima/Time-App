package com.timemaster.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.timemaster.sound.RingDurationMode
import com.timemaster.ui.accessibility.pageEntryTitleFocus
import com.timemaster.ui.layout.pageContentPadding
import com.timemaster.ui.theme.FontSizeMode
import com.timemaster.ui.theme.ThemeMode

internal data class GeneralSettingUiItem(
    val label: String,
    val value: String?
)

internal fun generalSettingsUiItems(
    themeMode: ThemeMode,
    fontSizeMode: FontSizeMode,
    ringDurationMode: RingDurationMode
) = listOf(
    GeneralSettingUiItem("\u4e3b\u9898", themeMode.label),
    GeneralSettingUiItem("\u5b57\u4f53\u5927\u5c0f", fontSizeMode.label),
    GeneralSettingUiItem("\u54cd\u94c3\u65f6\u957f", ringDurationMode.label),
    GeneralSettingUiItem("\u9707\u52a8", null),
    GeneralSettingUiItem("\u9759\u97f3", null)
)

@Composable
fun SettingsScreen(
    themeMode: ThemeMode,
    fontSizeMode: FontSizeMode,
    ringDurationMode: RingDurationMode,
    vibrationEnabled: Boolean,
    silentModeEnabled: Boolean,
    appVersion: String,
    onCheckUpdate: () -> Unit,
    onThemeModeChange: (ThemeMode) -> Unit,
    onFontSizeModeChange: (FontSizeMode) -> Unit,
    onRingDurationModeChange: (RingDurationMode) -> Unit,
    onVibrationEnabledChange: (Boolean) -> Unit,
    onSilentModeEnabledChange: (Boolean) -> Unit,
    onBack: () -> Unit
) {
    var showThemeDialog by remember { mutableStateOf(false) }
    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showRingDurationDialog by remember { mutableStateOf(false) }
    val generalItems = generalSettingsUiItems(
        themeMode = themeMode,
        fontSizeMode = fontSizeMode,
        ringDurationMode = ringDurationMode
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
            .pageContentPadding(),
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
                text = "\u8bbe\u7f6e",
                style = MaterialTheme.typography.headlineLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 64.dp)
                    .pageEntryTitleFocus()
            )
        }

        SettingsSection(title = "\u901a\u7528") {
            SettingsValueRow(
                label = generalItems[0].label,
                value = generalItems[0].value,
                onClick = { showThemeDialog = true }
            )
            SettingsValueRow(
                label = generalItems[1].label,
                value = generalItems[1].value,
                onClick = { showFontSizeDialog = true }
            )
            SettingsValueRow(
                label = generalItems[2].label,
                value = generalItems[2].value,
                onClick = { showRingDurationDialog = true }
            )
            SettingsSwitchRow(
                label = generalItems[3].label,
                checked = vibrationEnabled,
                onCheckedChange = onVibrationEnabledChange
            )
            SettingsSwitchRow(
                label = generalItems[4].label,
                checked = silentModeEnabled,
                onCheckedChange = onSilentModeEnabledChange
            )
        }

        SettingsSection(title = "\u5173\u4e8e") {
            SettingsValueRow(
                label = "\u68c0\u67e5\u66f4\u65b0",
                onClick = onCheckUpdate
            )
            SettingsValueRow(label = "APP\u7248\u672c\u53f7", value = appVersion)
        }
    }

    if (showThemeDialog) {
        SettingsChoiceDialog(
            title = "\u9009\u62e9\u4e3b\u9898",
            options = ThemeMode.entries,
            selectedOption = themeMode,
            optionLabel = ThemeMode::label,
            onSelect = { mode ->
                onThemeModeChange(mode)
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }

    if (showFontSizeDialog) {
        SettingsChoiceDialog(
            title = "\u9009\u62e9\u5b57\u4f53\u5927\u5c0f",
            options = FontSizeMode.entries,
            selectedOption = fontSizeMode,
            optionLabel = FontSizeMode::label,
            onSelect = { mode ->
                onFontSizeModeChange(mode)
                showFontSizeDialog = false
            },
            onDismiss = { showFontSizeDialog = false }
        )
    }

    if (showRingDurationDialog) {
        SettingsChoiceDialog(
            title = "\u9009\u62e9\u54cd\u94c3\u65f6\u957f",
            options = RingDurationMode.entries,
            selectedOption = ringDurationMode,
            optionLabel = RingDurationMode::label,
            onSelect = { mode ->
                onRingDurationModeChange(mode)
                showRingDurationDialog = false
            },
            onDismiss = { showRingDurationDialog = false }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                role = Role.Switch,
                onValueChange = onCheckedChange
            )
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = null
        )
    }
}

@Composable
private fun SettingsValueRow(
    label: String,
    value: String? = null,
    onClick: (() -> Unit)? = null
) {
    val rowModifier = if (onClick == null) {
        Modifier.fillMaxWidth()
    } else {
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    }
    Row(
        modifier = rowModifier
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val textClickModifier = if (onClick == null) {
            Modifier
        } else {
            Modifier.clickable(onClick = onClick)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = textClickModifier
        )
        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.End,
                modifier = textClickModifier
            )
        }
    }
}

@Composable
private fun <T> SettingsChoiceDialog(
    title: String,
    options: List<T>,
    selectedOption: T,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                options.forEach { option ->
                    SettingsChoiceOptionRow(
                        label = optionLabel(option),
                        selected = selectedOption == option,
                        option = option,
                        onSelect = onSelect
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("\u53d6\u6d88")
            }
        }
    )
}

@Composable
private fun <T> SettingsChoiceOptionRow(
    label: String,
    selected: Boolean,
    option: T,
    onSelect: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(option) }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = { onSelect(option) }
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .padding(start = 8.dp)
                .clickable { onSelect(option) }
        )
    }
}
