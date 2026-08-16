package com.example.fragments_of_life

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.content.ContextCompat
import com.example.fragments_of_life.data.local.AppDatabase
import com.example.fragments_of_life.data.local.CouplePreferences
import com.example.fragments_of_life.data.local.seedIfEmpty
import com.example.fragments_of_life.data.local.seedLettersIfEmpty
import com.example.fragments_of_life.data.local.seedPartnerNotesIfEmpty
import com.example.fragments_of_life.data.reminder.ReminderScheduler
import com.example.fragments_of_life.ui.components.SplashScreen
import com.example.fragments_of_life.ui.navigation.AppNavigation
import com.example.fragments_of_life.ui.screens.lock.LockScreen
import com.example.fragments_of_life.ui.theme.FragmentsOfLifeTheme
import com.example.fragments_of_life.ui.theme.LocalAppColors
import com.example.fragments_of_life.ui.theme.LocalThemeController
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = CouplePreferences.getInstance(this)

        // 同步补种演示数据:首次安装时方便预览效果。
        // 用户执行过「初始化数据」后不再自动填充(尊重用户清空数据的意愿)。
        if (!prefs.isDemoSeedDisabled()) {
            try {
                runBlocking(Dispatchers.IO) {
                    withTimeout(10_000) {
                        val db = AppDatabase.getInstance(this@MainActivity)
                        db.seedIfEmpty()
                        db.seedLettersIfEmpty()
                        db.seedPartnerNotesIfEmpty()
                    }
                }
            } catch (e: Exception) {
                Log.e("SeedData", "启动播种失败", e)
            }
        }

        // 每天检查一次纪念日/年度回顾提醒(Worker 内部会尊重提醒开关)
        ReminderScheduler.schedule(this)

        setContent {
            var themeKey by rememberSaveable { mutableStateOf(prefs.themeKey) }

            CompositionLocalProvider(
                LocalThemeController provides { key ->
                    themeKey = key
                    prefs.saveThemeKey(key)
                }
            ) {
                FragmentsOfLifeTheme(themeKey = themeKey) {
                var stage by rememberSaveable { mutableStateOf(0) } // 0=启动动画 1=应用锁 2=主界面

                // ── 首次进入主界面时,礼貌地申请通知权限(只问一次) ──
                var showNotifDialog by remember { mutableStateOf(false) }
                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { }

                LaunchedEffect(stage) {
                    if (stage == 2 && !prefs.notifAsked &&
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(
                            this@MainActivity, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        prefs.setNotifAsked(true)
                        showNotifDialog = true
                    }
                }

                when (stage) {
                    0 -> SplashScreen(onFinished = {
                        stage = if (prefs.lockEnabled) 1 else 2
                    })
                    1 -> LockScreen(
                        prefs = prefs,
                        onUnlocked = { stage = 2 }
                    )
                    else -> AppNavigation()
                }

                if (showNotifDialog) {
                    val colors = LocalAppColors.current
                    AlertDialog(
                        onDismissRequest = { showNotifDialog = false },
                        containerColor = colors.card,
                        shape = RoundedCornerShape(24.dp),
                        title = {
                            Text("🔔 想提醒你,不错过重要日子", color = colors.textPrimary, fontWeight = FontWeight.SemiBold)
                        },
                        text = {
                            Text(
                                "开启通知后,纪念日当天、年度回顾生成时,我们会温柔地提醒你,不会打扰你们的二人世界。",
                                color = colors.textSecondary
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                showNotifDialog = false
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }) {
                                Text("去开启", color = colors.rose, fontWeight = FontWeight.SemiBold)
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showNotifDialog = false }) {
                                Text("暂不", color = colors.textSecondary)
                            }
                        }
                    )
                }
                }
            }
        }
    }
}
