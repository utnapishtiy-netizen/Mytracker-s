package com.example.pixeltracker.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.data.DailyTracker
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDate

class AddPushUpAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val dao = AppDatabase.getDatabase(context).trackerDao()
        val today = LocalDate.now().toString()

        val current = dao.getTrackerByDate(today).firstOrNull() ?: DailyTracker(date = today)
        dao.insertOrUpdate(current.copy(pushUpsCount = current.pushUpsCount + 10))

        PushUpWidget().update(context, glanceId)
    }
}
