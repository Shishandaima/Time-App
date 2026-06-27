package com.timemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.timemaster.sound.readRingDurationMode
import com.timemaster.sound.readVibrationEnabled
import com.timemaster.sound.saveRingDurationMode
import com.timemaster.sound.saveVibrationEnabled
import com.timemaster.ui.TimeMasterApp
import com.timemaster.ui.theme.readFontSizeMode
import com.timemaster.ui.theme.readThemeMode
import com.timemaster.ui.theme.saveFontSizeMode
import com.timemaster.ui.theme.saveThemeMode
import com.timemaster.ui.theme.TimeMasterTheme

class MainActivity : ComponentActivity() {
    private val app: TimeMasterApplication
        get() = application as TimeMasterApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var themeMode by rememberSaveable {
                mutableStateOf(readThemeMode(this))
            }
            var fontSizeMode by rememberSaveable {
                mutableStateOf(readFontSizeMode(this))
            }
            var ringDurationMode by rememberSaveable {
                mutableStateOf(readRingDurationMode(this))
            }
            var vibrationEnabled by rememberSaveable {
                mutableStateOf(readVibrationEnabled(this))
            }
            TimeMasterTheme(
                themeMode = themeMode,
                fontSizeMode = fontSizeMode
            ) {
                TimeMasterApp(
                    repository = app.reminderRepository,
                    alarmScheduler = app.alarmScheduler,
                    onPreviewRingtone = app.ringtonePlayer::preview,
                    themeMode = themeMode,
                    fontSizeMode = fontSizeMode,
                    ringDurationMode = ringDurationMode,
                    vibrationEnabled = vibrationEnabled,
                    onThemeModeChange = { nextMode ->
                        themeMode = nextMode
                        saveThemeMode(this, nextMode)
                    },
                    onFontSizeModeChange = { nextMode ->
                        fontSizeMode = nextMode
                        saveFontSizeMode(this, nextMode)
                    },
                    onRingDurationModeChange = { nextMode ->
                        ringDurationMode = nextMode
                        saveRingDurationMode(this, nextMode)
                    },
                    onVibrationEnabledChange = { enabled ->
                        vibrationEnabled = enabled
                        saveVibrationEnabled(this, enabled)
                    },
                    appVersion = BuildConfig.VERSION_NAME
                )
            }
        }
    }
}
