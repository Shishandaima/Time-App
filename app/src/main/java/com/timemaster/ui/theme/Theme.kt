package com.timemaster.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp

private val Colors = lightColorScheme(
    primary = Color(0xFF006A60),
    onPrimary = Color.White,
    surface = Color(0xFFFFFBFE),
    onSurface = Color(0xFF1F1B1B)
)

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
fun TimeMasterTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Colors,
        typography = TimeMasterTypography,
        content = content
    )
}
