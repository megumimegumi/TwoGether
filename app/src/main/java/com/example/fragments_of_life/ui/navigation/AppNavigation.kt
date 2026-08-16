package com.example.fragments_of_life.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.fragments_of_life.data.local.CouplePreferences
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.MomentType
import com.example.fragments_of_life.ui.screens.add.AddMomentScreen
import com.example.fragments_of_life.ui.screens.anniversary.AnniversaryScreen
import com.example.fragments_of_life.ui.screens.detail.MomentDetailScreen
import com.example.fragments_of_life.ui.screens.mailbox.WhisperMailboxScreen
import com.example.fragments_of_life.ui.screens.moments.MomentsScreen
import com.example.fragments_of_life.ui.screens.review.YearReviewScreen
import com.example.fragments_of_life.ui.screens.today.QuickAction
import com.example.fragments_of_life.ui.screens.today.TodayScreen
import com.example.fragments_of_life.ui.screens.universe.PartnerUniverseScreen
import com.example.fragments_of_life.ui.screens.universe.QuickNoteDialog
import com.example.fragments_of_life.ui.screens.universe.defaultNoteCategories
import com.example.fragments_of_life.ui.screens.us.UsScreen
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import java.time.LocalDate

private data class BottomTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
)

private val bottomTabs = listOf(
    BottomTab("today", "今天", Icons.Filled.WbSunny, Icons.Outlined.WbSunny),
    BottomTab("moments", "点滴", Icons.Filled.Timeline, Icons.Outlined.Timeline),
    BottomTab("anniversaries", "纪念日", Icons.Filled.Cake, Icons.Outlined.Cake),
    BottomTab("us", "我们", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
)

/** 中央 + 号弹出的快捷记录选项 */
private enum class QuickOption(val emoji: String, val label: String, val desc: String) {
    WRITE("✍️", "写一段话", "记下此刻的小事"),
    PHOTO("📷", "拍一张", "选张照片配文字"),
    LETTER("💌", "写封情书", "把心里话说给TA"),
    MOOD("😊", "记心情", "开心 · 想念 · 期待"),
    LOCATION("📍", "记地点", "去过的地方都值得"),
    ABOUT_TA("📝", "关于TA", "记一件TA的小事"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation() {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val viewModel: LifeViewModel = viewModel()
    val prefs = remember { CouplePreferences.getInstance(context) }
    // 情侣信息做成可变状态:在「我们」页编辑或初始化数据后,今天/纪念日/回顾等页面同步更新
    var coupleInfo by remember { mutableStateOf(prefs.loadCoupleInfo()) }

    var currentTab by remember { mutableStateOf("today") }

    // 弹层状态
    var showQuickSheet by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editMoment by remember { mutableStateOf<Moment?>(null) }
    var addForDate by remember { mutableStateOf<LocalDate?>(null) }
    var addType by remember { mutableStateOf<MomentType?>(null) }
    var autoPickImage by remember { mutableStateOf(false) }
    var selectedMoment by remember { mutableStateOf<Moment?>(null) }
    var showReview by remember { mutableStateOf(false) }
    var showMailbox by remember { mutableStateOf(false) }
    var showUniverse by remember { mutableStateOf(false) }
    var showQuickNote by remember { mutableStateOf(false) }
    var quickNoteInitialContent by remember { mutableStateOf("") }
    var quickNoteInitialCategory by remember { mutableStateOf<String?>(null) }
    var quickNoteLinkedMoment by remember { mutableStateOf<Long?>(null) }

    val hasOverlay = showQuickSheet || showAddDialog || selectedMoment != null ||
            showReview || showMailbox || showUniverse || showQuickNote

    BackHandler(enabled = hasOverlay) {
        showQuickSheet = false
        showAddDialog = false
        editMoment = null
        addForDate = null
        addType = null
        autoPickImage = false
        selectedMoment = null
        showReview = false
        showMailbox = false
        showUniverse = false
        showQuickNote = false
        quickNoteInitialContent = ""
        quickNoteInitialCategory = null
        quickNoteLinkedMoment = null
    }

    fun openRecord(
        type: MomentType? = null,
        date: LocalDate? = null,
        pickImage: Boolean = false,
        editing: Moment? = null,
    ) {
        showQuickSheet = false
        editMoment = editing
        addForDate = date
        addType = type
        autoPickImage = pickImage
        showAddDialog = true
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentTab) {
            "today" -> TodayScreen(
                viewModel = viewModel,
                coupleInfo = coupleInfo,
                onQuickRecord = { action ->
                    when (action) {
                        QuickAction.PHOTO -> openRecord(pickImage = true)
                        QuickAction.DIARY -> openRecord(type = MomentType.DAILY)
                        QuickAction.MOOD -> openRecord(type = MomentType.DAILY)
                        QuickAction.LETTER -> openRecord(type = MomentType.LETTER)
                        QuickAction.LOCATION -> openRecord(type = MomentType.TRAVEL)
                    }
                },
                onMomentClick = { selectedMoment = it },
                onOpenUniverse = { showUniverse = true },
            )
            "moments" -> MomentsScreen(
                viewModel = viewModel,
                onAddForDate = { openRecord(date = it) },
                onMomentClick = { selectedMoment = it },
            )
            "anniversaries" -> AnniversaryScreen(
                viewModel = viewModel,
                coupleInfo = coupleInfo,
            )
            "us" -> UsScreen(
                viewModel = viewModel,
                prefs = prefs,
                onOpenReview = { showReview = true },
                onOpenMailbox = { showMailbox = true },
                onOpenUniverse = { showUniverse = true },
                onCoupleInfoChanged = { coupleInfo = it },
            )
        }

        // ── 底部导航(4 Tab + 中央悬浮 +) ──
        if (!hasOverlay) {
            BottomBar(
                currentTab = currentTab,
                onTabClick = { currentTab = it },
                onAddClick = { showQuickSheet = true },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // ── 快捷记录半屏面板 ──
    if (showQuickSheet) {
        ModalBottomSheet(
            onDismissRequest = { showQuickSheet = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    Modifier
                        .padding(top = 12.dp, bottom = 4.dp)
                        .size(width = 40.dp, height = 5.dp)
                        .clip(RoundedCornerShape(50))
                        .background(colors.textTertiary.copy(alpha = 0.6f))
                )
            }
        ) {
            Column(modifier = Modifier.padding(bottom = 28.dp)) {
                Text(
                    "记下这一刻 💕",
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "支持选日期、加标签、记心情",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textTertiary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(Modifier.height(16.dp))

                QuickOption.entries.forEach { option ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (option) {
                                    QuickOption.WRITE -> openRecord(type = MomentType.DAILY)
                                    QuickOption.PHOTO -> openRecord(pickImage = true)
                                    QuickOption.LETTER -> openRecord(type = MomentType.LETTER)
                                    QuickOption.MOOD -> openRecord(type = MomentType.DAILY)
                                    QuickOption.LOCATION -> openRecord(type = MomentType.TRAVEL)
                                    QuickOption.ABOUT_TA -> {
                                        showQuickSheet = false
                                        quickNoteInitialContent = ""
                                        quickNoteInitialCategory = null
                                        quickNoteLinkedMoment = null
                                        showQuickNote = true
                                    }
                                }
                            }
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(colors.peachLight.copy(alpha = 0.8f), colors.taroLight.copy(alpha = 0.8f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(option.emoji, fontSize = 20.sp)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                option.label,
                                style = MaterialTheme.typography.bodyLarge,
                                color = colors.textPrimary,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                option.desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textTertiary
                            )
                        }
                    }
                }
            }
        }
    }

    // ── 记录编辑全屏 ──
    if (showAddDialog) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            AddMomentScreen(
                editMoment = editMoment,
                initialDate = addForDate,
                initialType = addType,
                autoPickImage = autoPickImage,
                onSave = { moment ->
                    if (editMoment != null) viewModel.updateMoment(moment)
                    else viewModel.insertMoment(moment)
                    showAddDialog = false
                    editMoment = null
                    addForDate = null
                    addType = null
                    autoPickImage = false
                },
                onCancel = {
                    showAddDialog = false
                    editMoment = null
                    addForDate = null
                    addType = null
                    autoPickImage = false
                }
            )
        }
    }

    // ── 详情弹层 ──
    selectedMoment?.let { moment ->
        MomentDetailScreen(
            moment = moment,
            onDelete = {
                viewModel.deleteMoment(moment)
                selectedMoment = null
            },
            onEdit = {
                val current = selectedMoment
                selectedMoment = null
                openRecord(editing = current)
            },
            onSaveToUniverse = { m ->
                selectedMoment = null
                quickNoteInitialContent = ""
                quickNoteInitialCategory = null
                quickNoteLinkedMoment = m.id
                showQuickNote = true
            },
            onClose = { selectedMoment = null }
        )
    }

    // ── TA的小宇宙 ──
    if (showUniverse) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            PartnerUniverseScreen(
                viewModel = viewModel,
                coupleInfo = coupleInfo,
                onBack = { showUniverse = false },
            )
        }
    }

    // ── 关于TA快速记录 ──
    if (showQuickNote) {
        QuickNoteDialog(
            initialContent = quickNoteInitialContent,
            initialCategory = quickNoteInitialCategory,
            linkedMomentId = quickNoteLinkedMoment,
            categories = defaultNoteCategories + prefs.getCustomNoteCategories(),
            onSave = { note ->
                viewModel.insertPartnerNote(note)
                showQuickNote = false
                quickNoteInitialContent = ""
                quickNoteInitialCategory = null
                quickNoteLinkedMoment = null
            },
            onDismiss = {
                showQuickNote = false
                quickNoteInitialContent = ""
                quickNoteInitialCategory = null
                quickNoteLinkedMoment = null
            }
        )
    }

    // ── 年度回顾 ──
    if (showReview) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            YearReviewScreen(
                viewModel = viewModel,
                coupleInfo = coupleInfo,
                onClose = { showReview = false },
            )
        }
    }

    // ── 悄悄话信箱 ──
    if (showMailbox) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colors.background
        ) {
            WhisperMailboxScreen(
                viewModel = viewModel,
                onBack = { showMailbox = false },
            )
        }
    }
}

/** 底部导航栏:两个 Tab + 中央悬浮按钮 + 两个 Tab */
@Composable
private fun BottomBar(
    currentTab: String,
    onTabClick: (String) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(18.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)),
        color = colors.card,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(80.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TabButton(
                tab = bottomTabs[0],
                selected = currentTab == bottomTabs[0].route,
                onClick = { onTabClick(bottomTabs[0].route) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                tab = bottomTabs[1],
                selected = currentTab == bottomTabs[1].route,
                onClick = { onTabClick(bottomTabs[1].route) },
                modifier = Modifier.weight(1f)
            )

            // 中央悬浮 +
            Box(
                modifier = Modifier
                    .weight(1.1f)
                    .fillMaxHeight(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        // 中性小阴影,不再使用彩色光晕(光晕会溢出到上方内容,视觉上破坏圆形)
                        .shadow(6.dp, CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(colors.peach, colors.rose)
                            ),
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable { onAddClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "记一笔",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            TabButton(
                tab = bottomTabs[2],
                selected = currentTab == bottomTabs[2].route,
                onClick = { onTabClick(bottomTabs[2].route) },
                modifier = Modifier.weight(1f)
            )
            TabButton(
                tab = bottomTabs[3],
                selected = currentTab == bottomTabs[3].route,
                onClick = { onTabClick(bottomTabs[3].route) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TabButton(
    tab: BottomTab,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = LocalAppColors.current
    val tint by androidx.compose.animation.animateColorAsState(
        targetValue = if (selected) colors.rose else colors.textTertiary,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "tabTint"
    )
    Column(
        modifier = modifier
            .fillMaxHeight()
            .clickable { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (selected) tab.selectedIcon else tab.unselectedIcon,
            contentDescription = tab.label,
            tint = tint,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.height(3.dp))
        Text(
            tab.label,
            style = MaterialTheme.typography.labelSmall,
            color = tint,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
