package com.timemaster.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Test

class FontSizeModeTest {
    @Test
    fun labelsMatchSettingsOptions() {
        assertEquals("\u6807\u51c6", FontSizeMode.Standard.label)
        assertEquals("\u5927", FontSizeMode.Large.label)
        assertEquals("\u7279\u5927", FontSizeMode.ExtraLarge.label)
    }

    @Test
    fun firstReadDefaultsToStandardForNewInstallAndExtraLargeForExistingInstall() {
        assertEquals(FontSizeMode.Standard, defaultFontSizeModeForFirstRead(isExistingInstall = false))
        assertEquals(FontSizeMode.ExtraLarge, defaultFontSizeModeForFirstRead(isExistingInstall = true))
    }

    @Test
    fun extraLargeTypographyKeepsCurrentVisualSizes() {
        val typography = timeMasterTypography(FontSizeMode.ExtraLarge)

        assertEquals(36f, typography.displaySmall.fontSize.value)
        assertEquals(32f, typography.headlineLarge.fontSize.value)
        assertEquals(28f, typography.headlineMedium.fontSize.value)
        assertEquals(24f, typography.titleLarge.fontSize.value)
        assertEquals(20f, typography.bodyLarge.fontSize.value)
        assertEquals(22f, typography.labelLarge.fontSize.value)
    }

    @Test
    fun smallerModesScaleOnlyFontSizeAndKeepLineHeightStable() {
        val standard = timeMasterTypography(FontSizeMode.Standard)
        val large = timeMasterTypography(FontSizeMode.Large)
        val extraLarge = timeMasterTypography(FontSizeMode.ExtraLarge)

        assertEquals(28.8f, standard.displaySmall.fontSize.value, 0.01f)
        assertEquals(32.4f, large.displaySmall.fontSize.value, 0.01f)
        assertEquals(44f, standard.displaySmall.lineHeight.value)
        assertEquals(44f, large.displaySmall.lineHeight.value)
        assertEquals(extraLarge.displaySmall.lineHeight, standard.displaySmall.lineHeight)

        assertEquals(16f, standard.bodyLarge.fontSize.value, 0.01f)
        assertEquals(18f, large.bodyLarge.fontSize.value, 0.01f)
        assertEquals(28f, standard.bodyLarge.lineHeight.value)
        assertEquals(extraLarge.bodyLarge.lineHeight, large.bodyLarge.lineHeight)
    }
}
