package com.example.pixeltracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import com.example.pixeltracker.MainActivity

class PixelTrackerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            // Используем прямой стабильный UI-контейнер для Glance
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(ColorProvider(android.graphics.Color.BLACK))
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Text(
                        text = "MYTRACKER",
                        style = TextStyle(
                            color = ColorProvider(android.graphics.Color.WHITE),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = GlanceModifier.height(4.dp))
                    Text(
                        text = "Нажмите для открытия",
                        style = TextStyle(
                            color = ColorProvider(android.graphics.Color.LTGRAY),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}

class PixelTrackerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PixelTrackerWidget()
}
