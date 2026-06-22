package com.timemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.timemaster.ui.TimeMasterApp
import com.timemaster.ui.theme.ThemeMode
import com.timemaster.ui.theme.TimeMasterTheme

class MainActivity : ComponentActivity() {
    private val app: TimeMasterApplication
        get() = application as TimeMasterApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val settings = getSharedPreferences("app_settings", MODE_PRIVATE)
        setContent {
            var themeMode by rememberSaveable {
                mutableStateOf(
                    runCatching {
                        ThemeMode.valueOf(settings.getString("theme_mode", ThemeMode.System.name)!!)
                    }.getOrDefault(ThemeMode.System)
                )
            }
            TimeMasterTheme(themeMode = themeMode) {
                TimeMasterApp(
                    repository = app.reminderRepository,
                    alarmScheduler = app.alarmScheduler,
                    onPreviewRingtone = app.ringtonePlayer::preview,
                    themeMode = themeMode,
                    onThemeModeChange = { nextMode ->
                        themeMode = nextMode
                        settings.edit().putString("theme_mode", nextMode.name).apply()
                    },
                    appVersion = BuildConfig.VERSION_NAME
                )
            }
        }
    }
}
