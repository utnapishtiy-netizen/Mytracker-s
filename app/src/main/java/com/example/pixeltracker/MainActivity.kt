package com.example.pixeltracker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Note
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pixeltracker.ui.HistoryScreen
import com.example.pixeltracker.ui.NotesScreen
import com.example.pixeltracker.ui.TrackerViewModel
import com.example.pixeltracker.worker.ReminderWorker
import java.util.concurrent.TimeUnit

enum class Screen { Main, History, Notes }
enum class ExerciseType { PushUps, Squats }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel()

        val dailyWorkRequest = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS).build()
        WorkManager.getInstance(applicationContext).enqueue(dailyWorkRequest)

        setContent {
            val context = LocalContext.current
            val darkTheme = isSystemInDarkTheme()
            
            val colorScheme = when {
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
                }
                darkTheme -> darkColorScheme()
                else -> lightColorScheme()
            }

            MaterialTheme(colorScheme = colorScheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Daily Reminders"
            val descriptionText = "Напоминания о дневной активности"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("tracker_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}

@Composable
fun AppNavigation(viewModel: TrackerViewModel = viewModel()) {
    var currentScreen by remember { mutableStateOf(Screen.Main) }

    when (currentScreen) {
        Screen.Main -> TrackerScreen(
            viewModel = viewModel,
            onOpenHistory = { currentScreen = Screen.History },
            onOpenNotes = { currentScreen = Screen.Notes }
        )
        Screen.History -> HistoryScreen(
            viewModel = viewModel,
            onBackClick = { currentScreen = Screen.Main }
        )
        Screen.Notes -> NotesScreen(
            viewModel = viewModel,
            onBackClick = { currentScreen = Screen.Main }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackerScreen(
    viewModel: TrackerViewModel,
    onOpenHistory: () -> Unit,
    onOpenNotes: () -> Unit
) {
    val stepsCount by viewModel.todaySteps.collectAsState()
    val distanceKm by viewModel.todayDistanceKm.collectAsState()
    val pushUpsCount by viewModel.todayPushUps.collectAsState()
    val squatsCount by viewModel.todaySquats.collectAsState()
    val currentStreak by viewModel.currentStreak.collectAsState()

    val pushUpsGoal by viewModel.pushUpsGoal.collectAsState()
    val squatsGoal by viewModel.squatsGoal.collectAsState()
    val stepsGoal by viewModel.stepsGoal.collectAsState()
    val distanceGoalKm by viewModel.distanceGoalKm.collectAsState()

    val customActivities by viewModel.customActivities.collectAsState()
    val customProgress by viewModel.todayCustomProgress.collectAsState()

    var activeDialogExercise by remember { mutableStateOf<ExerciseType?>(null) }
    var showGoalDialog by remember { mutableStateOf(false) }
    var showAddActivityDialog by remember { mutableStateOf(false) }
    var selectedCustomActivityId by remember { mutableStateOf<Long?>(null) }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        val launcher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { }
        LaunchedEffect(Unit) {
            launcher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("MyTracker", fontWeight = FontWeight.Bold)
                        if (currentStreak > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.tertiaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.LocalFireDepartment,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                    Text(
                                        text = "$currentStreak дн.",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onOpenNotes) {
                        Icon(imageVector = Icons.Rounded.Note, contentDescription = "Заметки")
                    }
                    IconButton(onClick = { showAddActivityDialog = true }) {
                        Icon(imageVector = Icons.Rounded.Add, contentDescription = "Добавить активность")
                    }
                    IconButton(onClick = { showGoalDialog = true }) {
                        Icon(imageVector = Icons.Rounded.Settings, contentDescription = "Цели")
                    }
                    IconButton(onClick = onOpenHistory) {
                        Icon(imageVector = Icons.Rounded.BarChart, contentDescription = "История")
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            MetricCard(
                title = "Шаги",
                value = "$stepsCount",
                unit = "/ $stepsGoal",
                progress = if (stepsGoal > 0) stepsCount.toFloat() / stepsGoal else 0f,
                icon = Icons.Rounded.DirectionsWalk,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            )

            MetricCard(
                title = "Километры",
                value = "$distanceKm",
                unit = "км / $distanceGoalKm км",
                progress = if (distanceGoalKm > 0f) distanceKm / distanceGoalKm else 0f,
                icon = Icons.Rounded.Map,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )

            MetricCard(
                title = "Отжимания",
                value = "$pushUpsCount",
                unit = "/ $pushUpsGoal",
                progress = if (pushUpsGoal > 0) pushUpsCount.toFloat() / pushUpsGoal else 0f,
                icon = Icons.Rounded.FitnessCenter,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                onActionClick = { activeDialogExercise = ExerciseType.PushUps }
            )

            MetricCard(
                title = "Приседания",
                value = "$squatsCount",
                unit = "/ $squatsGoal",
                progress = if (squatsGoal > 0) squatsCount.toFloat() / squatsGoal else 0f,
                icon = Icons.Rounded.DirectionsRun,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                onActionClick = { activeDialogExercise = ExerciseType.Squats }
            )

            customActivities.forEach { activity ->
                val progressItem = customProgress.find { it.activityId == activity.id }
                val currentCount = progressItem?.currentCount ?: 0
                val goal = progressItem?.goalCount ?: activity.defaultGoal

                MetricCard(
                    title = activity.name,
                    value = "$currentCount",
                    unit = "${activity.unit} / $goal",
                    progress = if (goal > 0) currentCount.toFloat() / goal else 0f,
                    icon = Icons.Rounded.FitnessCenter,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    onActionClick = { selectedCustomActivityId = activity.id }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        activeDialogExercise?.let { exercise ->
            val isPushUps = exercise == ExerciseType.PushUps
            EditExerciseDialog(
                title = if (isPushUps) "Отжимания" else "Приседания",
                currentValue = if (isPushUps) pushUpsCount else squatsCount,
                onDismiss = { activeDialogExercise = null },
                onAdd = { added ->
                    if (isPushUps) viewModel.addPushUps(added) else viewModel.addSquats(added)
                    activeDialogExercise = null
                },
                onSetExact = { exact ->
                    if (isPushUps) viewModel.setPushUps(exact) else viewModel.setSquats(exact)
                    activeDialogExercise = null
                }
            )
        }

        selectedCustomActivityId?.let { activityId ->
            val activity = customActivities.find { it.id == activityId }
            val progressItem = customProgress.find { it.activityId == activityId }
            val currentCount = progressItem?.currentCount ?: 0
            val goal = progressItem?.goalCount ?: activity?.defaultGoal ?: 100

            if (activity != null) {
                EditExerciseDialog(
                    title = activity.name,
                    currentValue = currentCount,
                    onDismiss = { selectedCustomActivityId = null },
                    onAdd = { added ->
                        viewModel.addCustomProgress(activityId, added, goal)
                        selectedCustomActivityId = null
                    },
                    onSetExact = { exact ->
                        viewModel.updateCustomProgress(activityId, exact, goal)
                        selectedCustomActivityId = null
                    }
                )
            }
        }

        if (showGoalDialog) {
            SetGoalsDialog(
                currentPushUpsGoal = pushUpsGoal,
                currentSquatsGoal = squatsGoal,
                currentStepsGoal = stepsGoal,
                currentDistanceGoal = distanceGoalKm,
                onDismiss = { showGoalDialog = false },
                onConfirm = { pGoal, sGoal, stGoal, dGoal ->
                    viewModel.updateGoals(pGoal, sGoal, stGoal, dGoal)
                    showGoalDialog = false
                }
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
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    unit: String,
    progress: Float,
    icon: ImageVector,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onActionClick: (() -> Unit)? = null
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 800),
        label = "ProgressAnimation"
    )

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
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
                    Icon(imageVector = icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(28.dp))
                    Text(text = title, style = MaterialTheme.typography.titleMedium, color = contentColor, fontWeight = FontWeight.Medium)
                }

                if (onActionClick != null) {
                    FilledIconButton(
                        onClick = onActionClick,
                        shape = RoundedCornerShape(16.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Icon(imageVector = Icons.Rounded.Edit, contentDescription = "Управлять", tint = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(text = value, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Text(text = unit, style = MaterialTheme.typography.bodyLarge, color = contentColor.copy(alpha = 0.7f), modifier = Modifier.padding(bottom = 8.dp))
            }

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                color = contentColor,
                trackColor = contentColor.copy(alpha = 0.2f)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExerciseDialog(
    title: String,
    currentValue: Int,
    onDismiss: () -> Unit,
    onAdd: (Int) -> Unit,
    onSetExact: (Int) -> Unit
) {
    var isSetExactMode by remember { mutableStateOf(false) }
    var amountText by remember { mutableStateOf(if (isSetExactMode) "$currentValue" else "10") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        selected = !isSetExactMode,
                        onClick = { isSetExactMode = false; amountText = "10" },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                    ) { Text("Прибавить") }
                    SegmentedButton(
                        selected = isSetExactMode,
                        onClick = { isSetExactMode = true; amountText = "$currentValue" },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                    ) { Text("Исправить") }
                }

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                    label = { Text(if (isSetExactMode) "Всего за день" else "Прибавить повторений") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val valNumber = amountText.toIntOrNull() ?: 0
                if (isSetExactMode) onSetExact(valNumber) else if (valNumber > 0) onAdd(valNumber)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun AddCustomActivityDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, unit: String, goal: Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("раз") }
    var goalText by remember { mutableStateOf("50") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Новая активность") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (например, Планка)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    label = { Text("Единица (сек, раз, км)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { goalText = it.filter { char -> char.isDigit() } },
                    label = { Text("Цель на день") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val goal = goalText.toIntOrNull() ?: 50
                if (name.isNotBlank()) {
                    onConfirm(name.trim(), unit.trim(), goal)
                }
            }) { Text("Создать") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun SetGoalsDialog(
    currentPushUpsGoal: Int,
    currentSquatsGoal: Int,
    currentStepsGoal: Int,
    currentDistanceGoal: Float,
    onDismiss: () -> Unit,
    onConfirm: (pushUps: Int, squats: Int, steps: Int, distanceKm: Float) -> Unit
) {
    var pGoal by remember { mutableIntStateOf(currentPushUpsGoal) }
    var sGoal by remember { mutableIntStateOf(currentSquatsGoal) }
    var stGoal by remember { mutableIntStateOf(currentStepsGoal) }
    var dGoalText by remember { mutableStateOf("$currentDistanceGoal") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Настройка целей") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                GoalAdjusterRow(label = "Шаги", value = stGoal, onValueChange = { stGoal = it })
                
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(text = "Километры", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = dGoalText,
                        onValueChange = { dGoalText = it.filter { char -> char.isDigit() || char == '.' } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                GoalAdjusterRow(label = "Отжимания", value = pGoal, onValueChange = { pGoal = it })
                GoalAdjusterRow(label = "Приседания", value = sGoal, onValueChange = { sGoal = it })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val dVal = dGoalText.toFloatOrNull() ?: 5.0f
                onConfirm(pGoal, sGoal, stGoal, dVal)
            }) { Text("Сохранить") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Отмена") } },
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun GoalAdjusterRow(label: String, value: Int, onValueChange: (Int) -> Unit) {
    var textValue by remember(value) { mutableStateOf("$value") }

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilledIconButton(
                onClick = { onValueChange((value - 1).coerceAtLeast(1)) },
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) { Text("-", style = MaterialTheme.typography.titleLarge) }

            OutlinedTextField(
                value = textValue,
                onValueChange = { newText ->
                    val filtered = newText.filter { it.isDigit() }
                    textValue = filtered
                    val parsed = filtered.toIntOrNull()
                    if (parsed != null && parsed >= 1) onValueChange(parsed)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            )

            FilledIconButton(
                onClick = { onValueChange(value + 1) },
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) { Text("+", style = MaterialTheme.typography.titleLarge) }
        }
    }
}
