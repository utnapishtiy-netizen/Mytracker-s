package com.example.pixeltracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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

// Тип редактируемой метки для диалога корректировки
sealed class EditMetricType {
    data class PushUps(val current: Int) : EditMetricType()
    data class Squats(val current: Int) : EditMetricType()
    data class Custom(val activityId: Long, val title: String, val current: Int, val goal: Int) : EditMetricType()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTrackerApp(viewModel: TrackerViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Tracker) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var editingMetric by remember { mutableStateOf<EditMetricType?>(null) }

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
    val customActivities by viewModel.customActivities.collectAsState()
    val customProgress by viewModel.todayCustomProgress.collectAsState()

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
                            IconButton(onClick = { viewModel.refreshData() }) {
                                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Обновить")
                            }
                            IconButton(onClick = { currentScreen = Screen.History }) {
                                Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = "История")
                            }
                            IconButton(onClick = { currentScreen = Screen.Notes }) {
                                Icon(imageVector = Icons.Rounded.Note, contentDescription = "Заметки")
                            }
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(imageVector = Icons.Rounded.Settings, contentDescription = "Настройки целей")
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { showAddActivityDialog = true },
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Добавить активность")
                    }
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
                        onActionClick = { viewModel.addPushUps(10) },
                        onEditClick = { editingMetric = EditMetricType.PushUps(pushUps) }
                    )

                    MetricCard(
                        title = "Приседания",
                        value = "$squats",
                        unit = "/ $squatsGoal",
                        progress = if (squatsGoal > 0) squats.toFloat() / squatsGoal else 0f,
                        icon = Icons.Rounded.AccessibilityNew,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        onActionClick = { viewModel.addSquats(10) },
                        onEditClick = { editingMetric = EditMetricType.Squats(squats) }
                    )

                    customActivities.forEach { activity ->
                        val progressItem = customProgress.find { it.activityId == activity.id }
                        val currentCount = progressItem?.currentCount ?: 0
                        val goalCount = progressItem?.goalCount ?: activity.defaultGoal

                        MetricCard(
                            title = activity.name,
                            value = "$currentCount",
                            unit = "${activity.unit} / $goalCount",
                            progress = if (goalCount > 0) currentCount.toFloat() / goalCount else 0f,
                            icon = Icons.Rounded.Star,
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onActionClick = { viewModel.addCustomProgress(activity.id, 5, goalCount) },
                            onEditClick = { editingMetric = EditMetricType.Custom(activity.id, activity.name, currentCount, goalCount) }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(64.dp))
                }
            }
        }
    }

    // Диалог точной корректировки значения счетчика
    editingMetric?.let { metric ->
        var inputVal by remember {
            mutableStateOf(
                when (metric) {
                    is EditMetricType.PushUps -> metric.current.toString()
                    is EditMetricType.Squats -> metric.current.toString()
                    is EditMetricType.Custom -> metric.current.toString()
                }
            )
        }
        val titleText = when (metric) {
            is EditMetricType.PushUps -> "Отжимания: точное значение"
            is EditMetricType.Squats -> "Приседания: точное значение"
            is EditMetricType.Custom -> "Корректировка: ${metric.title}"
        }

        AlertDialog(
            onDismissRequest = { editingMetric = null },
            title = { Text(titleText) },
            text = {
                OutlinedTextField(
                    value = inputVal,
                    onValueChange = { inputVal = it },
                    label = { Text("Значение") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val newVal = inputVal.toIntOrNull() ?: 0
                    when (metric) {
                        is EditMetricType.PushUps -> viewModel.setPushUps(newVal)
                        is EditMetricType.Squats -> viewModel.setSquats(newVal)
                        is EditMetricType.Custom -> viewModel.updateCustomProgress(metric.activityId, newVal, metric.goal)
                    }
                    editingMetric = null
                }) {
                    Text("Установить")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingMetric = null }) { Text("Отмена") }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }

    if (showAddActivityDialog) {
        AddCustomActivityDialog(
            onDismiss = { showAddActivityDialog = false },
            onConfirm = { name, unit, goal ->
                viewModel.addCustomActivity(name, unit, goal)
                showAddActivityDialog = false
            }
        )
    }

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

@Composable
fun AddCustomActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая активность") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (например: Подтягивания)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Единица (например: раз, мин)") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = goal,
                    onValueChange = { goal = it },
                    label = { Text("Цель") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) {
                    val g = goal.toIntOrNull() ?: 50
                    onConfirm(name.trim(), unit.trim().ifBlank { "раз" }, g)
                }
            }) {
                Text("Создать")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена") }
        },
        shape = RoundedCornerShape(28.dp)
    )
}

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
    onActionClick: (() -> Unit)? = null,
    onEditClick: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .let { if (onEditClick != null) it.clickable(onClick = onEditClick) else it }
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

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onEditClick != null) {
                        FilledIconButton(
                            onClick = onEditClick,
                            shape = RoundedCornerShape(16.dp),
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Edit,
                                contentDescription = "Править",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
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
