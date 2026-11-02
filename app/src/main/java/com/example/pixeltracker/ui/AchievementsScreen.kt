package com.example.pixeltracker.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.pixeltracker.data.DailyTracker
import com.example.pixeltracker.triggerHapticFeedback

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(historyList: List<DailyTracker>, currentStreak: Int, onBackClick: () -> Unit) {
    val context = LocalContext.current
    val totalSteps = historyList.sumOf { it.stepsCount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Достижения", fontWeight = FontWeight.Bold) },
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
                AchievementCard(
                    title = "Первые шаги",
                    description = "Сделайте хотя бы 1000 шагов за день",
                    isUnlocked = totalSteps >= 1000
                )
            }
            item {
                AchievementCard(
                    title = "Марафонец",
                    description = "Наберите суммарно более 50 000 шагов",
                    isUnlocked = totalSteps >= 50000
                )
            }
            item {
                AchievementCard(
                    title = "Серия побед",
                    description = "Удерживайте стрик от 3 дней",
                    isUnlocked = currentStreak >= 3
                )
            }
        }
    }
}

@Composable
fun AchievementCard(title: String, description: String, isUnlocked: Boolean) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
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
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = if (isUnlocked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUnlocked) MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
