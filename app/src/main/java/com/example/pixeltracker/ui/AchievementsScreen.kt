package com.example.pixeltracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pixeltracker.data.DailyTracker

data class Achievement(
    val title: String,
    val description: String,
    val isUnlocked: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    historyList: List<DailyTracker>,
    currentStreak: Int,
    onBackClick: () -> Unit
) {
    val totalPushUps = historyList.sumOf { it.pushUpsCount }
    val totalSquats = historyList.sumOf { it.squatsCount }
    val totalSteps = historyList.sumOf { it.stepsCount }

    val achievements = listOf(
        Achievement("Первые шаги", "Сделайте первую запись в трекере", historyList.isNotEmpty()),
        Achievement("Старт серии", "Удерживайте стрик от 3 дней", currentStreak >= 3),
        Achievement("Недельный марафонец", "Удерживайте стрик от 7 дней", currentStreak >= 7),
        Achievement("Железная выносливость", "Сделайте суммарно 100 отжиманий", totalPushUps >= 100),
        Achievement("Приседайте на здоровье", "Сделайте суммарно 200 приседаний", totalSquats >= 200),
        Achievement("Путешественник", "Пройдите суммарно 50 000 шагов", totalSteps >= 50000)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достижения", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(achievements) { item ->
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (item.isUnlocked)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isUnlocked) Icons.Rounded.EmojiEvents else Icons.Rounded.Lock,
                            contentDescription = null,
                            tint = if (item.isUnlocked)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (item.isUnlocked)
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (item.isUnlocked)
                                    MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
