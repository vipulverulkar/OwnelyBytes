package com.ovenlybytes.app.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
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
        AppTheme.ROYAL_RED_VELVET -> lightColorScheme(
            primary = Color(0xFF800020),
            onPrimary = Color(0xFFFFFAF6),
            primaryContainer = Color(0xFFFFCDD2),
            onPrimaryContainer = Color(0xFF33000C),
            secondary = Color(0xFFB71C1C),
            background = Color(0xFFFFFAF6),
            onBackground = Color(0xFF210008),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF210008),
            surfaceVariant = Color(0xFFFFEBEE),
            onSurfaceVariant = Color(0xFF800020),
            outline = Color(0xFFEF9A9A)
        )
        AppTheme.GOLDEN_VANILLA -> lightColorScheme(
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
        AppTheme.SALTED_CARAMEL -> lightColorScheme(
            primary = Color(0xFF9F5113),
            onPrimary = Color(0xFFFFF8F0),
            primaryContainer = Color(0xFFFED7AA),
            onPrimaryContainer = Color(0xFF431D04),
            secondary = Color(0xFFD97706),
            background = Color(0xFFFFF8F0),
            onBackground = Color(0xFF2B1202),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF2B1202),
            surfaceVariant = Color(0xFFFFEDD5),
            onSurfaceVariant = Color(0xFF7C2D12),
            outline = Color(0xFFFDBA74)
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
        AppTheme.EMERALD_MINT -> lightColorScheme(
            primary = Color(0xFF1B4332),
            onPrimary = Color(0xFFF0F7F4),
            primaryContainer = Color(0xFFB7E4C7),
            onPrimaryContainer = Color(0xFF081C15),
            secondary = Color(0xFF2D6A4F),
            background = Color(0xFFF0F7F4),
            onBackground = Color(0xFF081C15),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF081C15),
            surfaceVariant = Color(0xFFD8F3DC),
            onSurfaceVariant = Color(0xFF1B4332),
            outline = Color(0xFF95D5B2)
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
        AppTheme.LAVENDER_EARL_GREY -> lightColorScheme(
            primary = Color(0xFF4A3B6B),
            onPrimary = Color(0xFFF7F5FA),
            primaryContainer = Color(0xFFDDD6FE),
            onPrimaryContainer = Color(0xFF1E1B4B),
            secondary = Color(0xFF6B5B95),
            background = Color(0xFFF7F5FA),
            onBackground = Color(0xFF1E1533),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1E1533),
            surfaceVariant = Color(0xFFECE7F6),
            onSurfaceVariant = Color(0xFF382A52),
            outline = Color(0xFFC4B5FD)
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
        AppTheme.MIDNIGHT_TRUFFLE -> darkColorScheme(
            primary = Color(0xFFF59E0B),
            onPrimary = Color(0xFF0F0F12),
            primaryContainer = Color(0xFF332002),
            onPrimaryContainer = Color(0xFFFDE68A),
            secondary = Color(0xFFD97706),
            background = Color(0xFF0F0F12),
            onBackground = Color(0xFFF3F4F6),
            surface = Color(0xFF1A181C),
            onSurface = Color(0xFFF3F4F6),
            surfaceVariant = Color(0xFF262329),
            onSurfaceVariant = Color(0xFFE5E7EB),
            outline = Color(0xFF4B4552)
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
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = (appTheme == AppTheme.MIDNIGHT_TRUFFLE)
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
