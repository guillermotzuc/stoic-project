package com.proyecto360.health.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface EveningExamDao {
    @Query("SELECT * FROM evening_exams ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<EveningExam>>

    @Query("SELECT * FROM evening_exams WHERE id = :id")
    fun observeById(id: Long): Flow<EveningExam?>

    @Query("SELECT * FROM evening_exams WHERE id = :id")
    suspend fun getById(id: Long): EveningExam?

    @Query(
        """
        SELECT * FROM evening_exams
        WHERE createdAt >= :startOfDay AND createdAt < :endOfDay
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    fun observeForDay(startOfDay: Long, endOfDay: Long): Flow<EveningExam?>

    @Query(
        """
        SELECT * FROM evening_exams
        WHERE createdAt >= :startOfDay AND createdAt < :endOfDay
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getForDay(startOfDay: Long, endOfDay: Long): EveningExam?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(exam: EveningExam): Long

    @Update
    suspend fun update(exam: EveningExam)

    @Query("DELETE FROM evening_exams WHERE id = :id")
    suspend fun delete(id: Long)
}
