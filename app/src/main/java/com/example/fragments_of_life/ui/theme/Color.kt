package com.example.fragments_of_life.ui.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────
// 🌸 拾光 · 多主题配色系统
// 中性色固定,品牌色由 ThemeCore 注入,浅/深模式自动适配
// ──────────────────────────────────────────────

// 浅色中性
val CreamWhite = Color(0xFFFFF9F7)      // 奶油白背景
val SurfaceWhite = Color(0xFFFFFFFF)    // 纯白表面
val CardCream = Color(0xFFFFFDFB)       // 卡片奶油色
val TextPrimary = Color(0xFF4A3F44)     // 深暖灰
val TextSecondary = Color(0xFF9A8F93)
val TextTertiary = Color(0xFFC9BEC2)

// 功能色(柔和)
val SoftGreen = Color(0xFFA9D6B5)
val SoftBlue = Color(0xFFB5D8EA)
val SoftYellow = Color(0xFFFFEDBE)
val SoftRed = Color(0xFFF5A3A3)

// 深色中性
val DarkBackground = Color(0xFF221C35)
val DarkSurface = Color(0xFF2E2745)
val DarkCard = Color(0xFF362E52)
val DarkTextPrimary = Color(0xFFF2EBF5)
val DarkTextSecondary = Color(0xFFA79FB8)
val DarkTextTertiary = Color(0xFF6E6690)

/** 一套主题的品牌色 */
data class ThemeCore(
    val primary: Color,
    val primaryLight: Color,
    val secondary: Color,
    val secondaryLight: Color,
    val gold: Color,
    val goldLight: Color,
    val rose: Color,
    val roseLight: Color,
    val gradientStart: Color,
    val gradientMid: Color,
    val gradientEnd: Color,
    val onPrimary: Color,
)

/** 内置主题 */
data class AppThemeOption(
    val key: String,
    val name: String,
    val emoji: String,
    val core: ThemeCore,
)

val appThemeOptions = listOf(
    AppThemeOption("peach", "奶油蜜桃", "🍑", ThemeCore(
        primary = Color(0xFFFF9A8B), primaryLight = Color(0xFFFFE3DC),
        secondary = Color(0xFFC3A6FF), secondaryLight = Color(0xFFEDE5FF),
        gold = Color(0xFFF6C177), goldLight = Color(0xFFFFEBC9),
        rose = Color(0xFFFF5C8A), roseLight = Color(0xFFFFD3E0),
        gradientStart = Color(0xFFFFC7B8), gradientMid = Color(0xFFD9C9FF), gradientEnd = Color(0xFFFFE0AE),
        onPrimary = Color.White,
    )),
    AppThemeOption("lavender", "香芋梦境", "🪻", ThemeCore(
        primary = Color(0xFFB9A6F5), primaryLight = Color(0xFFE6DFFB),
        secondary = Color(0xFFF2A7C3), secondaryLight = Color(0xFFFBE1EB),
        gold = Color(0xFFF0C48A), goldLight = Color(0xFFFBEBD3),
        rose = Color(0xFFD97BA6), roseLight = Color(0xFFF8D7E6),
        gradientStart = Color(0xFFD9CCFB), gradientMid = Color(0xFFF2C4DC), gradientEnd = Color(0xFFF8E0BE),
        onPrimary = Color.White,
    )),
    AppThemeOption("matcha", "抹茶奶绿", "🍵", ThemeCore(
        primary = Color(0xFF9FC088), primaryLight = Color(0xFFE1EFD6),
        secondary = Color(0xFFB8B5D8), secondaryLight = Color(0xFFE8E6F5),
        gold = Color(0xFFE8C97A), goldLight = Color(0xFFF9EECB),
        rose = Color(0xFFE8848C), roseLight = Color(0xFFF9D9DC),
        gradientStart = Color(0xFFCBE4BC), gradientMid = Color(0xFFDCD8F0), gradientEnd = Color(0xFFF7E4B8),
        onPrimary = Color(0xFF2F3D28),
    )),
    AppThemeOption("ocean", "雾蓝海岸", "🌊", ThemeCore(
        primary = Color(0xFF9DB8D2), primaryLight = Color(0xFFDCEAF2),
        secondary = Color(0xFFB5A8E8), secondaryLight = Color(0xFFE9E5F9),
        gold = Color(0xFFE8C78A), goldLight = Color(0xFFF9EED4),
        rose = Color(0xFFD97B8E), roseLight = Color(0xFFF9DCE2),
        gradientStart = Color(0xFFC9DEEE), gradientMid = Color(0xFFD7CDF4), gradientEnd = Color(0xFFF6E0BA),
        onPrimary = Color(0xFF23303C),
    )),
    AppThemeOption("caramel", "落日奶咖", "🌇", ThemeCore(
        primary = Color(0xFFE8A87C), primaryLight = Color(0xFFF7E2CC),
        secondary = Color(0xFFCBA8B8), secondaryLight = Color(0xFFF3E2E9),
        gold = Color(0xFFE8C48A), goldLight = Color(0xFFF9EED5),
        rose = Color(0xFFC97B6B), roseLight = Color(0xFFF6DCD4),
        gradientStart = Color(0xFFF2C9A4), gradientMid = Color(0xFFE8CFC8), gradientEnd = Color(0xFFF6E0B4),
        onPrimary = Color(0xFF3C2A20),
    )),
)

fun themeOption(key: String?): AppThemeOption =
    appThemeOptions.firstOrNull { it.key == key } ?: appThemeOptions.first()

/**
 * 主题化应用色板 —— 由 FragmentsOfLifeTheme 根据所选主题与浅/深模式注入。
 */
data class AppColors(
    val background: Color,
    val surface: Color,
    val card: Color,
    val peach: Color,
    val peachLight: Color,
    val taro: Color,
    val taroLight: Color,
    val gold: Color,
    val goldLight: Color,
    val rose: Color,
    val roseLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val softGreen: Color,
    val softBlue: Color,
    val softYellow: Color,
    val softRed: Color,
    val gradientPeach: Color,
    val gradientTaro: Color,
    val gradientGold: Color,
    val onPeach: Color,
)

/** 把颜色向深色背景压暗,用于生成深色模式的浅色变体 */
internal fun darken(c: Color): Color {
    fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
    val bg = DarkBackground
    return Color(
        red = lerp(c.red, bg.red, 0.6f),
        green = lerp(c.green, bg.green, 0.6f),
        blue = lerp(c.blue, bg.blue, 0.6f),
        alpha = 1f,
    )
}

fun lightAppColors(core: ThemeCore) = AppColors(
    background = CreamWhite,
    surface = SurfaceWhite,
    card = CardCream,
    peach = core.primary,
    peachLight = core.primaryLight,
    taro = core.secondary,
    taroLight = core.secondaryLight,
    gold = core.gold,
    goldLight = core.goldLight,
    rose = core.rose,
    roseLight = core.roseLight,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    softGreen = SoftGreen,
    softBlue = SoftBlue,
    softYellow = SoftYellow,
    softRed = SoftRed,
    gradientPeach = core.gradientStart,
    gradientTaro = core.gradientMid,
    gradientGold = core.gradientEnd,
    onPeach = core.onPrimary,
)

fun darkAppColors(core: ThemeCore) = AppColors(
    background = DarkBackground,
    surface = DarkSurface,
    card = DarkCard,
    peach = core.primary,
    peachLight = darken(core.primaryLight),
    taro = core.secondary,
    taroLight = darken(core.secondaryLight),
    gold = core.gold,
    goldLight = darken(core.goldLight),
    rose = core.rose,
    roseLight = darken(core.roseLight),
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    softGreen = Color(0xFF3F5A4A),
    softBlue = Color(0xFF3A5362),
    softYellow = Color(0xFF54483A),
    softRed = Color(0xFF5A3B45),
    gradientPeach = darken(core.gradientStart),
    gradientTaro = darken(core.gradientMid),
    gradientGold = darken(core.gradientEnd),
    onPeach = core.onPrimary,
)
