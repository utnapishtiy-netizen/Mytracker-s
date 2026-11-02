package com.example.pixeltracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pixeltracker.data.DailyTracker
import com.example.pixeltracker.triggerHapticFeedback
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(historyList: List<DailyTracker>, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val totalDays = historyList.size
    val totalSteps = historyList.sumOf { it.stepsCount }
    val avgSteps = if (totalDays > 0) totalSteps / totalDays else 0
    val totalDistance = historyList.sumOf { it.distanceKm.toDouble() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Статистика", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { triggerHapticFeedback(context); onBackClick() }) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Общие показатели",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                        StatRow(label = "Всего дней трекинга", value = "$totalDays")
                        StatRow(label = "Всего шагов", value = "$totalSteps")
                        StatRow(label = "Среднее шагов в день", value = "$avgSteps")
                        StatRow(label = "Пройдено километров", value = "%.2f км".format(totalDistance))
                    }
                }
            }
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
