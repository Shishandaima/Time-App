package com.timemaster.sound

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VolumeButtonRingingInterrupterTest {
    @Test
    fun unchangedVolumeSnapshotDoesNotInterrupt() {
        val snapshot = VolumeSnapshot(alarmVolume = 5, musicVolume = 8)

        assertFalse(shouldInterruptForVolumeChange(snapshot, snapshot))
    }

    @Test
    fun alarmOrMusicVolumeChangeInterrupts() {
        val previous = VolumeSnapshot(alarmVolume = 5, musicVolume = 8)

        assertTrue(
            shouldInterruptForVolumeChange(
                previous,
                VolumeSnapshot(alarmVolume = 6, musicVolume = 8)
            )
        )
        assertTrue(
            shouldInterruptForVolumeChange(
                previous,
                VolumeSnapshot(alarmVolume = 5, musicVolume = 7)
            )
        )
    }

    @Test
    fun firstVolumeSnapshotDoesNotInterrupt() {
        assertFalse(
            shouldInterruptForVolumeChange(
                previous = null,
                current = VolumeSnapshot(alarmVolume = 5, musicVolume = 8)
            )
        )
    }
}
