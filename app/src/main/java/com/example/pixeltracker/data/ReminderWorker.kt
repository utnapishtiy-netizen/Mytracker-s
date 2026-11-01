package com.example.pixeltracker.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.pixeltracker.R
import kotlinx.coroutines.flow.first
import java.time.LocalDate

class ReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val dao = database.trackerDao()
        val todayStr = LocalDate.now().toString()
        val tracker = dao.getTrackerByDateSync(todayStr)

        val isCompleted = tracker != null && (
                tracker.stepsCount >= tracker.stepsGoal ||
                        tracker.pushUpsCount >= tracker.pushUpsGoal ||
                        tracker.squatsCount >= tracker.squatsGoal ||
                        tracker.distanceKm >= tracker.distanceGoalKm
                )

        if (!isCompleted) {
            sendNotification("MyTracker", "Не забудьте выполнить цели на сегодня и сохранить стрик!")
        }

        return Result.success()
    }

    private fun sendNotification(title: String, message: String) {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "tracker_reminder_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Напоминания трекера", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(android.R.drawable.ic_menu_agenda)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
