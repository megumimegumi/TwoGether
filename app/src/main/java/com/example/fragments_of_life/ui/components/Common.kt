package com.example.fragments_of_life.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fragments_of_life.ui.theme.CardShape
import com.example.fragments_of_life.ui.theme.LocalAppColors

/** 奶油软卡片:柔和大圆角 + 蜜桃色柔和阴影 */
@Composable
fun SoftCard(
    modifier: Modifier = Modifier,
    shape: Shape = CardShape,
    containerColor: Color = LocalAppColors.current.card,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val colors = LocalAppColors.current
    Card(
        modifier = modifier
            .shadow(
                elevation = 10.dp,
                shape = shape,
                ambientColor = colors.peach.copy(alpha = 0.12f),
                spotColor = colors.peach.copy(alpha = 0.12f)
            )
            .let { if (onClick != null) it.clickableCard(onClick) else it },
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = containerColor),
        content = content
    )
}

/** 圆形头像:渐变底 + emoji */
@Composable
fun EmojiAvatar(
    emoji: String,
    size: Dp = 56.dp,
    ringColor: Color = Color.White,
    ringWidth: Dp = 3.dp,
) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .size(size)
            .shadow(6.dp, CircleShape, ambientColor = colors.peach.copy(alpha = 0.3f))
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(colors.peachLight, colors.taroLight)
                )
            )
            .then(
                Modifier.padding(ringWidth)
                    .clip(CircleShape)
                    .background(ringColor)
                    .padding(2.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(emoji, fontSize = (size.value * 0.42f).sp)
    }
}

/** 小节标题 */
@Composable
fun SectionTitle(
    emoji: String,
    title: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    val colors = LocalAppColors.current
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(6.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.weight(1f))
        trailing?.invoke()
    }
}

/** 小圆角标签 */
@Composable
fun TagChip(
    text: String,
    color: Color,
    background: Color,
) {
    Text(
        text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        style = MaterialTheme.typography.labelSmall,
        color = color,
    )
}

/** 空状态提示 */
@Composable
fun EmptyHint(emoji: String, title: String, subtitle: String = "") {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(emoji, fontSize = 44.sp)
        Spacer(Modifier.height(10.dp))
        Text(title, style = MaterialTheme.typography.bodyLarge, color = colors.textSecondary)
        if (subtitle.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
        }
    }
}

private fun Modifier.clickableCard(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable(onClick = onClick))
