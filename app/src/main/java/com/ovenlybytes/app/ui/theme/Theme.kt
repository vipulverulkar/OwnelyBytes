package com.ovenlybytes.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.ovenlybytes.app.model.AppTheme

fun getColorSchemeForTheme(appTheme: AppTheme): ColorScheme {
    return when (appTheme) {
        AppTheme.ARTISAN_COCOA -> lightColorScheme(
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
        AppTheme.STRAWBERRY_SHORTCAKE -> lightColorScheme(
            primary = Color(0xFFC2185B),
            onPrimary = Color(0xFFFFF0F5),
            primaryContainer = Color(0xFFF8BBD0),
            onPrimaryContainer = Color(0xFF4A0E1C),
            secondary = Color(0xFFD81B60),
            background = Color(0xFFFFF0F5),
            onBackground = Color(0xFF2C0B14),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2C0B14),
            surfaceVariant = Color(0xFFFCE4EC),
            onSurfaceVariant = Color(0xFF880E4F),
            outline = Color(0xFFF48FB1)
        )
        AppTheme.HONEY_VANILLA -> lightColorScheme(
            primary = Color(0xFFB45309),
            onPrimary = Color(0xFFFFFDF5),
            primaryContainer = Color(0xFFFDE68A),
            onPrimaryContainer = Color(0xFF451A03),
            secondary = Color(0xFFD97706),
            background = Color(0xFFFFFDF5),
            onBackground = Color(0xFF2B1700),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2B1700),
            surfaceVariant = Color(0xFFFEF3C7),
            onSurfaceVariant = Color(0xFF78350F),
            outline = Color(0xFFFCD34D)
        )
        AppTheme.PISTACHIO_MATCHA -> lightColorScheme(
            primary = Color(0xFF2D5A27),
            onPrimary = Color(0xFFF4F9F4),
            primaryContainer = Color(0xFFC8E6C9),
            onPrimaryContainer = Color(0xFF0F2B0B),
            secondary = Color(0xFF487B32),
            background = Color(0xFFF4F9F4),
            onBackground = Color(0xFF132A10),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF132A10),
            surfaceVariant = Color(0xFFE8F5E9),
            onSurfaceVariant = Color(0xFF1B5E20),
            outline = Color(0xFFA5D6A7)
        )
        AppTheme.LEMON_CHIFFON -> lightColorScheme(
            primary = Color(0xFFA16207),
            onPrimary = Color(0xFFFFFCF0),
            primaryContainer = Color(0xFFFEF08A),
            onPrimaryContainer = Color(0xFF422006),
            secondary = Color(0xFFCA8A04),
            background = Color(0xFFFFFCF0),
            onBackground = Color(0xFF291400),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF291400),
            surfaceVariant = Color(0xFFFEF9C3),
            onSurfaceVariant = Color(0xFF713F12),
            outline = Color(0xFFFDE047)
        )
        AppTheme.BLUEBERRY_MACARON -> lightColorScheme(
            primary = Color(0xFF5B21B6),
            onPrimary = Color(0xFFF5F3FF),
            primaryContainer = Color(0xFFDDD6FE),
            onPrimaryContainer = Color(0xFF1E1B4B),
            secondary = Color(0xFF7C3AED),
            background = Color(0xFFF5F3FF),
            onBackground = Color(0xFF1E1B4B),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1E1B4B),
            surfaceVariant = Color(0xFFEDE9FE),
            onSurfaceVariant = Color(0xFF4C1D95),
            outline = Color(0xFFC4B5FD)
        )
        AppTheme.PEACH_COBBLER -> lightColorScheme(
            primary = Color(0xFFC2410C),
            onPrimary = Color(0xFFFFF7ED),
            primaryContainer = Color(0xFFFFEDD5),
            onPrimaryContainer = Color(0xFF431407),
            secondary = Color(0xFFEA580C),
            background = Color(0xFFFFF7ED),
            onBackground = Color(0xFF270900),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF270900),
            surfaceVariant = Color(0xFFFFE4E6),
            onSurfaceVariant = Color(0xFF9F1239),
            outline = Color(0xFFFDBA74)
        )
        AppTheme.DARK_MOCHA -> darkColorScheme(
            primary = Color(0xFFFFB300),
            onPrimary = Color(0xFF121212),
            primaryContainer = Color(0xFF3E2723),
            onPrimaryContainer = Color(0xFFFFECB3),
            secondary = Color(0xFFFF8F00),
            background = Color(0xFF121212),
            onBackground = Color(0xFFEFEBE9),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFEFEBE9),
            surfaceVariant = Color(0xFF2A1C18),
            onSurfaceVariant = Color(0xFFD7CCC8),
            outline = Color(0xFF4E342E)
        )
    }
}

@Composable
fun OvenlyBytesTheme(
    appTheme: AppTheme = AppTheme.ARTISAN_COCOA,
    content: @Composable () -> Unit
) {
    val colorScheme = getColorSchemeForTheme(appTheme)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.primary.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = (appTheme == AppTheme.DARK_MOCHA)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

