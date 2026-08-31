package com.proyecto360.health.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        DayPost::class,
        DayCommitment::class,
        CommitmentTodo::class,
        EveningExam::class,
        DayMood::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dayPostDao(): DayPostDao
    abstract fun dayCommitmentDao(): DayCommitmentDao
    abstract fun eveningExamDao(): EveningExamDao
    abstract fun dayMoodDao(): DayMoodDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_360.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { instance = it }
            }
        }
    }
}
