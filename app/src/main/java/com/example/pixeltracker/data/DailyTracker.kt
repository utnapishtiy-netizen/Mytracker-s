package com.example.pixeltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tracker")
data class DailyTracker(
    @PrimaryKey
    val date: String, // Формат: YYYY-MM-DD
    val stepsCount: Int = 0,
    val distanceKm: Float = 0.0f,
    val pushUpsCount: Int = 0,
    val squatsCount: Int = 0,
    val pushUpsGoal: Int = 100,
    val squatsGoal: Int = 100,
    val stepsGoal: Int = 10000,
    val distanceGoalKm: Float = 5.0f
)

@Entity(tableName = "custom_activities")
data class CustomActivityEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val unit: String,
    val defaultGoal: Int,
    val iconType: String
)

@Entity(tableName = "activity_progress", primaryKeys = ["date", "activityId"])
data class ActivityProgressEntity(
    val date: String,
    val activityId: Long,
    val currentCount: Int = 0,
    val goalCount: Int = 100
)
