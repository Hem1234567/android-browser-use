package com.browseruse.android.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Bg = Color(0xFF09090B)
val Surface = Color(0xFF18181B)
val SurfaceAlt = Color(0xFF27272A)
val Border = Color(0xFF27272A)
val BorderStrong = Color(0xFF3F3F46)
val TextPrimary = Color(0xFFF4F4F5)
val TextSecondary = Color(0xFFA1A1AA)
val TextMuted = Color(0xFF71717A)
val Success = Color(0xFF22C55E)
val ErrorRed = Color(0xFFEF4444)
val AccentAmber = Color(0xFFFBBF24)
val AccentGreen = Color(0xFF4ADE80)
val AccentPurple = Color(0xFFC084FC)
val AccentBlue = Color(0xFF60A5FA)

private val AppColors = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    secondary = TextSecondary,
    onSecondary = Color.Black,
    background = Bg,
    onBackground = TextPrimary,
    surface = Surface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceAlt,
    onSurfaceVariant = TextSecondary,
    outline = Border,
    error = ErrorRed,
    onError = Color.White
)

private val AppTypography = Typography(
    headlineMedium = TextStyle(fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleLarge = TextStyle(fontSize = 20.sp, lineHeight = 28.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary),
    titleMedium = TextStyle(fontSize = 16.sp, lineHeight = 22.sp, fontWeight = FontWeight.Medium, color = TextPrimary),
    bodyLarge = TextStyle(fontSize = 15.sp, lineHeight = 24.sp, color = TextPrimary),
    bodyMedium = TextStyle(fontSize = 14.sp, lineHeight = 20.sp, color = TextPrimary),
    bodySmall = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, color = TextSecondary),
    labelSmall = TextStyle(fontSize = 11.sp, lineHeight = 14.sp, color = TextMuted)
)

@Composable
fun BrowserUseTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = AppColors, typography = AppTypography, content = content)
}
