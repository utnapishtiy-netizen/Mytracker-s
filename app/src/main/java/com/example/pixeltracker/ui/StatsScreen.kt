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
    val activities by viewModel.customActivities.collectAsState(initial = emptyList())

    var goalInput by remember { mutableStateOf(currentGoal.toString()) }
    var newActivityName by remember { mutableStateOf("") }
    var backupStatus by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "ЦЕЛИ И НАСТРОЙКИ",
            color = Color.White,
            fontSize = 18.sp,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        // Блок цели по шагам
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Цель шагов на день:", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
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
                            val goal = goalInput.toIntOrNull()
                            if (goal != null) viewModel.updateGoal(goal)
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(text = "ИЗМЕНИТЬ", color = Color.White, fontSize = 12.sp)
                }
            }
        }

        // Блок кастомных активностей
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(text = "Кастомные активности:", color = Color.Gray, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = newActivityName,
                    onValueChange = { newActivityName = it },
                    placeholder = { Text("Название активности", color = Color.DarkGray) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.White)
                        .clickable {
                            if (newActivityName.isNotBlank()) {
                                viewModel.addCustomActivity(newActivityName)
                                newActivityName = ""
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(text = "ДОБАВИТЬ", color = Color.White, fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.height(120.dp)) {
                items(activities) { act ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = act.name, color = Color.White, fontSize = 14.sp)
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

        // Блок бэкапов
        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Резервное копирование:", color = Color.Gray, fontSize = 14.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.White)
                        .clickable { viewModel.createBackup { backupStatus = it } }
                        .padding(12.dp)
                ) {
                    Text(text = "СОЗДАТЬ БЭКАП", color = Color.White, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .border(1.dp, Color.White)
                        .clickable { viewModel.restoreBackup { backupStatus = it } }
                        .padding(12.dp)
                ) {
                    Text(text = "ЗАГРУЗИТЬ БЭКАП", color = Color.White, fontSize = 12.sp)
                }
            }
            if (backupStatus.isNotEmpty()) {
                Text(text = backupStatus, color = Color.LightGray, fontSize = 12.sp)
            }
        }
    }
}
