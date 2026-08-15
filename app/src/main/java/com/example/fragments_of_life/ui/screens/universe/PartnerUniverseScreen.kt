@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.example.fragments_of_life.ui.screens.universe

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fragments_of_life.data.local.CouplePreferences
import com.example.fragments_of_life.data.model.CoupleInfo
import com.example.fragments_of_life.data.model.Importance
import com.example.fragments_of_life.data.model.PartnerNote
import com.example.fragments_of_life.ui.components.EmojiAvatar
import com.example.fragments_of_life.ui.components.EmptyHint
import com.example.fragments_of_life.ui.components.TagChip
import com.example.fragments_of_life.ui.theme.FieldShape
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.theme.beautifulFieldColors
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/** 默认分类 */
val defaultNoteCategories = listOf(
    "❤️ 喜欢的食物",
    "🚫 忌口 / 不喜欢",
    "🏃 兴趣爱好",
    "🧠 性格特点",
    "🛌 生活习惯",
    "💣 雷区 / 注意",
    "🎁 愿望 / 想要的",
    "📝 其他",
)

/** 根据内容关键词自动推荐分类 */
fun suggestCategory(text: String): String {
    val t = text
    return when {
        Regex("不吃|讨厌|忌口|不喜欢|别点|不能吃|过敏|怕辣|香菜").containsMatchIn(t) -> "🚫 忌口 / 不喜欢"
        Regex("想要|想买|愿望|生日礼物|礼物|种草").containsMatchIn(t) -> "🎁 愿望 / 想要的"
        Regex("跑步|健身|运动|打球|游戏|电影|音乐|看书|画画|跳舞|唱歌|追剧").containsMatchIn(t) -> "🏃 兴趣爱好"
        Regex("睡觉|起床|习惯|早上|晚上|熬夜|喝温水").containsMatchIn(t) -> "🛌 生活习惯"
        Regex("生气|雷区|不要|别|冷战|讲道理|抱抱").containsMatchIn(t) -> "💣 雷区 / 注意"
        Regex("慢热|内向|外向|性格|安全感|话很多|话少").containsMatchIn(t) -> "🧠 性格特点"
        Regex("爱吃|喜欢喝|爱喝|喜欢吃|最爱吃|喜欢").containsMatchIn(t) -> "❤️ 喜欢的食物"
        else -> "📝 其他"
    }
}

@Composable
fun PartnerUniverseScreen(
    viewModel: LifeViewModel,
    coupleInfo: CoupleInfo,
    onBack: () -> Unit,
) {
    val colors = LocalAppColors.current
    val context = LocalContext.current
    val prefs = remember { CouplePreferences.getInstance(context) }
    val notes by viewModel.allPartnerNotes.collectAsState()

    var search by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var customCategories by remember { mutableStateOf(prefs.getCustomNoteCategories()) }

    var showAdd by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PartnerNote?>(null) }
    var confirmDelete by remember { mutableStateOf<PartnerNote?>(null) }
    var showNewCategory by remember { mutableStateOf(false) }

    BackHandler { onBack() }

    val allCategories = remember(customCategories) { defaultNoteCategories + customCategories }

    val filtered = remember(notes, search, selectedCategory) {
        notes.filter { n ->
            val passCat = selectedCategory == null || n.category == selectedCategory
            val passSearch = search.isBlank() ||
                    n.content.contains(search, ignoreCase = true) ||
                    n.note.contains(search, ignoreCase = true) ||
                    n.tags.any { it.contains(search, ignoreCase = true) }
            passCat && passSearch
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            // 顶部头部
            UniverseHeader(coupleInfo, notes.size)

            // 搜索
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                placeholder = { Text("搜索:辣、生日、游戏…", color = colors.textTertiary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = colors.textTertiary) },
                shape = FieldShape,
                colors = beautifulFieldColors(),
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // 分类胶囊
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    CategoryChip("全部", selected = selectedCategory == null) { selectedCategory = null }
                }
                items(allCategories) { cat ->
                    CategoryChip(cat, selected = selectedCategory == cat) {
                        selectedCategory = if (selectedCategory == cat) null else cat
                    }
                }
                item {
                    CategoryChip("＋ 新分类", selected = false, color = colors.taro) {
                        showNewCategory = true
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (filtered.isEmpty()) {
                EmptyHint("🌌", "这里还空着", "点右下角 +,记下第一件关于TA的小事吧")
            } else {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 110.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp
                ) {
                    items(filtered, key = { it.id }) { note ->
                        FlipNoteCard(
                            note = note,
                            onEdit = { editing = note },
                            onDelete = { confirmDelete = note },
                            onToggleImportance = {
                                val next = when (note.importance) {
                                    Importance.VERY.name -> Importance.NORMAL.name
                                    else -> Importance.entries[
                                        Importance.entries.indexOfFirst { e -> e.name == note.importance } + 1
                                    ].name
                                }
                                viewModel.updatePartnerNote(note.copy(importance = next))
                            },
                        )
                    }
                }
            }
        }

        // 右下角添加
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp)
                .size(58.dp)
                .shadow(12.dp, CircleShape, ambientColor = colors.rose, spotColor = colors.rose)
                .background(
                    Brush.linearGradient(listOf(colors.peach, colors.rose)),
                    CircleShape
                )
                .clickable { showAdd = true },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, "记一条", tint = Color.White, modifier = Modifier.size(28.dp))
        }
    }

    // 添加 / 编辑
    if (showAdd || editing != null) {
        QuickNoteDialog(
            initial = editing,
            initialCategory = selectedCategory,
            categories = allCategories,
            onSave = { note ->
                if (editing != null) viewModel.updatePartnerNote(note.copy(id = editing!!.id))
                else viewModel.insertPartnerNote(note)
                showAdd = false
                editing = null
            },
            onDismiss = {
                showAdd = false
                editing = null
            }
        )
    }

    // 删除确认
    confirmDelete?.let { note ->
        AlertDialog(
            onDismissRequest = { confirmDelete = null },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("删除这条备忘?", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = { Text("「${note.content}」", color = colors.textSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePartnerNote(note)
                    confirmDelete = null
                }) { Text("删除", color = colors.softRed) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = null }) { Text("取消", color = colors.textSecondary) }
            }
        )
    }

    // 新建自定义分类
    if (showNewCategory) {
        var name by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showNewCategory = false },
            containerColor = colors.card,
            shape = RoundedCornerShape(24.dp),
            title = { Text("新建分类", color = colors.textPrimary, fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("例:工作相关、家人相关", style = MaterialTheme.typography.bodySmall, color = colors.textTertiary)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = name,
                        onValueChange = { if (it.length <= 10) name = it },
                        placeholder = { Text("分类名称") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = FieldShape,
                        colors = beautifulFieldColors(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val n = name.trim()
                    if (n.isNotBlank() && n !in allCategories) {
                        customCategories = customCategories + "📌 $n"
                        prefs.saveCustomNoteCategories(customCategories)
                    }
                    showNewCategory = false
                }) { Text("添加", color = colors.rose, fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategory = false }) { Text("取消", color = colors.textSecondary) }
            }
        )
    }
}

@Composable
private fun UniverseHeader(info: CoupleInfo, noteCount: Int) {
    val colors = LocalAppColors.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Brush.linearGradient(listOf(colors.gradientTaro, colors.gradientPeach)))
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(emoji = info.partnerEmoji, size = 54.dp)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    "TA的小宇宙",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "关于 ${info.partnerName},你已经记下了 $noteCount 件小事",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    "真正爱你的人,会记得你随口说过的每一句话。",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    selected: Boolean,
    color: Color = LocalAppColors.current.peach,
    onClick: () -> Unit,
) {
    val colors = LocalAppColors.current
    Text(
        label,
        style = MaterialTheme.typography.labelMedium,
        color = if (selected) Color.White else colors.textSecondary,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (selected) color else colors.card)
            .border(1.dp, if (selected) color else colors.taro.copy(alpha = 0.2f), RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 13.dp, vertical = 7.dp)
    )
}

/** 情报卡:正面一句话,点击 3D 翻转到背面详情 */
@Composable
private fun FlipNoteCard(
    note: PartnerNote,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleImportance: () -> Unit,
) {
    val colors = LocalAppColors.current
    val density = LocalDensity.current
    var flipped by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (flipped) 180f else 0f,
        animationSpec = tween(550, easing = EaseInOutCubic),
        label = "flip"
    )
    val showBack = rotation > 90f

    val importance = try { Importance.valueOf(note.importance) } catch (_: Exception) { Importance.NORMAL }
    val starEmoji = when (importance) {
        Importance.VERY -> "💛"
        Importance.IMPORTANT -> "⭐"
        Importance.NORMAL -> ""
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.82f)
            .graphicsLayer {
                rotationY = rotation
                cameraDistance = 14f * density.density
            }
            .clickable { flipped = !flipped }
            .shadow(6.dp, RoundedCornerShape(18.dp), ambientColor = colors.peach.copy(alpha = 0.2f))
            .background(colors.card, RoundedCornerShape(18.dp))
            .border(1.dp, colors.peach.copy(alpha = 0.2f), RoundedCornerShape(18.dp))
    ) {
        if (showBack) {
            // 背面(反向旋转,避免镜像)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { rotationY = 180f }
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("备注", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                    Spacer(Modifier.weight(1f))
                    Text(
                        note.createdAt.let { Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate() }
                            .format(DateTimeFormatter.ofPattern("M月d日")),
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textTertiary
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    note.note.ifBlank { "暂无备注" },
                    style = MaterialTheme.typography.bodySmall,
                    color = if (note.note.isBlank()) colors.textTertiary else colors.textPrimary,
                    lineHeight = 18.sp
                )
                Spacer(Modifier.weight(1f))

                // 重要程度
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Importance.entries.forEach { imp ->
                        val selected = imp.name == note.importance
                        Text(
                            if (imp == Importance.VERY) "💛 特别重要" else if (imp == Importance.IMPORTANT) "⭐ 重要" else "普通",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) colors.gold else colors.textTertiary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) colors.goldLight.copy(alpha = 0.5f) else Color.Transparent)
                                .clickable { onToggleImportance() }
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, null, Modifier.size(14.dp), tint = colors.rose)
                        Spacer(Modifier.width(3.dp))
                        Text("编辑", style = MaterialTheme.typography.labelSmall, color = colors.rose)
                    }
                    TextButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, null, Modifier.size(14.dp), tint = colors.softRed)
                        Spacer(Modifier.width(3.dp))
                        Text("删除", style = MaterialTheme.typography.labelSmall, color = colors.softRed)
                    }
                }
            }
        } else {
            // 正面
            Column(modifier = Modifier.fillMaxSize().padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TagChip(
                        text = note.category,
                        color = colors.taro,
                        background = colors.taro.copy(alpha = 0.14f)
                    )
                    Spacer(Modifier.weight(1f))
                    if (starEmoji.isNotEmpty()) {
                        Text(starEmoji, fontSize = 15.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 21.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.weight(1f))
                if (note.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        note.tags.take(2).forEach { tag ->
                            TagChip(
                                text = "#$tag",
                                color = colors.textSecondary,
                                background = colors.taroLight.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** 快速添加/编辑对话框(3 秒内记一条) */
@Composable
fun QuickNoteDialog(
    initial: PartnerNote? = null,
    initialContent: String = "",
    initialCategory: String? = null,
    linkedMomentId: Long? = null,
    categories: List<String> = defaultNoteCategories,
    onSave: (PartnerNote) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    var content by remember { mutableStateOf(initial?.content ?: initialContent) }
    var category by remember { mutableStateOf(initial?.category ?: initialCategory ?: "") }
    var importance by remember {
        mutableStateOf(
            initial?.importance?.let { try { Importance.valueOf(it) } catch (_: Exception) { null } }
                ?: Importance.NORMAL
        )
    }
    var noteText by remember { mutableStateOf(initial?.note ?: "") }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember { mutableStateOf(initial?.tags ?: emptyList()) }

    val suggested = remember(content) { suggestCategory(content) }
    val effective = if (category.isBlank()) suggested else category

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = colors.card,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (initial == null) "🌌 记一条关于TA的" else "✏️ 编辑备忘",
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
                    value = content,
                    onValueChange = { if (it.length <= 50) content = it },
                    placeholder = { Text("一句话,例如:不喜欢吃辣", color = colors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    colors = beautifulFieldColors(),
                    maxLines = 2
                )
                // 自动推荐提示
                Text(
                    "✨ 自动识别为:${effective}${if (category.isBlank()) "" else ""}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.taro
                )
                // 分类选择
                androidx.compose.foundation.layout.FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val selected = effective == cat
                        Text(
                            cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Color.White else colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) colors.taro else colors.taroLight.copy(alpha = 0.4f))
                                .clickable { category = cat }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
                // 重要程度
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("重要程度", style = MaterialTheme.typography.labelMedium, color = colors.textSecondary)
                    Spacer(Modifier.weight(1f))
                    Importance.entries.forEach { imp ->
                        val selected = importance == imp
                        val label = when (imp) {
                            Importance.NORMAL -> "普通"
                            Importance.IMPORTANT -> "⭐重要"
                            Importance.VERY -> "💛特别"
                        }
                        Text(
                            label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) colors.gold else colors.textSecondary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) colors.goldLight.copy(alpha = 0.5f) else Color.Transparent)
                                .clickable { importance = imp }
                                .padding(horizontal = 9.dp, vertical = 5.dp)
                        )
                    }
                }
                // 备注
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = { Text("备注(可选):补充细节", color = colors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FieldShape,
                    colors = beautifulFieldColors(),
                    maxLines = 3,
                    textStyle = MaterialTheme.typography.bodySmall
                )
                // 标签
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = tagInput,
                        onValueChange = { if (it.length <= 10) tagInput = it },
                        placeholder = { Text("加个标签,如:她自己说的", color = colors.textTertiary) },
                        modifier = Modifier.weight(1f),
                        shape = FieldShape,
                        colors = beautifulFieldColors(),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.width(8.dp))
                    TextButton(onClick = {
                        val t = tagInput.trim()
                        if (t.isNotBlank() && t !in tags) tags = tags + t
                        tagInput = ""
                    }) { Text("添加", color = colors.rose) }
                }
                if (tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { t ->
                            Box(
                                modifier = Modifier.clickable { tags = tags - t }
                            ) {
                                TagChip(
                                    text = "#$t ×",
                                    color = colors.rose,
                                    background = colors.roseLight.copy(alpha = 0.4f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (content.isNotBlank()) {
                    onSave(
                        PartnerNote(
                            content = content.trim(),
                            category = effective,
                            tags = tags,
                            importance = importance.name,
                            note = noteText.trim(),
                            linkedMomentId = linkedMomentId ?: initial?.linkedMomentId,
                        )
                    )
                }
            }) { Text("完成 ✨", color = colors.rose, fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消", color = colors.textSecondary) }
        }
    )
}
