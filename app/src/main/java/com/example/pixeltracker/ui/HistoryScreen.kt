package com.example.pixeltracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixeltracker.data.DailyTracker

@Composable
fun HistoryScreen(
    viewModel: TrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    val historyList by viewModel.allTrackers.collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp)
    ) {
        Text(
            text = "ИСТОРИЯ ДНЕЙ",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (historyList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Нет записей", color = Color.Gray, fontSize = 14.sp)
        }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(historyList) { item ->
                    HistoryItemRow(item = item)
                }
            }
        }
    }
}

@Composable
fun HistoryItemRow(item: DailyTracker) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray.copy(alpha = 0.3f))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = item.date, color = Color.White, fontSize = 14.sp)
        Text(text = "${item.stepsCount} шагов", color = Color.LightGray, fontSize = 14.sp)
        Text(text = "${String.format("%.1f", item.distanceKm)} км", color = Color.Gray, fontSize = 12.sp)
    }
}
