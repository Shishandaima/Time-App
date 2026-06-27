package com.timemaster.update

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

fun formatReleasePublishedDate(publishedAt: String): String {
    if (publishedAt.isBlank()) return publishedAt

    return runCatching {
        Instant.parse(publishedAt)
            .atZone(ZoneOffset.UTC)
            .toLocalDate()
            .format(DateTimeFormatter.ISO_LOCAL_DATE)
    }.getOrDefault(publishedAt)
}
