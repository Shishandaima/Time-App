package com.timemaster.sound

import android.view.KeyEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RingtoneInterruptKeysTest {
    @Test
    fun volumeUpAndDownInterruptRinging() {
        assertTrue(isRingtoneInterruptKey(KeyEvent.KEYCODE_VOLUME_UP))
        assertTrue(isRingtoneInterruptKey(KeyEvent.KEYCODE_VOLUME_DOWN))
    }

    @Test
    fun otherKeysDoNotInterruptRinging() {
        assertFalse(isRingtoneInterruptKey(KeyEvent.KEYCODE_BACK))
        assertFalse(isRingtoneInterruptKey(KeyEvent.KEYCODE_VOLUME_MUTE))
    }

    @Test
    fun volumeKeysAreHandledOnlyWhileRinging() {
        assertTrue(shouldHandleRingtoneInterruptKey(KeyEvent.KEYCODE_VOLUME_UP, isRinging = true))
        assertFalse(shouldHandleRingtoneInterruptKey(KeyEvent.KEYCODE_VOLUME_UP, isRinging = false))
    }
}
