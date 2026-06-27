package com.timemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.timemaster.ui.TimeMasterApp
import com.timemaster.ui.theme.readThemeMode
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
            TimeMasterTheme(themeMode = themeMode) {
                TimeMasterApp(
                    repository = app.reminderRepository,
                    alarmScheduler = app.alarmScheduler,
                    onPreviewRingtone = app.ringtonePlayer::preview,
                    themeMode = themeMode,
                    onThemeModeChange = { nextMode ->
                        themeMode = nextMode
                        saveThemeMode(this, nextMode)
                    },
                    appVersion = BuildConfig.VERSION_NAME,
                    onDueReminder = app.reminderDueHandler::handleDueReminder
                )
            }
        }
    }
}
