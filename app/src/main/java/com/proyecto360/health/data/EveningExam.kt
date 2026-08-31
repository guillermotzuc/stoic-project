package com.proyecto360.health.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evening_exams")
data class EveningExam(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val didWell: String,
    val didNotWell: String,
    val courseCorrection: String,
    /** Comma-separated optional prompt tags the user tapped */
    val focusTags: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
