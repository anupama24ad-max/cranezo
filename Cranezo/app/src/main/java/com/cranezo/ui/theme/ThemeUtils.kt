package com.cranezo.ui.theme

import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource

/**
 * Returns true if the device is currently in Dark Theme based on phone settings.
 */
@Composable
fun isDarkTheme(): Boolean = isSystemInDarkTheme()

/**
 * Dynamically selects a [Color] based on phone setting (Light or Dark mode).
 *
 * Example:
 * ```
 * val textColor = themeColor(light = Blackish, dark = White)
 * val cardBg = themeColor(light = White, dark = BlackGrey)
 * ```
 *
 * @param light Color for Light Mode.
 * @param dark Color for Dark Mode.
 */
@Composable
fun themeColor(light: Color, dark: Color): Color {
    return if (isSystemInDarkTheme()) dark else light
}

/**
 * Dynamically selects a [Painter] for images/icons based on phone setting (Light or Dark mode).
 *
 * Example:
 * ```
 * Image(
 *     painter = themePainter(lightResId = R.drawable.logo_light, darkResId = R.drawable.logo_dark),
 *     contentDescription = null
 * )
 * ```
 *
 * @param lightResId Drawable resource ID for Light Mode.
 * @param darkResId Drawable resource ID for Dark Mode.
 */
@Composable
fun themePainter(
    @DrawableRes lightResId: Int,
    @DrawableRes darkResId: Int
): Painter {
    return painterResource(id = if (isSystemInDarkTheme()) darkResId else lightResId)
}

/**
 * Dynamically selects a Drawable resource ID based on phone setting (Light or Dark mode).
 *
 * @param lightResId Drawable resource ID for Light Mode.
 * @param darkResId Drawable resource ID for Dark Mode.
 */
@Composable
fun themeDrawable(
    @DrawableRes lightResId: Int,
    @DrawableRes darkResId: Int
): Int {
    return if (isSystemInDarkTheme()) darkResId else lightResId
}

/**
 * Dynamically selects any generic value (Dp, Float, String, Modifier, etc.)
 * based on phone setting (Light or Dark mode).
 *
 * Example:
 * ```
 * val cardElevation = themeValue(light = 4.dp, dark = 0.dp)
 * val subtitleText = themeValue(light = "Light Mode", dark = "Dark Mode")
 * ```
 *
 * @param light Value for Light Mode.
 * @param dark Value for Dark Mode.
 */
@Composable
fun <T> themeValue(light: T, dark: T): T {
    return if (isSystemInDarkTheme()) dark else light
}

/**
 * Unified helper object for accessing theme colors, painters, and states.
 *
 * Usage:
 * - AppTheme.isDark
 * - AppTheme.colors.background
 * - AppTheme.color(light = White, dark = DarkBlack)
 * - AppTheme.painter(light = R.drawable.light_logo, dark = R.drawable.dark_logo)
 * - AppTheme.value(light = 8.dp, dark = 0.dp)
 */
object AppTheme {
    val isDark: Boolean
        @Composable
        get() = isSystemInDarkTheme()

    val colors: ColorScheme
        @Composable
        get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable
        get() = MaterialTheme.typography

    @Composable
    fun color(light: Color, dark: Color): Color =
        if (isDark) dark else light

    @Composable
    fun painter(@DrawableRes light: Int, @DrawableRes dark: Int): Painter =
        painterResource(id = if (isDark) dark else light)

    @Composable
    fun drawable(@DrawableRes light: Int, @DrawableRes dark: Int): Int =
        if (isDark) dark else light

    @Composable
    fun <T> value(light: T, dark: T): T =
        if (isDark) dark else light
}
