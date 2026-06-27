package com.timemaster.sound

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SilentModeSettingsTest {
    @Test
    fun silentModeDefaultsToDisabled() {
        assertFalse(defaultSilentModeEnabled())
    }

    @Test
    fun strongAlertPlaysSoundOnlyWhenAudibleAndNotSilent() {
        assertTrue(shouldPlayAlertSound(audibleAllowed = true, silentModeEnabled = false))
        assertFalse(shouldPlayAlertSound(audibleAllowed = true, silentModeEnabled = true))
        assertFalse(shouldPlayAlertSound(audibleAllowed = false, silentModeEnabled = false))
    }
}
