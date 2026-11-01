package com.example.pixeltracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackerDao {

    @Query("SELECT * FROM daily_tracker WHERE date = :date LIMIT 1")
    fun getTrackerByDate(date: String): Flow<DailyTracker?>

    @Query("SELECT * FROM daily_tracker WHERE date = :date LIMIT 1")
    suspend fun getTrackerByDateSync(date: String): DailyTracker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(tracker: DailyTracker)

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

    @Query("DELETE FROM custom_activities WHERE id = :activityId")
    suspend fun deleteCustomActivity(activityId: Long)

    @Query("DELETE FROM activity_progress WHERE activityId = :activityId")
    suspend fun deleteProgressForActivity(activityId: Long)

    @Query("SELECT * FROM activity_progress WHERE date = :date")
    fun getProgressForDate(date: String): Flow<List<ActivityProgressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: ActivityProgressEntity)

    // Методы для заметок (Notes)
    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAllNotes(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity)

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Delete
    suspend fun deleteNote(note: NoteEntity)
}
