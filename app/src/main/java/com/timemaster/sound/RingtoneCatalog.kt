package com.timemaster.sound

import com.timemaster.R

data class RingtoneOption(
    val id: String,
    val label: String,
    val rawRes: Int
)

object RingtoneCatalog {
    val all = listOf(
        RingtoneOption("gentle_chime", "柔和提示", R.raw.gentle_chime),
        RingtoneOption("clear_bell", "清脆铃", R.raw.clear_bell),
        RingtoneOption("digital_tick", "电子滴答", R.raw.digital_tick),
        RingtoneOption("long_tone", "长鸣提醒", R.raw.long_tone),
        RingtoneOption("bright_prompt", "轻快提示", R.raw.bright_prompt)
    )

    fun byId(id: String): RingtoneOption =
        all.firstOrNull { it.id == id } ?: all.first()
}
