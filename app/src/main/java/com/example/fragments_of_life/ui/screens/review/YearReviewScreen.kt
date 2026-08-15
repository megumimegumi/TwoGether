package com.example.fragments_of_life.ui.screens.review

import android.content.ContentValues
import android.graphics.Bitmap
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fragments_of_life.data.model.CoupleInfo
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.MomentType
import com.example.fragments_of_life.data.model.PartnerNote
import com.example.fragments_of_life.ui.components.EmojiAvatar
import com.example.fragments_of_life.ui.components.rememberHeartbeatScale
import com.example.fragments_of_life.ui.viewmodel.LifeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlin.random.Random

// ──────────────────────────────────────────
// 电影场景
// ──────────────────────────────────────────
private sealed interface ReviewScene {
    data object Title : ReviewScene
    data class Season(
        val chapter: Int,
        val season: Int,       // 1春 2夏 3秋 4冬
        val chapterLabel: String,
        val moments: List<Moment>,
    ) : ReviewScene

    data class Stats(
        val year: Int,
        val recordCount: Int,
        val photoCount: Int,
        val placeCount: Int,
        val wishCount: Int,
    ) : ReviewScene

    data class KnowYou(
        val year: Int,
        val notes: List<PartnerNote>,
    ) : ReviewScene

    data class Ending(
        val year: Int,
        val yearsTogether: Int,
    ) : ReviewScene
}

private fun seasonOf(month: Int): Int = when (month) {
    in 3..5 -> 1
    in 6..8 -> 2
    in 9..11 -> 3
    else -> 4
}

private val seasonMeta = mapOf(
    1 to Triple("第一章 · 春天", "初见与日常", "🌸"),
    2 to Triple("第二章 · 夏天", "热烈与旅行", "✨"),
    3 to Triple("第三章 · 秋天", "陪伴与小事", "🍂"),
    4 to Triple("第四章 · 冬天", "拥抱与期待", "❄️"),
)

/** 根据季节记录生成文案 */
private fun captionsFor(season: Int, moments: List<Moment>): Pair<String, String?> {
    val travel = moments.firstOrNull { it.type == "TRAVEL" }
    val fight = moments.firstOrNull { it.type == "FIGHT" || it.type == "MAKEUP" }
    val main = when {
        travel != null -> "我们一起去了${travel.location ?: "远方"},把风景收进回忆。"
        season == 1 -> "这一年,我们从一顿热气腾腾的饭开始。"
        season == 2 -> "夏天很长,蝉鸣很吵,好在每天都有你。"
        season == 3 -> "最安心的是,每天醒来你都在。"
        else -> "冬天好冷,但你的手很暖。"
    }
    val sub = moments.firstOrNull { it.content.contains("第一次") }?.title
        ?: if (fight != null) "吵过架,也和好,越走越近。" else null
    return main to sub
}

@Composable
fun YearReviewScreen(
    viewModel: LifeViewModel,
    coupleInfo: CoupleInfo,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val allMoments by viewModel.allMoments.collectAsState()
    val allWishes by viewModel.allWishes.collectAsState()
    val allNotes by viewModel.allPartnerNotes.collectAsState()

    val years = remember(allMoments) {
        (allMoments.map { it.date.year } + LocalDate.now().year).distinct().sortedDescending()
    }
    val latestWithData = remember(allMoments) { allMoments.map { it.date.year }.maxOrNull() ?: LocalDate.now().year }
    var year by remember { mutableStateOf(latestWithData) }

    val yearMoments = remember(allMoments, year) { allMoments.filter { it.date.year == year } }

    // 组装场景
    val scenes = remember(yearMoments, allWishes, allNotes, year, coupleInfo) {
        buildList {
            add(ReviewScene.Title)
            val bySeason = yearMoments.groupBy { seasonOf(it.date.monthValue) }
            var chapter = 1
            listOf(1, 2, 3, 4).forEach { s ->
                val ms = bySeason[s] ?: emptyList()
                if (ms.isNotEmpty()) {
                    val meta = seasonMeta[s]!!
                    add(
                        ReviewScene.Season(
                            chapter = chapter++,
                            season = s,
                            chapterLabel = "${meta.first} · ${meta.second}",
                            moments = ms.sortedByDescending { it.date },
                        )
                    )
                }
            }
            val wishesDone = allWishes.count { w ->
                w.done && w.doneAt?.let { doneAt ->
                    Instant.ofEpochMilli(doneAt).atZone(ZoneId.systemDefault()).toLocalDate().year == year
                } == true
            }
            add(
                ReviewScene.Stats(
                    year = year,
                    recordCount = yearMoments.size,
                    photoCount = yearMoments.count { !it.imageUri.isNullOrBlank() },
                    placeCount = yearMoments.mapNotNull { it.location?.takeIf { l -> l.isNotBlank() } }.distinct().size,
                    wishCount = wishesDone,
                )
            )
            // 这一年,你更懂TA了
            val yearNotes = allNotes.filter {
                Instant.ofEpochMilli(it.createdAt).atZone(ZoneId.systemDefault()).toLocalDate().year == year
            }
            if (yearNotes.isNotEmpty()) {
                add(ReviewScene.KnowYou(year = year, notes = yearNotes.take(3)))
            }
            add(
                ReviewScene.Ending(
                    year = year,
                    yearsTogether = (year - coupleInfo.anniversaryDate.year + 1).coerceAtLeast(1),
                )
            )
        }
    }

    if (yearMoments.isEmpty()) {
        // 该年没有记录
        EmptyYearView(year, years, onYearChange = { year = it }, onClose = onClose)
        return
    }

    var sceneIndex by remember { mutableIntStateOf(0) }
    val scene = scenes[sceneIndex.coerceIn(0, scenes.lastIndex)]

    // 自动推进(片尾停留)
    LaunchedEffect(sceneIndex, scenes.size) {
        if (scene !is ReviewScene.Ending) {
            delay(4600)
            if (sceneIndex < scenes.lastIndex) sceneIndex++
        }
    }

    BackHandler { onClose() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }) {
                // 任意处点击:未到尾页 → 下一幕;尾页 → 从头再放一遍
                if (sceneIndex < scenes.lastIndex) sceneIndex++ else sceneIndex = 0
            }
    ) {
        Crossfade(targetState = sceneIndex, label = "scene") { idx ->
            when (val s = scenes[idx]) {
                is ReviewScene.Title -> TitleScene(year, coupleInfo, yearsTogether = (year - coupleInfo.anniversaryDate.year + 1).coerceAtLeast(1))
                is ReviewScene.Season -> SeasonScene(s, coupleInfo)
                is ReviewScene.Stats -> StatsScene(s)
                is ReviewScene.KnowYou -> KnowYouScene(s)
                is ReviewScene.Ending -> EndingScene(s, coupleInfo, onSaveCard = { cardBitmap ->
                    saveCardToGallery(context, cardBitmap, year)
                })
            }
        }

        // 顶部:关闭 + 年份
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, "关闭", tint = Color.White)
            }
            Spacer(Modifier.weight(1f))
            YearChip(year, years) { newYear ->
                year = newYear
                sceneIndex = 0
            }
        }

        // 底部进度点
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 22.dp),
            horizontalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            scenes.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .size(if (i == sceneIndex) 9.dp else 6.dp)
                        .clip(CircleShape)
                        .background(
                            if (i == sceneIndex) Color.White else Color.White.copy(alpha = 0.35f)
                        )
                )
            }
        }
    }
}

/** 该年没有记录时的空态 */
@Composable
private fun EmptyYearView(
    year: Int,
    years: List<Int>,
    onYearChange: (Int) -> Unit,
    onClose: () -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFC7B8), Color(0xFFFFF9F7))))) {
        Column(
            modifier = Modifier.align(Alignment.Center).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎞️", fontSize = 56.sp)
            Spacer(Modifier.height(14.dp))
            Text("${year}年还没有记录", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4A3F44))
            Spacer(Modifier.height(8.dp))
            Text("去「点滴」记下几笔,再来生成你们的电影吧", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF9A8F93))
            Spacer(Modifier.height(24.dp))
            YearChip(year, years) { onYearChange(it) }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onClose) { Text("返回", color = Color(0xFFFF5C8A)) }
        }
    }
}

@Composable
private fun YearChip(year: Int, years: List<Int>, onSelect: (Int) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Text(
            "${year}年 ▾",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { expanded = true }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )
        androidx.compose.material3.DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            years.forEach { y ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text("${y}年") },
                    onClick = { expanded = false; onSelect(y) }
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// 片头
// ═══════════════════════════════════════════
@Composable
private fun TitleScene(year: Int, coupleInfo: CoupleInfo, yearsTogether: Int) {
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val tFade1 by animateFloatAsState(if (shown) 1f else 0f, tween(1400), label = "title")
    val tFade2 by animateFloatAsState(if (shown) 1f else 0f, tween(1400, delayMillis = 700), label = "title2")

    Box(
        modifier = Modifier.fillMaxSize().background(Color(0xFF1B1526)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "献给我们的第 $yearsTogether 年",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 6.sp,
                modifier = Modifier.graphicsLayer { alpha = tFade1 },
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(14.dp))
            Text(
                "「${coupleInfo.myName} & ${coupleInfo.partnerName} · $year」",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.6f),
                letterSpacing = 3.sp,
                modifier = Modifier.graphicsLayer { alpha = tFade2 }
            )
            Spacer(Modifier.height(40.dp))
            Text(
                "🎬 只属于我们的电影",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.45f),
                letterSpacing = 4.sp,
                modifier = Modifier.graphicsLayer { alpha = tFade2 }
            )
        }
    }
}

// ═══════════════════════════════════════════
// 季节章节
// ═══════════════════════════════════════════
@Composable
private fun SeasonScene(scene: ReviewScene.Season, coupleInfo: CoupleInfo) {
    val seasonColors = when (scene.season) {
        1 -> listOf(Color(0xFFFFE0E4), Color(0xFFF3E8FF))   // 春
        2 -> listOf(Color(0xFFFFF1D6), Color(0xFFFFD9C7))   // 夏
        3 -> listOf(Color(0xFFFDEBD0), Color(0xFFFFE3D0))   // 秋
        else -> listOf(Color(0xFFE8EBFF), Color(0xFFE0F0F5)) // 冬
    }
    val particleEmoji = seasonMeta[scene.season]!!.third

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(seasonColors))) {
        SeasonParticles(emoji = particleEmoji)

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 章节标签
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { shown = true }
            val fade by animateFloatAsState(if (shown) 1f else 0f, tween(900), label = "chap")

            Text(
                scene.chapterLabel,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9A8F93),
                letterSpacing = 3.sp,
                modifier = Modifier.graphicsLayer { alpha = fade }
            )
            Spacer(Modifier.height(26.dp))

            // 拍立得照片(1-2 张)
            val photos = scene.moments.filter { !it.imageUri.isNullOrBlank() }
            val cards = (photos + scene.moments).distinctBy { it.id }.take(2)
            Row(horizontalArrangement = Arrangement.spacedBy(18.dp)) {
                cards.forEachIndexed { i, m ->
                    PolaroidCard(
                        moment = m,
                        index = i,
                        baseRotation = if (i == 0) -4f else 3.5f,
                    )
                }
            }

            Spacer(Modifier.height(30.dp))

            // 文案逐行浮现
            val (main, sub) = captionsFor(scene.season, scene.moments)
            val lineAlpha by animateFloatAsState(if (shown) 1f else 0f, tween(900, delayMillis = 500), label = "line")
            Text(
                main,
                fontSize = 17.sp,
                color = Color(0xFF4A3F44),
                fontWeight = FontWeight.Medium,
                lineHeight = 27.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.graphicsLayer { alpha = lineAlpha }
            )
            sub?.let {
                Spacer(Modifier.height(8.dp))
                val subAlpha by animateFloatAsState(if (shown) 1f else 0f, tween(900, delayMillis = 900), label = "sub")
                Text(
                    "「$it」",
                    fontSize = 14.sp,
                    color = Color(0xFF9A8F93),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.graphicsLayer { alpha = subAlpha }
                )
            }
        }
    }
}

/** 拍立得卡片:飘落 + 轻微旋转 */
@Composable
private fun PolaroidCard(moment: Moment, index: Int, baseRotation: Float) {
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }
    val fall by animateFloatAsState(
        targetValue = if (appeared) 0f else 160f,
        animationSpec = tween(900, delayMillis = 200 + index * 350, easing = EaseOutCubic),
        label = "fall"
    )
    val rot by animateFloatAsState(
        targetValue = if (appeared) baseRotation else baseRotation + 8f,
        animationSpec = tween(900, delayMillis = 200 + index * 350, easing = EaseOutCubic),
        label = "rot"
    )
    val cardAlpha by animateFloatAsState(if (appeared) 1f else 0f, tween(700, delayMillis = 200 + index * 350), label = "pa")

    val typeEmoji = try { MomentType.valueOf(moment.type).emoji } catch (_: Exception) { "✨" }

    Column(
        modifier = Modifier
            .width(128.dp)
            .graphicsLayer {
                translationY = fall
                rotationZ = rot
                alpha = cardAlpha
            }
            .shadow(10.dp, RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp))
            .padding(7.dp)
    ) {
        if (!moment.imageUri.isNullOrBlank()) {
            AsyncImage(
                model = moment.imageUri,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(108.dp).clip(RoundedCornerShape(4.dp)),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(108.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.linearGradient(listOf(Color(0xFFFFE3DC), Color(0xFFEDE5FF)))),
                contentAlignment = Alignment.Center
            ) {
                Text(typeEmoji, fontSize = 40.sp)
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            moment.date.toString().substring(5).replace("-", "."),
            fontSize = 9.sp,
            color = Color(0xFFB0A6A6)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            moment.title,
            fontSize = 11.sp,
            color = Color(0xFF4A3F44),
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
    }
}

/** 季节粒子:樱花/光斑/枫叶/雪花 缓缓飘落 */
@Composable
private fun SeasonParticles(emoji: String, count: Int = 14) {
    val heightPx = with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    data class P(val x: Float, val size: Float, val dur: Int, val delay: Int)
    val parts = remember { List(count) { P(Random.nextFloat(), Random.nextInt(12, 24).toFloat(), Random.nextInt(5000, 9000), Random.nextInt(0, 4000)) } }

    parts.forEach { p ->
        key(p.x) {
            val t = rememberInfiniteTransition(label = "sp")
            val y by t.animateFloat(
                initialValue = -0.2f, targetValue = 1.2f,
                animationSpec = infiniteRepeatable(tween(p.dur, delayMillis = p.delay, easing = LinearEasing), RepeatMode.Restart),
                label = "y"
            )
            val sway by t.animateFloat(
                initialValue = -1f, targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(p.dur / 3, easing = EaseInOutSine), RepeatMode.Reverse),
                label = "sway"
            )
            Text(
                emoji,
                fontSize = p.size.sp,
                modifier = Modifier.graphicsLayer {
                    translationY = heightPx * y
                    translationX = (p.x - 0.5f) * 320f + sway * 26f
                    alpha = 0.55f
                }
            )
        }
    }
}

// ═══════════════════════════════════════════
// 数据彩蛋
// ═══════════════════════════════════════════
@Composable
private fun StatsScene(scene: ReviewScene.Stats) {
    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFF7EC), Color(0xFFFFE9DC)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { shown = true }
            val fade by animateFloatAsState(if (shown) 1f else 0f, tween(800), label = "st")

            Text(
                "${scene.year} · 数据彩蛋",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9A8F93),
                letterSpacing = 3.sp,
                modifier = Modifier.graphicsLayer { alpha = fade }
            )
            Spacer(Modifier.height(24.dp))

            val stats = listOf(
                Triple("📝", "${scene.recordCount} 条", "珍贵记录"),
                Triple("📷", "${scene.photoCount} 张", "照片时光"),
                Triple("📍", "${scene.placeCount} 个", "去过的地方"),
                Triple("⭐", "${scene.wishCount} 个", "点亮的愿望"),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                stats.forEachIndexed { i, (emoji, num, label) ->
                    val itemFade by animateFloatAsState(
                        if (shown) 1f else 0f,
                        tween(700, delayMillis = 300 + i * 250),
                        label = "item$i"
                    )
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .graphicsLayer { alpha = itemFade }
                            .background(Color.White, RoundedCornerShape(18.dp))
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(emoji, fontSize = 26.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(num, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3F44))
                        Spacer(Modifier.height(3.dp))
                        Text(label, fontSize = 11.sp, color = Color(0xFF9A8F93))
                    }
                }
            }

            Spacer(Modifier.height(28.dp))
            val lineAlpha by animateFloatAsState(if (shown) 1f else 0f, tween(900, delayMillis = 1400), label = "line")
            Text(
                "原来这一年,我们一起经历了这么多。",
                fontSize = 15.sp,
                color = Color(0xFF9A8F93),
                modifier = Modifier.graphicsLayer { alpha = lineAlpha }
            )
        }
    }
}

// ═══════════════════════════════════════════
// 这一年,你更懂TA了
// ═══════════════════════════════════════════
@Composable
private fun KnowYouScene(scene: ReviewScene.KnowYou) {
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(listOf(Color(0xFFF0EAFF), Color(0xFFFFE9F0)))
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            var shown by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) { shown = true }
            val fade by animateFloatAsState(if (shown) 1f else 0f, tween(900), label = "ky")

            Text(
                "${scene.year} · 这一年,你更懂TA了",
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9A8F93),
                letterSpacing = 3.sp,
                modifier = Modifier.graphicsLayer { alpha = fade }
            )
            Spacer(Modifier.height(22.dp))

            scene.notes.forEachIndexed { i, note ->
                val itemFade by animateFloatAsState(
                    if (shown) 1f else 0f,
                    tween(700, delayMillis = 400 + i * 350),
                    label = "note$i"
                )
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer { alpha = itemFade }
                        .background(Color.White.copy(alpha = 0.8f), RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 13.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        "${note.category}  ${note.content}",
                        fontSize = 14.sp,
                        color = Color(0xFF4A3F44),
                        fontWeight = FontWeight.Medium,
                        lineHeight = 21.sp
                    )
                }
                if (i < scene.notes.size - 1) Spacer(Modifier.height(10.dp))
            }

            Spacer(Modifier.height(24.dp))
            val lineAlpha by animateFloatAsState(if (shown) 1f else 0f, tween(900, delayMillis = 1400), label = "line")
            Text(
                "TA随口说过的话,你都记在心里。",
                fontSize = 15.sp,
                color = Color(0xFF9A8F93),
                modifier = Modifier.graphicsLayer { alpha = lineAlpha }
            )
        }
    }
}

// ═══════════════════════════════════════════
// 片尾 + 纪念卡片
// ═══════════════════════════════════════════
@Composable
private fun EndingScene(
    scene: ReviewScene.Ending,
    coupleInfo: CoupleInfo,
    onSaveCard: (Bitmap) -> Unit,
) {
    val beat by rememberHeartbeatScale(min = 1f, max = 1.12f, durationMillis = 1200)
    var shown by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { shown = true }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFFFFC7B8), Color(0xFFD9C9FF)))),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // 纪念卡片(可保存)
            val layer = rememberGraphicsLayer()
            Column(
                modifier = Modifier
                    .padding(horizontal = 40.dp)
                    .shadow(18.dp, RoundedCornerShape(24.dp))
                    .drawWithContent {
                        layer.record { this@drawWithContent.drawContent() }
                        drawLayer(layer)
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MemorialCardContent(scene, coupleInfo, beat)
                Spacer(Modifier.height(18.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                try {
                                    val bmp = layer.toImageBitmap().asAndroidBitmap()
                                    onSaveCard(bmp)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "保存失败,试试截图吧", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.85f))
                    ) {
                        Text("📥 保存卡片", color = Color(0xFFFF5C8A), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
            val tipAlpha by animateFloatAsState(if (shown) 1f else 0f, tween(1200, delayMillis = 1600), label = "tip")
            Text(
                "点击屏幕,再放映一遍 🎬",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.graphicsLayer { alpha = tipAlpha }
            )
        }
    }
}

@Composable
private fun MemorialCardContent(
    scene: ReviewScene.Ending,
    coupleInfo: CoupleInfo,
    beat: Float,
) {
    Column(
        modifier = Modifier
            .background(Color(0xFFFFFDFB), RoundedCornerShape(24.dp))
            .padding(horizontal = 30.dp, vertical = 28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("拾光 · ${scene.year}", fontSize = 12.sp, color = Color(0xFFB0A6A6), letterSpacing = 3.sp)
        Spacer(Modifier.height(10.dp))
        Text("我们的这一年", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A3F44))
        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiAvatar(emoji = coupleInfo.myEmoji, size = 46.dp)
            Text(
                "❤️",
                fontSize = 20.sp,
                modifier = Modifier
                    .padding(horizontal = 10.dp)
                    .graphicsLayer { scaleX = beat; scaleY = beat }
            )
            EmojiAvatar(emoji = coupleInfo.partnerEmoji, size = 46.dp)
        }
        Spacer(Modifier.height(12.dp))

        Text(
            "第 ${scene.yearsTogether + 1} 年,也请多多指教。",
            fontSize = 15.sp,
            color = Color(0xFF4A3F44),
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "${coupleInfo.myName} ❤ ${coupleInfo.partnerName}",
            fontSize = 12.sp,
            color = Color(0xFF9A8F93)
        )
        Spacer(Modifier.height(12.dp))
        Text("💕 🌸 ✨ 🍂 ❄️", fontSize = 12.sp, letterSpacing = 4.sp)
    }
}

/** 保存到相册(Pictures/拾光) */
private fun saveCardToGallery(context: android.content.Context, bitmap: Bitmap, year: Int) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        Toast.makeText(context, "系统版本较低,请长按截图保存", Toast.LENGTH_SHORT).show()
        return
    }
    try {
        val name = "拾光_${year}年_我们的这一年.jpg"
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/拾光")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        resolver.openOutputStream(uri!!)?.use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(uri, values, null, null)
        Toast.makeText(context, "已保存到相册 Pictures/拾光 💕", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Toast.makeText(context, "保存失败:${e.message}", Toast.LENGTH_SHORT).show()
    }
}
