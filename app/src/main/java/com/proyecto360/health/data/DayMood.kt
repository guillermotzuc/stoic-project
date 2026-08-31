package com.proyecto360.health.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "day_moods",
    indices = [Index(value = ["dayStart"]), Index(value = ["createdAt"])]
)
data class DayMood(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val moodId: String,
    /** Midnight timestamp of the calendar day this mood belongs to */
    val dayStart: Long,
    /** When this mood entry was first created */
    val createdAt: Long = System.currentTimeMillis(),
    /** Last time this entry was updated (within the 1-hour window) */
    val updatedAt: Long = System.currentTimeMillis()
)
