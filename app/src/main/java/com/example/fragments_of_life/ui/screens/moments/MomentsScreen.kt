package com.example.fragments_of_life.ui.screens.moments

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Circle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fragments_of_life.data.model.ImportantDateType
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.MomentType
import com.example.fragments_of_life.data.model.Mood
import com.example.fragments_of_life.ui.components.DreamyBackground
import com.example.fragments_of_life.ui.components.EmptyHint
import com.example.fragments_of_life.ui.components.SoftCard
import com.example.fragments_of_life.ui.components.TagChip
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import com.example.fragments_of_life.ui.viewmodel.YearMonth as VmYearMonth
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import kotlinx.coroutines.flow.distinctUntilChanged

enum class MomentsView(val label: String) {
    TIMELINE("时间轴"), WALL("照片墙"), CALENDAR("日历"),
}

enum class MomentFilter(val label: String) {
    ALL("全部"), PHOTO("📷 照片"), TEXT("✍️ 文字"), PLACE("📍 地点"),
}

@Composable
fun MomentsScreen(
    viewModel: LifeViewModel,
    onAddForDate: (LocalDate) -> Unit = {},
    onMomentClick: (Moment) -> Unit = {},
) {
    val colors = LocalAppColors.current
    val allMoments by viewModel.allMoments.collectAsState()

    var currentView by remember { mutableStateOf(MomentsView.TIMELINE) }
    var filter by remember { mutableStateOf(MomentFilter.ALL) }
    var selectedYear by remember { mutableStateOf<Int?>(null) }

    val sortedMoments = remember(allMoments) { allMoments.sortedByDescending { it.date } }
    val years = remember(sortedMoments) {
        sortedMoments.map { it.date.year }.distinct().sortedDescending()
    }

    val filtered = remember(sortedMoments, filter, selectedYear) {
        sortedMoments.filter { m ->
            val passFilter = when (filter) {
                MomentFilter.ALL -> true
                MomentFilter.PHOTO -> !m.imageUri.isNullOrBlank()
                MomentFilter.TEXT -> m.imageUri.isNullOrBlank()
                MomentFilter.PLACE -> !m.location.isNullOrBlank()
            }
            val passYear = selectedYear == null || m.date.year == selectedYear
            passFilter && passYear
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        // 温馨梦幻背景:光斑漂移 + 爱心漂浮
        DreamyBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
        // 标题
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "点滴",
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${allMoments.size} 个珍贵片段",
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.textSecondary
                )
            }
            Spacer(Modifier.weight(1f))
            // 年份选择
            YearSelector(
                years = years,
                selectedYear = selectedYear,
                onSelect = { selectedYear = it }
            )
        }

        // 视图切换
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
        ) {
            MomentsView.entries.forEachIndexed { index, view ->
                SegmentedButton(
                    selected = currentView == view,
                    onClick = { currentView = view },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = MomentsView.entries.size
                    ),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = colors.peach.copy(alpha = 0.22f),
                        activeContentColor = colors.rose,
                        inactiveContainerColor = colors.card,
                        inactiveContentColor = colors.textSecondary,
                    )
                ) {
                    Text(view.label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // 筛选
        if (currentView != MomentsView.CALENDAR) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MomentFilter.entries.forEach { f ->
                    val selected = filter == f
                    Text(
                        f.label,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (selected) Color.White else colors.textSecondary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) colors.peach else colors.card)
                            .clickable { filter = f }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        when (currentView) {
            MomentsView.TIMELINE -> TimelineView(
                moments = filtered,
                onMomentClick = onMomentClick,
            )
            MomentsView.WALL -> PhotoWall(
                moments = filtered.filter { !it.imageUri.isNullOrBlank() },
                onMomentClick = onMomentClick,
            )
            MomentsView.CALENDAR -> CalendarView(
                viewModel = viewModel,
                onAddForDate = onAddForDate,
                onMomentClick = onMomentClick,
            )
        }
        }
    }
}

@Composable
private fun YearSelector(years: List<Int>, selectedYear: Int?, onSelect: (Int?) -> Unit) {
    val colors = LocalAppColors.current
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.card)
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                selectedYear?.let { "${it}年" } ?: "年份",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textSecondary
            )
            Spacer(Modifier.width(4.dp))
            Text("▾", fontSize = 10.sp, color = colors.textTertiary)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("全部年份") },
                onClick = { onSelect(null); expanded = false }
            )
            years.forEach { y ->
                DropdownMenuItem(
                    text = { Text("${y}年") },
                    onClick = { onSelect(y); expanded = false }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// 时间轴视图
// ═══════════════════════════════════════════

private sealed interface TimelineEntry {
    data class MonthHeader(val yearMonth: Pair<Int, Int>, val cover: Moment?) : TimelineEntry
    data class MomentItem(val moment: Moment) : TimelineEntry
}

@Composable
private fun TimelineView(
    moments: List<Moment>,
    onMomentClick: (Moment) -> Unit,
) {
    val colors = LocalAppColors.current

    val entries = remember(moments) {
        buildList {
            val grouped = moments.groupBy { it.date.year to it.date.monthValue }
                .toSortedMap(compareByDescending<Pair<Int, Int>> { it.first }.thenByDescending { it.second })
            grouped.forEach { (ym, list) ->
                val cover = list.firstOrNull { !it.imageUri.isNullOrBlank() } ?: list.firstOrNull()
                add(TimelineEntry.MonthHeader(ym, cover))
                list.forEach { add(TimelineEntry.MomentItem(it)) }
            }
        }
    }

    if (moments.isEmpty()) {
        EmptyHint("🍂", "还没有记录", "点中间的 + 记下第一笔吧")
        return
    }

    val listState = rememberLazyListState()

    // ── 滚动揭示:进入视口的卡片依次淡入 ──
    var revealed by remember { mutableIntStateOf(0) }
    LaunchedEffect(listState) {
        snapshotFlow {
            listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
        }.distinctUntilChanged().collect { last ->
            if (last + 2 > revealed) revealed = last + 2
        }
    }

    // ── 视口中心活跃卡片 ──
    val activeIndex by remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val center = info.viewportStartOffset + info.viewportSize.height / 2
            var best = -1
            var bestDist = Int.MAX_VALUE
            info.visibleItemsInfo.forEach { item ->
                val idx = item.index
                if (idx < entries.size && entries[idx] is TimelineEntry.MomentItem) {
                    val dist = kotlin.math.abs(item.offset + item.size / 2 - center)
                    if (dist < bestDist) {
                        bestDist = dist
                        best = idx
                    }
                }
            }
            best
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // 中央引导线
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .width(2.dp)
                .fillMaxHeight()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Transparent,
                            colors.peach.copy(alpha = 0.35f),
                            colors.taro.copy(alpha = 0.45f),
                            colors.peach.copy(alpha = 0.35f),
                            Color.Transparent,
                        )
                    )
                )
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 6.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            itemsIndexed(entries, key = { _, e ->
                when (e) {
                    is TimelineEntry.MonthHeader -> "h${e.yearMonth.first}-${e.yearMonth.second}"
                    is TimelineEntry.MomentItem -> "m${e.moment.id}"
                }
            }) { itemIndex, entry ->
                when (entry) {
                    is TimelineEntry.MonthHeader -> MonthHeaderItem(entry)
                    is TimelineEntry.MomentItem -> {
                        TimelineCard(
                            moment = entry.moment,
                            index = itemIndex,
                            isLeft = itemIndex % 2 == 0,
                            isActive = itemIndex == activeIndex,
                            revealed = itemIndex < revealed,
                            onClick = { onMomentClick(entry.moment) }
                        )
                    }
                }
            }
        }
    }
}

/** 月份分组头:胶囊 + 封面缩略图 */
@Composable
private fun MonthHeaderItem(entry: TimelineEntry.MonthHeader) {
    val colors = LocalAppColors.current
    val (year, month) = entry.yearMonth
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.card)
                .border(1.dp, colors.peach.copy(alpha = 0.25f), RoundedCornerShape(50))
                .padding(start = 12.dp, end = 14.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (entry.cover?.imageUri != null) {
                AsyncImage(
                    model = entry.cover.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                "${year}年${month}月",
                style = MaterialTheme.typography.labelLarge,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

/** 时间轴卡片:左右交错 + 依次浮现 */
@Composable
private fun TimelineCard(
    moment: Moment,
    index: Int,
    isLeft: Boolean,
    isActive: Boolean,
    revealed: Boolean,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    val type = try { MomentType.valueOf(moment.type) } catch (_: Exception) { MomentType.OTHER }
    val mood = moment.mood?.let { try { Mood.valueOf(it) } catch (_: Exception) { null } }
    val typeColor = momentTypeColor(type, colors)

    // ── 依次浮现动画 ──
    val appear by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = 550,
            delayMillis = (index % 6) * 70,
            easing = EaseOutCubic
        ),
        label = "appear"
    )
    val slide = if (isLeft) -60f else 60f
    val slidePx by animateFloatAsState(
        targetValue = if (revealed) 0f else slide,
        animationSpec = tween(
            durationMillis = 550,
            delayMillis = (index % 6) * 70,
            easing = EaseOutCubic
        ),
        label = "slide"
    )

    // ── 活跃节点动画 ──
    val nodeScale by animateFloatAsState(
        targetValue = if (isActive) 1.25f else 1f,
        animationSpec = tween(450, easing = EaseInOutCubic),
        label = "node"
    )

    val cardElevation by animateDpAsState(
        targetValue = if (isActive) 14.dp else 5.dp,
        animationSpec = tween(450),
        label = "elev"
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // 中央节点
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .graphicsLayer { alpha = 0.35f }
                        .background(typeColor, CircleShape)
                )
            }
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .graphicsLayer { scaleX = nodeScale; scaleY = nodeScale }
                    .shadow(if (isActive) 8.dp else 2.dp, CircleShape,
                        ambientColor = typeColor, spotColor = typeColor)
                    .background(colors.card, CircleShape)
                    .border(1.5.dp, typeColor.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(type.emoji, fontSize = 12.sp)
            }
        }

        // 卡片
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isLeft) 12.dp else 108.dp,
                    end = if (isLeft) 108.dp else 12.dp,
                    top = 4.dp, bottom = 4.dp
                )
                .graphicsLayer {
                    translationX = slidePx
                    alpha = appear
                },
            horizontalArrangement = if (isLeft) Arrangement.Start else Arrangement.End
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .shadow(cardElevation, RoundedCornerShape(22.dp),
                        ambientColor = colors.peach.copy(alpha = 0.2f),
                        spotColor = typeColor.copy(alpha = 0.25f))
                    .border(
                        if (isActive) 1.5.dp else 1.dp,
                        if (isActive) typeColor.copy(alpha = 0.5f) else colors.peach.copy(alpha = 0.18f),
                        RoundedCornerShape(22.dp)
                    )
                    .clickable { onClick() },
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = colors.card)
            ) {
                // 照片
                if (!moment.imageUri.isNullOrBlank()) {
                    AsyncImage(
                        model = moment.imageUri,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(118.dp)
                            .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    // 手账风色条头
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(5.dp)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(typeColor.copy(alpha = 0.85f), typeColor.copy(alpha = 0.1f))
                                )
                            )
                    )
                }

                Column(modifier = Modifier.padding(14.dp)) {
                    // 类型 + 日期
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TagChip(
                            text = "${type.emoji} ${type.label}",
                            color = typeColor,
                            background = typeColor.copy(alpha = 0.13f)
                        )
                        Spacer(Modifier.weight(1f))
                        Text(
                            moment.date.format(DateTimeFormatter.ofPattern("M月d日")) +
                                    " · " + moment.date.dayOfWeek.getDisplayName(TextStyle.SHORT, java.util.Locale.CHINESE),
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textTertiary
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        moment.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(5.dp))
                    Text(
                        moment.content,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 19.sp
                    )

                    // 标签
                    if (moment.tags.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            moment.tags.take(3).forEach { tag ->
                                TagChip(
                                    text = "#$tag",
                                    color = colors.taro,
                                    background = colors.taro.copy(alpha = 0.12f)
                                )
                            }
                        }
                    }

                    // 心情 / 地点
                    if (mood != null || !moment.location.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (mood != null) {
                                Text(
                                    "${mood.emoji} ${mood.label}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textSecondary
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            if (!moment.location.isNullOrBlank()) {
                                Text(
                                    "📍 ${moment.location}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = colors.textTertiary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** 类型 → 颜色 */
private fun momentTypeColor(type: MomentType, colors: com.example.fragments_of_life.ui.theme.AppColors): Color =
    when (type) {
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

// ═══════════════════════════════════════════
// 照片墙视图(瀑布流)
// ═══════════════════════════════════════════
@Composable
private fun PhotoWall(
    moments: List<Moment>,
    onMomentClick: (Moment) -> Unit,
) {
    if (moments.isEmpty()) {
        EmptyHint("📷", "还没有照片", "记一笔时添加照片,这里就会亮起来")
        return
    }

    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(2),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 14.dp, end = 14.dp, bottom = 96.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalItemSpacing = 10.dp
    ) {
        items(moments, key = { it.id }) { moment ->
            PhotoWallCard(moment, onClick = { onMomentClick(moment) })
        }
    }
}

@Composable
private fun PhotoWallCard(moment: Moment, onClick: () -> Unit) {
    val ratio = when (moment.id % 3) {
        0L -> 0.78f
        1L -> 1.05f
        else -> 0.92f
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(ratio)
            .shadow(6.dp, RoundedCornerShape(18.dp))
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = moment.imageUri,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        // 底部信息渐变遮罩
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))
                    )
                )
                .padding(10.dp)
        ) {
            Column {
                Text(
                    moment.title,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    moment.date.format(DateTimeFormatter.ofPattern("yyyy.M.d")),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// 日历视图
// ═══════════════════════════════════════════
@Composable
private fun CalendarView(
    viewModel: LifeViewModel,
    onAddForDate: (LocalDate) -> Unit,
    onMomentClick: (Moment) -> Unit,
) {
    val colors = LocalAppColors.current
    val vmMonth by viewModel.selectedMonth.collectAsState()
    val yearMonth = java.time.YearMonth.of(vmMonth.year, vmMonth.month)
    val today = remember { LocalDate.now() }
    val importantDates by viewModel.importantDatesInMonth.collectAsState()
    val momentsInMonth by viewModel.momentsInMonth.collectAsState()

    var selectedDate by remember { mutableStateOf(today) }
    val momentsOnSelected = remember(momentsInMonth, selectedDate) {
        momentsInMonth.filter { it.date == selectedDate }
    }
    val importantOnSelected = remember(importantDates, selectedDate) {
        importantDates.filter {
            it.date.month == selectedDate.month && it.date.dayOfMonth == selectedDate.dayOfMonth
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // 月份切换
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                val prev = yearMonth.minusMonths(1)
                viewModel.setMonth(VmYearMonth(prev.year, prev.monthValue))
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "上个月", tint = colors.peach)
            }
            Text(
                yearMonth.format(DateTimeFormatter.ofPattern("yyyy年 M月")),
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = {
                val next = yearMonth.plusMonths(1)
                viewModel.setMonth(VmYearMonth(next.year, next.monthValue))
            }) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "下个月", tint = colors.peach)
            }
        }

        // 星期表头
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
            listOf("日", "一", "二", "三", "四", "五", "六").forEach { d ->
                Text(
                    d,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (d == "日" || d == "六") colors.peach else colors.textSecondary
                )
            }
        }

        Spacer(Modifier.height(4.dp))

        // 网格
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            val firstDay = yearMonth.atDay(1)
            val firstDayOfWeek = firstDay.dayOfWeek.value % 7 // 周日 = 0
            val days = buildList {
                repeat(firstDayOfWeek) { add(null) }
                for (d in 1..yearMonth.lengthOfMonth()) add(yearMonth.atDay(d))
                while (size < 42) add(null)
            }
            for (row in 0..5) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val date = days.getOrNull(row * 7 + col)
                        DayCell(
                            date = date,
                            today = today,
                            selected = selectedDate,
                            importantDates = importantDates,
                            hasMoment = date != null && momentsInMonth.any { it.date == date },
                            modifier = Modifier.weight(1f),
                            onClick = { date?.let { selectedDate = it } }
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // 选中日期记录
        SoftCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📅 ${selectedDate.format(DateTimeFormatter.ofPattern("M月d日"))}",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { onAddForDate(selectedDate) }) {
                    Icon(Icons.Default.Add, null, Modifier.size(15.dp), tint = colors.rose)
                    Spacer(Modifier.width(4.dp))
                    Text("记一笔", color = colors.rose, style = MaterialTheme.typography.labelMedium)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp)
            ) {
                importantOnSelected.forEach { event ->
                    val type = try {
                        ImportantDateType.valueOf(event.type)
                    } catch (_: Exception) {
                        ImportantDateType.CUSTOM
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 5.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(colors.taro.copy(alpha = 0.1f))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(type.emoji, fontSize = 16.sp)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            event.title,
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (momentsOnSelected.isEmpty() && importantOnSelected.isEmpty()) {
                    EmptyHint("✨", "这一天还没有记录", "点击「记一笔」添加美好记忆吧")
                } else {
                    momentsOnSelected.forEach { moment ->
                        val type = try {
                            MomentType.valueOf(moment.type)
                        } catch (_: Exception) {
                            MomentType.OTHER
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onMomentClick(moment) }
                                .background(colors.peach.copy(alpha = 0.07f))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(type.emoji, fontSize = 16.sp)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                moment.title,
                                style = MaterialTheme.typography.bodySmall,
                                color = colors.textPrimary,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                Icons.Default.ChevronRight, null,
                                Modifier.size(15.dp), tint = colors.textTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    today: LocalDate,
    selected: LocalDate,
    importantDates: List<com.example.fragments_of_life.data.model.ImportantDate>,
    hasMoment: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val colors = LocalAppColors.current
    if (date == null) {
        Box(modifier = modifier.aspectRatio(1f))
        return
    }

    val isToday = date == today
    val isSelected = date == selected
    val hasEvent = importantDates.any {
        it.date.month == date.month && it.date.dayOfMonth == date.dayOfMonth
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(
                when {
                    isToday -> colors.rose
                    isSelected -> colors.peach.copy(alpha = 0.22f)
                    else -> Color.Transparent
                }
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${date.dayOfMonth}",
                style = MaterialTheme.typography.bodySmall,
                color = when {
                    isToday -> Color.White
                    isSelected -> colors.rose
                    date.dayOfWeek == DayOfWeek.SUNDAY || date.dayOfWeek == DayOfWeek.SATURDAY -> colors.peach
                    else -> colors.textPrimary
                },
                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
            )
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (hasEvent && !isToday) {
                    Box(Modifier.size(4.dp).background(colors.taro, CircleShape))
                }
                if (hasMoment) {
                    Box(Modifier.size(4.dp).background(colors.rose.copy(alpha = 0.7f), CircleShape))
                }
            }
        }
    }
}
