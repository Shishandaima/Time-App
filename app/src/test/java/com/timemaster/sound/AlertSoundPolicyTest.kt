package com.timemaster.sound

import android.app.NotificationManager
import android.media.AudioManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AlertSoundPolicyTest {
    @Test
    fun allowsSoundOnlyWhenRingerIsNormalAndDoNotDisturbIsOff() {
        assertTrue(
            shouldPlayAudibleStrongAlert(
                ringerMode = AudioManager.RINGER_MODE_NORMAL,
                interruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL
            )
        )

        assertFalse(
            shouldPlayAudibleStrongAlert(
                ringerMode = AudioManager.RINGER_MODE_SILENT,
                interruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL
            )
        )
        assertFalse(
            shouldPlayAudibleStrongAlert(
                ringerMode = AudioManager.RINGER_MODE_VIBRATE,
                interruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALL
            )
        )
        assertFalse(
            shouldPlayAudibleStrongAlert(
                ringerMode = AudioManager.RINGER_MODE_NORMAL,
                interruptionFilter = NotificationManager.INTERRUPTION_FILTER_PRIORITY
            )
        )
        assertFalse(
            shouldPlayAudibleStrongAlert(
                ringerMode = AudioManager.RINGER_MODE_NORMAL,
                interruptionFilter = NotificationManager.INTERRUPTION_FILTER_ALARMS
            )
        )
        assertFalse(
            shouldPlayAudibleStrongAlert(
                ringerMode = AudioManager.RINGER_MODE_NORMAL,
                interruptionFilter = NotificationManager.INTERRUPTION_FILTER_NONE
            )
        )
    }
}
