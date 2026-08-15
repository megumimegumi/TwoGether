package com.example.fragments_of_life.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/** 生活碎片类型 */
enum class MomentType(val emoji: String, val label: String) {
    DATE("💕", "约会"),
    MOVIE("🎬", "电影"),
    FOOD("🍽️", "美食"),
    TRAVEL("✈️", "旅行"),
    GIFT("🎁", "礼物"),
    ANNIVERSARY("💍", "纪念"),
    FIGHT("💢", "吵架"),
    MAKEUP("💝", "和好"),
    DAILY("📝", "日常"),
    LETTER("💌", "情书"),
    OTHER("✨", "其他"),
}

/** 心情 */
enum class Mood(val emoji: String, val label: String) {
    HAPPY("😊", "开心"),
    TOUCHED("🥹", "感动"),
    ROMANTIC("🥰", "浪漫"),
    SAD("😢", "难过"),
    ANGRY("😤", "生气"),
    MISSING("🥺", "想念"),
    GRATEFUL("🙏", "感恩"),
    EXCITED("🤩", "期待"),
}

/** 重要日子类型 */
enum class ImportantDateType(val emoji: String, val label: String) {
    BIRTHDAY("🎂", "生日"),
    ANNIVERSARY("💍", "纪念日"),
    PERIOD("🩸", "生理期"),
    FIRST_DATE("🌸", "第一次约会"),
    FIRST_KISS("💋", "初吻"),
    FIRST_TRIP("🧳", "第一次旅行"),
    CUSTOM("📌", "自定义"),
}

/** 重要等级 */
enum class Importance(val label: String) {
    NORMAL("普通"),
    IMPORTANT("重要"),
    VERY("非常重要"),
}

/** 生活碎片 - 核心实体 */
@Entity(tableName = "moments")
data class Moment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String = MomentType.DAILY.name,
    val title: String,
    val content: String,
    val date: LocalDate,
    val mood: String? = null,
    val imageUri: String? = null,
    val location: String? = null,
    val tags: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
)

/** 重要日子 */
@Entity(tableName = "important_dates")
data class ImportantDate(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String = ImportantDateType.CUSTOM.name,
    val title: String,
    val date: LocalDate,
    val repeatYearly: Boolean = true,
    val remindBeforeDays: Int = 3,
    val importance: String = Importance.NORMAL.name,
    val note: String = "",
)

/** 愿望清单条目 - 一起做的 100 件事 */
@Entity(tableName = "wishes")
data class WishItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val emoji: String = "⭐",
    val done: Boolean = false,
    val doneAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/** 悄悄话信件 - 到解锁时间才能拆开 */
@Entity(tableName = "whisper_letters")
data class WhisperLetter(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,
    val paperStyle: String = "cream",     // 信纸样式
    val sign: String = "",                // 署名,空 = 匿名
    val unlockDate: LocalDate,            // 解锁日期
    val createdAt: Long = System.currentTimeMillis(),
    val opened: Boolean = false,          // 是否已拆开
    val imageUri: String? = null,
)

/** 关于 TA 的一条小记录(TA的小宇宙) */
@Entity(tableName = "partner_notes")
data class PartnerNote(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val content: String,                          // 一句话内容
    val category: String = "📝 其他",              // 分类(默认或自定义)
    val tags: List<String> = emptyList(),
    val importance: String = Importance.NORMAL.name,
    val note: String = "",                        // 备注
    val linkedMomentId: Long? = null,             // 关联的点滴记录
    val createdAt: Long = System.currentTimeMillis(),
)

/** 情侣信息 */
data class CoupleInfo(
    val myName: String = "我",
    val partnerName: String = "TA",
    val myEmoji: String = "🐰",
    val partnerEmoji: String = "🐻",
    val myBirthday: LocalDate = LocalDate.now(),
    val partnerBirthday: LocalDate = LocalDate.now(),
    val anniversaryDate: LocalDate = LocalDate.now(),
    val partnerPeriodCycleDays: Int = 28,
    val partnerPeriodDuration: Int = 5,
    val partnerPeriodLastStart: LocalDate? = null,
)
