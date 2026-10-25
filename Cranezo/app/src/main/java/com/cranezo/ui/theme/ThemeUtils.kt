package com.cranezo.ui.theme

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.cranezo.utils.SessionManager

/**
 * Theme selection modes.
 */
enum class AppThemeMode(val key: String, val title: String) {
    LIGHT("light", "Always Light"),
    DARK("dark", "Always Dark"),
    SYSTEM("system", "System");

    companion object {
        fun fromKey(key: String?): AppThemeMode {
            return entries.firstOrNull { it.key.equals(key, ignoreCase = true) } ?: SYSTEM
        }
    }
}

/**
 * Global reactive state for application theme mode.
 */
object AppThemeState {
    var currentThemeMode = mutableStateOf(AppThemeMode.SYSTEM)
        private set

    private var isInitialized = false

    fun init(context: Context) {
        if (!isInitialized) {
            val sm = SessionManager(context.applicationContext)
            val saved = sm.getAppTheme()
            currentThemeMode.value = AppThemeMode.fromKey(saved)
            isInitialized = true
        }
    }

    fun setThemeMode(mode: AppThemeMode, context: Context) {
        currentThemeMode.value = mode
        SessionManager(context.applicationContext).setAppTheme(mode.key)
    }
}

/**
 * Returns true if the device / app is currently displaying Dark Theme,
 * taking into account the user's preference (Always Light, Always Dark, or System).
 */
@Composable
fun isDarkTheme(): Boolean {
    val mode by AppThemeState.currentThemeMode
    val systemDark = isSystemInDarkTheme()
    return when (mode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> systemDark
    }
}

/**
 * Dynamically selects a [Color] based on active theme (Always Light, Always Dark, or System).
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
    return if (isDarkTheme()) dark else light
}

/**
 * Dynamically selects a [Painter] for images/icons based on active theme (Always Light, Always Dark, or System).
 *
 * Example:
 * ```
 * Image(
 *     painter = themePainter(lightResId = R.drawable.cranezo_light_logo, darkResId = R.drawable.cranezo_logo),
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
    return painterResource(id = if (isDarkTheme()) darkResId else lightResId)
}

/**
 * Dynamically selects a Drawable resource ID based on active theme (Always Light, Always Dark, or System).
 *
 * @param lightResId Drawable resource ID for Light Mode.
 * @param darkResId Drawable resource ID for Dark Mode.
 */
@Composable
fun themeDrawable(
    @DrawableRes lightResId: Int,
    @DrawableRes darkResId: Int
): Int {
    return if (isDarkTheme()) darkResId else lightResId
}

/**
 * Dynamically selects any generic value (Dp, Float, String, Modifier, etc.)
 * based on active theme (Always Light, Always Dark, or System).
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
    return if (isDarkTheme()) dark else light
}

/**
 * Unified helper object for accessing theme colors, painters, and states.
 *
 * Usage:
 * - AppTheme.mode
 * - AppTheme.isDark
 * - AppTheme.colors.background
 * - AppTheme.color(light = White, dark = DarkBlack)
 * - AppTheme.painter(light = R.drawable.light_logo, dark = R.drawable.dark_logo)
 * - AppTheme.value(light = 8.dp, dark = 0.dp)
 */
object AppTheme {
    val mode: AppThemeMode
        @Composable
        get() = AppThemeState.currentThemeMode.value

    val isDark: Boolean
        @Composable
        get() = isDarkTheme()

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
