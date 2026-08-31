package com.proyecto360.health

import android.app.Application
import com.proyecto360.health.data.AppDatabase
import com.proyecto360.health.data.CommitmentRepository
import com.proyecto360.health.data.EveningExamRepository
import com.proyecto360.health.data.MoodRepository
import com.proyecto360.health.data.PhraseRepository
import com.proyecto360.health.data.PostRepository

class HealthApplication : Application() {
    val database by lazy { AppDatabase.getInstance(this) }
    val phraseRepository by lazy { PhraseRepository(this) }
    val postRepository by lazy { PostRepository(database.dayPostDao(), this) }
    val commitmentRepository by lazy { CommitmentRepository(database.dayCommitmentDao(), this) }
    val eveningExamRepository by lazy { EveningExamRepository(database.eveningExamDao()) }
    val moodRepository by lazy { MoodRepository(database.dayMoodDao()) }
}
