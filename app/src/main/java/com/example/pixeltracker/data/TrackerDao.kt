package com.example.pixeltracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    @Query("SELECT * FROM daily_tracker WHERE date = :date LIMIT 1")
    fun getTrackerByDate(date: String): Flow<DailyTracker?>

    @Query("SELECT * FROM daily_tracker WHERE date = :date LIMIT 1")
    fun getTrackerByDateSync(date: String): DailyTracker?

    @Query("SELECT * FROM daily_tracker ORDER BY date DESC")
    fun getAllTrackers(): Flow<List<DailyTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tracker: DailyTracker)

    @Query("UPDATE daily_tracker SET stepsCount = :steps WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int)

    @Query("UPDATE daily_tracker SET stepsGoal = :goal WHERE date = :date")
    suspend fun updateGoal(date: String, goal: Int)

    @Query("UPDATE daily_tracker SET pushUpsCount = :count WHERE date = :date")
    suspend fun updatePushUps(date: String, count: Int)

    @Query("UPDATE daily_tracker SET squatsCount = :count WHERE date = :date")
    suspend fun updateSquats(date: String, count: Int)

    // Кастомные активности
    @Query("SELECT * FROM custom_activities")
    fun getAllCustomActivities(): Flow<List<CustomActivityEntity>>

    @Query("SELECT * FROM custom_activities")
    suspend fun getAllCustomActivitiesSync(): List<CustomActivityEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomActivity(activity: CustomActivityEntity)

    @Query("DELETE FROM custom_activities WHERE id = :id")
    suspend fun deleteCustomActivity(id: Long)

    // Заметки
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    suspend fun getAllNotesSync(): List<NoteEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    // GPS точки
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoint(point: RoutePointEntity)

    @Query("SELECT * FROM route_points ORDER BY timestamp ASC")
    fun getAllRoutePoints(): Flow<List<RoutePointEntity>>

    @Query("DELETE FROM route_points")
    suspend fun clearRoutePoints()
}
