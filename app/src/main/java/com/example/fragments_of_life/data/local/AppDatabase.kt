package com.example.fragments_of_life.data.local

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.fragments_of_life.data.model.ImportantDate
import com.example.fragments_of_life.data.model.Moment
import com.example.fragments_of_life.data.model.PartnerNote
import com.example.fragments_of_life.data.model.WhisperLetter
import com.example.fragments_of_life.data.model.WishItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/** 4→5:新增悄悄话信箱表(真正的迁移,不清空用户数据) */
val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `whisper_letters` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`paperStyle` TEXT NOT NULL, " +
                    "`sign` TEXT NOT NULL, " +
                    "`unlockDate` TEXT NOT NULL, " +
                    "`createdAt` INTEGER NOT NULL, " +
                    "`opened` INTEGER NOT NULL, " +
                    "`imageUri` TEXT)"
        )
    }
}

/** 5→6:新增 TA的小宇宙 表 */
val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `partner_notes` (" +
                    "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "`content` TEXT NOT NULL, " +
                    "`category` TEXT NOT NULL, " +
                    "`tags` TEXT NOT NULL, " +
                    "`importance` TEXT NOT NULL, " +
                    "`note` TEXT NOT NULL, " +
                    "`linkedMomentId` INTEGER, " +
                    "`createdAt` INTEGER NOT NULL)"
        )
    }
}

@Database(
    entities = [Moment::class, ImportantDate::class, WishItem::class, WhisperLetter::class, PartnerNote::class],
    version = 6,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun momentDao(): MomentDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        @Volatile
        private var asyncSeedFired = false

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fragments_of_life.db"
                )
                    .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { db ->
                        INSTANCE = db
                        // 兜底异步补种(主路径在 MainActivity 同步播种)
                        if (!asyncSeedFired) {
                            asyncSeedFired = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    db.seedIfEmpty()
                                    db.seedLettersIfEmpty()
                                    db.seedPartnerNotesIfEmpty()
                                } catch (e: Exception) {
                                    Log.e("SeedData", "异步补种失败", e)
                                }
                            }
                        }
                    }
            }
        }
    }
}
