package com.timemaster.ui.alert

import androidx.compose.ui.graphics.Color
import com.timemaster.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Test

class AlertActivityTest {
    @Test
    fun alertBackgroundUsesWhiteForLightTheme() {
        assertEquals(Color.White, alertBackgroundColor(ThemeMode.Light, systemInDarkTheme = true))
    }

    @Test
    fun alertBackgroundUsesBlackForDarkTheme() {
        assertEquals(Color.Black, alertBackgroundColor(ThemeMode.Dark, systemInDarkTheme = false))
    }

    @Test
    fun alertBackgroundFollowsSystemTheme() {
        assertEquals(Color.White, alertBackgroundColor(ThemeMode.System, systemInDarkTheme = false))
        assertEquals(Color.Black, alertBackgroundColor(ThemeMode.System, systemInDarkTheme = true))
    }
}
