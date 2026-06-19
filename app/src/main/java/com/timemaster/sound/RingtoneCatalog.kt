package com.timemaster.sound

import com.timemaster.R

data class RingtoneOption(
    val id: String,
    val label: String,
    val rawRes: Int
)

object RingtoneCatalog {
    val all = listOf(
        RingtoneOption("gentle_chime", "\u67d4\u548c\u63d0\u793a", R.raw.gentle_chime),
        RingtoneOption("clear_bell", "\u6e05\u8106\u94c3", R.raw.clear_bell),
        RingtoneOption("digital_tick", "\u7535\u5b50\u6ef4\u7b54", R.raw.digital_tick),
        RingtoneOption("long_tone", "\u957f\u9e23\u63d0\u9192", R.raw.long_tone),
        RingtoneOption("bright_prompt", "\u8f7b\u5feb\u63d0\u793a", R.raw.bright_prompt)
    )

    fun byId(id: String): RingtoneOption =
        all.firstOrNull { it.id == id } ?: all.first()
}
