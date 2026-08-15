package com.example.fragments_of_life.util

import com.example.fragments_of_life.data.model.ImportantDate
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * 计算重要日子的下一次到来日期。
 * - 每年重复:取今年同月日;若已过则顺延一年。
 * - 不重复:仅当还未过去时返回,已过则返回 null。
 */
fun nextOccurrence(date: LocalDate, repeatYearly: Boolean, today: LocalDate): LocalDate? {
    if (repeatYearly) {
        val thisYear = date.withYear(today.year)
        return if (thisYear >= today) thisYear else thisYear.plusYears(1)
    }
    return if (date >= today) date else null
}

/** 倒计时事件(用于首页 & 纪念日页) */
data class UpcomingEvent(
    val event: ImportantDate,
    val next: LocalDate,          // 下一次到来日期
    val daysLeft: Long,           // 距离今天的天数(当天为 0)
    val progress: Float,          // 进度环填充比例 0..1
    val isToday: Boolean,
)

/**
 * 把重要日子列表转换为按「最近先到」排序的倒计时事件。
 * 已过且不重复的日子排到末尾(daysLeft 为负数)。
 */
fun buildUpcomingEvents(dates: List<ImportantDate>, today: LocalDate): List<UpcomingEvent> {
    val upcoming = mutableListOf<UpcomingEvent>()
    val past = mutableListOf<UpcomingEvent>()

    dates.forEach { d ->
        val next = nextOccurrence(d.date, d.repeatYearly, today)
        if (next == null) {
            val daysLeft = ChronoUnit.DAYS.between(today, d.date)
            past += UpcomingEvent(d, d.date, daysLeft, 0f, false)
        } else {
            val daysLeft = ChronoUnit.DAYS.between(today, next)
            val isToday = daysLeft == 0L
            val progress = if (d.repeatYearly) {
                val last = next.minusYears(1)
                val interval = ChronoUnit.DAYS.between(last, next).coerceAtLeast(1).toFloat()
                (ChronoUnit.DAYS.between(last, today).coerceIn(0, interval.toLong())) / interval
            } else {
                if (isToday) 1f else 0f
            }
            upcoming += UpcomingEvent(d, next, daysLeft, progress.coerceIn(0f, 1f), isToday)
        }
    }
    return upcoming.sortedBy { it.daysLeft } + past.sortedByDescending { it.daysLeft }
}

/** 今天是否有「在一起纪念日」 */
fun isCoupleAnniversaryToday(anniversaryDate: LocalDate, today: LocalDate): Boolean =
    anniversaryDate.month == today.month && anniversaryDate.dayOfMonth == today.dayOfMonth

/** "MM-dd" 格式,用于那年今日查询 */
fun mmdd(date: LocalDate): String =
    "%02d-%02d".format(date.monthValue, date.dayOfMonth)
