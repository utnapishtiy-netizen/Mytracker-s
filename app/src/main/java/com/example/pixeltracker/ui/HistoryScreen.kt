package com.example.pixeltracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DirectionsRun
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.pixeltracker.data.DailyTracker
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: TrackerViewModel,
    onBackClick: () -> Unit
) {
    val historyList by viewModel.allHistory.collectAsState()
    val historyMap = remember(historyList) { historyList.associateBy { it.date } }

    var displayedYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDateString by remember { mutableStateOf(LocalDate.now().toString()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("История активности", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // Календарь-сетка месяца
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Шапка месяца с переключателями
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { displayedYearMonth = displayedYearMonth.minusMonths(1) }) {
                                Icon(imageVector = Icons.Rounded.ChevronLeft, contentDescription = "Месяц назад")
                            }
                            Text(
                                text = "${displayedYearMonth.month.getDisplayName(TextStyle.FULL, Locale("ru"))} ${displayedYearMonth.year}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = { displayedYearMonth = displayedYearMonth.plusMonths(1) }) {
                                Icon(imageVector = Icons.Rounded.ChevronRight, contentDescription = "Месяц вперед")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Дни недели
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс").forEach { day ->
                                Text(
                                    text = day,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.width(36.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Сетка дней месяца
                        MonthCalendarGrid(
                            yearMonth = displayedYearMonth,
                            historyMap = historyMap,
                            selectedDate = selectedDateString,
                            onDateSelected = { selectedDateString = it }
                        )
                    }
                }
            }

            // Карточка выбранного дня или всех дней, если ничего не выбрано
            item {
                Text(
                    text = if (selectedDateString == LocalDate.now().toString()) "Сегодня" else selectedDateString,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val selectedTracker = historyMap[selectedDateString]
            if (selectedTracker != null) {
                item {
                    HistoryItemCard(item = selectedTracker)
                }
            } else {
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Нет данных за этот день",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Все записи",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (historyList.isEmpty()) {
                item {
                    Text("История пока пуста", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(historyList) { item ->
                    HistoryItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun MonthCalendarGrid(
    yearMonth: YearMonth,
    historyMap: Map<String, DailyTracker>,
    selectedDate: String,
    onDateSelected: (String) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    // Понедельник = 1, Воскресенье = 7
    val dayOfWeekOffset = firstDayOfMonth.dayOfWeek.value - 1

    val totalCells = daysInMonth + dayOfWeekOffset
    val rows = (totalCells + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                for (col in 0 until 7) {
                    val dayIndex = row * 7 + col - dayOfWeekOffset
                    if (dayIndex in 0 until daysInMonth) {
                        val dayNumber = dayIndex + 1
                        val date = yearMonth.atDay(dayNumber)
                        val dateString = date.toString()
                        val tracker = historyMap[dateString]

                        val isSelected = selectedDate == dateString
                        val isCompleted = tracker != null && (
                                tracker.stepsCount >= tracker.stepsGoal ||
                                        tracker.pushUpsCount >= tracker.pushUpsGoal ||
                                        tracker.squatsCount >= tracker.squatsGoal
                                )

                        DayCell(
                            dayNumber = dayNumber,
                            isSelected = isSelected,
                            isCompleted = isCompleted,
                            hasData = tracker != null,
                            onClick = { onDateSelected(dateString) }
                        )
                    } else {
                        Box(modifier = Modifier.size(36.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun DayCell(
    dayNumber: Int,
    isSelected: Boolean,
    isCompleted: Boolean,
    hasData: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primary
        isCompleted -> MaterialTheme.colorScheme.primaryContainer
        hasData -> MaterialTheme.colorScheme.surfaceContainerHighest
        else -> Color.Transparent
    }

    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isCompleted) FontWeight.Bold else FontWeight.Normal,
                color = textColor
            )
        }
    }
}

@Composable
fun HistoryItemCard(item: DailyTracker) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = item.date,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistoryMetricRow(
                    icon = Icons.Rounded.DirectionsWalk,
                    label = "Шаги",
                    current = item.stepsCount,
                    goal = item.stepsGoal
                )
                HistoryMetricRow(
                    icon = Icons.Rounded.FitnessCenter,
                    label = "Отжимания",
                    current = item.pushUpsCount,
                    goal = item.pushUpsGoal
                )
                HistoryMetricRow(
                    icon = Icons.Rounded.DirectionsRun,
                    label = "Приседания",
                    current = item.squatsCount,
                    goal = item.squatsGoal
                )
            }
        }
    }
}

@Composable
fun HistoryMetricRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    current: Int,
    goal: Int
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.secondary)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            text = "$current / $goal",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (current >= goal) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}
