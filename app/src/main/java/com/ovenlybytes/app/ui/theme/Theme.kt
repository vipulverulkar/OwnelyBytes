package com.ovenlybytes.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryCocoa,
    onPrimary = WarmCream,
    primaryContainer = WarmBorder,
    onPrimaryContainer = PrimaryCocoaDark,
    secondary = SecondaryRose,
    onSecondary = WarmCream,
    tertiary = TertiaryCaramel,
    background = WarmCream,
    onBackground = TextPrimary,
    surface = WarmCard,
    onSurface = TextPrimary,
    surfaceVariant = WarmCream,
    onSurfaceVariant = TextSecondary,
    outline = WarmBorder
)

@Composable
fun OvenlyBytesTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = PrimaryCocoa.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

