package com.example.fragments_of_life.data.local

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * 数据备份与恢复:把 Room 数据库和偏好设置打包成 zip,或从 zip 恢复。
 * 备份内容:
 *   databases/fragments_of_life.db(-wal/-shm)
 *   shared_prefs/couple_prefs.xml
 */
object BackupHelper {

    private const val TAG = "BackupHelper"
    const val DB_NAME = "fragments_of_life.db"

    /** 导出备份到指定 Uri(通常来自 CreateDocument 选择器) */
    fun exportTo(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                ZipOutputStream(os).use { zip ->
                    // 数据库文件(含 WAL,保证最新数据不丢)
                    val dbDir = context.getDatabasePath(DB_NAME).parentFile
                    listOf(DB_NAME, "$DB_NAME-wal", "$DB_NAME-shm").forEach { name ->
                        val f = File(dbDir, name)
                        if (f.exists()) {
                            zip.putNextEntry(ZipEntry("databases/$name"))
                            f.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                    // 偏好设置
                    val prefsFile = File(context.applicationInfo.dataDir, "shared_prefs/couple_prefs.xml")
                    if (prefsFile.exists()) {
                        zip.putNextEntry(ZipEntry("shared_prefs/couple_prefs.xml"))
                        prefsFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                }
            } != null
        } catch (e: Exception) {
            Log.e(TAG, "导出失败", e)
            false
        }
    }

    /** 从 zip 恢复数据(覆盖当前数据,完成后需重启应用生效) */
    fun importFrom(context: Context, uri: Uri): Boolean {
        return try {
            val dbDir = context.getDatabasePath(DB_NAME).parentFile
            val prefsDir = File(context.applicationInfo.dataDir, "shared_prefs")
            prefsDir.mkdirs()

            context.contentResolver.openInputStream(uri)?.use { ins ->
                ZipInputStream(ins).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        val name = entry.name.substringAfterLast('/')
                        val target: File? = when {
                            name == DB_NAME || name == "$DB_NAME-wal" || name == "$DB_NAME-shm" ->
                                File(dbDir, name)
                            name == "couple_prefs.xml" -> File(prefsDir, name)
                            else -> null
                        }
                        if (target != null) {
                            target.outputStream().use { out -> zip.copyTo(out) }
                        }
                        zip.closeEntry()
                    }
                }
            } != null
        } catch (e: Exception) {
            Log.e(TAG, "恢复失败", e)
            false
        }
    }
}
