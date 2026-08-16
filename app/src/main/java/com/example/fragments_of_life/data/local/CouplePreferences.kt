package com.example.fragments_of_life.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.fragments_of_life.data.model.CoupleInfo
import java.security.MessageDigest
import java.time.LocalDate

/**
 * 情侣信息 + 应用设置的本地存储(SharedPreferences)
 */
class CouplePreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("couple_prefs", Context.MODE_PRIVATE)

    fun saveCoupleInfo(info: CoupleInfo) {
        prefs.edit()
            .putString("my_name", info.myName)
            .putString("partner_name", info.partnerName)
            .putString("my_emoji", info.myEmoji)
            .putString("partner_emoji", info.partnerEmoji)
            .putString("my_birthday", info.myBirthday.toString())
            .putString("partner_birthday", info.partnerBirthday.toString())
            .putString("anniversary", info.anniversaryDate.toString())
            .putInt("period_cycle", info.partnerPeriodCycleDays)
            .putInt("period_duration", info.partnerPeriodDuration)
            .putString("period_last_start", info.partnerPeriodLastStart?.toString())
            .apply()
    }

    fun loadCoupleInfo(): CoupleInfo {
        val today = LocalDate.now()
        return CoupleInfo(
            myName = prefs.getString("my_name", "我") ?: "我",
            partnerName = prefs.getString("partner_name", "TA") ?: "TA",
            myEmoji = prefs.getString("my_emoji", "🐰") ?: "🐰",
            partnerEmoji = prefs.getString("partner_emoji", "🐻") ?: "🐻",
            myBirthday = parseDate(prefs.getString("my_birthday", null), today),
            partnerBirthday = parseDate(prefs.getString("partner_birthday", null), today),
            anniversaryDate = parseDate(prefs.getString("anniversary", null), today.minusDays(520)),
            partnerPeriodCycleDays = prefs.getInt("period_cycle", 28),
            partnerPeriodDuration = prefs.getInt("period_duration", 5),
            partnerPeriodLastStart = prefs.getString("period_last_start", null)?.let {
                try { LocalDate.parse(it) } catch (_: Exception) { null }
            }
        )
    }

    // ===== 应用锁(PIN) =====
    val lockEnabled: Boolean get() = prefs.getBoolean("lock_enabled", false)

    fun setLockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("lock_enabled", enabled).apply()
    }

    fun savePin(pin: String) {
        prefs.edit().putString("pin_hash", sha256(pin)).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val hash = prefs.getString("pin_hash", null) ?: return false
        return hash == sha256(pin)
    }

    private fun sha256(input: String): String =
        MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray())
            .joinToString("") { "%02x".format(it) }

    // ===== 提醒设置 =====
    val remindersEnabled: Boolean get() = prefs.getBoolean("reminders_enabled", true)

    fun setRemindersEnabled(enabled: Boolean) {
        prefs.edit().putBoolean("reminders_enabled", enabled).apply()
    }

    /** 是否已经询问过通知权限(只问一次) */
    val notifAsked: Boolean get() = prefs.getBoolean("notif_asked", false)

    fun setNotifAsked(asked: Boolean) {
        prefs.edit().putBoolean("notif_asked", asked).apply()
    }

    // ===== TA的小宇宙:自定义分类 =====
    fun getCustomNoteCategories(): List<String> =
        prefs.getStringSet("custom_note_categories", emptySet())?.toList() ?: emptyList()

    fun saveCustomNoteCategories(categories: List<String>) {
        prefs.edit().putStringSet("custom_note_categories", categories.toSet()).apply()
    }

    // ===== 主题配色 =====
    val themeKey: String get() = prefs.getString("theme_key", "peach") ?: "peach"

    fun saveThemeKey(key: String) {
        prefs.edit().putString("theme_key", key).apply()
    }

    // ===== 演示数据开关 =====
    /** 用户执行「初始化数据」后为 true:不再自动填充演示数据 */
    fun isDemoSeedDisabled(): Boolean = prefs.getBoolean("demo_seed_disabled", false)

    fun setDemoSeedDisabled(disabled: Boolean) {
        prefs.edit().putBoolean("demo_seed_disabled", disabled).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private fun parseDate(str: String?, default: LocalDate): LocalDate {
        return str?.let {
            try { LocalDate.parse(it) } catch (_: Exception) { default }
        } ?: default
    }

    companion object {
        @Volatile
        private var INSTANCE: CouplePreferences? = null

        fun getInstance(context: Context): CouplePreferences {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CouplePreferences(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
