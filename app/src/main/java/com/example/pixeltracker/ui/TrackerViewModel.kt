package com.example.pixeltracker.ui

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import kotlinx.coroutines.runBlocking
import java.time.LocalDate

class TrackerViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.trackerDao()

    private val sensorManager = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
    
    // Храним базовое значение шагов устройства за день, чтобы корректно прибавлять дельту
    private var lastSensorValue = -1f

    private val _currentDate = MutableStateFlow(LocalDate.now().toString())
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    private val todayTrackerFlow: StateFlow<DailyTracker?> = _currentDate.flatMapLatest { date ->
        dao.getTrackerByDate(date)
    }.stateIn(viewModelScope, SharingStarted.Lazily, null)

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

    val todayCustomProgress: StateFlow<List<ActivityProgressEntity>> = _currentDate.flatMapLatest { date ->
        dao.getProgressForDate(date)
    }.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val notes: StateFlow<List<NoteEntity>> = dao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentStreak: StateFlow<Int> = allHistory.map { history ->
        calculateStreak(history)
    }.stateIn(viewModelScope, SharingStarted.Lazily, 0)

    init {
        checkAndUpdateDate()
        registerStepSensor()
    }

    private fun registerStepSensor() {
        stepSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            val currentSensorSteps = event.values[0]
            
            if (lastSensorValue < 0f) {
                lastSensorValue = currentSensorSteps
                return
            }

            val deltaSteps = (currentSensorSteps - lastSensorValue).toInt()
            if (deltaSteps > 0) {
                lastSensorValue = currentSensorSteps
                viewModelScope.launch(Dispatchers.IO) {
                    checkAndUpdateDate()
                    val existingTracker = dao.getTrackerByDateSync(_currentDate.value)
                    val currentStoredSteps = existingTracker?.stepsCount ?: 0
                    val newSteps = currentStoredSteps + deltaSteps
                    val newDistance = newSteps * 0.00075f
                    
                    dao.updateSteps(_currentDate.value, newSteps)
                    dao.updateDistance(_currentDate.value, newDistance)
                }
            } else if (deltaSteps < 0) {
                // Если телефон перезагружался, системный счетчик мог сброситься в 0
                lastSensorValue = currentSensorSteps
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    fun refreshData() {
        checkAndUpdateDate()
    }

    private fun checkAndUpdateDate() {
        val todayStr = LocalDate.now().toString()
        if (_currentDate.value != todayStr) {
            _currentDate.value = todayStr
            lastSensorValue = -1f
        }
        runBlocking(Dispatchers.IO) {
            val existing = dao.getTrackerByDateSync(todayStr)
            if (existing == null) {
                val lastTracker = dao.getAllTrackers().firstOrNull()?.firstOrNull()
                val newTracker = DailyTracker(
                    date = todayStr,
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

    fun addSteps(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val current = todaySteps.value
            val newSteps = current + amount
            val newDistance = newSteps * 0.00075f
            dao.updateSteps(_currentDate.value, newSteps)
            dao.updateDistance(_currentDate.value, newDistance)
        }
    }

    fun setSteps(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val newDistance = count * 0.00075f
            dao.updateSteps(_currentDate.value, count.coerceAtLeast(0))
            dao.updateDistance(_currentDate.value, newDistance.coerceAtLeast(0f))
        }
    }

    fun addDistance(amountKm: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val current = todayDistanceKm.value
            dao.updateDistance(_currentDate.value, current + amountKm)
        }
    }

    fun setDistance(distanceKm: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            dao.updateDistance(_currentDate.value, distanceKm.coerceAtLeast(0f))
        }
    }

    fun addPushUps(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val current = todayPushUps.value
            dao.updatePushUps(_currentDate.value, current + amount)
        }
    }

    fun setPushUps(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            dao.updatePushUps(_currentDate.value, count.coerceAtLeast(0))
        }
    }

    fun addSquats(amount: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val current = todaySquats.value
            dao.updateSquats(_currentDate.value, current + amount)
        }
    }

    fun setSquats(count: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            dao.updateSquats(_currentDate.value, count.coerceAtLeast(0))
        }
    }

    fun updateGoals(pushUps: Int, squats: Int, steps: Int, distance: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val current = dao.getTrackerByDateSync(_currentDate.value) ?: DailyTracker(date = _currentDate.value)
            val updated = current.copy(
                pushUpsGoal = pushUps,
                squatsGoal = squats,
                stepsGoal = steps,
                distanceGoalKm = distance
            )
            dao.insertOrUpdate(updated)
        }
    }

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

    fun addCustomActivityProgress(activityId: Long, delta: Int, goal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val existingList = dao.getProgressForDate(_currentDate.value).firstOrNull() ?: emptyList()
            val currentItem = existingList.find { it.activityId == activityId }
            val newCount = (currentItem?.currentCount ?: 0) + delta

            val progressEntity = ActivityProgressEntity(
                activityId = activityId,
                date = _currentDate.value,
                currentCount = newCount.coerceAtLeast(0),
                goalCount = goal
            )
            dao.insertOrUpdateProgress(progressEntity)
        }
    }

    fun addCustomProgress(activityId: Long, delta: Int, goal: Int) {
        addCustomActivityProgress(activityId, delta, goal)
    }

    fun updateCustomProgress(activityId: Long, newCount: Int, goal: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            checkAndUpdateDate()
            val progressEntity = ActivityProgressEntity(
                activityId = activityId,
                date = _currentDate.value,
                currentCount = newCount.coerceAtLeast(0),
                goalCount = goal
            )
            dao.insertOrUpdateProgress(progressEntity)
        }
    }

    fun addNote(title: String, content: String, colorHex: String = "#FFB7B2") {
        viewModelScope.launch(Dispatchers.IO) {
            val note = NoteEntity(title = title, content = content, colorHex = colorHex)
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

    override fun onCleared() {
        super.onCleared()
        try {
            sensorManager.unregisterListener(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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
