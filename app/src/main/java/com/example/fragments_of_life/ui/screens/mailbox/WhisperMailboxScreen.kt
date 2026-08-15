package com.example.fragments_of_life.ui.screens.mailbox

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fragments_of_life.data.model.WhisperLetter
import com.example.fragments_of_life.ui.components.EmptyHint
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun WhisperMailboxScreen(
    viewModel: LifeViewModel,
    onBack: () -> Unit,
) {
    val colors = LocalAppColors.current
    val letters by viewModel.allLetters.collectAsState()
    val today = remember { LocalDate.now() }

    var showWriter by remember { mutableStateOf(false) }
    var reading by remember { mutableStateOf<WhisperLetter?>(null) }
    var celebrating by remember { mutableStateOf<WhisperLetter?>(null) }
    var confirmDelete by remember { mutableStateOf<WhisperLetter?>(null) }
    var showLockedHint by remember { mutableStateOf(false) }

    // 排序:未解锁(倒计时) → 可拆 → 已拆
    val sorted = remember(letters, today) {
        letters.sortedWith(
            compareBy<WhisperLetter> {
                when {
                    it.opened -> 2
                    it.unlockDate > today -> 0
                    else -> 1
                }
            }.thenBy { it.unlockDate }
        )
    }
    val unopenedCount = remember(letters, today) { letters.count { !it.opened } }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // 顶部:木纹邮箱氛围头部
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF8C6240), Color(0xFFA97C54), Color(0xFFC29A6E))
                        )
                    )
                    .padding(vertical = 22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回", tint = Color(0xFFFFEBC9))
                    }
                    Spacer(Modifier.width(4.dp))
                    Text("📮", fontSize = 30.sp)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "悄悄话信箱",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            if (unopenedCount > 0) "$unopenedCount 封未拆开的信,安静地等你" else "写一封,让爱意晚一点到达",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.75f)
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.2f))
                            .clickable { showWriter = true }
                            .padding(horizontal = 14.dp, vertical = 9.dp),
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("写信", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            if (sorted.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    EmptyHint("📮", "信箱还空着", "写一封悄悄话,寄给未来的 TA 吧")
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    sorted.forEach { letter ->
                        LetterRow(
                            letter = letter,
                            today = today,
                            onClick = {
                                when {
                                    letter.opened -> reading = letter
                                    !letter.unlockDate.isAfter(today) -> celebrating = letter
                                    else -> showLockedHint = true
                                }
                            },
                            onLongClick = { confirmDelete = letter },
                        )
                    }
                    Spacer(Modifier.height(20.dp))
                }
            }
        }
    }

    // 写信页
    if (showWriter) {
        WriteLetterScreen(
            onSave = { letter ->
                viewModel.insertLetter(letter)
                showWriter = false
            },
            onCancel = { showWriter = false }
        )
    }

    // 火漆开封信动画 → 读信
    celebrating?.let { letter ->
        UnlockCeremony(
            onFinished = {
                celebrating = null
                reading = letter
            },
            onSkip = { celebrating = null }
        )
    }

    // 读信
    reading?.let { letter ->
        LetterReaderOverlay(
            letter = letter,
            onOpened = { viewModel.markLetterOpened(letter) },
            onClose = { reading = null }
        )
    }

    // 删除确认
    confirmDelete?.let { letter ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("删除这封信?", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text("删除后无法找回。", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLetter(letter)
                    confirmDelete = null
                }) { Text("删除", color = colors.softRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("取消", color = colors.textSecondary) }
            }
        )
    }

    // 未解锁提示弹窗
    val lockedLetter = sorted.firstOrNull { !it.opened && it.unlockDate.isAfter(today) }
    lockedLetter?.let { letter ->
        val days = ChronoUnit.DAYS.between(today, letter.unlockDate)
        if (showLockedHint) {
            AlertDialog(
                onDismissRequest = { showLockedHint = false },
                containerColor = colors.card,
                shape = RoundedCornerShape(24.dp),
                title = { Text("🕯️ 这封信还封着火漆", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
                text = {
                    Text(
                        "还要再等 ${days.coerceAtLeast(1)} 天才能打开哦。等待,也是爱意的一部分 💕",
                        color = colors.textSecondary
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showLockedHint = false }) {
                        Text("好的,我慢慢等", color = colors.rose, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    }
}

/** 信封行 */
@Composable
private fun LetterRow(
    letter: WhisperLetter,
    today: LocalDate,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val locked = !letter.opened && letter.unlockDate.isAfter(today)
    val openable = !letter.opened && !locked
    val daysLeft = ChronoUnit.DAYS.between(today, letter.unlockDate).coerceAtLeast(1)

    // 未解锁信封的微光呼吸
    val glowTransition = rememberInfiniteTransition(label = "glow")
    val glow by glowTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.65f,
        animationSpec = infiniteRepeatable(tween(1600, easing = EaseInOutCubic), RepeatMode.Reverse),
        label = "glowA"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickableCompat(onClick = onClick, onLongClick = onLongClick)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFF7E7C9),
                        Color(0xFFF3DFBA)
                    )
                )
            )
            .then(
                if (locked) Modifier.border(1.5.dp, Color(0xFFFF8BA7).copy(alpha = glow), RoundedCornerShape(20.dp))
                else Modifier.border(1.dp, Color(0xFFE0C9A0), RoundedCornerShape(20.dp))
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 信封图案
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (letter.opened) Color(0xFFEFE3C8) else Color(0xFFEAD7AF)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                when {
                    letter.opened -> "📖"
                    locked -> "✉️"
                    else -> "💌"
                },
                fontSize = 24.sp
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(Modifier.weight(1f)) {
            Text(
                if (letter.opened) "来自 ${letter.sign.ifBlank { "一个很爱你的人" }}" else "来自 ${letter.sign.ifBlank { "一位神秘的朋友" }}",
                style = MaterialTheme.typography.titleSmall,
                color = Color(0xFF4A3F44),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                when {
                    letter.opened -> letter.content.replace("\n", " ").take(24) + "…"
                    locked -> "信封用火漆封着,内容保密 🕯️"
                    else -> "火漆已经软化,今天可以拆开啦 ✨"
                },
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF9A8F93),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.width(8.dp))

        // 右侧状态
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            when {
                locked -> {
                    Text(
                        "🕯️",
                        fontSize = 16.sp,
                        modifier = Modifier.graphicsLayer { alpha = glow + 0.4f }
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "还有 ${daysLeft} 天",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFD4707A)
                    )
                }
                letter.opened -> Text(
                    "已拆开",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFB0A6A6)
                )
                else -> Text(
                    "拆信 →",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFF5C8A),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/** 拆信仪式:火漆融化 → 信纸抽出 */
@Composable
private fun UnlockCeremony(
    onFinished: () -> Unit,
    onSkip: () -> Unit,
) {
    // 按返回键跳过仪式,直接回到信箱
    BackHandler { onSkip() }

    var phase by remember { mutableIntStateOf(0) } // 0 封印发亮 1 融化 2 展开 3 抽信
    LaunchedEffect(Unit) {
        delay(900); phase = 1
        delay(900); phase = 2
        delay(800); phase = 3
        delay(1000); onFinished()
    }

    val sealScale by animateFloatAsState(if (phase >= 1) 0.15f else 1f, tween(900, easing = EaseInCubic), label = "sealS")
    val sealAlpha by animateFloatAsState(if (phase >= 1) 0f else 1f, tween(900), label = "sealA")
    val letterRise by animateFloatAsState(
        targetValue = if (phase >= 3) -46f else 26f,
        animationSpec = tween(900, easing = EaseOutCubic),
        label = "letter"
    )
    val shake by animateFloatAsState(
        targetValue = if (phase == 1) 1f else 0f,
        animationSpec = tween(500),
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier.size(width = 230.dp, height = 150.dp),
                contentAlignment = Alignment.Center
            ) {
                // 信纸(从信封中抽出)
                Box(
                    modifier = Modifier
                        .size(width = 190.dp, height = 110.dp)
                        .graphicsLayer { translationY = letterRise; rotationZ = if (phase >= 3) 0f else -3f }
                        .background(Color(0xFFFFFDF8), RoundedCornerShape(6.dp))
                        .padding(14.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        repeat(3) {
                            Box(
                                Modifier
                                    .fillMaxWidth(if (it == 2) 0.6f else 1f)
                                    .height(5.dp)
                                    .background(Color(0xFFE8DCC8), RoundedCornerShape(50))
                            )
                        }
                    }
                }
                // 信封主体
                Box(
                    modifier = Modifier
                        .size(width = 220.dp, height = 140.dp)
                        .graphicsLayer {
                            translationX = if (phase == 1) shake * 4f - 2f else 0f
                        }
                        .background(Color(0xFFEAD7AF), RoundedCornerShape(12.dp))
                )
                // 火漆封印
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .graphicsLayer { scaleX = sealScale; scaleY = sealScale; alpha = sealAlpha }
                        .background(Color(0xFFE0506A), CircleShape)
                        .border(3.dp, Color(0xFFC73B54), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("❤️", fontSize = 18.sp)
                }
            }

            Spacer(Modifier.height(20.dp))
            Text(
                when (phase) {
                    0 -> "火漆封印发着微光…"
                    1 -> "蜡封正在慢慢融化…"
                    2 -> "信封缓缓展开…"
                    else -> "信纸抽出,爱意抵达 💕"
                },
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/** 读信页 */
@Composable
private fun LetterReaderOverlay(
    letter: WhisperLetter,
    onOpened: () -> Unit,
    onClose: () -> Unit,
) {
    // 独立返回处理:一次返回 = 合上信,回到信箱
    BackHandler { onClose() }

    LaunchedEffect(Unit) { onOpened() }

    // 点击右上角可以切换信纸样式
    var displayStyle by remember(letter.paperStyle) { mutableStateOf(letter.paperStyle) }
    val style = paperStyleOf(displayStyle)
    val textColor = paperTextColor(displayStyle)
    val subColor = paperSubColor(displayStyle)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(paperGradient(displayStyle)))
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "合上信", tint = subColor)
            }
            Spacer(Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.06f))
                    .clickable {
                        val idx = PaperStyle.entries.indexOfFirst { it.key == displayStyle }
                        displayStyle = PaperStyle.entries[(idx + 1) % PaperStyle.entries.size].key
                    }
                    .padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${style.emoji} ${style.label}",
                    style = MaterialTheme.typography.labelSmall,
                    color = subColor
                )
                Spacer(Modifier.width(6.dp))
                Text("换一换 ↻", style = MaterialTheme.typography.labelSmall, color = subColor.copy(alpha = 0.7f))
            }
            Spacer(Modifier.width(12.dp))
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 30.dp, vertical = 10.dp)
        ) {
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { shown = true }
            val fade by animateFloatAsState(if (shown) 1f else 0f, tween(1200), label = "read")

            Column(modifier = Modifier.graphicsLayer { alpha = fade }) {
                Text("💌", fontSize = 34.sp)
                Spacer(Modifier.height(14.dp))
                Text(
                    letter.content,
                    fontSize = 16.sp,
                    lineHeight = 30.sp,
                    color = textColor
                )
                Spacer(Modifier.height(26.dp))
                Text(
                    "—— ${letter.sign.ifBlank { "一个很爱你的人" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = subColor,
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "解锁于 ${letter.unlockDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))} · 现在拆开,刚刚好",
                    style = MaterialTheme.typography.labelSmall,
                    color = subColor.copy(alpha = 0.8f),
                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(30.dp))
        }
    }
}

/** 兼容长按 + 点击 */
@SuppressLint("ModifierFactoryUnreferencedReceiver")
@OptIn(ExperimentalFoundationApi::class)
private fun Modifier.combinedClickableCompat(
    onClick: () -> Unit,
    onLongClick: () -> Unit,
): Modifier = this.then(combinedClickable(onClick = onClick, onLongClick = onLongClick))
