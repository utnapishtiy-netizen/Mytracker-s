package com.example.pixeltracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixeltracker.TrackerViewModel

@Composable
fun StatsScreen(
    viewModel: TrackerViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "СТАТИСТИКА И ЦЕЛИ", color = Color.White, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Здесь будет аналитика активности", color = Color.Gray, fontSize = 14.sp)
    }
}
