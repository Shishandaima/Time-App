package com.timemaster.sound

import android.content.Context
import android.database.ContentObserver
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings

data class VolumeSnapshot(
    val alarmVolume: Int,
    val musicVolume: Int
)

fun shouldInterruptForVolumeChange(previous: VolumeSnapshot?, current: VolumeSnapshot): Boolean =
    previous != null && previous != current

class VolumeButtonRingingInterrupter(
    context: Context,
    private val onVolumeButtonPressed: () -> Unit
) {
    private val appContext = context.applicationContext
    private val audioManager: AudioManager? = appContext.getSystemService(AudioManager::class.java)
    private val handler = Handler(Looper.getMainLooper())
    private var lastSnapshot: VolumeSnapshot? = null
    private var registered = false

    private val observer = object : ContentObserver(handler) {
        override fun onChange(selfChange: Boolean) {
            handleVolumeChange()
        }
    }

    @Synchronized
    fun start() {
        lastSnapshot = readSnapshot()
        if (registered) return

        appContext.contentResolver.registerContentObserver(
            Settings.System.CONTENT_URI,
            true,
            observer
        )
        registered = true
    }

    @Synchronized
    fun stop() {
        if (!registered) return

        appContext.contentResolver.unregisterContentObserver(observer)
        registered = false
        lastSnapshot = null
    }

    @Synchronized
    private fun handleVolumeChange() {
        val currentSnapshot = readSnapshot()
        val shouldInterrupt = shouldInterruptForVolumeChange(lastSnapshot, currentSnapshot)
        lastSnapshot = currentSnapshot
        if (shouldInterrupt) {
            onVolumeButtonPressed()
        }
    }

    private fun readSnapshot(): VolumeSnapshot =
        VolumeSnapshot(
            alarmVolume = audioManager?.getStreamVolume(AudioManager.STREAM_ALARM) ?: 0,
            musicVolume = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 0
        )
}
