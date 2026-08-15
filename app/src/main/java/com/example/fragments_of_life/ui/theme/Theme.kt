package com.example.fragments_of_life.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/** 全局取色的 CompositionLocal */
val LocalAppColors = staticCompositionLocalOf { LightAppColors }

private val LightColorScheme = lightColorScheme(
    primary = PeachPink,
    onPrimary = Color.White,
    primaryContainer = PeachLight,
    onPrimaryContainer = RoseDeep,
    secondary = TaroPurple,
    onSecondary = Color.White,
    secondaryContainer = TaroLight,
    onSecondaryContainer = Color(0xFF4A3F68),
    tertiary = ChampagneGold,
    onTertiary = TextPrimary,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = TextSecondary,
    error = SoftRed,
    onError = Color.White,
    background = CreamWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = CardCream,
    onSurfaceVariant = TextSecondary,
    outline = PeachLight,
    outlineVariant = TaroLight.copy(alpha = 0.6f)
)

private val DarkColorScheme = darkColorScheme(
    primary = PeachPink,
    onPrimary = Color(0xFF3B1F2B),
    primaryContainer = Color(0xFF4A3550),
    onPrimaryContainer = PeachLight,
    secondary = TaroPurple,
    onSecondary = Color(0xFF2A2140),
    secondaryContainer = Color(0xFF3E3560),
    onSecondaryContainer = TaroLight,
    tertiary = DarkGold,
    onTertiary = Color(0xFF3B2F1F),
    tertiaryContainer = Color(0xFF54483A),
    onTertiaryContainer = DarkGold,
    error = SoftRed,
    onError = Color(0xFF3B1F2B),
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkCard,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF4A3F68),
    outlineVariant = Color(0xFF3E3560)
)

@Composable
fun FragmentsOfLifeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val appColors = if (darkTheme) DarkAppColors else LightAppColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    CompositionLocalProvider(LocalAppColors provides appColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}
