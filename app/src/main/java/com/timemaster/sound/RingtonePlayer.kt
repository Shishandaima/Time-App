package com.timemaster.sound

import android.content.Context
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class RingtonePlayer(
    context: Context
) {
    private val appContext = context.applicationContext
    private val audioManager: AudioManager? = appContext.getSystemService(AudioManager::class.java)
    private val notificationManager: NotificationManager? =
        appContext.getSystemService(NotificationManager::class.java)
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        appContext.getSystemService(VibratorManager::class.java)?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Vibrator::class.java)
    }
    private var player: MediaPlayer? = null
    private var ringing = false
    private val autoStopHandler = Handler(Looper.getMainLooper())
    private val autoStopRunnable = Runnable { stop() }
    private val volumeButtonInterrupter: VolumeButtonRingingInterrupter by lazy {
        VolumeButtonRingingInterrupter(appContext) { stop() }
    }

    @Synchronized
    fun playLooping(ringtoneId: String) {
        start(
            ringtoneId = ringtoneId,
            loop = true,
            playSound = shouldPlayAlertSound(
                audibleAllowed = shouldPlayStrongAlertSound(),
                silentModeEnabled = readSilentModeEnabled(appContext)
            ),
            vibrationEnabled = readVibrationEnabled(appContext),
            autoStopMillis = readRingDurationMode(appContext).durationMillis
        )
    }

    @Synchronized
    fun preview(ringtoneId: String) {
        start(ringtoneId = ringtoneId, loop = false, playSound = true)
    }

    @Synchronized
    fun isRinging(): Boolean = ringing

    @Synchronized
    fun stop() {
        autoStopHandler.removeCallbacks(autoStopRunnable)
        volumeButtonInterrupter.stop()
        ringing = false
        player?.release()
        player = null
        vibrator?.cancel()
    }

    private fun start(
        ringtoneId: String,
        loop: Boolean,
        playSound: Boolean,
        vibrationEnabled: Boolean = true,
        autoStopMillis: Long = DEFAULT_AUTO_STOP_MILLIS
    ) {
        stop()
        val shouldVibrate = shouldStartVibration(loop, vibrationEnabled)
        if (!playSound) {
            if (shouldVibrate) {
                beginInterruptibleRinging()
                startVibration()
                autoStopHandler.postDelayed(autoStopRunnable, autoStopMillis)
            }
            return
        }
        val option = RingtoneCatalog.byId(ringtoneId)
        val nextPlayer = MediaPlayer.create(appContext, option.rawRes) ?: return
        nextPlayer.isLooping = loop
        if (!loop) {
            nextPlayer.setOnCompletionListener { completedPlayer ->
                synchronized(this) {
                    if (player === completedPlayer) {
                        player = null
                    }
                    completedPlayer.release()
                }
            }
        }
        player = nextPlayer
        if (loop) {
            beginInterruptibleRinging()
        }
        nextPlayer.start()
        if (shouldVibrate) {
            startVibration()
        }
        if (loop) {
            autoStopHandler.postDelayed(autoStopRunnable, autoStopMillis)
        }
    }

    private fun beginInterruptibleRinging() {
        ringing = true
        volumeButtonInterrupter.start()
    }

    private fun shouldPlayStrongAlertSound(): Boolean =
        shouldPlayAudibleStrongAlert(
            ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL,
            interruptionFilter = notificationManager?.currentInterruptionFilter
                ?: NotificationManager.INTERRUPTION_FILTER_UNKNOWN
        )

    private fun startVibration() {
        val currentVibrator = vibrator ?: return
        if (!currentVibrator.hasVibrator()) return

        val pattern = longArrayOf(0L, 700L, 350L, 700L, 900L)
        val alarmAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val amplitudes = intArrayOf(0, 220, 0, 220, 0)
            currentVibrator.vibrate(VibrationEffect.createWaveform(pattern, amplitudes, 0), alarmAttributes)
        } else {
            @Suppress("DEPRECATION")
            currentVibrator.vibrate(pattern, 0, alarmAttributes)
        }
    }

    private companion object {
        const val DEFAULT_AUTO_STOP_MILLIS = 10_000L
    }
}
