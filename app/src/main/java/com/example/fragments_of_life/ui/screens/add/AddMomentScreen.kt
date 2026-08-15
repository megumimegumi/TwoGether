package com.example.fragments_of_life.ui.screens.add

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.MomentType
import com.example.fragments_of_life.data.model.Mood
import com.example.fragments_of_life.ui.theme.FieldShape
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.theme.beautifulFieldColors
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle

/** 预置标签 */
private val presetTags = listOf("第一次", "旅行", "日常", "吵架和好", "小惊喜", "纪念日")

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddMomentScreen(
    editMoment: Moment? = null,
    initialDate: LocalDate? = null,
    initialType: MomentType? = null,
    autoPickImage: Boolean = false,
    onSave: (Moment) -> Unit = {},
    onCancel: () -> Unit = {},
) {
    val colors = LocalAppColors.current
    val isEditMode = editMoment != null

    var title by remember { mutableStateOf(editMoment?.title ?: "") }
    var content by remember { mutableStateOf(editMoment?.content ?: "") }
    var selectedType by remember {
        val t = editMoment?.type?.let { try { MomentType.valueOf(it) } catch (_: Exception) { null } }
        mutableStateOf(t ?: initialType ?: MomentType.DAILY)
    }
    var selectedMood by remember {
        val m = editMoment?.mood?.let { try { Mood.valueOf(it) } catch (_: Exception) { null } }
        mutableStateOf(m)
    }
    var location by remember { mutableStateOf(editMoment?.location ?: "") }
    var date by remember { mutableStateOf(editMoment?.date ?: initialDate ?: LocalDate.now()) }
    var imageUri by remember { mutableStateOf(editMoment?.imageUri) }
    var tags by remember { mutableStateOf(editMoment?.tags ?: emptyList()) }
    var customTag by remember { mutableStateOf("") }
    var showDatePicker by remember { mutableStateOf(false) }

    // 保存成功动画
    var saved by remember { mutableStateOf(false) }
    var pendingMoment by remember { mutableStateOf<Moment?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> if (uri != null) imageUri = uri.toString() }

    // 快速入口自动唤起选照片
    LaunchedEffect(Unit) {
        if (autoPickImage && imageUri == null) {
            imagePicker.launch("image/*")
        }
    }

    // 保存 → 播放成功动画 → 通知父级关闭
    LaunchedEffect(saved) {
        if (saved) {
            delay(1000)
            pendingMoment?.let(onSave)
        }
    }

    BackHandler(enabled = !saved) { onCancel() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .imePadding()
    ) {
        TopAppBar(
            title = {
                Text(
                    if (isEditMode) "✏️ 编辑碎片" else "✨ 记录此刻",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.textPrimary
                )
            },
            navigationIcon = {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "取消", tint = colors.textSecondary)
                }
            },
            actions = {
                TextButton(
                    onClick = {
                        if (title.isNotBlank() && !saved) {
                            pendingMoment = Moment(
                                id = editMoment?.id ?: 0,
                                type = selectedType.name,
                                title = title.trim(),
                                content = content.ifBlank { "值得记住的一天 ✨" },
                                date = date,
                                mood = selectedMood?.name,
                                location = location.ifBlank { null },
                                imageUri = imageUri,
                                tags = tags,
                                createdAt = editMoment?.createdAt ?: System.currentTimeMillis()
                            )
                            saved = true
                        }
                    },
                    enabled = title.isNotBlank() && !saved
                ) {
                    Text(
                        "保存 💾",
                        color = if (title.isNotBlank()) colors.rose else colors.textTertiary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        if (saved) {
            SavedSuccessMessage(Modifier.weight(1f))
            return@Column
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
        ) {
            // === 类型 ===
            SectionLabel("📂 类型")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MomentType.entries.forEach { type ->
                    val selected = selectedType == type
                    Text(
                        "${type.emoji} ${type.label}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) colors.rose else colors.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) colors.roseLight.copy(alpha = 0.6f) else colors.card)
                            .border(
                                1.dp,
                                if (selected) colors.rose.copy(alpha = 0.5f) else Color.Transparent,
                                RoundedCornerShape(50)
                            )
                            .clickable { selectedType = type }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // === 标题 ===
            SectionLabel("📝 标题")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { if (it.length <= 30) title = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("发生了什么美好的事?", color = colors.textTertiary) },
                colors = beautifulFieldColors(),
                shape = FieldShape,
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // === 照片 ===
            SectionLabel("📷 照片(可选)")
            Spacer(Modifier.height(8.dp))
            if (imageUri != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .clip(RoundedCornerShape(18.dp))
                ) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "照片预览",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = { imageUri = null },
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    ) {
                        Icon(Icons.Default.Close, "移除照片", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            } else {
                OutlinedButton(
                    onClick = { imagePicker.launch("image/*") },
                    modifier = Modifier.fillMaxWidth().height(96.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.textSecondary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, colors.taro.copy(alpha = 0.35f))
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(26.dp), tint = colors.textTertiary)
                        Spacer(Modifier.height(4.dp))
                        Text("添加照片", style = MaterialTheme.typography.labelMedium, color = colors.textTertiary)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // === 日期 ===
            SectionLabel("📅 日期")
            Spacer(Modifier.height(8.dp))
            DateSelector(date, showDatePicker,
                onOpenPicker = { showDatePicker = true },
                onConfirm = { newDate -> date = newDate; showDatePicker = false },
                onDismiss = { showDatePicker = false }
            )

            Spacer(Modifier.height(16.dp))

            // === 心情 ===
            SectionLabel("😊 心情")
            Spacer(Modifier.height(8.dp))
            MoodSelector(selectedMood) { mood -> selectedMood = mood }

            Spacer(Modifier.height(16.dp))

            // === 标签 ===
            SectionLabel("🏷️ 标签")
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                presetTags.forEach { tag ->
                    val selected = tags.contains(tag)
                    Text(
                        "#$tag",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else colors.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) colors.taro else colors.taroLight.copy(alpha = 0.5f))
                            .clickable {
                                tags = if (selected) tags - tag else tags + tag
                            }
                            .padding(horizontal = 12.dp, vertical = 7.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = customTag,
                    onValueChange = { if (it.length <= 10) customTag = it },
                    placeholder = { Text("自定义标签...", style = MaterialTheme.typography.bodySmall) },
                    modifier = Modifier.weight(1f),
                    shape = FieldShape,
                    colors = beautifulFieldColors(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(colors.taro)
                        .clickable {
                            val t = customTag.trim()
                            if (t.isNotBlank() && t !in tags) {
                                tags = tags + t
                                customTag = ""
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            // === 内容 ===
            SectionLabel("💬 详细记录")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().height(140.dp),
                placeholder = { Text("写下此刻的感受...", color = colors.textTertiary) },
                colors = beautifulFieldColors(),
                shape = FieldShape,
                maxLines = 10
            )

            Spacer(Modifier.height(16.dp))

            // === 地点 ===
            SectionLabel("📍 地点(可选)")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("在哪里发生的?", color = colors.textTertiary) },
                colors = beautifulFieldColors(),
                shape = FieldShape,
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.LocationOn, null, tint = colors.peach.copy(alpha = 0.7f))
                }
            )

            Spacer(Modifier.height(28.dp))
        }
    }
}

/** 保存成功:一颗爱心弹出来,像拍立得落地 */
@Composable
private fun SavedSuccessMessage(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.45f, stiffness = 220f),
        label = "success"
    )

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "💖",
                fontSize = 64.sp,
                modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale }
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "保存成功!",
                style = MaterialTheme.typography.headlineSmall,
                color = colors.rose,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "美好的一刻已飘进你们的时光里 💕",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    val colors = LocalAppColors.current
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = colors.textPrimary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun MoodSelector(selected: Mood?, onSelect: (Mood?) -> Unit) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Mood.entries.forEach { mood ->
            val isSelected = selected == mood
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (isSelected) moodColor(mood, colors).copy(alpha = 0.2f) else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) moodColor(mood, colors) else colors.textTertiary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onSelect(if (isSelected) null else mood) }
                    .padding(vertical = 7.dp),
            ) {
                Text(mood.emoji, fontSize = 17.sp)
                Text(
                    mood.label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) moodColor(mood, colors) else colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Visible
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DateSelector(
    date: LocalDate,
    showPicker: Boolean,
    onOpenPicker: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    if (showPicker) {
        val dps = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    dps.selectedDateMillis?.let {
                        onConfirm(Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate())
                    } ?: onDismiss()
                }) { Text("确定", color = colors.rose) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text("取消", color = colors.textSecondary) }
            }
        ) { DatePicker(state = dps) }
    } else {
        val dateStr = date.format(DateTimeFormatter.ofPattern("yyyy年 M月 d日"))
        val weekday = date.dayOfWeek.getDisplayName(TextStyle.FULL, java.util.Locale.CHINESE)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(colors.card)
                .border(1.dp, colors.taro.copy(alpha = 0.25f), RoundedCornerShape(16.dp))
                .clickable { onOpenPicker() }
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("📅", fontSize = 20.sp)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(dateStr, style = MaterialTheme.typography.bodyLarge, color = colors.textPrimary, fontWeight = FontWeight.Medium)
                Text(weekday, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
            }
            Icon(Icons.Default.EditCalendar, null, tint = colors.rose.copy(alpha = 0.5f))
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
