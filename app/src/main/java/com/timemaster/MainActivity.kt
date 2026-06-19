package com.timemaster

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.timemaster.ui.TimeMasterApp
import com.timemaster.ui.theme.TimeMasterTheme

class MainActivity : ComponentActivity() {
    private val app: TimeMasterApplication
        get() = application as TimeMasterApplication

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimeMasterTheme {
                TimeMasterApp(
                    repository = app.reminderRepository,
                    alarmScheduler = app.alarmScheduler,
                    onPreviewRingtone = app.ringtonePlayer::preview
                )
            }
        }
    }
}
