package com.example.pixeltracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.pixeltracker.MainActivity
import com.example.pixeltracker.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate

class PushUpWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Получаем данные из базы Room прямо в фоновом потоке виджета
        val todayStr = LocalDate.now().toString()
        val tracker = withContext(Dispatchers.IO) {
            try {
                AppDatabase.getDatabase(context).trackerDao().getTrackerByDateSync(todayStr)
            } catch (e: Exception) {
                null
            }
        }

        val steps = tracker?.stepsCount ?: 0
        val stepsGoal = tracker?.stepsGoal ?: 10000
        val pushUps = tracker?.pushUpsCount ?: 0
        val pushUpsGoal = tracker?.pushUpsGoal ?: 100
        val squats = tracker?.squatsCount ?: 0
        val squatsGoal = tracker?.squatsGoal ?: 100

        provideContent {
            GlanceTheme {
                WidgetContent(
                    steps = steps,
                    stepsGoal = stepsGoal,
                    pushUps = pushUps,
                    pushUpsGoal = pushUpsGoal,
                    squats = squats,
                    squatsGoal = squatsGoal
                )
            }
        }
    }

    @Composable
    fun WidgetContent(
        steps: Int,
        stepsGoal: Int,
        pushUps: Int,
        pushUpsGoal: Int,
        squats: Int,
        squatsGoal: Int
    ) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surfaceVariant)
                .padding(14.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.Start
        ) {
            Text(
                text = "MyTracker — Сегодня",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            
            Text(
                text = "👣 Шаги: $steps / $stepsGoal",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = "💪 Отжимания: $pushUps / $pushUpsGoal",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.height(4.dp))
            
            Text(
                text = "🦵 Приседания: $squats / $squatsGoal",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
        }
    }
}
