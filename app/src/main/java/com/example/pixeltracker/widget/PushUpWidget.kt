package com.example.pixeltracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.pixeltracker.MainActivity
import com.example.pixeltracker.data.AppDatabase
import java.time.LocalDate

class PushUpWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getDatabase(context).trackerDao()
        val today = LocalDate.now().toString()

        provideContent {
            val tracker by dao.getTrackerByDate(today).collectAsState(initial = null)
            val steps = tracker?.stepsCount ?: 0
            val stepsGoal = tracker?.stepsGoal ?: 10000
            val distanceKm = String.format("%.2f", steps * 0.00075f).replace(',', '.')
            val distanceGoal = tracker?.distanceGoalKm ?: 5.0f
            val pushUps = tracker?.pushUpsCount ?: 0
            val pushUpsGoal = tracker?.pushUpsGoal ?: 100
            val squats = tracker?.squatsCount ?: 0
            val squatsGoal = tracker?.squatsGoal ?: 100

            GlanceTheme {
                Column(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(GlanceTheme.colors.widgetBackground)
                        .cornerRadius(24.dp)
                        .padding(16.dp)
                        // Клик по всему виджету открывает MainActivity
                        .clickable(actionStartActivity<MainActivity>()),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "MyTracker",
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.primary
                        )
                    )
                    
                    Spacer(modifier = GlanceModifier.height(8.dp))

                    WidgetMetricRow(label = "Шаги", value = "$steps / $stepsGoal")
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    WidgetMetricRow(label = "Км", value = "$distanceKm / $distanceGoal")
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    WidgetMetricRow(label = "Отжимания", value = "$pushUps / $pushUpsGoal")
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    WidgetMetricRow(label = "Приседания", value = "$squats / $squatsGoal")
                }
            }
        }
    }
}

@Composable
fun WidgetMetricRow(label: String, value: String) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Horizontal.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = TextStyle(
                fontSize = 12.sp,
                color = GlanceTheme.colors.onSurfaceVariant
            ),
            modifier = GlanceModifier.defaultWeight()
        )
        Text(
            text = value,
            style = TextStyle(
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GlanceTheme.colors.onSurface
            )
        )
    }
}
