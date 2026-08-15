package com.example.fragments_of_life.ui.theme

import androidx.compose.ui.graphics.Color

// ──────────────────────────────────────────────
// 🌸 拾光 · 奶油手账配色
// 低饱和、温暖、柔软,像奶油一样
// ──────────────────────────────────────────────

// 浅色模式
val CreamWhite = Color(0xFFFFF9F7)      // 奶油白背景
val SurfaceWhite = Color(0xFFFFFFFF)    // 纯白表面
val CardCream = Color(0xFFFFFDFB)       // 卡片奶油色

val PeachPink = Color(0xFFFF9A8B)       // 蜜桃粉(主色)
val PeachLight = Color(0xFFFFE3DC)      // 浅蜜桃
val TaroPurple = Color(0xFFC3A6FF)      // 香芋紫(辅助)
val TaroLight = Color(0xFFEDE5FF)       // 浅香芋
val ChampagneGold = Color(0xFFF6C177)   // 香槟金(点缀)
val GoldLight = Color(0xFFFFEBC9)       // 浅香槟
val RoseDeep = Color(0xFFFF5C8A)        // 强调(倒计时/重要按钮)
val RoseLight = Color(0xFFFFD3E0)       // 浅玫瑰

val TextPrimary = Color(0xFF4A3F44)     // 深暖灰
val TextSecondary = Color(0xFF9A8F93)
val TextTertiary = Color(0xFFC9BEC2)

// 柔和功能色
val SoftGreen = Color(0xFFA9D6B5)
val SoftBlue = Color(0xFFB5D8EA)
val SoftYellow = Color(0xFFFFEDBE)
val SoftRed = Color(0xFFF5A3A3)

// 渐变
val GradientPeach = Color(0xFFFFC7B8)
val GradientTaro = Color(0xFFD9C9FF)
val GradientGold = Color(0xFFFFE0AE)

// 深色模式
val DarkBackground = Color(0xFF221C35)
val DarkSurface = Color(0xFF2E2745)
val DarkCard = Color(0xFF362E52)
val DarkGold = Color(0xFFF5C97B)
val DarkTextPrimary = Color(0xFFF2EBF5)
val DarkTextSecondary = Color(0xFFA79FB8)
val DarkTextTertiary = Color(0xFF6E6690)

/**
 * 主题化的应用色板 —— 由 FragmentsOfLifeTheme 根据浅/深色模式注入。
 * 所有界面统一从 LocalAppColors 取色,自动适配夜间模式。
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
    val onPeach: Color,   // 蜜桃/玫瑰色块上的文字色
)

val LightAppColors = AppColors(
    background = CreamWhite,
    surface = SurfaceWhite,
    card = CardCream,
    peach = PeachPink,
    peachLight = PeachLight,
    taro = TaroPurple,
    taroLight = TaroLight,
    gold = ChampagneGold,
    goldLight = GoldLight,
    rose = RoseDeep,
    roseLight = RoseLight,
    textPrimary = TextPrimary,
    textSecondary = TextSecondary,
    textTertiary = TextTertiary,
    softGreen = SoftGreen,
    softBlue = SoftBlue,
    softYellow = SoftYellow,
    softRed = SoftRed,
    gradientPeach = GradientPeach,
    gradientTaro = GradientTaro,
    gradientGold = GradientGold,
    onPeach = Color.White,
)

val DarkAppColors = AppColors(
    background = DarkBackground,
    surface = DarkSurface,
    card = DarkCard,
    peach = PeachPink,
    peachLight = Color(0xFF4A3550),
    taro = TaroPurple,
    taroLight = Color(0xFF3E3560),
    gold = DarkGold,
    goldLight = Color(0xFF4E4260),
    rose = RoseDeep,
    roseLight = Color(0xFF4A2E44),
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    softGreen = Color(0xFF3F5A4A),
    softBlue = Color(0xFF3A5362),
    softYellow = Color(0xFF54483A),
    softRed = Color(0xFF5A3B45),
    gradientPeach = Color(0xFF4A3550),
    gradientTaro = Color(0xFF3E3560),
    gradientGold = Color(0xFF54483A),
    onPeach = Color(0xFF3B1F2B),
)
