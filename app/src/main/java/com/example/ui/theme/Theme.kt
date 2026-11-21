package com.example.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.R

val StickerDisplayFont = FontFamily(
    Font(R.font.baloo_2, FontWeight.Normal),
    Font(R.font.baloo_2, FontWeight.Bold),
)

val UiBodyFont = FontFamily(
    Font(R.font.plus_jakarta_sans, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans, FontWeight.SemiBold),
)

val AppTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = StickerDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = StickerDisplayFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = StickerDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = UiBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 22.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = UiBodyFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = StickerDisplayFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun SekaiStickersTheme(
    dominantColor: Color,
    backgroundColor: Color,
    content: @Composable () -> Unit,
) {
    val animatedDominant by animateColorAsState(
        targetValue = dominantColor,
        animationSpec = tween(durationMillis = 350),
        label = "dominantColor",
    )
    val animatedBackground by animateColorAsState(
        targetValue = backgroundColor,
        animationSpec = tween(durationMillis = 350),
        label = "backgroundColor",
    )

    val onPrimary = if (animatedDominant.luminance() > 0.45f) Color(0xFF141118) else Color.White
    val surfaceElevated = lerp(animatedBackground, Color.White, 0.06f)
    val surfaceContainer = lerp(animatedBackground, Color.White, 0.10f)
    val outlineColor = lerp(animatedDominant, Color.White, 0.25f).copy(alpha = 0.45f)

    val colorScheme = darkColorScheme(
        primary = animatedDominant,
        onPrimary = onPrimary,
        primaryContainer = lerp(animatedBackground, animatedDominant, 0.28f),
        onPrimaryContainer = Color.White,
        secondary = animatedDominant,
        onSecondary = onPrimary,
        background = animatedBackground,
        onBackground = Color(0xFFF5F2F8),
        surface = surfaceElevated,
        onSurface = Color(0xFFF5F2F8),
        surfaceVariant = surfaceContainer,
        onSurfaceVariant = Color(0xFFD5CEDC),
        surfaceContainer = surfaceContainer,
        surfaceContainerHigh = lerp(animatedBackground, Color.White, 0.14f),
        outline = outlineColor,
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
