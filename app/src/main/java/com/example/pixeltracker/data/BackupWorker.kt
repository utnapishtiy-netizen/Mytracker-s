package com.example.pixeltracker.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val database = AppDatabase.getDatabase(applicationContext)
            val dao = database.trackerDao()

            // Получаем все данные из базы
            val history = dao.getAllTrackers().first()
            val customActivities = dao.getAllCustomActivities().first()
            val notes = dao.getAllNotes().first()

            // Формируем простой текст бэкапа (можно заменить на Gson, если подключена библиотека)
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm", Locale.getDefault()).format(Date())
            val backupContent = buildString {
                appendLine("=== MYTRACKER BACKUP ($timestamp) ===")
                appendLine("\n--- HISTORY (${history.size}) ---")
                history.forEach { appendLine(it.toString()) }
                
                appendLine("\n--- CUSTOM ACTIVITIES (${customActivities.size}) ---")
                customActivities.forEach { appendLine(it.toString()) }
                
                appendLine("\n--- NOTES (${notes.size}) ---")
                notes.forEach { appendLine(it.toString()) }
            }

            // Сохраняем в файл в директории документов приложения
            val file = File(applicationContext.getExternalFilesDir(null), "MyTracker_Backup_$timestamp.txt")
            file.writeText(backupContent)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
