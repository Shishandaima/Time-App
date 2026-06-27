package com.timemaster.ui.settings

import com.timemaster.ui.theme.FontSizeMode
import com.timemaster.sound.RingDurationMode
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneralSettingsUiContentTest {
    @Test
    fun rowsKeepRequiredOrderAndCurrentFontSizeValue() {
        assertEquals(
            listOf(
                GeneralSettingUiItem("\u5b57\u4f53\u5927\u5c0f", "\u5927"),
                GeneralSettingUiItem("\u54cd\u94c3\u65f6\u957f", "5\u79d2"),
                GeneralSettingUiItem("\u9707\u52a8\u5f00\u5173", null)
            ),
            generalSettingsUiItems(
                fontSizeMode = FontSizeMode.Large,
                ringDurationMode = RingDurationMode.FiveSeconds
            )
        )
    }
}
