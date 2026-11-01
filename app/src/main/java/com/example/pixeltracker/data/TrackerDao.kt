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
    suspend fun getTrackerByDateSync(date: String): DailyTracker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tracker: DailyTracker)

    // Точечные апдейты для предотвращения затирания данных
    @Query("UPDATE daily_tracker SET stepsCount = :steps WHERE date = :date")
    suspend fun updateSteps(date: String, steps: Int)

    @Query("UPDATE daily_tracker SET pushUpsCount = :count WHERE date = :date")
    suspend fun updatePushUps(date: String, count: Int)

    @Query("UPDATE daily_tracker SET squatsCount = :count WHERE date = :date")
    suspend fun updateSquats(date: String, count: Int)

    @Query("UPDATE daily_tracker SET distanceKm = :dist WHERE date = :date")
    suspend fun updateDistance(date: String, dist: Float)

    @Query("SELECT * FROM daily_tracker ORDER BY date DESC")
    fun getAllTrackers(): Flow<List<DailyTracker>>

    // Кастомные активности
    @Query("SELECT * FROM custom_activities")
    fun getAllCustomActivities(): Flow<List<CustomActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomActivity(activity: CustomActivityEntity)

    // Удаление кастомной активности по её ID
    @Query("DELETE FROM custom_activities WHERE id = :activityId")
    suspend fun deleteCustomActivity(activityId: Long)

    // Удаление прогресса удаленной активности за день
    @Query("DELETE FROM activity_progress WHERE activityId = :activityId")
    suspend fun deleteProgressForActivity(activityId: Long)

    @Query("SELECT * FROM activity_progress WHERE date = :date")
    fun getProgressForDate(date: String): Flow<List<ActivityProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ActivityProgressEntity)
}
