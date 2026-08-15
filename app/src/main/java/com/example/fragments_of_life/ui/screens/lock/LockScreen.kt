package com.example.fragments_of_life.ui.screens.lock

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.example.fragments_of_life.data.local.CouplePreferences
import com.example.fragments_of_life.ui.components.FloatingHeartsBackground
import com.example.fragments_of_life.ui.theme.LocalAppColors
import kotlinx.coroutines.delay

/** 应用锁:4 位 PIN 解锁 */
@Composable
fun LockScreen(
    prefs: CouplePreferences,
    onUnlocked: () -> Unit,
) {
    val colors = LocalAppColors.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    var shake by remember { mutableStateOf(0f) }
    val beat by rememberHeartbeatScale2()

    // 输满 4 位校验
    LaunchedEffect(pin) {
        if (pin.length == 4) {
            if (prefs.verifyPin(pin)) {
                delay(150)
                onUnlocked()
            } else {
                error = true
                shake = 1f
                delay(450)
                pin = ""
                error = false
            }
        }
    }

    val shakeOffset by animateFloatAsState(
        targetValue = shake,
        animationSpec = tween(80),
        label = "shake"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(colors.gradientPeach.copy(alpha = 0.4f), colors.background)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        FloatingHeartsBackground(count = 6)

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "🔒",
                fontSize = 44.sp,
                modifier = Modifier.graphicsLayer {
                    scaleX = beat
                    scaleY = beat
                    translationX = if (shakeOffset > 0f) (shakeOffset * 12f) - 6f else 0f
                }
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "只属于你们的小世界",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "输入密码解锁",
                style = MaterialTheme.typography.labelMedium,
                color = colors.textTertiary
            )

            Spacer(Modifier.height(22.dp))

            // 密码圆点
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.graphicsLayer {
                    translationX = if (shakeOffset > 0f) (shakeOffset * 12f) - 6f else 0f
                }
            ) {
                repeat(4) { i ->
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    error -> colors.softRed
                                    pin.length > i -> colors.peach
                                    else -> colors.taroLight.copy(alpha = 0.5f)
                                }
                            )
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            PinPad(
                onDigit = { d -> if (pin.length < 4) pin += d },
                onBackspace = { pin = pin.dropLast(1) }
            )

            Spacer(Modifier.height(20.dp))
            Text(
                "忘记密码?清除应用数据后重新设置",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textTertiary
            )
        }
    }
}

@Composable
private fun rememberHeartbeatScale2(): State<Float> {
    val transition = rememberInfiniteTransition(label = "lockBeat")
    return transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "beat"
    )
}

@Composable
private fun PinPad(onDigit: (Int) -> Unit, onBackspace: () -> Unit) {
    val colors = LocalAppColors.current
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        for (row in listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9))) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { d ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
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
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Box(Modifier.size(60.dp))
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(colors.peachLight.copy(alpha = 0.6f))
                    .clickable { onDigit(0) },
                contentAlignment = Alignment.Center
            ) {
                Text("0", style = MaterialTheme.typography.titleMedium, color = colors.textPrimary)
            }
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(colors.card)
                    .clickable { onBackspace() },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Backspace, null, tint = colors.textSecondary, modifier = Modifier.size(22.dp))
            }
        }
    }
}
