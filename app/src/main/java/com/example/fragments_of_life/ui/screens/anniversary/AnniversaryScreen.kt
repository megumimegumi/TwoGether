package com.example.fragments_of_life.ui.screens.anniversary

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fragments_of_life.data.model.CoupleInfo
import com.example.fragments_of_life.data.model.ImportantDate
import com.example.fragments_of_life.data.model.ImportantDateType
import com.example.fragments_of_life.data.model.Importance
import com.example.fragments_of_life.ui.components.*
import com.example.fragments_of_life.ui.theme.CardShape
import com.example.fragments_of_life.ui.theme.FieldShape
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.theme.beautifulFieldColors
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import com.example.fragments_of_life.util.UpcomingEvent
import com.example.fragments_of_life.util.buildUpcomingEvents
import com.example.fragments_of_life.util.isCoupleAnniversaryToday
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun AnniversaryScreen(
    viewModel: LifeViewModel,
    coupleInfo: CoupleInfo,
) {
    val colors = LocalAppColors.current
    val today = remember { LocalDate.now() }
    val importantDates by viewModel.allImportantDates.collectAsState()
    val events = remember(importantDates, today) { buildUpcomingEvents(importantDates, today) }
    val daysTogether = remember(coupleInfo) {
        ChronoUnit.DAYS.between(coupleInfo.anniversaryDate, today).toInt().coerceAtLeast(0)
    }

    // ── 当天庆祝 ──
    val hasTodayEvent = remember(events) { events.any { it.isToday } }
    val coupleAnniversaryToday = remember(coupleInfo, today) {
        isCoupleAnniversaryToday(coupleInfo.anniversaryDate, today)
    }
    var celebrateShown by remember { mutableStateOf(false) }
    LaunchedEffect(hasTodayEvent, coupleAnniversaryToday) {
        if (!celebrateShown && (hasTodayEvent || coupleAnniversaryToday)) {
            celebrateShown = true
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<ImportantDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .statusBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 96.dp)
        ) {
            // 在一起总天数大卡片
            TogetherDaysCard(coupleInfo, daysTogether, coupleAnniversaryToday)

            Spacer(Modifier.height(16.dp))

            SectionTitle(
                emoji = "🗓️",
                title = "纪念日倒计时",
                modifier = Modifier.padding(horizontal = 20.dp),
                trailing = {
                    TextButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, null, Modifier.size(16.dp), tint = colors.rose)
                        Spacer(Modifier.width(4.dp))
                        Text("添加", color = colors.rose, style = MaterialTheme.typography.labelMedium)
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            if (events.isEmpty()) {
                EmptyHint("💫", "还没有重要日子", "把属于你们的日子都记下来吧")
            } else {
                events.forEach { event ->
                    AnniversaryRow(
                        event = event,
                        onEdit = { editing = event.event },
                        onDelete = { viewModel.deleteImportantDate(event.event) },
                    )
                }
            }
        }
    }

    // 庆祝动画
    if (celebrateShown && (hasTodayEvent || coupleAnniversaryToday)) {
        val mainTitle = when {
            coupleAnniversaryToday -> "在一起 ${daysTogether} 天快乐!"
            else -> "${events.firstOrNull { it.isToday }?.event?.title ?: "今天"} 快乐!"
        }
        CelebrationOverlay(
            title = mainTitle,
            subtitle = "谢谢你们彼此陪伴,把这一天变成糖 🍬",
            onDismiss = { celebrateShown = false }
        )
    }

    if (showAddDialog) {
        AddImportantDateDialog(
            initial = null,
            onSave = { viewModel.insertImportantDate(it); showAddDialog = false },
            onDismiss = { showAddDialog = false }
        )
    }

    editing?.let { date ->
        AddImportantDateDialog(
            initial = date,
            onSave = { viewModel.updateImportantDate(it); editing = null },
            onDismiss = { editing = null }
        )
    }
}

/** 在一起总天数大卡片 */
@Composable
private fun TogetherDaysCard(info: CoupleInfo, days: Int, isToday: Boolean) {
    val colors = LocalAppColors.current
    val beat by rememberHeartbeatScale(min = 1f, max = 1.05f, durationMillis = 1600)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.gradientPeach, colors.gradientTaro, colors.gradientGold)
                )
            )
            .padding(vertical = 26.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(emoji = info.myEmoji, size = 46.dp)
                Text(
                    "💕",
                    fontSize = 20.sp,
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .graphicsLayer { scaleX = beat; scaleY = beat }
                )
                EmojiAvatar(emoji = info.partnerEmoji, size = 46.dp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                if (isToday) "今天是我们的纪念日 🎉" else "我们已经在一起",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onPeach.copy(alpha = 0.85f)
            )
            Spacer(Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                CountUpText(
                    target = days,
                    modifier = Modifier.graphicsLayer { scaleX = beat; scaleY = beat },
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                    ),
                    color = Color.White,
                )
                Text(
                    " 天",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "从 ${info.anniversaryDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))} 开始",
                style = MaterialTheme.typography.bodySmall,
                color = colors.onPeach.copy(alpha = 0.7f)
            )
        }
    }
}

/** 纪念日列表行 */
@Composable
private fun AnniversaryRow(
    event: UpcomingEvent,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalAppColors.current
    val d = event.event
    val type = try { ImportantDateType.valueOf(d.type) } catch (_: Exception) { ImportantDateType.CUSTOM }
    val imp = try { Importance.valueOf(d.importance) } catch (_: Exception) { Importance.NORMAL }
    val accent = when (imp) {
        Importance.VERY -> colors.rose
        Importance.IMPORTANT -> colors.peach
        Importance.NORMAL -> colors.taro
    }
    val isPast = event.daysLeft < 0

    var menuOpen by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }

    SoftCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 进度环
            RingProgress(
                progress = if (isPast) 1f else event.progress,
                modifier = Modifier.size(58.dp),
                stroke = 6.dp,
                trackColor = accent.copy(alpha = 0.15f),
                color = accent,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(type.emoji, fontSize = 16.sp)
                    Text(
                        if (event.isToday) "今天" else if (isPast) "已过" else "${event.daysLeft}天",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        d.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isPast) colors.textSecondary else colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f, fill = false),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(8.dp))
                    TagChip(
                        text = imp.label,
                        color = accent,
                        background = accent.copy(alpha = 0.14f)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    when {
                        isPast -> "${d.date.format(DateTimeFormatter.ofPattern("M月d日"))} · 已过去 ${-event.daysLeft} 天"
                        event.isToday -> "就是今天 🎉"
                        else -> "${event.next.format(DateTimeFormatter.ofPattern("M月d日"))} · 还有 ${event.daysLeft} 天"
                    } + if (d.repeatYearly) " · 每年" else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
                if (d.note.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        d.note,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // 更多菜单
            Box {
                IconButton(onClick = { menuOpen = true }) {
                    Icon(
                        Icons.Default.MoreVert, null,
                        tint = colors.textTertiary, modifier = Modifier.size(18.dp)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("✏️ 编辑") },
                        onClick = { menuOpen = false; onEdit() }
                    )
                    DropdownMenuItem(
                        text = { Text("🗑️ 删除", color = colors.softRed) },
                        onClick = { menuOpen = false; confirmDelete = true }
                    )
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("删除「${d.title}」?", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text("这个重要日子将被移除。", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { confirmDelete = false; onDelete() }) {
                    Text("删除", color = colors.softRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("取消", color = colors.textSecondary)
                }
            }
        )
    }
}

// ═══════════════════════════════════════════
// 添加 / 编辑重要日子
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddImportantDateDialog(
    initial: ImportantDate?,
    onSave: (ImportantDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var selectedType by remember {
        mutableStateOf(
            initial?.type?.let { try { ImportantDateType.valueOf(it) } catch (_: Exception) { null } }
                ?: ImportantDateType.ANNIVERSARY
        )
    }
    var date by remember { mutableStateOf(initial?.date ?: LocalDate.now()) }
    var repeatYearly by remember { mutableStateOf(initial?.repeatYearly ?: true) }
    var importance by remember {
        mutableStateOf(
            initial?.importance?.let { try { Importance.valueOf(it) } catch (_: Exception) { null } }
                ?: Importance.NORMAL
        )
    }
    var remindDays by remember { mutableStateOf(initial?.remindBeforeDays ?: 3) }
    var note by remember { mutableStateOf(initial?.note ?: "") }
    var showDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (initial == null) "📌 添加重要日子" else "✏️ 编辑重要日子",
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { if (it.length <= 20) title = it },
                    label = { Text("名称") },
                    placeholder = { Text("例如:第一次旅行") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    colors = beautifulFieldColors(),
                    singleLine = true
                )

                Text("类型", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ImportantDateType.entries.take(3).forEach { t ->
                        TypeChip(t, selectedType == t, colors) { selectedType = t }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ImportantDateType.entries.drop(3).take(4).forEach { t ->
                        TypeChip(t, selectedType == t, colors) { selectedType = t }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("日期") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = FieldShape,
                        trailingIcon = { Icon(Icons.Default.EditCalendar, null, tint = colors.rose) },
                        colors = beautifulFieldColors()
                    )
                    // 透明覆盖层:确保点击一定打开日期选择器(输入框会吞掉点击)
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                Text("重要等级", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Importance.entries.forEach { imp ->
                        val selected = importance == imp
                        val accent = when (imp) {
                            Importance.VERY -> colors.rose
                            Importance.IMPORTANT -> colors.peach
                            Importance.NORMAL -> colors.taro
                        }
                        Text(
                            imp.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) accent else accent.copy(alpha = 0.12f))
                                .clickable { importance = imp }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }

                Text("提前提醒", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(0 to "当天", 1 to "前1天", 3 to "前3天", 7 to "前一周").forEach { (days, label) ->
                        val selected = remindDays == days
                        Text(
                            label,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (selected) Color.White else colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) colors.gold else colors.goldLight.copy(alpha = 0.5f))
                                .clickable { remindDays = days }
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("每年重复", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                    Spacer(Modifier.weight(1f))
                    Switch(
                        checked = repeatYearly,
                        onCheckedChange = { repeatYearly = it },
                        colors = SwitchDefaults.colors(checkedTrackColor = colors.peach)
                    )
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注(可选)") },
                    placeholder = { Text("比如:准备小惊喜") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    colors = beautifulFieldColors()
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (title.isNotBlank()) {
                    onSave(
                        (initial ?: ImportantDate(title = title, date = date)).copy(
                            type = selectedType.name,
                            title = title,
                            date = date,
                            repeatYearly = repeatYearly,
                            remindBeforeDays = remindDays,
                            importance = importance.name,
                            note = note,
                        )
                    )
                }
            }) {
                Text("保存", color = colors.rose, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消", color = colors.textSecondary)
            }
        }
    )

    if (showDatePicker) {
        val dps = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dps.selectedDateMillis?.let {
                        date = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定", color = colors.rose) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消", color = colors.textSecondary) }
            }
        ) { DatePicker(state = dps) }
    }
}

@Composable
private fun TypeChip(
    type: ImportantDateType,
    selected: Boolean,
    colors: com.example.fragments_of_life.ui.theme.AppColors,
    onClick: () -> Unit,
) {
    Text(
        "${type.emoji} ${type.label}",
        style = MaterialTheme.typography.labelSmall,
        color = if (selected) colors.rose else colors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) colors.roseLight.copy(alpha = 0.6f) else colors.card)
            .clickable { onClick() }
            .padding(horizontal = 10.dp, vertical = 6.dp)
    )
}
