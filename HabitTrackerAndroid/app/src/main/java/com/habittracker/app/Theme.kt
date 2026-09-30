package com.habittracker.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF0B0E14)
val Surface = Color(0xFF151A24)
val Surface2 = Color(0xFF1E232D)
val TextC = Color(0xFFE7E9EE)
val Muted = Color(0xFF8B93A7)
val Accent = Color(0xFFF2A65A)
val Success = Color(0xFF4FD1C5)
val Danger = Color(0xFFF27E7E)
val Border = Color(0xFF262C38)
val DarkText = Color(0xFF0B0E14)

private val DarkScheme = darkColorScheme(
    primary = Accent,
    onPrimary = DarkText,
    background = Bg,
    onBackground = TextC,
    surface = Surface,
    onSurface = TextC,
    surfaceVariant = Surface2,
    onSurfaceVariant = Muted,
    outline = Border,
    error = Danger
)

private val AppTypography = Typography(
    headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 28.sp, lineHeight = 34.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp)
)

@Composable
fun HabitTheme(content: @Composable () -> Unit) {
    // The web app is dark-only; keep the same look in both system modes.
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = AppTypography,
        content = content
    )
}
