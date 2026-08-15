package com.example.fragments_of_life.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * 统一输入框样式 —— 奶油手账风,圆角柔和
 */
@Composable
fun beautifulFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = LocalAppColors.current.peach,
    unfocusedBorderColor = LocalAppColors.current.taro.copy(alpha = 0.25f),
    focusedContainerColor = LocalAppColors.current.card,
    unfocusedContainerColor = LocalAppColors.current.card,
    cursorColor = LocalAppColors.current.rose,
    focusedLabelColor = LocalAppColors.current.rose,
    unfocusedLabelColor = LocalAppColors.current.textSecondary,
    focusedPlaceholderColor = LocalAppColors.current.textTertiary,
    unfocusedPlaceholderColor = LocalAppColors.current.textTertiary.copy(alpha = 0.6f),
    focusedTextColor = LocalAppColors.current.textPrimary,
    unfocusedTextColor = LocalAppColors.current.textPrimary,
)

/** 输入框圆角 */
val FieldShape = RoundedCornerShape(16.dp)

/** 卡片统一圆角 —— 设计稿要求 24-32px 的温柔圆角 */
val CardShape = RoundedCornerShape(24.dp)
val CardShapeSmall = RoundedCornerShape(16.dp)

/** 柔和阴影颜色(基于蜜桃色) */
val SoftShadowColor = Color(0x33FF9A8B)
