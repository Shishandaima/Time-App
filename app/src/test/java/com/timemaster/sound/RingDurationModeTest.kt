package com.timemaster.sound

import org.junit.Assert.assertEquals
import org.junit.Test

class RingDurationModeTest {
    @Test
    fun optionsHaveRequiredLabelsAndDurations() {
        assertEquals("\u0035\u79d2", RingDurationMode.FiveSeconds.label)
        assertEquals("\u0031\u0030\u79d2", RingDurationMode.TenSeconds.label)
        assertEquals("\u0031\u0035\u79d2", RingDurationMode.FifteenSeconds.label)

        assertEquals(5_000L, RingDurationMode.FiveSeconds.durationMillis)
        assertEquals(10_000L, RingDurationMode.TenSeconds.durationMillis)
        assertEquals(15_000L, RingDurationMode.FifteenSeconds.durationMillis)
    }

    @Test
    fun defaultKeepsExistingTenSecondDuration() {
        assertEquals(RingDurationMode.TenSeconds, defaultRingDurationMode())
    }
}
