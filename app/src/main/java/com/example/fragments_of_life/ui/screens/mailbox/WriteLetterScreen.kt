package com.example.fragments_of_life.ui.screens.mailbox

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fragments_of_life.data.model.WhisperLetter
import com.example.fragments_of_life.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 解锁时间预设 */
private data class UnlockPreset(val label: String, val days: Int?, val compute: (LocalDate) -> LocalDate)

private val unlockPresets = listOf(
    UnlockPreset("明天早上", null) { it.plusDays(1) },
    UnlockPreset("3天后", null) { it.plusDays(3) },
    UnlockPreset("一周后", null) { it.plusDays(7) },
    UnlockPreset("一个月后", null) { it.plusMonths(1) },
    UnlockPreset("明年今天", null) { it.plusYears(1) },
    UnlockPreset("自定义", null) { it.plusDays(3) },
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WriteLetterScreen(
    onSave: (WhisperLetter) -> Unit,
    onCancel: () -> Unit,
) {
    val colors = LocalAppColors.current
    var content by remember { mutableStateOf("") }
    var style by remember { mutableStateOf(PaperStyle.CREAM) }
    var sign by remember { mutableStateOf("") }
    var anonymous by remember { mutableStateOf(false) }
    var presetIndex by remember { mutableIntStateOf(0) }
    var unlockDate by remember { mutableStateOf(LocalDate.now().plusDays(1)) }
    var showDatePicker by remember { mutableStateOf(false) }
    var saved by remember { mutableStateOf(false) }

    BackHandler { onCancel() }

    LaunchedEffect(saved) {
        if (saved) {
            delay(700)
            onSave(
                WhisperLetter(
                    content = content.trim(),
                    paperStyle = style.key,
                    sign = if (anonymous) "一个很爱你的人" else sign.trim(),
                    unlockDate = unlockDate,
                )
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(paperGradient(style.key)))
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, "取消", tint = paperSubColor(style.key))
                }
                Spacer(Modifier.width(4.dp))
                Text(
                    "写一封悄悄话",
                    style = MaterialTheme.typography.titleMedium,
                    color = paperTextColor(style.key),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(
                    onClick = { if (content.isNotBlank() && !saved) saved = true },
                    enabled = content.isNotBlank() && !saved
                ) {
                    Text(
                        "封蜡寄出 🕯️",
                        color = if (content.isNotBlank()) Color(0xFFE0506A) else Color(0xFFB0A6A6),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (saved) {
                // 寄出动画
                var shown by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) { shown = true }
                val scale by androidx.compose.animation.core.animateFloatAsState(
                    targetValue = if (shown) 1f else 0f,
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = 0.45f, stiffness = 220f
                    ),
                    label = "sent"
                )
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("💌", fontSize = 64.sp, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
                        Spacer(Modifier.height(14.dp))
                        Text("信已封蜡,静待开启", color = paperTextColor(style.key), fontWeight = FontWeight.SemiBold)
                    }
                }
                return@Column
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
            ) {
                // 信纸选择(可横向滚动,防止格子被挤压变形)
                Text("信纸", style = MaterialTheme.typography.labelMedium, color = paperSubColor(style.key))
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaperStyle.entries.forEach { p ->
                        val selected = style == p
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (selected) Color.White.copy(alpha = 0.85f) else Color.Transparent
                                )
                                .border(
                                    if (selected) 2.dp else 1.dp,
                                    if (selected) Color(0xFFE0506A) else Color(0xFFB0A6A6).copy(alpha = 0.4f),
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { style = p }
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Brush.verticalGradient(paperGradient(p.key))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(p.emoji, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(p.label, style = MaterialTheme.typography.labelSmall, color = paperSubColor(style.key))
                        }
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 正文
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color.White.copy(alpha = 0.55f))
                        .border(1.dp, Color(0xFFE0C9A0).copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    androidx.compose.foundation.text.BasicTextField(
                        value = content,
                        onValueChange = { content = it },
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = paperTextColor(style.key),
                            lineHeight = 28.sp
                        ),
                        cursorBrush = androidx.compose.ui.graphics.SolidColor(Color(0xFFE0506A)),
                        decorationBox = { inner ->
                            if (content.isEmpty()) {
                                Text(
                                    "想对 TA 说些什么?\n那些不好意思当面说的话,就写在这里吧…",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = paperSubColor(style.key).copy(alpha = 0.6f),
                                    lineHeight = 28.sp
                                )
                            }
                            inner()
                        }
                    )
                }

                Spacer(Modifier.height(18.dp))

                // 署名
                Text("署名", style = MaterialTheme.typography.labelMedium, color = paperSubColor(style.key))
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = sign,
                        onValueChange = { if (it.length <= 12) sign = it },
                        placeholder = { Text("你的名字,或不留名") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        enabled = !anonymous,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFE0506A),
                            unfocusedBorderColor = Color(0xFFB0A6A6).copy(alpha = 0.4f),
                            focusedTextColor = paperTextColor(style.key),
                            unfocusedTextColor = paperTextColor(style.key),
                            disabledTextColor = paperSubColor(style.key),
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Switch(
                            checked = anonymous,
                            onCheckedChange = { anonymous = it },
                            colors = SwitchDefaults.colors(checkedTrackColor = Color(0xFFE0506A))
                        )
                        Text("匿名", style = MaterialTheme.typography.labelSmall, color = paperSubColor(style.key))
                    }
                }

                Spacer(Modifier.height(18.dp))

                // 解锁时间
                Text(
                    "TA 什么时候能打开?",
                    style = MaterialTheme.typography.labelMedium,
                    color = paperSubColor(style.key)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    unlockPresets.forEachIndexed { i, preset ->
                        val selected = presetIndex == i
                        Text(
                            preset.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Color.White else paperSubColor(style.key),
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) Color(0xFFE0506A) else Color.White.copy(alpha = 0.55f))
                                .clickable {
                                    presetIndex = i
                                    if (preset.label != "自定义") {
                                        unlockDate = preset.compute(LocalDate.now())
                                    } else {
                                        showDatePicker = true
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    "解锁于 ${unlockDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))} · 到那天,爱意准时抵达 💌",
                    style = MaterialTheme.typography.labelSmall,
                    color = paperSubColor(style.key)
                )

                Spacer(Modifier.height(30.dp))
            }
        }
    }

    if (showDatePicker) {
        val dps = rememberDatePickerState(
            initialSelectedDateMillis = unlockDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dps.selectedDateMillis?.let {
                        unlockDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) { Text("确定", color = Color(0xFFE0506A)) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消", color = Color(0xFF9A8F93)) }
            }
        ) { DatePicker(state = dps) }
    }
}
