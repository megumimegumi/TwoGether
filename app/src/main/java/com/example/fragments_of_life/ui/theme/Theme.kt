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
val LocalAppColors = staticCompositionLocalOf { lightAppColors(themeOption(null).core) }

/** 主题切换控制器:由 MainActivity 注入,设置页用它换主题 */
val LocalThemeController = staticCompositionLocalOf<((String) -> Unit)?> { null }

@Composable
fun FragmentsOfLifeTheme(
    themeKey: String = "peach",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val core = themeOption(themeKey).core
    val appColors = if (darkTheme) darkAppColors(core) else lightAppColors(core)
    val colorScheme = if (darkTheme) darkColorSchemeFor(core) else lightColorSchemeFor(core)

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

private fun lightColorSchemeFor(core: ThemeCore) = lightColorScheme(
    primary = core.primary,
    onPrimary = core.onPrimary,
    primaryContainer = core.primaryLight,
    onPrimaryContainer = core.rose,
    secondary = core.secondary,
    onSecondary = core.onPrimary,
    secondaryContainer = core.secondaryLight,
    onSecondaryContainer = core.rose,
    tertiary = core.gold,
    onTertiary = TextPrimary,
    tertiaryContainer = core.goldLight,
    onTertiaryContainer = TextSecondary,
    error = SoftRed,
    onError = Color.White,
    background = CreamWhite,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = CardCream,
    onSurfaceVariant = TextSecondary,
    outline = core.primaryLight,
    outlineVariant = core.secondaryLight.copy(alpha = 0.6f)
)

private fun darkColorSchemeFor(core: ThemeCore) = darkColorScheme(
    primary = core.primary,
    onPrimary = core.onPrimary,
    primaryContainer = darken(core.primaryLight),
    onPrimaryContainer = core.primaryLight,
    secondary = core.secondary,
    onSecondary = core.onPrimary,
    secondaryContainer = darken(core.secondaryLight),
    onSecondaryContainer = core.secondaryLight,
    tertiary = core.gold,
    onTertiary = Color(0xFF3B2F1F),
    tertiaryContainer = darken(core.goldLight),
    onTertiaryContainer = core.gold,
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
