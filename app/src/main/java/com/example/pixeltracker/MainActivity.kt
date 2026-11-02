package com.example.pixeltracker

import android.Manifest
import android.app.Activity
import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.data.BackupWorker
import com.example.pixeltracker.data.DailyTracker
import com.example.pixeltracker.data.ReminderWorker
import com.example.pixeltracker.ui.AchievementsScreen
import com.example.pixeltracker.ui.NotesScreen
import com.example.pixeltracker.ui.StatsScreen
import com.example.pixeltracker.ui.TrackerViewModel
import com.example.pixeltracker.ui.theme.PixelTrackerTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.time.YearMonth
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { _ -> }

    private val openFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                try {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { reader ->
                            val content = reader.readText()
                            Toast.makeText(this, "Бэкап успешно прочитан (${content.length} символов)", Toast.LENGTH_LONG).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Ошибка чтения файла бэкапа", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun triggerFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        openFileLauncher.launch(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val permissions = mutableListOf(Manifest.permission.ACTIVITY_RECOGNITION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }
        requestPermissionLauncher.launch(permissions.toTypedArray())

        val backupRequest = PeriodicWorkRequestBuilder<BackupWorker>(24, TimeUnit.HOURS)
            .setConstraints(Constraints.Builder().setRequiresDeviceIdle(true).build())
            .build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "MyTrackerDailyBackup", ExistingPeriodicWorkPolicy.KEEP, backupRequest
        )

        val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "MyTrackerReminder", ExistingPeriodicWorkPolicy.KEEP, reminderRequest
        )

        setContent {
            var darkThemeMode by remember { mutableStateOf(2) }
            val useDarkTheme = when (darkThemeMode) {
                1 -> false
                2 -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            PixelTrackerTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainTrackerApp(
                        darkThemeMode = darkThemeMode,
                        onThemeChange = { darkThemeMode = it },
                        onImportClick = { triggerFilePicker() }
                    )
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
    object Achievements : Screen()
}

sealed class EditMetricType {
    data class Steps(val current: Int) : EditMetricType()
    data class Distance(val current: Float) : EditMetricType()
    data class PushUps(val current: Int) : EditMetricType()
    data class Squats(val current: Int) : EditMetricType()
    data class Custom(val activityId: Long, val title: String, val current: Int, val goal: Int) : EditMetricType()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTrackerApp(
    viewModel: TrackerViewModel = viewModel(),
    darkThemeMode: Int,
    onThemeChange: (Int) -> Unit,
    onImportClick: () -> Unit
) {
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

    when (currentScreen) {
        is Screen.Notes -> {
            NotesScreen(viewModel = viewModel, onBackClick = { currentScreen = Screen.Tracker })
        }
        is Screen.History -> {
            HistoryScreen(historyList = historyList, onBackClick = { currentScreen = Screen.Tracker })
        }
        is Screen.Stats -> {
            StatsScreen(historyList = historyList, onBackClick = { currentScreen = Screen.Tracker })
        }
        is Screen.Achievements -> {
            AchievementsScreen(
                historyList = historyList,
                currentStreak = streak,
                onBackClick = { currentScreen = Screen.Tracker }
            )
        }
        is Screen.Tracker -> {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                "🔥 Стрик: $streak дн.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        actions = {
                            IconButton(onClick = { viewModel.refreshData() }) {
                                Icon(imageVector = Icons.Rounded.Refresh, contentDescription = "Обновить")
                            }
                            IconButton(onClick = { currentScreen = Screen.Achievements }) {
                                Icon(imageVector = Icons.Rounded.EmojiEvents, contentDescription = "Достижения")
                            }
                            IconButton(onClick = { currentScreen = Screen.Stats }) {
                                Icon(imageVector = Icons.Rounded.BarChart, contentDescription = "Статистика")
                            }
                            IconButton(onClick = { currentScreen = Screen.History }) {
                                Icon(imageVector = Icons.Rounded.CalendarMonth, contentDescription = "Календарь")
                            }
                            IconButton(onClick = { currentScreen = Screen.Notes }) {
                                Icon(imageVector = Icons.Rounded.Note, contentDescription = "Заметки")
                            }
                            IconButton(onClick = { showSettingsDialog = true }) {
                                Icon(image
