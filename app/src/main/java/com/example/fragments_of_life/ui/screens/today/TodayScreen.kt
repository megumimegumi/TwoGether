package com.example.fragments_of_life.ui.screens.today

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fragments_of_life.data.local.dailyQuote
import com.example.fragments_of_life.data.model.CoupleInfo
import com.example.fragments_of_life.data.model.Importance
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.PartnerNote
import com.example.fragments_of_life.ui.components.*
import com.example.fragments_of_life.ui.theme.CardShape
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import com.example.fragments_of_life.util.buildUpcomingEvents
import com.example.fragments_of_life.util.isCoupleAnniversaryToday
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** 快速记录入口类型 */
enum class QuickAction(val emoji: String, val label: String) {
    PHOTO("📷", "拍照"),
    DIARY("✍️", "日记"),
    MOOD("😊", "心情"),
    LETTER("💌", "情书"),
    LOCATION("📍", "地点"),
}

@Composable
fun TodayScreen(
    viewModel: LifeViewModel,
    coupleInfo: CoupleInfo,
    onQuickRecord: (QuickAction) -> Unit = {},
    onMomentClick: (Moment) -> Unit = {},
    onOpenUniverse: () -> Unit = {},
) {
    val colors = LocalAppColors.current
    val today = remember { LocalDate.now() }
    val daysTogether = remember(coupleInfo) {
        ChronoUnit.DAYS.between(coupleInfo.anniversaryDate, today).toInt().coerceAtLeast(0)
    }
    val quote = remember(today) { dailyQuote(today) }
    val importantDates by viewModel.allImportantDates.collectAsState()
    val onThisDay by viewModel.momentsOnThisDay.collectAsState()
    val partnerNotes by viewModel.allPartnerNotes.collectAsState()

    // 关于TA:每日一条,可换一条
    var shuffle by remember { mutableIntStateOf(0) }
    val dailyNote = remember(partnerNotes, shuffle) {
        if (partnerNotes.isEmpty()) null
        else partnerNotes[(today.dayOfYear + shuffle) % partnerNotes.size]
    }

    val upcoming = remember(importantDates, today) { buildUpcomingEvents(importantDates, today) }
    val nextEvent = remember(upcoming) { upcoming.firstOrNull { it.daysLeft >= 0 } }
    val anniversaryToday = remember(coupleInfo, today) {
        isCoupleAnniversaryToday(coupleInfo.anniversaryDate, today)
    }
    val lastYearMoment = remember(onThisDay) { onThisDay.firstOrNull() }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        FloatingHeartsBackground(count = 9)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                if (coupleInfo.hasPartner) {
                    CoupleDaysHeader(coupleInfo, daysTogether, anniversaryToday)
                } else {
                    SingleDaysHeader()
                }
            }

            item { DailyQuoteCard(quote) }

            item {
                NextAnniversaryCard(event = nextEvent, today = today)
            }

            // TA的小宇宙:每日一条(单身时隐藏)
            if (coupleInfo.hasPartner) {
                item {
                    PartnerNoteCard(
                        note = dailyNote,
                        onShuffle = { shuffle++ },
                        onOpenUniverse = onOpenUniverse,
                    )
                }
            }

            // 那年今日
            item {
                OnThisDayCard(moment = lastYearMoment, onMomentClick = onMomentClick)
            }

            // 快捷入口
            item { QuickEntryRow(onQuickRecord = onQuickRecord) }

            // 接下来的重要日子
            if (upcoming.isNotEmpty()) {
                item {
                    SectionTitle(
                        emoji = "🗓️",
                        title = "接下来",
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                }
                items(upcoming.filter { it.daysLeft >= 0 }.take(3)) { event ->
                    UpcomingMiniRow(event)
                }
            }
        }
    }
}

/** 顶部:单身状态 */
@Composable
private fun SingleDaysHeader() {
    val colors = LocalAppColors.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.gradientPeach.copy(alpha = 0.55f), colors.gradientTaro.copy(alpha = 0.45f))
                )
            )
            .padding(vertical = 26.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🥺", fontSize = 40.sp)
        Spacer(Modifier.height(10.dp))
        Text(
            "目前单身",
            style = MaterialTheme.typography.titleMedium,
            color = colors.textPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "等 TA 出现的那天,去「我们」页填写另一半的信息,开始记录两个人的时光吧 💕",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/** 顶部:情侣头像 + 在一起天数(心跳) */
@Composable
private fun CoupleDaysHeader(info: CoupleInfo, days: Int, anniversaryToday: Boolean) {
    val colors = LocalAppColors.current
    val beat by rememberHeartbeatScale(min = 1f, max = 1.05f, durationMillis = 1500)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 两个头像 + 中间爱心
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(emoji = info.myEmoji, size = 56.dp)
            Text(
                "💕",
                fontSize = 22.sp,
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .graphicsLayer { scaleX = beat; scaleY = beat }
            )
            EmojiAvatar(emoji = info.partnerEmoji, size = 56.dp)
        }

        Spacer(Modifier.height(14.dp))

        Text(
            if (anniversaryToday) "今天是我们的纪念日 🎉" else "我们已经在一起",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            CountUpText(
                target = days,
                modifier = Modifier.graphicsLayer { scaleX = beat; scaleY = beat },
                style = MaterialTheme.typography.displayMedium.copy(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                ),
                color = colors.rose,
            )
            Text(
                " 天",
                style = MaterialTheme.typography.titleLarge,
                color = colors.textPrimary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "从 ${info.anniversaryDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))} 开始",
            style = MaterialTheme.typography.bodySmall,
            color = colors.textTertiary
        )
    }
}

/** TA的小宇宙:每日一条随机备忘 */
@Composable
private fun PartnerNoteCard(
    note: PartnerNote?,
    onShuffle: () -> Unit,
    onOpenUniverse: () -> Unit,
) {
    val colors = LocalAppColors.current
    SoftCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        onClick = onOpenUniverse,
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🌌", fontSize = 20.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "TA的小宇宙",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onShuffle) {
                    Text("换一条 ↻", style = MaterialTheme.typography.labelSmall, color = colors.taro)
                }
            }
            Spacer(Modifier.height(8.dp))
            if (note == null) {
                Text(
                    "还没有关于TA的记录,去记下第一件小事吧 💕",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            } else {
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                TagChip(
                    text = note.category,
                    color = colors.taro,
                    background = colors.taro.copy(alpha = 0.12f)
                )
            }
        }
    }
}

/** 每日情话卡片 */
@Composable
private fun DailyQuoteCard(quote: String) {
    val colors = LocalAppColors.current
    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 18.dp)) {
            Text("💌", fontSize = 26.sp)
            Spacer(Modifier.width(14.dp))
            Column {
                Text(
                    "今日情话",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.rose,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "「$quote」",
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.textPrimary,
                    lineHeight = 26.sp
                )
            }
        }
    }
}

/** 下一个纪念日倒计时卡片(进度环) */
@Composable
private fun NextAnniversaryCard(
    event: com.example.fragments_of_life.util.UpcomingEvent?,
    today: LocalDate,
) {
    val colors = LocalAppColors.current
    if (event == null) {
        SoftCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            EmptyHint("🎂", "还没有添加重要日子", "去「纪念日」页添加一个吧")
        }
        return
    }

    val imp = try {
        Importance.valueOf(event.event.importance)
    } catch (_: Exception) {
        Importance.NORMAL
    }
    val accent = when (imp) {
        Importance.VERY -> colors.rose
        Importance.IMPORTANT -> colors.peach
        Importance.NORMAL -> colors.taro
    }
    val nextStr = event.next.format(DateTimeFormatter.ofPattern("M月d日"))
    val weekday = event.next.dayOfWeek.getDisplayName(
        java.time.format.TextStyle.FULL, java.util.Locale.CHINESE
    )

    SoftCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 进度环
            RingProgress(
                progress = if (event.isToday) 1f else event.progress,
                modifier = Modifier.size(92.dp),
                stroke = 9.dp,
                trackColor = accent.copy(alpha = 0.15f),
                color = accent,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        event.event.type.let { t ->
                            try {
                                com.example.fragments_of_life.data.model.ImportantDateType.valueOf(t).emoji
                            } catch (_: Exception) {
                                "📌"
                            }
                        },
                        fontSize = 22.sp
                    )
                    Text(
                        if (event.isToday) "今天!" else "${event.daysLeft}天",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (event.isToday) "就是今天 🎉" else "下一个重要日子",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    event.event.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$nextStr · $weekday",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                    TagChip(
                        text = imp.label,
                        color = accent,
                        background = accent.copy(alpha = 0.14f)
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    if (event.isToday) "就是今天,好好庆祝吧! 💕"
                    else "提前准备小惊喜,期待感满满呀",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textTertiary
                )
            }
        }
    }
}

/** 那年今日 */
@Composable
private fun OnThisDayCard(moment: Moment?, onMomentClick: (Moment) -> Unit) {
    val colors = LocalAppColors.current
    SoftCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        if (moment == null) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row {
                    Text("📖", fontSize = 20.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "那年今日",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "还没有往年的今天。坚持记录,明年的今天就有惊喜啦 ✨",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .clickable { onMomentClick(moment) }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 缩略图
                if (!moment.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = moment.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(colors.peachLight, colors.taroLight)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            moment.type.let { t ->
                                try {
                                    com.example.fragments_of_life.data.model.MomentType.valueOf(t).emoji
                                } catch (_: Exception) {
                                    "✨"
                                }
                            },
                            fontSize = 26.sp
                        )
                    }
                }

                Spacer(Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${moment.date.year}年的今天",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.gold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        moment.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        moment.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/** 快捷记录入口 */
@Composable
private fun QuickEntryRow(onQuickRecord: (QuickAction) -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickAction.entries.forEach { action ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.clickable { onQuickRecord(action) }
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(colors.peach.copy(alpha = 0.22f), colors.taro.copy(alpha = 0.22f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(action.emoji, fontSize = 22.sp)
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    action.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondary
                )
            }
        }
    }
}

/** 接下来小列表行 */
@Composable
private fun UpcomingMiniRow(event: com.example.fragments_of_life.util.UpcomingEvent) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(colors.card)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            event.event.type.let { t ->
                try {
                    com.example.fragments_of_life.data.model.ImportantDateType.valueOf(t).emoji
                } catch (_: Exception) {
                    "📌"
                }
            },
            fontSize = 18.sp
        )
        Spacer(Modifier.width(10.dp))
        Text(
            event.event.title,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            when {
                event.isToday -> "就是今天 🎉"
                event.daysLeft == 1L -> "明天"
                else -> "${event.daysLeft} 天后"
            },
            style = MaterialTheme.typography.labelMedium,
            color = colors.rose
        )
    }
}
