package com.example.pixeltracker.ui

import android.app.Application
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.data.ActivityProgressEntity
import com.example.pixeltracker.data.CustomActivityEntity
import com.example.pixeltracker.data.DailyTracker
import com.example.pixeltracker.data.NoteEntity
import com.example.pixeltracker.sensor.StepCounterManager
import com.example.pixeltracker.widget.PushUpWidget
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrackerViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).trackerDao()
    private val noteDao = AppDatabase.getDatabase(application).noteDao()
    private val todayDateString = LocalDate.now().toString()

    val stepCounterManager = StepCounterManager(application)

    val todayTracker: StateFlow<DailyTracker?> = dao.getTrackerByDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val todaySteps: StateFlow<Int> = todayTracker.map { it?.stepsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val todayDistanceKm: StateFlow<Float> = todayTracker.map { it?.distanceKm ?: 0.0f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0.0f)

    val todayPushUps: StateFlow<Int> = todayTracker.map { it?.pushUpsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val todaySquats: StateFlow<Int> = todayTracker.map { it?.squatsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    val pushUpsGoal: StateFlow<Int> = todayTracker.map { it?.pushUpsGoal ?: 100 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 100)

    val squatsGoal: StateFlow<Int> = todayTracker.map { it?.squatsGoal ?: 100 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 100)

    val stepsGoal: StateFlow<Int> = todayTracker.map { it?.stepsGoal ?: 10000 }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 10000)

    val distanceGoalKm: StateFlow<Float> = todayTracker.map { it?.distanceGoalKm ?: 5.0f }
        .stateIn(viewModelScope, SharingStarted.Eagerly, 5.0f)

    val customActivities: StateFlow<List<CustomActivityEntity>> = dao.getAllCustomActivities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val todayCustomProgress: StateFlow<List<ActivityProgressEntity>> = dao.getProgressForDate(todayDateString)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val allHistory: StateFlow<List<DailyTracker>> = dao.getAllTrackers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Заметки
    val notes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentStreak: StateFlow<Int> = allHistory.map { history ->
        calculateStreak(history)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        stepCounterManager.startListening()
        viewModelScope.launch {
            stepCounterManager.stepCount.collect { sensorSteps ->
                if (sensorSteps > 0) {
                    val current = getOrCreateToday()
                    val sensorKm = sensorSteps * 0.00075f
                    if (sensorSteps > current.stepsCount) {
                        dao.insertOrUpdate(
                            current.copy(
                                stepsCount = sensorSteps,
                                distanceKm = String.format("%.2f", sensorKm).replace(',', '.').toFloat()
                            )
                        )
                        triggerWidgetUpdate()
                    }
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepCounterManager.stopListening()
    }

    private fun getOrCreateToday(): DailyTracker {
        return todayTracker.value ?: DailyTracker(date = todayDateString)
    }

    private fun triggerWidgetUpdate() {
        viewModelScope.launch {
            try {
                PushUpWidget().updateAll(getApplication())
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addPushUps(count: Int) {
        viewModelScope.launch {
            val current = getOrCreateToday()
            dao.insertOrUpdate(current.copy(pushUpsCount = current.pushUpsCount + count))
            triggerWidgetUpdate()
        }
    }

    fun setPushUps(count: Int) {
        viewModelScope.launch {
            val current = getOrCreateToday()
            dao.insertOrUpdate(current.copy(pushUpsCount = maxOf(0, count)))
            triggerWidgetUpdate()
        }
    }

    fun addSquats(count: Int) {
        viewModelScope.launch {
            val current = getOrCreateToday()
            dao.insertOrUpdate(current.copy(squatsCount = current.squatsCount + count))
            triggerWidgetUpdate()
        }
    }

    fun setSquats(count: Int) {
        viewModelScope.launch {
            val current = getOrCreateToday()
            dao.insertOrUpdate(current.copy(squatsCount = maxOf(0, count)))
            triggerWidgetUpdate()
        }
    }

    fun updateGoals(pushUps: Int, squats: Int, steps: Int, distanceKm: Float) {
        viewModelScope.launch {
            val current = getOrCreateToday()
            dao.insertOrUpdate(
                current.copy(
                    pushUpsGoal = maxOf(1, pushUps),
                    squatsGoal = maxOf(1, squats),
                    stepsGoal = maxOf(1, steps),
                    distanceGoalKm = maxOf(0.1f, distanceKm)
                )
            )
            triggerWidgetUpdate()
        }
    }

    fun addCustomActivity(name: String, unit: String, defaultGoal: Int) {
        viewModelScope.launch {
            dao.insertCustomActivity(
                CustomActivityEntity(name = name, unit = unit, defaultGoal = defaultGoal, iconType = "fitness")
            )
            triggerWidgetUpdate()
        }
    }

    fun refreshData() {
        viewModelScope.launch {
            stepCounterManager.refresh()
            triggerWidgetUpdate()
        }
    }

    fun addCustomProgress(activityId: Long, count: Int, goal: Int) {
        viewModelScope.launch {
            val currentList = todayCustomProgress.value
            val existing = currentList.find { it.activityId == activityId }
            val newCount = (existing?.currentCount ?: 0) + count
            dao.insertOrUpdateProgress(
                ActivityProgressEntity(date = todayDateString, activityId = activityId, currentCount = newCount, goalCount = goal)
            )
            triggerWidgetUpdate()
        }
    }

    fun updateNote(id: Long, title: String, content: String) {
        viewModelScope.launch {
            // Находим текущую заметку, чтобы сохранить оригинальный timestamp, либо обновляем с новым
            val existingNotes = notes.value
            val target = existingNotes.find { it.id == id }
            if (target != null) {
                noteDao.insertNote(
                    target.copy(
                        title = title.trim(),
                        content = content.trim(),
                        timestamp = System.currentTimeMillis() // обновляем время изменения (по желанию)
                    )
                )
            }
        }
    }
    
    fun updateCustomProgress(activityId: Long, exactCount: Int, goal: Int) {
        viewModelScope.launch {
            dao.insertOrUpdateProgress(
                ActivityProgressEntity(
                    date = todayDateString,
                    activityId = activityId,
                    currentCount = maxOf(0, exactCount),
                    goalCount = goal
                )
            )
            triggerWidgetUpdate()
        }
    }

    // Методы для заметок
    fun addNote(title: String, content: String) {
        if (title.isBlank() && content.isBlank()) return
        viewModelScope.launch {
            noteDao.insertNote(NoteEntity(title = title.trim(), content = content.trim()))
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    private fun calculateStreak(history: List<DailyTracker>): Int {
        if (history.isEmpty()) return 0
        var streak = 0
        var expectedDate = LocalDate.now()

        val sorted = history.sortedByDescending { it.date }
        for (item in sorted) {
            val itemDate = LocalDate.parse(item.date)
            val isCompleted = item.stepsCount >= item.stepsGoal ||
                    item.pushUpsCount >= item.pushUpsGoal ||
                    item.squatsCount >= item.squatsGoal ||
                    item.distanceKm >= item.distanceGoalKm

            if (itemDate == expectedDate && isCompleted) {
                streak++
                expectedDate = expectedDate.minusDays(1)
            } else if (itemDate.isBefore(expectedDate)) {
                break
            }
        }
        return streak
    }
}
