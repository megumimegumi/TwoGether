package com.example.fragments_of_life.ui.screens.mailbox

import androidx.compose.ui.graphics.Color

/** 信纸样式 */
enum class PaperStyle(val key: String, val label: String, val emoji: String) {
    CREAM("cream", "奶油信纸", "🍦"),
    SAKURA("sakura", "粉樱信纸", "🌸"),
    KRAFT("kraft", "牛皮信纸", "📜"),
    GARDEN("garden", "花草信纸", "🌿"),
    NIGHT("night", "星空信纸", "🌌"),
}

fun paperStyleOf(key: String): PaperStyle =
    PaperStyle.entries.firstOrNull { it.key == key } ?: PaperStyle.CREAM

/** 信纸底色渐变 */
fun paperGradient(key: String): List<Color> = when (key) {
    "cream" -> listOf(Color(0xFFFFFDF8), Color(0xFFFFF1E4))
    "sakura" -> listOf(Color(0xFFFFF2F6), Color(0xFFFFDDE7))
    "kraft" -> listOf(Color(0xFFF5E6C6), Color(0xFFEAD3A4))
    "garden" -> listOf(Color(0xFFF1F8EE), Color(0xFFDDEEDC))
    "night" -> listOf(Color(0xFF29224E), Color(0xFF3A3266))
    else -> listOf(Color(0xFFFFFDF8), Color(0xFFFFF1E4))
}

/** 信纸上的文字颜色 */
fun paperTextColor(key: String): Color =
    if (key == "night") Color(0xFFF2EBF5) else Color(0xFF4A3F44)

/** 信纸次要文字颜色 */
fun paperSubColor(key: String): Color =
    if (key == "night") Color(0xFFA79FB8) else Color(0xFF9A8F93)
