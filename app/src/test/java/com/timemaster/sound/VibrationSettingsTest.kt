package com.timemaster.sound

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VibrationSettingsTest {
    @Test
    fun vibrationDefaultsToEnabled() {
        assertTrue(defaultVibrationEnabled())
    }

    @Test
    fun strongAlertVibratesOnlyWhenLoopingAndEnabled() {
        assertTrue(shouldStartVibration(loop = true, vibrationEnabled = true))
        assertFalse(shouldStartVibration(loop = true, vibrationEnabled = false))
        assertFalse(shouldStartVibration(loop = false, vibrationEnabled = true))
    }
}
