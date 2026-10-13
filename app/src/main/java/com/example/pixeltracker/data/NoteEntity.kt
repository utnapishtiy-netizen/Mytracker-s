package com.example.pixeltracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val title: String,
    val content: String,
    val timestamp: Long = System.currentTimeMillis(),
    val reminderTimestamp: Long? = null // Время напоминания
)
