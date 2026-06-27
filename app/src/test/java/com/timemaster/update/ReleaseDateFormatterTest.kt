package com.timemaster.update

import org.junit.Assert.assertEquals
import org.junit.Test

class ReleaseDateFormatterTest {
    @Test
    fun formatsIso8601PublishedDateAsDateOnly() {
        assertEquals("2026-06-27", formatReleasePublishedDate("2026-06-27T08:35:20Z"))
    }

    @Test
    fun keepsOriginalValueWhenPublishedDateCannotBeParsed() {
        assertEquals("not-a-date", formatReleasePublishedDate("not-a-date"))
    }

    @Test
    fun keepsBlankPublishedDateBlank() {
        assertEquals("", formatReleasePublishedDate(""))
    }
}
