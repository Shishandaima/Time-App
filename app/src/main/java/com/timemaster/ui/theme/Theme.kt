package com.timemaster.ui.theme

import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

enum class ThemeMode {
    Light,
    Dark,
    System
}

private val LightColors = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color.White,
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1F1B1B)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF71D8CA),
    onPrimary = Color(0xFF003732),
    surface = Color(0xFF151312),
    onSurface = Color(0xFFE8E1DF),
    background = Color(0xFF151312),
    onBackground = Color(0xFFE8E1DF),
    surfaceContainer = Color(0xFF211E1D),
    surfaceContainerHigh = Color(0xFF292625),
    surfaceContainerHighest = Color(0xFF312D2C)
)

private const val SETTINGS_PREFS = "app_settings"
private const val THEME_MODE_KEY = "theme_mode"

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

private val TimeMasterTypography = Typography(
    displaySmall = TextStyle(fontSize = 36.sp, lineHeight = 44.sp),
    headlineLarge = TextStyle(fontSize = 32.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontSize = 28.sp, lineHeight = 36.sp),
    titleLarge = TextStyle(fontSize = 24.sp, lineHeight = 32.sp),
    bodyLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    bodyMedium = TextStyle(fontSize = 20.sp, lineHeight = 28.sp),
    labelLarge = TextStyle(fontSize = 22.sp, lineHeight = 28.sp)
)

@Composable
fun TimeMasterTheme(
    themeMode: ThemeMode = ThemeMode.System,
    content: @Composable () -> Unit
) {
    val useDarkTheme = when (themeMode) {
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
        ThemeMode.System -> isSystemInDarkTheme()
    }
    MaterialTheme(
        colorScheme = if (useDarkTheme) DarkColors else LightColors,
        typography = TimeMasterTypography,
        content = content
    )
}
