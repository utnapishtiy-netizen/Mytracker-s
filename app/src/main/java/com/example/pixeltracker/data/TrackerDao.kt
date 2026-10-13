package com.example.pixeltracker.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {
    @Query("SELECT * FROM daily_tracker WHERE date = :date")
    fun getTrackerByDate(date: String): Flow<DailyTracker?>

    @Query("SELECT * FROM daily_tracker ORDER BY date DESC")
    fun getAllTrackers(): Flow<List<DailyTracker>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tracker: DailyTracker)

    // Методы для пользовательских активностей
    @Query("SELECT * FROM custom_activities")
    fun getAllCustomActivities(): Flow<List<CustomActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomActivity(activity: CustomActivityEntity)

    @Query("SELECT * FROM activity_progress WHERE date = :date")
    fun getProgressForDate(date: String): Flow<List<ActivityProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ActivityProgressEntity)

    @Query("SELECT * FROM daily_tracker WHERE date = :date LIMIT 1")
    suspend fun getTrackerByDateSync(date: String): DailyTracker?
}
