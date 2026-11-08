package com.cranezo.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The colour tokens of the app, one value per theme. This file is the only place for colour values:
 * screens and components read them with `appColors` and never contain a colour of their own.
 *
 * ```
 * Textview(text = "Hello", color = appColors.textPrimary)
 * ```
 * They are provided by [CranezoTheme], so previews and screens follow the active theme.
 */
@Immutable
data class AppColors(
    val isDark: Boolean,
    // Signature background: base, dark centre, edge, and the two corner glows
    val bgBase: Color,
    val bgCenter: Color,
    val bgEdge: Color,
    val glowBlue: Color,
    val glowBlueAlpha: Float,
    val glowAmber: Color,
    val glowAmberAlpha: Float,
    // Text
    val textPrimary: Color,
    val textSecondary: Color,
    val textHint: Color,
    // Amber used as text: darker in light mode so it is readable on white
    val primaryText: Color,
    // Fill of selected states
    val primaryTint: Color,
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
    val screenGlow: Color,
    // Curved hero header on home screens: navy gradient with an amber glow in the corner
    val heroTop: Color,
    val heroBottom: Color,
    val heroGlow: Color,
    // Light mode page body: champagne gold at the top fading to white, with a soft shine (dark mode uses bgBase)
    val bodyTop: Color,
    val bodyMid: Color,
    val bodyBottom: Color,
    val bodyShine: Color
)

val DarkAppColors = AppColors(
    isDark = true,
    bgBase = Color(0xFF040810),
    bgCenter = Color(0xFF02050C),
    bgEdge = Color(0xFF050A15),
    glowBlue = Color(0xFF2563EB),
    glowBlueAlpha = 0.38f,
    glowAmber = Color(0xFFF59E0B),
    glowAmberAlpha = 0.30f,
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFF94A3B8),
    textHint = Color(0xFF64748B),
    primaryText = Color(0xFFF59E0B),
    primaryTint = Color(0xFFF59E0B).copy(alpha = 0.12f),
    // card.fill: translucent glass, so the corner glows show through
    glassTop = Color(0xFF0A1226).copy(alpha = 0.55f),
    glassBottom = Color(0xFF0A1226).copy(alpha = 0.55f),
    glassSheen = Color(0xFFFFFFFF).copy(alpha = 0.04f),
    // card.borderTop (top-lit edge) to card.border
    glassBorderTop = Color(0xFFFFFFFF).copy(alpha = 0.18f),
    glassBorderBottom = Color(0xFFFFFFFF).copy(alpha = 0.10f),
    glassShadow = Color.Transparent,
    glassInset = Color(0xFFFFFFFF).copy(alpha = 0.04f),
    subtleFill = Color(0xFFFFFFFF).copy(alpha = 0.06f),
    subtleBorder = Color(0xFF1A2A47),
    imageVeil = Color.Transparent,
    screenTop = Color(0xFF0B1F3F),
    screenBottom = Color(0xFF050810),
    screenGlow = Color(0x33FFA000),
    heroTop = Color(0xFF0E2A55),
    heroBottom = Color(0xFF060D1C),
    heroGlow = Color(0xFFF59E0B),
    bodyTop = Color(0xFF040810),
    bodyMid = Color(0xFF040810),
    bodyBottom = Color(0xFF040810),
    bodyShine = Color.Transparent
)

val LightAppColors = AppColors(
    isDark = false,
    bgBase = Color(0xFFFFFFFF),
    bgCenter = Color(0xFFF6F7FB),
    bgEdge = Color(0xFFF6F7FB),
    glowBlue = Color(0xFF2563EB),
    glowBlueAlpha = 0.10f,
    glowAmber = Color(0xFFF59E0B),
    glowAmberAlpha = 0.16f,
    textPrimary = Color(0xFF0F172A),
    textSecondary = Color(0xFF64748B),
    textHint = Color(0xFF94A3B8),
    primaryText = Color(0xFFB45309),
    primaryTint = Color(0xFFF59E0B).copy(alpha = 0.14f),
    // card.fill: frosted white, the soft yellow glow shows faintly through
    glassTop = Color(0xFFFFFFFF).copy(alpha = 0.78f),
    glassBottom = Color(0xFFFFFFFF).copy(alpha = 0.78f),
    glassSheen = Color(0xFFFFFFFF),
    glassBorderTop = Color(0xFFFFFFFF).copy(alpha = 0.90f),
    glassBorderBottom = Color(0xFF0F172A).copy(alpha = 0.08f),
    glassShadow = Color(0xFF0F172A).copy(alpha = 0.08f),
    glassInset = Color(0xFF0F172A).copy(alpha = 0.04f),
    subtleFill = Color(0xFFF1F4F9),
    subtleBorder = Color(0xFFB8C2D0),
    imageVeil = Color(0xFFFFFFFF),
    screenTop = Color(0xFFEAF1FB),
    screenBottom = Color(0xFFF9FBFE),
    screenGlow = Color(0x33FFA000),
    heroTop = Color(0xFF0B1F3F),
    heroBottom = Color(0xFF050A15),
    heroGlow = Color(0xFFF59E0B),
    // A clean, light page: a soft butter-yellow touch at the very top fading quickly into a calm cool off-white,
    // so the white cards stand out and the page does not look heavy or yellow
    bodyTop = Color(0xFFFFEFC2),
    bodyMid = Color(0xFFF8F6F2),
    bodyBottom = Color(0xFFF2F5FA),
    bodyShine = Color(0xFFFFFFFF)
)

val LocalAppColors = staticCompositionLocalOf { DarkAppColors }

val appColors: AppColors
    @Composable
    @ReadOnlyComposable
    get() = LocalAppColors.current
