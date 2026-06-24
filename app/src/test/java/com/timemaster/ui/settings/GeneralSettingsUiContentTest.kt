package com.timemaster.ui.settings

import org.junit.Assert.assertEquals
import org.junit.Test

class GeneralSettingsUiContentTest {
    @Test
    fun placeholderRowsKeepRequiredOrderAndValues() {
        assertEquals(
            listOf(
                GeneralSettingUiItem("\u5b57\u4f53\u5927\u5c0f", "\u6807\u51c6"),
                GeneralSettingUiItem("\u54cd\u94c3\u65f6\u957f", "10\u79d2"),
                GeneralSettingUiItem("\u9707\u52a8\u5f00\u5173", null)
            ),
            GeneralSettingsUiItems
        )
    }
}
