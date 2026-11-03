package com.example.pixeltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_activities")
data class CustomActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val unit: String = "раз",
    val defaultGoal: Int = 1,
    val iconType: String = "default"
)
