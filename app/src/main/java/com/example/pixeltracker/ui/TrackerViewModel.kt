package com.example.pixeltracker

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pixeltracker.data.AppDatabase
import com.example.pixeltracker.data.DailyTracker
import com.example.pixeltracker.data.RoutePointEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TrackerViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.trackerDao()

    private val currentDate: String
        get() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val todayTrackerFlow: StateFlow<DailyTracker?> = dao.getTrackerByDate(currentDate)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    val todaySteps: StateFlow<Int> = todayTrackerFlow
        .map { it?.stepsCount ?: 0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val stepsGoal: StateFlow<Int> = todayTrackerFlow
        .map { it?.stepsGoal ?: 10000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 10000)

    val todayDistanceKm: StateFlow<Float> = todayTrackerFlow
        .map { it?.distanceKm ?: 0f }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    val allTrackers: Flow<List<DailyTracker>> = dao.getAllTrackers()

    fun updateSteps(newSteps: Int) {
        viewModelScope.launch {
            val existing = dao.getTrackerByDateSync(currentDate)
            if (existing != null) {
                dao.updateSteps(currentDate, newSteps)
            } else {
                dao.insertOrUpdate(
                    DailyTracker(
                        date = currentDate,
                        stepsCount = newSteps,
                        stepsGoal = 10000,
                        distanceKm = newSteps * 0.00075f
                    )
                )
            }
        }
    }
}
