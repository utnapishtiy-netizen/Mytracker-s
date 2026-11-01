package com.example.pixeltracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.data.CustomActivityEntity
import com.example.pixeltracker.data.DailyTracker
import com.example.pixeltracker.data.ActivityProgressEntity
import com.example.pixeltracker.data.NoteEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).trackerDao()
    private val currentDate = LocalDate.now().toString()

    private val todayTrackerFlow: StateFlow<DailyTracker?> = dao.getTrackerByDate(currentDate)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    val todaySteps: StateFlow<Int> = todayTrackerFlow.map { it?.stepsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val todayDistanceKm: StateFlow<Float> = todayTrackerFlow.map { it?.distanceKm ?: 0f }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val todayPushUps: StateFlow<Int> = todayTrackerFlow.map { it?.pushUpsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val todaySquats: StateFlow<Int> = todayTrackerFlow.map { it?.squatsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val stepsGoal: StateFlow<Int> = todayTrackerFlow.map { it?.stepsGoal ?: 10000 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 10000)

    val pushUpsGoal: StateFlow<Int> = todayTrackerFlow.map { it?.pushUpsGoal ?: 100 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 100)

    val squatsGoal: StateFlow<Int> = todayTrackerFlow.map { it?.squatsGoal ?: 100 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 100)

    val distanceGoalKm: StateFlow<Float> = todayTrackerFlow.map { it?.distanceGoalKm ?: 7.0f }
        .stateIn(viewModelScope, SharingStarted.Lazily, 7.0f)

    val allHistory: StateFlow<List<DailyTracker>> = dao.getAllTrackers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val customActivities: StateFlow<List<CustomActivityEntity>> = dao.getAllCustomActivities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val todayCustomProgress: StateFlow<List<ActivityProgressEntity>> = dao.getProgressForDate(currentDate)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Заметки
    val notes: StateFlow<List<NoteEntity>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentStreak: StateFlow<Int> = allHistory.map { history ->
        calculateStreak(history)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        ensureTodayRecordExists()
    }

    private fun ensureTodayRecordExists() {
        viewModelScope.launch(Dispatchers.IO) {
            val existing = dao.getTrackerByDateSync(currentDate)
            if (existing == null) {
                val lastTracker = dao.getAllTrackers().firstOrNull()?.firstOrNull()
                val newTracker = DailyTracker(
                    date = currentDate,
                    stepsCount = 0,
                    stepsGoal = lastTracker?.stepsGoal ?: 10000,
                    distanceKm = 0f,
                    distanceGoalKm = lastTracker?.distanceGoalKm ?: 7.0f,
                    pushUpsCount = 0,
                    pushUpsGoal = lastTracker?.pushUpsGoal ?: 100,
                    squatsCount = 0,
                    squatsGoal = lastTracker?.squatsGoal ?: 100
                )
                dao.insertOrUpdate(newTracker)
            }
        }
    }

    fun addPushUps(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = todayPushUps.value
            dao.updatePushUps(currentDate, current + amount)
        }
    }

    fun setPushUps(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updatePushUps(currentDate, count.coerceAtLeast(0))
        }
    }

    fun addSquats(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = todaySquats.value
            dao.updateSquats(currentDate, current + amount)
        }
    }

    fun setSquats(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateSquats(currentDate, count.coerceAtLeast(0))
        }
    }

    fun updateGoals(pushUps: Int, squats: Int, steps: Int, distance: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = dao.getTrackerByDateSync(currentDate) ?: DailyTracker(date = currentDate)
            val updated = current.copy(
                pushUpsGoal = pushUps,
                squatsGoal = squats,
                stepsGoal = steps,
                distanceGoalKm = distance
            )
            dao.insertOrUpdate(updated)
        }
    }

    // Кастомные активности (с параметром iconType по умолчанию)
    fun addCustomActivity(name: String, unit: String, defaultGoal: Int, iconType: String = "star") {
        viewModelScope.launch(Dispatchers.IO) {
            val activity = CustomActivityEntity(
                name = name,
                unit = unit,
                defaultGoal = defaultGoal,
                iconType = iconType
            )
            dao.insertCustomActivity(activity)
        }
    }

    fun deleteCustomActivity(activityId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteCustomActivity(activityId)
            dao.deleteProgressForActivity(activityId)
        }
    }

    fun addCustomProgress(activityId: Long, delta: Int, goal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val existingList = dao.getProgressForDate(currentDate).firstOrNull() ?: emptyList()
            val currentItem = existingList.find { it.activityId == activityId }
            val newCount = (currentItem?.currentCount ?: 0) + delta

            val progressEntity = ActivityProgressEntity(
                activityId = activityId,
                date = currentDate,
                currentCount = newCount.coerceAtLeast(0),
                goalCount = goal
            )
            dao.insertOrUpdateProgress(progressEntity)
        }
    }

    fun updateCustomProgress(activityId: Long, newCount: Int, goal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val progressEntity = ActivityProgressEntity(
                activityId = activityId,
                date = currentDate,
                currentCount = newCount.coerceAtLeast(0),
                goalCount = goal
            )
            dao.insertOrUpdateProgress(progressEntity)
        }
    }

    // Методы управления заметками
    fun addNote(title: String, content: String, colorHex: String = "#FFFFFF", reminderTime: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(
                title = title,
                content = content,
                colorHex = colorHex,
                reminderTime = reminderTime
            )
            dao.insertNote(note)
        }
    }

    fun updateNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateNote(note)
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteNote(note)
        }
    }

    fun refreshData() {
        ensureTodayRecordExists()
    }

    private fun calculateStreak(history: List<DailyTracker>): Int {
        if (history.isEmpty()) return 0
        var streak = 0
        var expectedDate = LocalDate.now()

        val sortedMap = history.associateBy { LocalDate.parse(it.date) }

        if (!sortedMap.containsKey(expectedDate) && !sortedMap.containsKey(expectedDate.minusDays(1))) {
            return 0
        }
        if (!sortedMap.containsKey(expectedDate)) {
            expectedDate = expectedDate.minusDays(1)
        }

        while (true) {
            val day = sortedMap[expectedDate]
            if (day != null) {
                val completed = day.stepsCount >= day.stepsGoal ||
                        day.pushUpsCount >= day.pushUpsGoal ||
                        day.squatsCount >= day.squatsGoal ||
                        day.distanceKm >= day.distanceGoalKm
                if (completed) {
                    streak++
                    expectedDate = expectedDate.minusDays(1)
                } else {
                    break
                }
            } else {
                break
            }
        }
        return streak
    }
}
