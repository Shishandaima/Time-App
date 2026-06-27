package com.timemaster.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.isSpecified
import androidx.compose.ui.unit.sp

enum class ThemeMode {
    Light,
    Dark,
    System
}

enum class FontSizeMode(
    val label: String,
    internal val scale: Float
) {
    Standard("\u6807\u51c6", 0.8f),
    Large("\u5927", 0.9f),
    ExtraLarge("\u7279\u5927", 1.0f)
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color.White,
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1F1B1B),
    surfaceContainer = Color(0xFFF1F1F1),
    surfaceContainerHigh = Color(0xFFEFEFEF),
    surfaceContainerHighest = Color(0xFFECECEC)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF71D8CA),
    onPrimary = Color(0xFF003732),
    surface = Color(0xFF151312),
    onSurface = Color(0xFFE8E1DF),
    background = Color(0xFF151312),
    onBackground = Color(0xFFE8E1DF),
    surfaceContainer = Color(0xFF222222),
    surfaceContainerHigh = Color(0xFF2A2A2A),
    surfaceContainerHighest = Color(0xFF303030)
)

private const val SETTINGS_PREFS = "app_settings"
private const val THEME_MODE_KEY = "theme_mode"
private const val FONT_SIZE_MODE_KEY = "font_size_mode"

fun readThemeMode(context: Context): ThemeMode =
    runCatching {
        ThemeMode.valueOf(
            context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
                .getString(THEME_MODE_KEY, ThemeMode.System.name)!!
        )
    }.getOrDefault(ThemeMode.System)

fun saveThemeMode(context: Context, themeMode: ThemeMode) {
    context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(THEME_MODE_KEY, themeMode.name)
        .apply()
}

fun readFontSizeMode(context: Context): FontSizeMode {
    val prefs = context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
    val savedValue = prefs.getString(FONT_SIZE_MODE_KEY, null)
    if (savedValue != null) {
        return runCatching { FontSizeMode.valueOf(savedValue) }.getOrDefault(FontSizeMode.Standard)
    }

    return defaultFontSizeModeForFirstRead(isExistingInstall = isExistingInstall(context))
}

fun saveFontSizeMode(context: Context, fontSizeMode: FontSizeMode) {
    context.getSharedPreferences(SETTINGS_PREFS, Context.MODE_PRIVATE)
        .edit()
        .putString(FONT_SIZE_MODE_KEY, fontSizeMode.name)
        .apply()
}

internal fun defaultFontSizeModeForFirstRead(isExistingInstall: Boolean): FontSizeMode =
    if (isExistingInstall) FontSizeMode.ExtraLarge else FontSizeMode.Standard

private fun isExistingInstall(context: Context): Boolean =
    runCatching {
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        packageInfo.firstInstallTime + 1_000L < packageInfo.lastUpdateTime
    }.getOrDefault(false)

private val ExtraLargeTypography = Typography(
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    labelLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp)
)

internal fun timeMasterTypography(fontSizeMode: FontSizeMode): Typography =
    ExtraLargeTypography.scaleFontSizes(fontSizeMode.scale)

private fun Typography.scaleFontSizes(scale: Float): Typography =
    Typography(
        displaySmall = displaySmall.scaledFontSize(scale),
        headlineLarge = headlineLarge.scaledFontSize(scale),
        headlineMedium = headlineMedium.scaledFontSize(scale),
        headlineSmall = headlineSmall.scaledFontSize(scale),
        titleLarge = titleLarge.scaledFontSize(scale),
        titleMedium = titleMedium.scaledFontSize(scale),
        titleSmall = titleSmall.scaledFontSize(scale),
        bodyLarge = bodyLarge.scaledFontSize(scale),
        bodyMedium = bodyMedium.scaledFontSize(scale),
        bodySmall = bodySmall.scaledFontSize(scale),
        labelLarge = labelLarge.scaledFontSize(scale),
        labelMedium = labelMedium.scaledFontSize(scale),
        labelSmall = labelSmall.scaledFontSize(scale)
    )

private fun TextStyle.scaledFontSize(scale: Float): TextStyle =
    if (fontSize.isSpecified) {
        copy(fontSize = (fontSize.value * scale).sp)
    } else {
        this
    }

@Composable
fun TimeMasterTheme(
    themeMode: ThemeMode = ThemeMode.System,
    fontSizeMode: FontSizeMode = FontSizeMode.Standard,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = timeMasterTypography(fontSizeMode)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            content = content
        )
    }
}
