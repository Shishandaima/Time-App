package com.timemaster.ui.alert

import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.timemaster.TimeMasterApplication
import com.timemaster.ui.theme.TimeMasterTheme

class AlertActivity : ComponentActivity() {
    private val app: TimeMasterApplication
        get() = application as TimeMasterApplication

    private var stopPlaybackOnDestroy = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        val reminderId = intent.getLongExtra(EXTRA_REMINDER_ID, -1L)
        val title = intent.getStringExtra(EXTRA_TITLE).orEmpty()
            .ifBlank { "\u65f6\u95f4\u63d0\u9192" }

        app.ringtonePlayer.stop()

        setContent {
            TimeMasterTheme {
                AlertScreen(
                    title = title,
                    onDismiss = {
                        app.ringtonePlayer.stop()
                        stopPlaybackOnDestroy = false
                        finish()
                    },
                    onSnooze = {
                        if (reminderId > 0L) {
                            app.alarmScheduler.schedule(
                                reminderId,
                                System.currentTimeMillis() + SNOOZE_MILLIS
                            )
                        }
                        app.ringtonePlayer.stop()
                        stopPlaybackOnDestroy = false
                        finish()
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        if (isFinishing && stopPlaybackOnDestroy) {
            app.ringtonePlayer.stop()
        }
        super.onDestroy()
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
    }

    companion object {
        const val EXTRA_REMINDER_ID = "reminder_id"
        const val EXTRA_TITLE = "title"
        const val EXTRA_RINGTONE_ID = "ringtone_id"
        private const val SNOOZE_MILLIS = 5 * 60 * 1000L
    }
}

@Composable
private fun AlertScreen(
    title: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "\u65f6\u95f4\u5230\u4e86",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(48.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("\u7a0d\u540e\u63d0\u9192")
                }
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("\u77e5\u9053\u4e86")
                }
            }
        }
    }
}
