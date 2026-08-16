package com.example.fragments_of_life.ui.screens.us

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fragments_of_life.data.local.CouplePreferences
import com.example.fragments_of_life.data.model.CoupleInfo
import com.example.fragments_of_life.data.model.WishItem
import com.example.fragments_of_life.data.reminder.ReminderScheduler
import com.example.fragments_of_life.ui.components.*
import com.example.fragments_of_life.ui.theme.CardShape
import com.example.fragments_of_life.ui.theme.FieldShape
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.theme.LocalThemeController
import com.example.fragments_of_life.ui.theme.appThemeOptions
import com.example.fragments_of_life.ui.theme.beautifulFieldColors
import com.example.fragments_of_life.ui.theme.themeOption
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun UsScreen(
    viewModel: LifeViewModel,
    prefs: CouplePreferences,
    onOpenReview: () -> Unit = {},
    onOpenMailbox: () -> Unit = {},
    onOpenUniverse: () -> Unit = {},
    onCoupleInfoChanged: (CoupleInfo) -> Unit = {},
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    var coupleInfo by remember { mutableStateOf(prefs.loadCoupleInfo()) }
    val wishes by viewModel.allWishes.collectAsState()
    val letters by viewModel.allLetters.collectAsState()
    val partnerNotes by viewModel.allPartnerNotes.collectAsState()
    val unopenedLetters = remember(letters) { letters.count { !it.opened } }

    var showEditCouple by remember { mutableStateOf(false) }
    var showLockSetup by remember { mutableStateOf(false) }
    var resetStep by remember { mutableIntStateOf(0) }   // 0 隐藏;1-3 三步确认
    var showThemePicker by remember { mutableStateOf(false) }

    var remindersEnabled by remember { mutableStateOf(prefs.remindersEnabled) }
    var lockEnabled by remember { mutableStateOf(prefs.lockEnabled) }

    // 通知权限
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            remindersEnabled = true
            prefs.setRemindersEnabled(true)
            ReminderScheduler.schedule(context)
        }
    }

    BackHandler(enabled = showEditCouple || showLockSetup || showThemePicker || resetStep > 0) {
        showEditCouple = false; showLockSetup = false; showThemePicker = false; resetStep = 0
    }

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
            CoupleHeaderCard(coupleInfo)

            Spacer(Modifier.height(14.dp))

            // TA的小宇宙入口(顶部显眼卡片)
            SoftCard(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                onClick = onOpenUniverse,
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("🌌", fontSize = 26.sp)
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "TA的小宇宙",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "已记下 ${partnerNotes.size} 件小事 · 爱是记得你的每一句",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }
                    Icon(Icons.Default.ChevronRight, null, Modifier.size(18.dp), tint = colors.textTertiary)
                }
            }

            Spacer(Modifier.height(14.dp))

            // 情侣档案
            SoftCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionTitle(emoji = "💕", title = "情侣档案", trailing = {
                        TextButton(onClick = { showEditCouple = true }) {
                            Icon(Icons.Default.Edit, null, Modifier.size(15.dp), tint = colors.rose)
                            Spacer(Modifier.width(4.dp))
                            Text("编辑", color = colors.rose, style = MaterialTheme.typography.labelMedium)
                        }
                    })
                    Spacer(Modifier.height(10.dp))
                    InfoRow("👤", "我的名字", "${coupleInfo.myEmoji} ${coupleInfo.myName}")
                    InfoRow("💝", "TA的名字", "${coupleInfo.partnerEmoji} ${coupleInfo.partnerName}")
                    InfoRow("💍", "纪念日", coupleInfo.anniversaryDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日")))
                    InfoRow("📆", "已在一起", "${ChronoUnit.DAYS.between(coupleInfo.anniversaryDate, LocalDate.now()).toInt()} 天")
                    InfoRow("🩸", "生理周期", "${coupleInfo.partnerPeriodCycleDays} 天 · 持续 ${coupleInfo.partnerPeriodDuration} 天")
                }
            }

            Spacer(Modifier.height(14.dp))

            // 愿望清单
            SoftCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                WishListContent(
                    wishes = wishes,
                    onToggle = { viewModel.toggleWish(it) },
                    onAdd = { title, emoji -> viewModel.insertWish(WishItem(title = title, emoji = emoji)) },
                    onDelete = { viewModel.deleteWish(it) },
                )
            }

            Spacer(Modifier.height(14.dp))

            // 设置
            SoftCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionTitle(emoji = "⚙️", title = "设置")
                    Spacer(Modifier.height(10.dp))

                    // 提醒开关
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔔", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("纪念日提醒", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            Text("重要日子临近时温柔地提醒你", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                        }
                        Switch(
                            checked = remindersEnabled,
                            onCheckedChange = { want ->
                                if (want) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                                        != PackageManager.PERMISSION_GRANTED
                                    ) {
                                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        remindersEnabled = true
                                        prefs.setRemindersEnabled(true)
                                        ReminderScheduler.schedule(context)
                                    }
                                } else {
                                    remindersEnabled = false
                                    prefs.setRemindersEnabled(false)
                                    ReminderScheduler.cancel(context)
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.peach)
                        )
                    }

                    // 应用锁
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔒", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("应用锁", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            Text("打开 App 时需要用 4 位密码", style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
                        }
                        Switch(
                            checked = lockEnabled,
                            onCheckedChange = { want ->
                                if (want) {
                                    showLockSetup = true
                                } else {
                                    prefs.setLockEnabled(false)
                                    lockEnabled = false
                                }
                            },
                            colors = SwitchDefaults.colors(checkedTrackColor = colors.taro)
                        )
                    }

                    // 主题配色
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.peach.copy(alpha = 0.05f))
                            .clickable { showThemePicker = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎨", fontSize = 18.sp)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("主题配色", style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
                            Text(
                                "${themeOption(prefs.themeKey).emoji} ${themeOption(prefs.themeKey).name} · 跟随系统深浅色",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textTertiary
                            )
                        }
                        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = colors.textTertiary)
                    }

                    SettingRow("ℹ️", "版本", "拾光 v2.1")

                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = { resetStep = 1 },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(Icons.Default.DeleteForever, null, Modifier.size(15.dp), tint = colors.softRed)
                        Spacer(Modifier.width(4.dp))
                        Text("初始化数据", color = colors.softRed, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))

            // 更多
            SoftCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Column(modifier = Modifier.padding(18.dp)) {
                    SectionTitle(emoji = "✨", title = "更多")
                    Spacer(Modifier.height(10.dp))
                    MoreActionRow(
                        emoji = "🎞️",
                        title = "年度回顾",
                        desc = "把一年时光做成一部小电影",
                        onClick = onOpenReview,
                    )
                    MoreActionRow(
                        emoji = "📮",
                        title = "悄悄话信箱",
                        desc = if (unopenedLetters > 0) "$unopenedLetters 封未拆开的信" else "让爱意晚一点到达",
                        badge = unopenedLetters,
                        onClick = onOpenMailbox,
                    )
                    ComingSoonRow("💬", "默契问答", "每天一问,看看你们多默契")
                }
            }
        }
    }

    if (showEditCouple) {
        EditCoupleDialog(
            info = coupleInfo,
            onSave = { info ->
                prefs.saveCoupleInfo(info)
                coupleInfo = info
                onCoupleInfoChanged(info)
                showEditCouple = false
            },
            onDismiss = { showEditCouple = false }
        )
    }

    if (showLockSetup) {
        PinSetupDialog(
            onSuccess = {
                prefs.setLockEnabled(true)
                lockEnabled = true
                showLockSetup = false
            },
            onDismiss = { showLockSetup = false }
        )
    }

    // ── 初始化数据:三步确认,防止误触 ──
    if (resetStep > 0) {
        val setTheme = LocalThemeController.current
        AlertDialog(
            onDismissRequest = { resetStep = 0 },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = {
                Text(
                    when (resetStep) {
                        1 -> "⚠️ 确认清除数据?(1/3)"
                        2 -> "⚠️ 真的要删除吗?(2/3)"
                        else -> "🚨 最后一次确认(3/3)"
                    },
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    when (resetStep) {
                        1 -> "这将清除你们的所有记录:碎片、纪念日、愿望清单、信件、关于TA的备忘,以及情侣档案等设置。清除后不会再自动填充演示数据。"
                        2 -> "所有数据将被永久删除、无法恢复。请再次确认你不是误触。"
                        else -> "最后一次提醒:此操作不可撤销!确认要删除全部数据吗?"
                    },
                    color = colors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (resetStep < 3) {
                        resetStep++
                    } else {
                        // 彻底清空用户数据,不重新填充演示数据
                        viewModel.clearAllData()
                        prefs.clearAll()
                        prefs.setDemoSeedDisabled(true)
                        coupleInfo = prefs.loadCoupleInfo()
                        onCoupleInfoChanged(coupleInfo)
                        lockEnabled = false
                        remindersEnabled = prefs.remindersEnabled
                        // 主题偏好已被清空,同步恢复默认主题
                        setTheme?.invoke("peach")
                        resetStep = 0
                    }
                }) {
                    Text(
                        if (resetStep < 3) "继续 →" else "确定删除",
                        color = colors.softRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { resetStep = 0 }) { Text("取消", color = colors.textSecondary) }
            }
        )
    }

    // ── 主题选择 ──
    if (showThemePicker) {
        val setTheme = LocalThemeController.current
        AlertDialog(
            onDismissRequest = { showThemePicker = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("🎨 选择主题", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    appThemeOptions.forEach { opt ->
                        val selected = prefs.themeKey == opt.key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) colors.peach.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable {
                                    setTheme?.invoke(opt.key)
                                    showThemePicker = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 主题色板
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf(opt.core.primary, opt.core.secondary, opt.core.gold).forEach { c ->
                                    Box(
                                        Modifier
                                            .size(20.dp)
                                            .background(c, CircleShape)
                                    )
                                }
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                "${opt.emoji} ${opt.name}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            if (selected) {
                                Text("✓ 使用中", style = MaterialTheme.typography.labelSmall, color = colors.rose)
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showThemePicker = false }) { Text("关闭", color = colors.textSecondary) }
            }
        )
    }
}

/** 情侣头部卡片 */
@Composable
private fun CoupleHeaderCard(info: CoupleInfo) {
    val colors = LocalAppColors.current
    val beat by rememberHeartbeatScale(min = 1f, max = 1.06f, durationMillis = 1500)
    val days = ChronoUnit.DAYS.between(info.anniversaryDate, LocalDate.now()).toInt().coerceAtLeast(0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(
                Brush.linearGradient(
                    listOf(colors.gradientTaro, colors.gradientPeach)
                )
            )
            .padding(vertical = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                EmojiAvatar(emoji = info.myEmoji, size = 54.dp)
                Text(
                    "❤️",
                    fontSize = 22.sp,
                    modifier = Modifier
                        .padding(horizontal = 14.dp)
                        .graphicsLayer { scaleX = beat; scaleY = beat }
                )
                EmojiAvatar(emoji = info.partnerEmoji, size = 54.dp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                "${info.myName} ❤️ ${info.partnerName}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "从 ${info.anniversaryDate.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))} 开始 · 已 ${days} 天",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun InfoRow(emoji: String, label: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.peach.copy(alpha = 0.05f))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 15.sp)
        Spacer(Modifier.width(10.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SettingRow(emoji: String, title: String, value: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
    }
}

@Composable
private fun ComingSoonRow(emoji: String, title: String, desc: String) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.taro.copy(alpha = 0.06f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
        }
        TagChip("敬请期待", colors.taro, colors.taro.copy(alpha = 0.12f))
    }
}

/** 可点击的入口行 */
@Composable
private fun MoreActionRow(
    emoji: String,
    title: String,
    desc: String,
    badge: Int = 0,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.peach.copy(alpha = 0.06f))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(emoji, fontSize = 18.sp)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            Text(desc, style = MaterialTheme.typography.labelSmall, color = colors.textTertiary)
        }
        if (badge > 0) {
            TagChip("$badge", colors.rose, colors.roseLight.copy(alpha = 0.5f))
            Spacer(Modifier.width(6.dp))
        }
        Icon(Icons.Default.ChevronRight, null, Modifier.size(16.dp), tint = colors.textTertiary)
    }
}

// ═══════════════════════════════════════════
// 愿望清单
// ═══════════════════════════════════════════
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WishListContent(
    wishes: List<WishItem>,
    onToggle: (WishItem) -> Unit,
    onAdd: (String, String) -> Unit,
    onDelete: (WishItem) -> Unit,
) {
    val colors = LocalAppColors.current
    val doneCount = wishes.count { it.done }
    var newTitle by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf<WishItem?>(null) }

    Column(modifier = Modifier.padding(18.dp)) {
        SectionTitle(emoji = "🌠", title = "愿望清单")

        Spacer(Modifier.height(12.dp))

        // 进度
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                "一起做的小事",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            Spacer(Modifier.weight(1f))
            Text(
                "$doneCount / ${wishes.size}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.gold
            )
        }
        Spacer(Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { if (wishes.isEmpty()) 0f else doneCount.toFloat() / wishes.size },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50)),
            color = colors.gold,
            trackColor = colors.goldLight.copy(alpha = 0.5f),
        )

        Spacer(Modifier.height(12.dp))

        if (wishes.isEmpty()) {
            EmptyHint("⭐", "还没有愿望", "把想一起做的事写下来吧")
        } else {
            wishes.forEach { wish ->
                WishRow(
                    wish = wish,
                    onToggle = { onToggle(wish) },
                    onDelete = { confirmDelete = wish }
                )
            }
        }

        // 添加
        Spacer(Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = newTitle,
                onValueChange = { if (it.length <= 20) newTitle = it },
                placeholder = { Text("再许一个愿...", style = MaterialTheme.typography.bodySmall) },
                modifier = Modifier.weight(1f),
                shape = FieldShape,
                colors = beautifulFieldColors(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(colors.peach)
                    .clickable {
                        if (newTitle.isNotBlank()) {
                            onAdd(newTitle.trim(), "⭐")
                            newTitle = ""
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(20.dp))
            }
        }
    }

    confirmDelete?.let { wish ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("删除这个愿望?", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text("「${wish.title}」", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = { onDelete(wish); confirmDelete = null }) {
                    Text("删除", color = colors.softRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) {
                    Text("取消", color = colors.textSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WishRow(
    wish: WishItem,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    val colors = LocalAppColors.current
    // 完成时星星弹跳 + 金色
    val starScale by animateFloatAsState(
        targetValue = if (wish.done) 1f else 0.75f,
        animationSpec = spring(dampingRatio = 0.35f, stiffness = 380f),
        label = "star"
    )
    val titleColor by animateColorAsState(
        targetValue = if (wish.done) colors.textTertiary else colors.textPrimary,
        animationSpec = tween(400),
        label = "titleColor"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onToggle, onLongClick = onDelete)
            .padding(vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .graphicsLayer { scaleX = starScale; scaleY = starScale },
            contentAlignment = Alignment.Center
        ) {
            Text(
                if (wish.done) "⭐" else "✩",
                fontSize = if (wish.done) 20.sp else 18.sp,
                color = if (wish.done) colors.gold else colors.textTertiary
            )
        }
        Spacer(Modifier.width(10.dp))
        Text(
            "${wish.emoji} ${wish.title}",
            style = MaterialTheme.typography.bodyMedium,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (wish.done) {
            Text("已完成", style = MaterialTheme.typography.labelSmall, color = colors.gold)
        }
    }
}

// ═══════════════════════════════════════════
// 编辑情侣信息
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCoupleDialog(
    info: CoupleInfo,
    onSave: (CoupleInfo) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    var myName by remember { mutableStateOf(info.myName) }
    var partnerName by remember { mutableStateOf(info.partnerName) }
    var myEmoji by remember { mutableStateOf(info.myEmoji) }
    var partnerEmoji by remember { mutableStateOf(info.partnerEmoji) }
    var anniversaryDate by remember { mutableStateOf(info.anniversaryDate) }
    var periodCycle by remember { mutableStateOf(info.partnerPeriodCycleDays.toString()) }
    var periodDuration by remember { mutableStateOf(info.partnerPeriodDuration.toString()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val emojiOptions = listOf("🐰", "🐻", "🐱", "🐶", "🦊", "🐼", "🐨", "🐷", "🦁", "🐸")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = RoundedCornerShape(24.dp),
        title = { Text("💕 编辑情侣档案", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = myName, onValueChange = { myName = it },
                    label = { Text("我的名字") },
                    modifier = Modifier.fillMaxWidth(), shape = FieldShape, colors = beautifulFieldColors(),
                    singleLine = true
                )
                EmojiPickerRow("我的头像", myEmoji, emojiOptions) { myEmoji = it }

                OutlinedTextField(
                    value = partnerName, onValueChange = { partnerName = it },
                    label = { Text("TA的名字") },
                    modifier = Modifier.fillMaxWidth(), shape = FieldShape, colors = beautifulFieldColors(),
                    singleLine = true
                )
                EmojiPickerRow("TA的头像", partnerEmoji, emojiOptions) { partnerEmoji = it }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = anniversaryDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("在一起纪念日") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = FieldShape,
                        trailingIcon = { Icon(Icons.Default.EditCalendar, null, tint = colors.rose) },
                        colors = beautifulFieldColors()
                    )
                    // 透明覆盖层:确保点击一定打开日期选择器
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = periodCycle,
                        onValueChange = { periodCycle = it.filter { c -> c.isDigit() } },
                        label = { Text("生理周期(天)") },
                        modifier = Modifier.weight(1f), shape = FieldShape, colors = beautifulFieldColors()
                    )
                    OutlinedTextField(
                        value = periodDuration,
                        onValueChange = { periodDuration = it.filter { c -> c.isDigit() } },
                        label = { Text("持续天数") },
                        modifier = Modifier.weight(1f), shape = FieldShape, colors = beautifulFieldColors()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(
                    info.copy(
                        myName = myName.ifBlank { info.myName },
                        partnerName = partnerName.ifBlank { info.partnerName },
                        myEmoji = myEmoji,
                        partnerEmoji = partnerEmoji,
                        anniversaryDate = anniversaryDate,
                        partnerPeriodCycleDays = periodCycle.toIntOrNull() ?: info.partnerPeriodCycleDays,
                        partnerPeriodDuration = periodDuration.toIntOrNull() ?: info.partnerPeriodDuration
                    )
                )
            }) { Text("保存", color = colors.rose, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = colors.textSecondary) }
        }
    )

    if (showDatePicker) {
        val dps = rememberDatePickerState(
            initialSelectedDateMillis = anniversaryDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dps.selectedDateMillis?.let {
                        anniversaryDate = Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
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
private fun EmojiPickerRow(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit) {
    val colors = LocalAppColors.current
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            options.forEach { e ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (selected == e) colors.peachLight else colors.taroLight.copy(alpha = 0.4f))
                        .clickable { onSelect(e) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(e, fontSize = 18.sp)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// PIN 应用锁设置
// ═══════════════════════════════════════════
@Composable
private fun PinSetupDialog(
    onSuccess: () -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val prefs = CouplePreferences.getInstance(LocalContext.current)
    var first by remember { mutableStateOf("") }
    var second by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (first.isEmpty()) "设置 4 位密码" else "再输一次确认",
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(4) { i ->
                        val filled = if (first.length < 4) first.length > i else second.length > i
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(if (filled) colors.peach else colors.taroLight.copy(alpha = 0.5f))
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
                PinPad(
                    onDigit = { d ->
                        if (first.length < 4) {
                            first = (first + d).take(4)
                        } else if (second.length < 4) {
                            second = (second + d).take(4)
                            if (second.length == 4) {
                                if (first == second) {
                                    prefs.savePin(first)
                                    onSuccess()
                                } else {
                                    error = "两次输入不一致,重新设置"
                                    first = ""
                                    second = ""
                                }
                            }
                        }
                    },
                    onBackspace = {
                        if (first.length < 4) first = first.dropLast(1)
                        else second = second.dropLast(1)
                    }
                )
                error?.let {
                    Spacer(Modifier.height(8.dp))
                    Text(it, style = MaterialTheme.typography.labelSmall, color = colors.softRed)
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = colors.textSecondary) }
        }
    )
}

@Composable
private fun PinPad(onDigit: (Int) -> Unit, onBackspace: () -> Unit) {
    val colors = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (row in listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                row.forEach { d ->
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(colors.taroLight.copy(alpha = 0.4f))
                            .clickable { onDigit(d) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$d", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(52.dp))
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.peachLight.copy(alpha = 0.6f))
                    .clickable { onDigit(0) },
                contentAlignment = Alignment.Center
            ) {
                Text("0", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            }
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(colors.card)
                    .clickable { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Backspace, null, tint = colors.textSecondary, modifier = Modifier.size(20.dp))
            }
        }
    }
}
