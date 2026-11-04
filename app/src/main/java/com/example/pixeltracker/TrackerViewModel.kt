package com.example.pixeltracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixeltracker.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.trackerDao()

    private val currentDate: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val todayTrackerFlow: StateFlow<DailyTracker?> = dao.getTrackerByDate(currentDate)
        .stateIn(scope = viewModelScope, started = SharingStarted.WhileSubscribed(5000), initialValue = null)

    val todaySteps: StateFlow<Int> = todayTrackerFlow
        .map { it?.stepsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stepsGoal: StateFlow<Int> = todayTrackerFlow
        .map { it?.stepsGoal ?: 10000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000)

    val todayDistanceKm: StateFlow<Float> = todayTrackerFlow
        .map { it?.distanceKm ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val todayPushUps: StateFlow<Int> = todayTrackerFlow
        .map { it?.pushUpsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val pushUpsGoal: StateFlow<Int> = todayTrackerFlow
        .map { it?.pushUpsGoal ?: 50 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    val todaySquats: StateFlow<Int> = todayTrackerFlow
        .map { it?.squatsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val squatsGoal: StateFlow<Int> = todayTrackerFlow
        .map { it?.squatsGoal ?: 50 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    val allTrackers: Flow<List<DailyTracker>> = dao.getAllTrackers()
    val customActivities: Flow<List<CustomActivityEntity>> = dao.getAllCustomActivities()
    val notes: Flow<List<NoteEntity>> = dao.getAllNotes()

    fun updateSteps(newSteps: Int) {
        viewModelScope.launch {
            val existing = dao.getTrackerByDateSync(currentDate)
            if (existing != null) {
                dao.updateSteps(currentDate, newSteps)
            } else {
                dao.insertOrUpdate(DailyTracker(date = currentDate, stepsCount = newSteps))
            }
        }
    }

    fun updateGoal(newGoal: Int) {
        viewModelScope.launch {
            val existing = dao.getTrackerByDateSync(currentDate)
            if (existing != null) {
                dao.updateGoal(currentDate, newGoal)
            } else {
                dao.insertOrUpdate(DailyTracker(date = currentDate, stepsGoal = newGoal))
            }
        }
    }

    fun addPushUps(count: Int) {
        viewModelScope.launch {
            val existing = dao.getTrackerByDateSync(currentDate)
            val newCount = (existing?.pushUpsCount ?: 0) + count
            if (existing != null) {
                dao.updatePushUps(currentDate, newCount)
            } else {
                dao.insertOrUpdate(DailyTracker(date = currentDate, pushUpsCount = newCount))
            }
        }
    }

    fun addSquats(count: Int) {
        viewModelScope.launch {
            val existing = dao.getTrackerByDateSync(currentDate)
            val newCount = (existing?.squatsCount ?: 0) + count
            if (existing != null) {
                dao.updateSquats(currentDate, newCount)
            } else {
                dao.insertOrUpdate(DailyTracker(date = currentDate, squatsCount = newCount))
            }
        }
    }

    fun addCustomActivity(name: String, unit: String = "раз", defaultGoal: Int = 10) {
        viewModelScope.launch {
            dao.insertCustomActivity(CustomActivityEntity(name = name, unit = unit, defaultGoal = defaultGoal))
        }
    }

    fun deleteCustomActivity(id: Long) {
        viewModelScope.launch {
            dao.deleteCustomActivity(id)
        }
    }

    fun addNote(text: String) {
        viewModelScope.launch {
            dao.insertNote(NoteEntity(text = text))
        }
    }

    fun deleteNote(id: Long) {
        viewModelScope.launch {
            dao.deleteNote(id)
        }
    }
}
