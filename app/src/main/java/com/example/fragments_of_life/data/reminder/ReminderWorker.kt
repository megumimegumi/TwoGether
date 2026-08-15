package com.example.fragments_of_life.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.fragments_of_life.R
import com.example.fragments_of_life.data.local.AppDatabase
import com.example.fragments_of_life.data.local.CouplePreferences
import com.example.fragments_of_life.util.buildUpcomingEvents
import kotlinx.coroutines.flow.firstOrNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 每日检查一次:重要日子临近(当天 / remindBeforeDays 内)时,发一条温柔的提醒。
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val context = applicationContext
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return Result.success() // 无通知权限,静默跳过
            }

            val prefs = CouplePreferences.getInstance(context)
            if (!prefs.remindersEnabled) return Result.success()

            val dao = AppDatabase.getInstance(context).momentDao()
            val today = LocalDate.now()
            val dates = dao.getAllImportantDates().firstOrNull() ?: emptyList()
            if (dates.isNotEmpty()) {
                val events = buildUpcomingEvents(dates, today)
                events.forEach { event ->
                    val d = event.event
                    if (event.isToday) {
                        notify(context, "就是今天啦 🎉", "${d.title} 快乐!今天要好好庆祝呀 💕", d.id.toInt())
                    } else if (event.daysLeft in 1..d.remindBeforeDays.toLong()) {
                        val msg = if (event.daysLeft == 1L)
                            "明天就是「${d.title}」啦,准备好了吗?"
                        else
                            "还有 ${event.daysLeft} 天就是「${d.title}」,要准备小惊喜吗?"
                        notify(context, "💌 温柔提醒", msg, d.id.toInt())
                    }
                }
            }

            // ── 年度回顾预告 / 跨年提醒 ──
            val couple = prefs.loadCoupleInfo()
            val annivThisYear = couple.anniversaryDate.withYear(today.year)
            val daysToAnniv = ChronoUnit.DAYS.between(today, annivThisYear)
            when {
                daysToAnniv == 0L -> notify(
                    context,
                    "📽️ 年度回顾已生成",
                    "献给你们的这一年,打开「我们 → 年度回顾」一起看看吧 💕",
                    2001
                )
                daysToAnniv in 1..3 -> notify(
                    context,
                    "🎬 年度回忆生成中",
                    "还有 ${daysToAnniv} 天就是你们的纪念日啦,年度回顾正在生成,敬请期待 ❤",
                    2002
                )
            }
            if (today.monthValue == 12 && today.dayOfMonth == 31) {
                notify(
                    context,
                    "🎆 跨年快乐",
                    "这一年你们的故事,值得一起回味。打开「年度回顾」重温一下吧 ✨",
                    2003
                )
            }

            // ── TA的小宇宙:每周 / 每月复习 ──
            val notes = dao.getAllPartnerNotes().firstOrNull() ?: emptyList()
            if (notes.isNotEmpty()) {
                val weekAgoMillis = today.minusDays(7).toEpochDay() * 86400000L
                val weekCount = notes.count { it.createdAt >= weekAgoMillis }
                if (today.dayOfWeek == DayOfWeek.SUNDAY && weekCount > 0) {
                    notify(
                        context,
                        "📝 TA的小宇宙",
                        "这周你记下了 $weekCount 件关于TA的小事,点开复习一下吧",
                        3001
                    )
                }
                if (today.dayOfMonth == 1) {
                    notify(
                        context,
                        "🗂️ 恋爱备忘录",
                        "本月备忘录已生成,看看有没有漏掉TA随口说过的话",
                        3002
                    )
                }
            }

            // ── 悄悄话信箱:信件解锁提醒 ──
            val letters = dao.getAllLetters().firstOrNull() ?: emptyList()
            val unlockTomorrow = letters.filter { !it.opened && it.unlockDate == today.plusDays(1) }
            if (unlockTomorrow.isNotEmpty()) {
                notify(
                    context,
                    "💌 有封悄悄话明天解锁",
                    "寄存在信箱里的信,明天就可以拆开啦,期待一下 💕",
                    4001
                )
            }
            val unlockedRecently = letters.filter {
                !it.opened && !it.unlockDate.isAfter(today) && it.unlockDate >= today.minusDays(1)
            }
            if (unlockedRecently.isNotEmpty()) {
                val from = unlockedRecently.first().sign.ifBlank { "一位神秘的朋友" }
                notify(
                    context,
                    "💌 有一封信可以打开了",
                    if (unlockedRecently.size > 1)
                        "有 ${unlockedRecently.size} 封寄存已久的悄悄话,今天可以拆开啦"
                    else
                        "「$from」寄来的信已解锁,现在拆开刚刚好",
                    4002
                )
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun notify(context: Context, title: String, text: String, id: Int) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "纪念日提醒", NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "重要日子临近时的温柔提醒"
            }
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(1000 + id, notification)
        } catch (_: SecurityException) {
            // 权限被系统拒绝时静默
        }
    }

    companion object {
        const val CHANNEL_ID = "anniversary_reminders"
    }
}
