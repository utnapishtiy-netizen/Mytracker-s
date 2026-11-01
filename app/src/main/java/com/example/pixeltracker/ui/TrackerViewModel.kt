package com.example.pixeltracker.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.data.CustomActivityEntity
import com.example.pixeltracker.data.DailyTracker
import com.example.pixeltracker.data.ActivityProgressEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).trackerDao()
    private val currentDate = LocalDate.now().toString()

    // Поток данных за сегодняшний день
    private val todayTrackerFlow: StateFlow<DailyTracker?> = dao.getTrackerByDate(currentDate)
        .stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Основные метрики дня
    val todaySteps: StateFlow<Int> = todayTrackerFlow.map { it?.stepsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val todayDistanceKm: StateFlow<Float> = todayTrackerFlow.map { it?.distanceKm ?: 0f }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0f)

    val todayPushUps: StateFlow<Int> = todayTrackerFlow.map { it?.pushUpsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    val todaySquats: StateFlow<Int> = todayTrackerFlow.map { it?.squatsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 0)

    // Цели
    val stepsGoal: StateFlow<Int> = todayTrackerFlow.map { it?.stepsGoal ?: 10000 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 10000)

    val pushUpsGoal: StateFlow<Int> = todayTrackerFlow.map { it?.pushUpsGoal ?: 100 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 100)

    val squatsGoal: StateFlow<Int> = todayTrackerFlow.map { it?.squatsGoal ?: 100 }
        .stateIn(viewModelScope, SharingStarted.Lazily, 100)

    val distanceGoalKm: StateFlow<Float> = todayTrackerFlow.map { it?.distanceGoalKm ?: 7.0f }
        .stateIn(viewModelScope, SharingStarted.Lazily, 7.0f)

    // История всех дней для календаря и списка
    val allHistory: StateFlow<List<DailyTracker>> = dao.getAllTrackers()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Кастомные активности
    val customActivities: StateFlow<List<CustomActivityEntity>> = dao.getAllCustomActivities()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val todayCustomProgress: StateFlow<List<ActivityProgressEntity>> = dao.getProgressForDate(currentDate)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Стрик (подряд дней выполнения целей)
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
                // Создаем новую запись на сегодня с дефолтными или скопированными с прошлого дня целями
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

    // Инкремент отжиманий (точечный SQL-запрос исключает гонки данных)
    fun addPushUps(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = todayPushUps.value
            dao.updatePushUps(currentDate, current + amount)
        }
    }

    // Точное задание отжиманий
    fun setPushUps(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updatePushUps(currentDate, count.coerceAtLeast(0))
        }
    }

    // Инкремент приседаний
    fun addSquats(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val current = todaySquats.value
            dao.updateSquats(currentDate, current + amount)
        }
    }

    // Точное задание приседаний
    fun setSquats(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateSquats(currentDate, count.coerceAtLeast(0))
        }
    }

    // Обновление целей
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

    // Добавление кастомной активности
    fun addCustomActivity(name: String, unit: String, defaultGoal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val activity = CustomActivityEntity(name = name, unit = unit, defaultGoal = defaultGoal)
            dao.insertCustomActivity(activity)
        }
    }

    // Удаление кастомной активности и её прогресса
    fun deleteCustomActivity(activityId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteCustomActivity(activityId)
            dao.deleteProgressForActivity(activityId)
        }
    }

    // Изменение прогресса кастомной активности
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

    fun refreshData() {
        ensureTodayRecordExists()
    }

    private fun calculateStreak(history: List<DailyTracker>): Int {
        if (history.isEmpty()) return 0
        var streak = 0
        var expectedDate = LocalDate.now()

        val sortedMap = history.associateBy { LocalDate.parse(it.date) }

        // Проверяем сегодня или вчера для старта стрика
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
