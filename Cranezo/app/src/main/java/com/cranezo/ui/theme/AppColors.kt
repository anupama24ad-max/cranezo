package com.cranezo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Semantic colors that change between light and dark mode.
 *
 * Read them inside any composable with `appColors`:
 * ```
 * Textview(text = "Hello", color = appColors.textPrimary)
 * ```
 * They are provided by [CranezoTheme], so previews and screens follow the active theme.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    // Glass surfaces (see Modifier.glassEffect)
    val glassTop: Color,
    val glassBottom: Color,
    val glassSheen: Color,
    val glassBorderTop: Color,
    val glassBorderBottom: Color,
    val glassShadow: Color,
    // Darker layer for tiles that sit on top of a glass card (nested glass)
    val glassInset: Color,
    // Small chips, dividers and icon backplates sitting on top of glass
    val subtleFill: Color,
    val subtleBorder: Color,
    // Veil drawn over a dark hero image in light mode
    val imageVeil: Color,
    // Plain screen gradient used when a screen has no background image
    val screenTop: Color,
    val screenBottom: Color,
    val screenGlow: Color
)

val DarkAppColors = AppColors(
    isDark = true,
    textPrimary = White,
    textSecondary = Grey,
    textHint = DarkSlate,
    // Nearly transparent: the screen background shows through, like real glass
    glassTop = White.copy(alpha = 0.07f),
    glassBottom = White.copy(alpha = 0.02f),
    glassSheen = White.copy(alpha = 0.05f),
    // Strongest point of the shine on the edge (see Modifier.glassEffect)
    glassBorderTop = White.copy(alpha = 0.90f),
    glassBorderBottom = White.copy(alpha = 0.05f),
    glassShadow = Color.Transparent,
    glassInset = Color.Black.copy(alpha = 0.30f),
    subtleFill = White.copy(alpha = 0.08f),
    subtleBorder = White.copy(alpha = 0.12f),
    imageVeil = Color.Transparent,
    screenTop = Color(0xFF0B1F3F),
    screenBottom = Color(0xFF050810),
    screenGlow = Color(0x33FFA000)
)

val LightAppColors = AppColors(
    isDark = false,
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF475569),
    textHint = Color(0xFF94A3B8),
    glassTop = White.copy(alpha = 0.88f),
    glassBottom = White.copy(alpha = 0.60f),
    glassSheen = White.copy(alpha = 0.70f),
    glassBorderTop = White,
    glassBorderBottom = Color(0xFFCBD5E1).copy(alpha = 0.70f),
    glassShadow = Color(0xFF1E3A5F).copy(alpha = 0.25f),
    glassInset = Color(0xFF0F172A).copy(alpha = 0.04f),
    subtleFill = Color(0xFF0F172A).copy(alpha = 0.05f),
    subtleBorder = Color(0xFF0F172A).copy(alpha = 0.10f),
    imageVeil = White,
    screenTop = Color(0xFFEAF1FB),
    screenBottom = Color(0xFFF9FBFE),
    screenGlow = Color(0x33FFA000)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

val appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
