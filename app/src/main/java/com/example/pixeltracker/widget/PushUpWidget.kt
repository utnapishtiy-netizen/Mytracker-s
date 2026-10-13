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
import java.time.LocalDate

class PushUpWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val dao = AppDatabase.getDatabase(context).trackerDao()
        val todayStr = LocalDate.now().toString()
        // Получаем данные за сегодняшний день синхронно для виджета
        // (или берем нули, если записей еще нет)
        
        provideContent {
            GlanceTheme {
                WidgetContent()
            }
        }
    }

    @Composable
    fun WidgetContent() {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(GlanceTheme.colors.surfaceVariant)
                .padding(16.dp)
                .clickable(actionStartActivity<MainActivity>()),
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            Text(
                text = "MyTracker",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = GlanceTheme.colors.onSurfaceVariant
                )
            )
            Spacer(modifier = GlanceModifier.height(8.dp))
            Text(
                text = "Нажмите для открытия",
                style = TextStyle(
                    fontSize = 12.sp,
                    color = GlanceTheme.colors.primary
                )
            )
        }
    }
}
