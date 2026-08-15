package com.example.fragments_of_life.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fragments_of_life.ui.theme.LocalAppColors
import kotlinx.coroutines.delay
import kotlin.random.Random

/** 数字滚动变化(在一起天数等) */
@Composable
fun CountUpText(
    target: Int,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle(fontSize = 44.sp, fontWeight = FontWeight.Bold),
    color: Color,
    suffix: String = "",
) {
    val animated by animateFloatAsState(
        targetValue = target.toFloat(),
        animationSpec = tween(1500, easing = EaseOutCubic),
        label = "countUp"
    )
    Text(
        text = "${animated.toInt()}$suffix",
        modifier = modifier,
        style = style,
        color = color,
    )
}

/**
 * 梦幻背景:奶油底 + 缓慢漂移的蜜桃/香芋/香槟光斑 + 淡淡漂浮的爱心。
 * 用于时间轴等需要温馨氛围的界面。
 */
@Composable
fun DreamyBackground(modifier: Modifier = Modifier) {
    val colors = LocalAppColors.current
    val transition = rememberInfiniteTransition(label = "dreamy")

    val drift1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(16000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift1"
    )
    val drift2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift2"
    )
    val drift3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift3"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // 顶部蜜桃光斑
        Box(
            Modifier
                .offset(x = (drift1 * 120 - 40).dp, y = (-60 + drift2 * 100).dp)
                .size(340.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.peach.copy(alpha = 0.16f), Color.Transparent),
                        radius = 340f
                    )
                )
        )
        // 右侧香芋光斑
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .offset(x = (40 - drift2 * 100).dp, y = (140 + drift1 * 120).dp)
                .size(320.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.taro.copy(alpha = 0.15f), Color.Transparent),
                        radius = 320f
                    )
                )
        )
        // 底部香槟光斑
        Box(
            Modifier
                .align(Alignment.BottomStart)
                .offset(x = (drift3 * 140 - 40).dp, y = (-40 - drift1 * 90).dp)
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(colors.gold.copy(alpha = 0.13f), Color.Transparent),
                        radius = 300f
                    )
                )
        )
        // 淡淡漂浮的爱心
        FloatingHeartsBackground(count = 9)
    }
}

/** 轻柔的呼吸/心跳缩放 */
@Composable
fun rememberHeartbeatScale(
    min: Float = 1f,
    max: Float = 1.07f,
    durationMillis: Int = 1400,
): State<Float> {
    val transition = rememberInfiniteTransition(label = "heartbeat")
    return transition.animateFloat(
        initialValue = min,
        targetValue = max,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heartbeatScale"
    )
}

// ─────────────────────────────────────────
// 爱心粒子(首页背景)
// ─────────────────────────────────────────
private data class HeartSpec(
    val xFraction: Float,
    val size: Float,
    val duration: Int,
    val delay: Int,
)

@Composable
fun FloatingHeartsBackground(
    modifier: Modifier = Modifier,
    count: Int = 10,
) {
    val colors = LocalAppColors.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val heightPx = with(density) { screenHeight.toPx() }

    val particles = remember {
        List(count) {
            HeartSpec(
                xFraction = Random.nextFloat(),
                size = Random.nextInt(10, 20).toFloat(),
                duration = Random.nextInt(10000, 18000),
                delay = Random.nextInt(0, 8000),
            )
        }
    }

    Box(modifier = modifier) {
        particles.forEach { p ->
            key(p.xFraction) {
                FloatingHeart(p, heightPx, colors.peach)
            }
        }
    }
}

@Composable
private fun FloatingHeart(spec: HeartSpec, heightPx: Float, tint: Color) {
    val transition = rememberInfiniteTransition(label = "heart")
    val y by transition.animateFloat(
        initialValue = 1.15f,
        targetValue = -0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.duration, delayMillis = spec.delay, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "y"
    )
    val sway by transition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.duration / 3, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sway"
    )

    Text(
        text = "🤍",
        fontSize = spec.size.sp,
        modifier = Modifier.graphicsLayer {
            translationY = heightPx * y
            translationX = (spec.xFraction - 0.5f) * 320f + sway * 22f
            alpha = (0.05f + 0.2f * y.coerceIn(0f, 1f))
        },
        color = tint.copy(alpha = 0.4f)
    )
}

// ─────────────────────────────────────────
// 庆祝花瓣雨
// ─────────────────────────────────────────
private data class PetalSpec(
    val xFraction: Float,
    val emoji: String,
    val size: Float,
    val duration: Int,
    val delay: Int,
)

/** 重要日子当天的全屏庆祝:花瓣飘落 + 祝福语 */
@Composable
fun CelebrationOverlay(
    title: String,
    subtitle: String,
    onDismiss: () -> Unit,
) {
    val colors = LocalAppColors.current
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current
    val heightPx = with(density) { screenHeight.toPx() }

    val petals = remember {
        List(18) {
            PetalSpec(
                xFraction = Random.nextFloat(),
                emoji = listOf("🌸", "💗", "✨", "🌷", "💛").random(),
                size = Random.nextInt(14, 26).toFloat(),
                duration = Random.nextInt(3500, 6500),
                delay = Random.nextInt(0, 2500),
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.35f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
    ) {
        petals.forEach { petal ->
            key(petal.xFraction) {
                FallingPetal(petal, heightPx)
            }
        }

        // 中央祝福卡片
        var appeared by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { delay(200); appeared = true }
        val cardScale by animateFloatAsState(
            targetValue = if (appeared) 1f else 0.6f,
            animationSpec = spring(dampingRatio = 0.55f, stiffness = 260f),
            label = "cardScale"
        )
        val cardAlpha by animateFloatAsState(
            targetValue = if (appeared) 1f else 0f,
            animationSpec = tween(500),
            label = "cardAlpha"
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { scaleX = cardScale; scaleY = cardScale; alpha = cardAlpha }
                .padding(24.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp))
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(colors.gradientPeach, colors.card)
                    )
                )
                .padding(horizontal = 28.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                title,
                style = MaterialTheme.typography.headlineSmall,
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.rose),
                shape = RoundedCornerShape(50)
            ) {
                Text("收下祝福 💕", color = Color.White)
            }
        }
    }
}

@Composable
private fun FallingPetal(spec: PetalSpec, heightPx: Float) {
    val transition = rememberInfiniteTransition(label = "petal")
    val y by transition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.duration, delayMillis = spec.delay, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "py"
    )
    val rot by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(spec.duration, delayMillis = spec.delay, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pr"
    )

    Text(
        text = spec.emoji,
        fontSize = spec.size.sp,
        modifier = Modifier.graphicsLayer {
            translationY = heightPx * y
            translationX = (spec.xFraction - 0.5f) * 2f * heightPx * 0.45f
            rotationZ = rot
        },
    )
}

/** 倒计时进度环 */
@Composable
fun RingProgress(
    progress: Float,
    modifier: Modifier = Modifier.size(64.dp),
    stroke: Dp = 6.dp,
    trackColor: Color,
    color: Color,
    content: @Composable BoxScope.() -> Unit = {},
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
            progress = { progress.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = stroke,
            strokeCap = StrokeCap.Round,
            color = color,
            trackColor = trackColor,
        )
        content()
    }
}

/** 启动动画:两颗爱心靠近合并成一颗 + 应用名浮现 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val colors = LocalAppColors.current
    var merged by remember { mutableStateOf(false) }
    var nameShown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(550)
        merged = true
        delay(650)
        nameShown = true
        delay(1300)
        onFinished()
    }

    val approach by animateFloatAsState(
        targetValue = if (merged) 1f else 0f,
        animationSpec = tween(900, easing = EaseInOutCubic),
        label = "approach"
    )
    val mergeScale by animateFloatAsState(
        targetValue = if (merged) 1f else 0f,
        animationSpec = spring(dampingRatio = 0.5f, stiffness = 240f),
        label = "merge"
    )
    val nameAlpha by animateFloatAsState(
        targetValue = if (nameShown) 1f else 0f,
        animationSpec = tween(900),
        label = "name"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.gradientPeach.copy(alpha = 0.55f), colors.background)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        val density = LocalDensity.current
        val halfW = with(density) { maxWidth.toPx() } * 0.22f

        Text(
            text = "💗",
            fontSize = 52.sp,
            modifier = Modifier.graphicsLayer {
                translationX = -halfW + (halfW - 14f) * approach
                alpha = if (merged) 0f else 1f
            }
        )
        Text(
            text = "💗",
            fontSize = 52.sp,
            modifier = Modifier.graphicsLayer {
                translationX = halfW - (halfW - 14f) * approach
                alpha = if (merged) 0f else 1f
            }
        )
        Text(
            text = "❤️",
            fontSize = 64.sp,
            modifier = Modifier.graphicsLayer {
                scaleX = mergeScale
                scaleY = mergeScale
                alpha = if (merged) 1f else 0f
            }
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { alpha = nameAlpha }
                .offset(y = 160.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "拾光",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = colors.textPrimary,
                letterSpacing = 12.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "把日子过成糖",
                fontSize = 14.sp,
                color = colors.textSecondary,
                letterSpacing = 4.sp
            )
        }
    }
}
