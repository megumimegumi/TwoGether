package com.example.fragments_of_life.ui.screens.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.MomentType
import com.example.fragments_of_life.data.model.Mood
import com.example.fragments_of_life.ui.components.SoftCard
import com.example.fragments_of_life.ui.components.TagChip
import com.example.fragments_of_life.ui.theme.LocalAppColors
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MomentDetailScreen(
    moment: Moment,
    onDelete: () -> Unit,
    onEdit: () -> Unit = {},
    onSaveToUniverse: (Moment) -> Unit = {},
    onClose: () -> Unit,
) {
    val colors = LocalAppColors.current
    val momentType = try { MomentType.valueOf(moment.type) } catch (_: Exception) { MomentType.OTHER }
    val mood = moment.mood?.let { try { Mood.valueOf(it) } catch (_: Exception) { null } }
    val dateStr = moment.date.format(DateTimeFormatter.ofPattern("yyyy年 M月 d日"))
    val weekDay = moment.date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.CHINESE)
    val typeColor = when (momentType) {
        MomentType.DATE -> colors.peach
        MomentType.MOVIE -> colors.taro
        MomentType.FOOD -> colors.gold
        MomentType.TRAVEL -> colors.softBlue
        MomentType.GIFT -> colors.rose
        MomentType.ANNIVERSARY -> colors.rose
        MomentType.FIGHT -> colors.softRed
        MomentType.MAKEUP -> colors.softGreen
        MomentType.DAILY -> colors.textSecondary
        MomentType.LETTER -> colors.taro
        MomentType.OTHER -> colors.textTertiary
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = !showDeleteConfirm) { onClose() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        TopAppBar(
            title = { Text("", style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, "关闭", tint = colors.textSecondary)
                }
            },
            actions = {
                IconButton(onClick = { onSaveToUniverse(moment) }) {
                    Icon(Icons.Default.Notes, "记入关于TA", tint = colors.taro.copy(alpha = 0.8f))
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "编辑", tint = colors.rose.copy(alpha = 0.7f))
                }
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(Icons.Default.Delete, "删除", tint = colors.softRed.copy(alpha = 0.7f))
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 照片
            if (!moment.imageUri.isNullOrBlank()) {
                AsyncImage(
                    model = moment.imageUri,
                    contentDescription = "照片",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // 类型标签
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                TagChip(
                    text = "${momentType.emoji} ${momentType.label}",
                    color = typeColor,
                    background = typeColor.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = moment.title,
                style = MaterialTheme.typography.headlineMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "$dateStr  $weekDay",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 记录内容
            SoftCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        "💬 记录",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = moment.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = colors.textPrimary,
                        lineHeight = 28.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 标签
            if (moment.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    moment.tags.forEach { tag ->
                        TagChip(
                            text = "#$tag",
                            color = colors.taro,
                            background = colors.taro.copy(alpha = 0.12f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // 心情 / 地点
            if (mood != null || !moment.location.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (mood != null) {
                        InfoCard(
                            emoji = mood.emoji,
                            label = "心情",
                            value = mood.label,
                            color = moodColor(mood, colors),
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (!moment.location.isNullOrBlank()) {
                        InfoCard(
                            emoji = "📍",
                            label = "地点",
                            value = moment.location!!,
                            color = colors.softBlue,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text("确定要删除吗?", fontWeight = FontWeight.SemiBold, color = colors.textPrimary)
            },
            text = {
                Text("这个美好的记忆将被永久删除,无法恢复。", color = colors.textSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete()
                }) {
                    Text("删除", color = colors.softRed, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消", color = colors.textSecondary)
                }
            }
        )
    }
}

@Composable
private fun InfoCard(
    emoji: String,
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    val colors = LocalAppColors.current
    SoftCard(modifier = modifier, containerColor = color.copy(alpha = 0.1f)) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
            Text(label, style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        }
    }
}

private fun moodColor(mood: Mood, colors: com.example.fragments_of_life.ui.theme.AppColors): Color = when (mood) {
    Mood.HAPPY -> colors.gold
    Mood.TOUCHED -> colors.softBlue
    Mood.ROMANTIC -> colors.rose
    Mood.SAD -> colors.taro
    Mood.ANGRY -> colors.softRed
    Mood.MISSING -> colors.taro
    Mood.GRATEFUL -> colors.softGreen
    Mood.EXCITED -> colors.peach
}
