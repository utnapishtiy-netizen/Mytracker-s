package com.example.pixeltracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pixeltracker.ui.NotesScreen
import com.example.pixeltracker.ui.TrackerViewModel

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = mutableListOf(
            Manifest.permission.ACTIVITY_RECOGNITION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainTrackerApp()
                }
            }
        }
    }
}

sealed class Screen {
    object Tracker : Screen()
    object Notes : Screen()
    object History : Screen()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTrackerApp(viewModel: TrackerViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Tracker) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val steps by viewModel.todaySteps.collectAsState()
    val distance by viewModel.todayDistanceKm.collectAsState()
    val pushUps by viewModel.todayPushUps.collectAsState()
    val squats by viewModel.todaySquats.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()

    val stepsGoal by viewModel.stepsGoal.collectAsState()
    val pushUpsGoal by viewModel.pushUpsGoal.collectAsState()
    val squatsGoal by viewModel.squatsGoal.collectAsState()
    val distanceGoal by viewModel.distanceGoalKm.collectAsState()

    val historyList by viewModel.allHistory.collectAsState()

    when (currentScreen) {
        is Screen.Notes -> {
            NotesScreen(
                viewModel = viewModel,
                onBackClick = { currentScreen = Screen.Tracker }
            )
        }
        is Screen.History -> {
            HistoryScreen(
                historyList = historyList,
                onBackClick = { currentScreen = Screen.Tracker }
            )
        }
        is Screen.Tracker -> {
            Scaffold(
                topBar = {
                    LargeTopAppBar(
                        title = {
                            Column {
                                Text("MyTracker", fontWeight = FontWeight.Bold)
                                Text(
                                    "🔥 Стрик: $streak дн.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        actions = {
                            // Кнопка обновления датчиков/данных
                            IconButton(onClick = { viewModel.refreshData() }) {
                                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Обновить")
                            }
                            // Кнопка истории (Календарь и результаты)
                            IconButton(onClick = { currentScreen = Screen.History }) {
                                Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = "История")
                            }
                            // Кнопка заметок
                            IconButton(onClick = { currentScreen = Screen.Notes }) {
                                Icon(imageVector = Icons.Rounded.Note, contentDescription = "Заметки")
                            }
                            // Кнопка настроек целей
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(imageVector = Icons.Rounded.Settings, contentDescription = "Настройки целей")
                            }
                        }
                    )
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    MetricCard(
                        title = "Шаги",
                        value = "$steps",
                        unit = "/ $stepsGoal",
                        progress = if (stepsGoal > 0) steps.toFloat() / stepsGoal else 0f,
                        icon = Icons.Rounded.DirectionsWalk,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    MetricCard(
                        title = "Дистанция",
                        value = "%.2f".format(distance),
                        unit = "км / $distanceGoal км",
                        progress = if (distanceGoal > 0f) distance / distanceGoal else 0f,
                        icon = Icons.Rounded.Map,
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )

                    MetricCard(
                        title = "Отжимания",
                        value = "$pushUps",
                        unit = "/ $pushUpsGoal",
                        progress = if (pushUpsGoal > 0) pushUps.toFloat() / pushUpsGoal else 0f,
                        icon = Icons.Rounded.FitnessCenter,
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                        onActionClick = { viewModel.addPushUps(10) }
                    )

                    MetricCard(
                        title = "Приседания",
                        value = "$squats",
                        unit = "/ $squatsGoal",
                        progress = if (squatsGoal > 0) squats.toFloat() / squatsGoal else 0f,
                        icon = Icons.Rounded.AccessibilityNew,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onActionClick = { viewModel.addSquats(10) }
                    )
                }
            }
        }
    }

    // Диалог настройки целей
    if (showSettingsDialog) {
        GoalsSettingsDialog(
            currentPushUpsGoal = pushUpsGoal,
            currentSquatsGoal = squatsGoal,
            currentStepsGoal = stepsGoal,
            currentDistanceGoal = distanceGoal,
            onDismiss = { showSettingsDialog = false },
            onSave = { pU, sQ, sG, dG ->
                viewModel.updateGoals(pU, sQ, sG, dG)
                showSettingsDialog = false
            }
        )
    }
}

// Экран истории и результатов по дням (Календарь)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    historyList: List<com.example.pixeltracker.data.DailyTracker>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История и результаты", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (historyList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("История пока пуста. Начните выполнять цели!", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(historyList) { day ->
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "📅 ${day.date}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                            Text("👣 Шаги: ${day.stepsCount} / ${day.stepsGoal}")
                            Text("📍 Дистанция: ${day.distanceKm} км / ${day.distanceGoalKm} км")
                            Text("💪 Отжимания: ${day.pushUpsCount} / ${day.pushUpsGoal}")
                            Text("🦵 Приседания: ${day.squatsCount} / ${day.squatsGoal}")
                        }
                    }
                }
            }
        }
    }
}

// Диалог настройки целей
@Composable
fun GoalsSettingsDialog(
    currentPushUpsGoal: Int,
    currentSquatsGoal: Int,
    currentStepsGoal: Int,
    currentDistanceGoal: Float,
    onDismiss: () -> Unit,
    onSave: (Int, Int, Int, Float) -> Unit
) {
    var pushUps by remember { mutableStateOf(currentPushUpsGoal.toString()) }
    var squats by remember { mutableStateOf(currentSquatsGoal.toString()) }
    var steps by remember { mutableStateOf(currentStepsGoal.toString()) }
    var distance by remember { mutableStateOf(currentDistanceGoal.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка целей") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = steps,
                    onValueChange = { steps = it },
                    label = { Text("Цель по шагам") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text("Цель по дистанции (км)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = pushUps,
                    onValueChange = { pushUps = it },
                    label = { Text("Цель по отжиманиям") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = squats,
                    onValueChange = { squats = it },
                    label = { Text("Цель по приседаниям") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val pU = pushUps.toIntOrNull() ?: currentPushUpsGoal
                val sQ = squats.toIntOrNull() ?: currentSquatsGoal
                val sG = steps.toIntOrNull() ?: currentStepsGoal
                val dG = distance.toFloatOrNull() ?: currentDistanceGoal
                onSave(pU, sQ, sG, dG)
            }) {
                Text("Сохранить")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    progress: Float,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onActionClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = contentColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (onActionClick != null) {
                    FilledIconButton(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Добавить",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentColor
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.bodyLarge,
                    color = contentColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(contentColor.copy(alpha = 0.15f))
            ) {
                LinearProgressIndicator(
                    progress = { progress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    color = contentColor,
                    trackColor = Color.Transparent,
                )
            }
        }
    }
}
