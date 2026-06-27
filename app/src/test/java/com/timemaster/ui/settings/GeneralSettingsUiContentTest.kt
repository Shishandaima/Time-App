package com.timemaster.ui.settings

import com.timemaster.ui.theme.FontSizeMode
import com.timemaster.sound.RingDurationMode
import com.timemaster.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class GeneralSettingsUiContentTest {
    @Test
    fun rowsKeepRequiredOrderAndCurrentValues() {
        assertEquals(
            listOf(
                GeneralSettingUiItem("\u4e3b\u9898", "\u8ddf\u968f\u7cfb\u7edf"),
                GeneralSettingUiItem("\u5b57\u4f53\u5927\u5c0f", "\u5927"),
                GeneralSettingUiItem("\u54cd\u94c3\u65f6\u957f", "5\u79d2"),
                GeneralSettingUiItem("\u9707\u52a8", null),
                GeneralSettingUiItem("\u9759\u97f3", null)
            ),
            generalSettingsUiItems(
                themeMode = ThemeMode.System,
                fontSizeMode = FontSizeMode.Large,
                ringDurationMode = RingDurationMode.FiveSeconds
            )
        )
    }
}
