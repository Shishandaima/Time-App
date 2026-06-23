package com.timemaster.ui.home

import com.timemaster.domain.Reminder
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import kotlin.math.max

fun reminderCountdownText(
    reminder: Reminder,
    nowMillis: Long,
    zoneId: ZoneId = ZoneId.systemDefault()
): String {
    if (!reminder.isEnabled) return "\u4e0b\u6b21\u63d0\u9192\uff1a\u672a\u542f\u7528"

    val targetMillis = reminder.nextTriggerAtMillis
        ?.takeIf { it > nowMillis }
        ?: return "\u4e0b\u6b21\u63d0\u9192\uff1a\u6b63\u5728\u91cd\u65b0\u8c03\u5ea6"

    val totalSeconds = max(0L, ChronoUnit.SECONDS.between(
        Instant.ofEpochMilli(nowMillis),
        Instant.ofEpochMilli(targetMillis)
    ))
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    val clock = "%02d:%02d:%02d".format(hours, minutes, seconds)

    return if (days > 0) {
        "\u4e0b\u6b21\u63d0\u9192\uff1a${days}\u5929 $clock"
    } else {
        "\u4e0b\u6b21\u63d0\u9192\uff1a$clock"
    }
}
