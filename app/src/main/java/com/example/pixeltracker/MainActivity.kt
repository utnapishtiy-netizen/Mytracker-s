package com.example.pixeltracker

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pixeltracker.ui.*

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        setContent {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black
            ) {
                MainContainer()
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissionsToRequest = mutableListOf<String>()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (permissionsToRequest.isNotEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}

enum class Screen {
    TRACKER, STATS, MAP, HISTORY, NOTES, ACHIEVEMENTS
}

@Composable
fun MainContainer() {
    val viewModel: TrackerViewModel = viewModel()
    var currentScreen by remember { mutableStateOf(Screen.TRACKER) }

    Scaffold(
        containerColor = Color.Black,
        bottomBar = {
            NavigationBar(
                containerColor = Color.Black,
                contentColor = Color.White
            ) {
                NavigationBarItem(
                    selected = currentScreen == Screen.TRACKER,
                    onClick = { currentScreen = Screen.TRACKER },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Главная", tint = Color.White) },
                    label = { Text("Трекер", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.STATS,
                    onClick = { currentScreen = Screen.STATS },
                    icon = { Icon(Icons.Default.List, contentDescription = "Статистика", tint = Color.White) },
                    label = { Text("Цели", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.MAP,
                    onClick = { currentScreen = Screen.MAP },
                    icon = { Icon(Icons.Default.Place, contentDescription = "Карта", tint = Color.White) },
                    label = { Text("Карта", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.HISTORY,
                    onClick = { currentScreen = Screen.HISTORY },
                    icon = { Icon(Icons.Default.DateRange, contentDescription = "История", tint = Color.White) },
                    label = { Text("История", color = Color.White, fontSize = 10.sp) }
                )
                NavigationBarItem(
                    selected = currentScreen == Screen.NOTES,
                    onClick = { currentScreen = Screen.NOTES },
                    icon = { Icon(Icons.Default.Create, contentDescription = "Заметки", tint = Color.White) },
                    label = { Text("Заметки", color = Color.White, fontSize = 10.sp) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            when (currentScreen) {
                Screen.TRACKER -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        val steps by viewModel.todaySteps.collectAsState()
                        val goal by viewModel.stepsGoal.collectAsState()
                        val distance by viewModel.todayDistanceKm.collectAsState()

                        Text(
                            text = "ШАГИ СЕГОДНЯ",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "$steps",
                            color = Color.White,
                            fontSize = 54.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Цель: $goal  |  Дистанция: ${String.format("%.2f", distance)} км",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }
                }
                Screen.STATS -> StatsScreen(viewModel = viewModel)
                Screen.MAP -> MapScreen()
                Screen.HISTORY -> HistoryScreen(viewModel = viewModel)
                Screen.NOTES -> NotesScreen(viewModel = viewModel)
                Screen.ACHIEVEMENTS -> AchievementsScreen(viewModel = viewModel)
            }
        }
    }
}
