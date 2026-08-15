package com.example.fragments_of_life.data.local

import androidx.room.*
import com.example.fragments_of_life.data.model.ImportantDate
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.PartnerNote
import com.example.fragments_of_life.data.model.WhisperLetter
import com.example.fragments_of_life.data.model.WishItem
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface MomentDao {
    // ===== 生活碎片 =====
    @Insert
    suspend fun insertMoment(moment: Moment): Long

    @Update
    suspend fun updateMoment(moment: Moment)

    @Delete
    suspend fun deleteMoment(moment: Moment)

    @Query("SELECT * FROM moments ORDER BY date DESC")
    fun getAllMoments(): Flow<List<Moment>>

    @Query("SELECT * FROM moments WHERE date = :date")
    suspend fun getMomentsByDate(date: LocalDate): List<Moment>

    @Query("SELECT * FROM moments WHERE date BETWEEN :start AND :end ORDER BY date ASC")
    fun getMomentsBetween(start: LocalDate, end: LocalDate): Flow<List<Moment>>

    @Query("SELECT * FROM moments ORDER BY date DESC LIMIT :limit")
    fun getRecentMoments(limit: Int): Flow<List<Moment>>

    @Query("SELECT DISTINCT date FROM moments WHERE date BETWEEN :start AND :end")
    fun getDatesWithMoments(start: LocalDate, end: LocalDate): Flow<List<LocalDate>>

    /** 那年今日:同一月日、更早年份的记录(mmdd 形如 "05-20") */
    @Query("SELECT * FROM moments WHERE substr(date, 6) = :mmdd AND date < :today ORDER BY date DESC")
    fun getMomentsOnThisDay(mmdd: String, today: LocalDate): Flow<List<Moment>>

    // ===== 重要日子 =====
    @Insert
    suspend fun insertImportantDate(date: ImportantDate): Long

    @Update
    suspend fun updateImportantDate(date: ImportantDate)

    @Delete
    suspend fun deleteImportantDate(date: ImportantDate)

    @Query("SELECT * FROM important_dates ORDER BY date ASC")
    fun getAllImportantDates(): Flow<List<ImportantDate>>

    @Query("""
        SELECT * FROM important_dates 
        WHERE date BETWEEN :start AND :end 
        ORDER BY date ASC
    """)
    fun getImportantDatesBetween(start: LocalDate, end: LocalDate): Flow<List<ImportantDate>>

    @Query("""
        SELECT * FROM important_dates 
        WHERE date >= :today 
        ORDER BY date ASC
    """)
    fun getUpcomingImportantDates(today: LocalDate): Flow<List<ImportantDate>>

    // ===== 愿望清单 =====
    @Insert
    suspend fun insertWish(wish: WishItem): Long

    @Update
    suspend fun updateWish(wish: WishItem)

    @Delete
    suspend fun deleteWish(wish: WishItem)

    @Query("SELECT * FROM wishes ORDER BY done ASC, createdAt DESC")
    fun getAllWishes(): Flow<List<WishItem>>

    // ===== 悄悄话信箱 =====
    @Insert
    suspend fun insertLetter(letter: WhisperLetter): Long

    @Update
    suspend fun updateLetter(letter: WhisperLetter)

    @Delete
    suspend fun deleteLetter(letter: WhisperLetter)

    @Query("SELECT * FROM whisper_letters ORDER BY unlockDate ASC, createdAt ASC")
    fun getAllLetters(): Flow<List<WhisperLetter>>

    // ===== TA的小宇宙 =====
    @Insert
    suspend fun insertPartnerNote(note: PartnerNote): Long

    @Update
    suspend fun updatePartnerNote(note: PartnerNote)

    @Delete
    suspend fun deletePartnerNote(note: PartnerNote)

    @Query("SELECT * FROM partner_notes ORDER BY createdAt DESC")
    fun getAllPartnerNotes(): Flow<List<PartnerNote>>

    // ===== 清理 =====
    @Query("DELETE FROM moments")
    suspend fun clearAllMoments()

    @Query("DELETE FROM important_dates")
    suspend fun clearAllImportantDates()

    @Query("DELETE FROM wishes")
    suspend fun clearAllWishes()

    @Query("DELETE FROM whisper_letters")
    suspend fun clearAllLetters()

    @Query("DELETE FROM partner_notes")
    suspend fun clearAllPartnerNotes()
}

// 类型转换器 - 用于存储 List<String> 和 LocalDate
class Converters {
    @TypeConverter
    fun fromTagList(tags: List<String>): String = tags.joinToString(",")

    @TypeConverter
    fun toTagList(tags: String): List<String> =
        if (tags.isEmpty()) emptyList() else tags.split(",")

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(date: String): LocalDate = LocalDate.parse(date)
}
