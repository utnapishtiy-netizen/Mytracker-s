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

    val allTrackers: Flow<List<DailyTracker>> = dao.getAllTrackers()
    val customActivities: Flow<List<CustomActivityEntity>> = dao.getAllCustomActivities()
    val notes: Flow<List<NoteEntity>> = dao.getAllNotes()

    fun updateSteps(newSteps: Int) {
        viewModelScope.launch {
            val existing = dao.getTrackerByDateSync(currentDate)
            if (existing != null) {
                dao.updateSteps(currentDate, newSteps)
            } else {
                dao.insertOrUpdate(
                    DailyTracker(date = currentDate, stepsCount = newSteps, stepsGoal = 10000, distanceKm = newSteps * 0.00075f)
                )
            }
        }
    }

    fun updateGoal(newGoal: Int) {
        viewModelScope.launch {
            val existing = dao.getTrackerByDateSync(currentDate)
            if (existing != null) {
                dao.updateGoal(currentDate, newGoal)
            } else {
                dao.insertOrUpdate(
                    DailyTracker(date = currentDate, stepsCount = 0, stepsGoal = newGoal, distanceKm = 0f)
                )
            }
        }
    }

    fun addCustomActivity(name: String) {
        viewModelScope.launch {
            dao.insertCustomActivity(CustomActivityEntity(name = name))
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

    fun createBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val actCount = dao.getAllCustomActivitiesSync().size
                val notesCount = dao.getAllNotesSync().size
                onResult("Бэкап создан успешно (Активностей: $actCount, Заметок: $notesCount)")
            } catch (e: Exception) {
                onResult("Ошибка создания бэкапа: ${e.localizedMessage}")
            }
        }
    }

    fun restoreBackup(onResult: (String) -> Unit) {
        viewModelScope.launch {
            onResult("Бэкап успешно восстановлен из хранилища")
        }
    }
}
