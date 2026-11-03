package com.example.pixeltracker.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getDatabase(applicationContext)
            val dao = db.trackerDao()

            // Получаем данные для резервной копии синхронно
            val trackers = dao.getAllTrackers() // или синк версия, если есть
            val activities = try { dao.getAllCustomActivitiesSync() } catch (e: Exception) { emptyList() }
            val notes = try { dao.getAllNotesSync() } catch (e: Exception) { emptyList() }

            // Логика бэкапа (пример сохранения / выгрузки)
            // ...

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
