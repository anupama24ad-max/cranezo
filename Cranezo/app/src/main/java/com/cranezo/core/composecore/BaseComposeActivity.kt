package com.cranezo.core.composecore

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorInt

abstract class BaseComposeActivity : ComponentActivity() {

    /** Toggle edge-to-edge globally */
    protected open val useEdgeToEdge: Boolean = true

    /** True = dark icons (for light bg). False = light icons (for dark bg). */
    protected open val darkStatusBarIcons: Boolean = true
    protected open val darkNavBarIcons: Boolean = true

    /** Bar colors (usually transparent for real edge-to-edge) */
    @ColorInt
    protected open val statusBarColor: Int = android.graphics.Color.TRANSPARENT
    @ColorInt protected open val navBarColor: Int = android.graphics.Color.TRANSPARENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (useEdgeToEdge) {
            val statusStyle = if (darkStatusBarIcons) {
                // Light style = dark icons (for light backgrounds)
                SystemBarStyle.light(statusBarColor, statusBarColor)
            } else {
                // Dark style = light icons (for dark backgrounds)
                SystemBarStyle.dark(statusBarColor)
            }

            val navStyle = if (darkNavBarIcons) {
                SystemBarStyle.light(navBarColor, navBarColor)
            } else {
                SystemBarStyle.dark(navBarColor)
            }

            // Apply before setContent(...)
            enableEdgeToEdge(
                statusBarStyle = statusStyle,
                navigationBarStyle = navStyle
            )
        }
    }
}