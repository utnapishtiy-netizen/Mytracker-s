package com.example.pixeltracker.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
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
import com.example.pixeltracker.MainActivity
import com.example.pixeltracker.ui.theme.EInkLightColorScheme // или цвета для виджета

class PixelTrackerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                // Минималистичный e-ink дизайн виджета: черный фон, белая рамка или наоборот
                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .background(androidx.glance.ColorProvider(android.graphics.Color.BLACK))
                        .clickable(actionStartActivity<MainActivity>())
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
                        verticalAlignment = Alignment.Vertical.CenterVertically
                    ) {
                        Text(
                            text = "MYTRACKER",
                            style = TextStyle(
                                color = androidx.glance.ColorProvider(android.graphics.Color.WHITE),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.height(8.dp))
                        Text(
                            text = "Открыть",
                            style = TextStyle(
                                color = androidx.glance.ColorProvider(android.graphics.Color.LIGHT_GRAY),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}

class PixelTrackerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = PixelTrackerWidget()
}
