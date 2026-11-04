package com.example.pixeltracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
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
    val currentGoal by viewModel.stepsGoal.collectAsState()
    val pushUps by viewModel.todayPushUps.collectAsState()
    val pGoal by viewModel.pushUpsGoal.collectAsState()
    val squats by viewModel.todaySquats.collectAsState()
    val sGoal by viewModel.squatsGoal.collectAsState()
    val activities by viewModel.customActivities.collectAsState(initial = emptyList())

    var goalInput by remember { mutableStateOf(currentGoal.toString()) }
    var activityName by remember { mutableStateOf("") }
    var activityUnit by remember { mutableStateOf("раз") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ЦЕЛИ И АКТИВНОСТИ",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Цель шагов
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Цель шагов на день ($currentGoal):", color = Color.Gray, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = goalInput,
                    onValueChange = { goalInput = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.White)
                        .clickable {
                            goalInput.toIntOrNull()?.let { viewModel.updateGoal(it) }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(text = "ЗАДАТЬ", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Быстрый прогресс отжиманий и приседаний
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Отжимания: $pushUps / $pGoal", color = Color.LightGray, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White)
                    .clickable { viewModel.addPushUps(10) }
                    .padding(8.dp)
            ) {
                Text(text = "+10 отжим.", color = Color.White, fontSize = 12.sp)
            }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "Приседания: $squats / $sGoal", color = Color.LightGray, fontSize = 14.sp)
            Box(
                modifier = Modifier
                    .border(1.dp, Color.White)
                    .clickable { viewModel.addSquats(10) }
                    .padding(8.dp)
            ) {
                Text(text = "+10 присед.", color = Color.White, fontSize = 12.sp)
            }
        }

        // Управление кастомными активностями
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Добавить кастомную активность:", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = activityName,
                    onValueChange = { activityName = it },
                    placeholder = { Text("Например: Подтягивания", color = Color.DarkGray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.White)
                        .clickable {
                            if (activityName.isNotBlank()) {
                                viewModel.addCustomActivity(activityName, activityUnit)
                                activityName = ""
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(text = "ДОБАВИТЬ", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(140.dp)) {
                items(activities) { act ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${act.name} (${act.unit})", color = Color.White, fontSize = 14.sp)
                        Text(
                            text = "[Удалить]",
                            color = Color.Red,
                            fontSize = 12.sp,
                            modifier = Modifier.clickable { viewModel.deleteCustomActivity(act.id) }
                        )
                    }
                }
            }
        }
    }
}
