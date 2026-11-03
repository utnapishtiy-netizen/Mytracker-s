package com.example.pixeltracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    @Query("SELECT * FROM custom_activities")
    suspend fun getAllCustomActivitiesSync(): List<CustomActivityEntity>

    @Query("SELECT * FROM notes")
    suspend fun getAllNotesSync(): List<NoteEntity>

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

    // Точки маршрута GPS
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutePoint(point: RoutePointEntity)

    @Query("SELECT * FROM route_points ORDER BY timestamp ASC")
    fun getAllRoutePoints(): Flow<List<RoutePointEntity>>

    @Query("DELETE FROM route_points")
    suspend fun clearRoutePoints()
}
