package com.example.fragments_of_life.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.fragments_of_life.data.local.AppDatabase
import com.example.fragments_of_life.data.model.ImportantDate
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.PartnerNote
import com.example.fragments_of_life.data.model.WhisperLetter
import com.example.fragments_of_life.data.model.WishItem
import com.example.fragments_of_life.util.mmdd
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class LifeViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).momentDao()

    // ===== 生活碎片 =====
    val allMoments: StateFlow<List<Moment>> = dao.getAllMoments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentMoments: StateFlow<List<Moment>> = dao.getRecentMoments(200)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** 那年今日:往年同月日的记录 */
    val momentsOnThisDay: StateFlow<List<Moment>> =
        dao.getMomentsOnThisDay(mmdd(LocalDate.now()), LocalDate.now())
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth.asStateFlow()

    val momentsInMonth: StateFlow<List<Moment>> = _selectedMonth.flatMapLatest { ym ->
        dao.getMomentsBetween(ym.atDay(1), ym.atEndOfMonth())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val datesWithMoments: StateFlow<List<LocalDate>> = _selectedMonth.flatMapLatest { ym ->
        dao.getDatesWithMoments(ym.atDay(1), ym.atEndOfMonth())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== 重要日子 =====
    val allImportantDates: StateFlow<List<ImportantDate>> = dao.getAllImportantDates()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val importantDatesInMonth: StateFlow<List<ImportantDate>> = _selectedMonth.flatMapLatest { ym ->
        dao.getImportantDatesBetween(ym.atDay(1), ym.atEndOfMonth())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== 愿望清单 =====
    val allWishes: StateFlow<List<WishItem>> = dao.getAllWishes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== 悄悄话信箱 =====
    val allLetters: StateFlow<List<WhisperLetter>> = dao.getAllLetters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== TA的小宇宙 =====
    val allPartnerNotes: StateFlow<List<PartnerNote>> = dao.getAllPartnerNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ===== 操作 =====
    fun setMonth(yearMonth: YearMonth) {
        _selectedMonth.value = yearMonth
    }

    fun insertMoment(moment: Moment) {
        viewModelScope.launch { dao.insertMoment(moment) }
    }

    fun updateMoment(moment: Moment) {
        viewModelScope.launch { dao.updateMoment(moment) }
    }

    fun deleteMoment(moment: Moment) {
        viewModelScope.launch { dao.deleteMoment(moment) }
    }

    fun insertImportantDate(date: ImportantDate) {
        viewModelScope.launch { dao.insertImportantDate(date) }
    }

    fun updateImportantDate(date: ImportantDate) {
        viewModelScope.launch { dao.updateImportantDate(date) }
    }

    fun deleteImportantDate(date: ImportantDate) {
        viewModelScope.launch { dao.deleteImportantDate(date) }
    }

    fun insertWish(wish: WishItem) {
        viewModelScope.launch { dao.insertWish(wish) }
    }

    fun updateWish(wish: WishItem) {
        viewModelScope.launch { dao.updateWish(wish) }
    }

    fun deleteWish(wish: WishItem) {
        viewModelScope.launch { dao.deleteWish(wish) }
    }

    fun toggleWish(wish: WishItem) {
        viewModelScope.launch {
            dao.updateWish(
                wish.copy(
                    done = !wish.done,
                    doneAt = if (!wish.done) System.currentTimeMillis() else null
                )
            )
        }
    }

    // ===== 悄悄话操作 =====
    fun insertLetter(letter: WhisperLetter) {
        viewModelScope.launch { dao.insertLetter(letter) }
    }

    fun markLetterOpened(letter: WhisperLetter) {
        if (letter.opened) return
        viewModelScope.launch { dao.updateLetter(letter.copy(opened = true)) }
    }

    fun deleteLetter(letter: WhisperLetter) {
        viewModelScope.launch { dao.deleteLetter(letter) }
    }

    // ===== TA的小宇宙操作 =====
    fun insertPartnerNote(note: PartnerNote) {
        viewModelScope.launch { dao.insertPartnerNote(note) }
    }

    fun updatePartnerNote(note: PartnerNote) {
        viewModelScope.launch { dao.updatePartnerNote(note) }
    }

    fun deletePartnerNote(note: PartnerNote) {
        viewModelScope.launch { dao.deletePartnerNote(note) }
    }

    fun clearAll() {
        viewModelScope.launch {
            dao.clearAllMoments()
            dao.clearAllImportantDates()
            dao.clearAllWishes()
            dao.clearAllLetters()
            dao.clearAllPartnerNotes()
        }
    }
}

// 轻量年月包装(避免 java.time.YearMonth 的 StateFlow 序列化问题)
data class YearMonth(val year: Int, val month: Int) {
    fun atDay(day: Int) = LocalDate.of(year, month, day)
    fun atEndOfMonth() = LocalDate.of(year, month, lengthOfMonth())
    fun lengthOfMonth() = LocalDate.of(year, month, 1).lengthOfMonth()
    fun minusMonths(n: Int): YearMonth {
        val d = LocalDate.of(year, month, 1).minusMonths(n.toLong())
        return YearMonth(d.year, d.monthValue)
    }
    fun plusMonths(n: Int): YearMonth {
        val d = LocalDate.of(year, month, 1).plusMonths(n.toLong())
        return YearMonth(d.year, d.monthValue)
    }

    companion object {
        fun now() = YearMonth(LocalDate.now().year, LocalDate.now().monthValue)
    }
}
