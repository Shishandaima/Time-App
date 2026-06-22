package com.timemaster.sound

import android.app.NotificationManager
import android.media.AudioManager

fun shouldPlayAudibleStrongAlert(
    ringerMode: Int,
    interruptionFilter: Int
): Boolean =
    ringerMode == AudioManager.RINGER_MODE_NORMAL &&
        (
            interruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALL ||
                interruptionFilter == NotificationManager.INTERRUPTION_FILTER_UNKNOWN
            )
