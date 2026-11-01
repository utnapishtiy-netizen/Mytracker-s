package com.example.pixeltracker

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.data.BackupWorker
import com.example.pixeltracker.ui.NotesScreen
import com.example.pixeltracker.ui.StatsScreen
import com.example.pixeltracker.ui.TrackerViewModel
import com.example.pixeltracker.ui.theme.PixelTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale

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

        // Настройка автоматического фонового бэкапа раз в сутки
        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresDeviceIdle(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "MyTrackerDailyBackup",
            ExistingPeriodicWorkPolicy.KEEP,
            backupRequest
        )

        setContent {
            PixelTrackerTheme {
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
    object Stats : Screen()
}

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
    var activityToDelete by remember { mutableStateOf<com.example.pixeltracker.data.CustomActivityEntity?>(null) }

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

    val context = androidx.ui.core.LocalContext.current // Если используется стандартный LocalContext, см. ниже

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
        is Screen.Stats -> {
            StatsScreen(
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
                            IconButton(onClick = { currentScreen = Screen.Stats }) {
                                Icon(imageVector = Icons.Rounded.BarChart, contentDescription = "Статистика")
                            }
                            IconButton(onClick = { currentScreen = Screen.History }) {
                                Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = "Календарь истории")
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
                            unit = "${activity.unit
