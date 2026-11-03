package com.example.pixeltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_tracker")
data class DailyTracker(
    @PrimaryKey val date: String, // Формат "yyyy-MM-dd"
    val stepsCount: Int = 0,
    val stepsGoal: Int = 10000,
    val distanceKm: Float = 0f
)
