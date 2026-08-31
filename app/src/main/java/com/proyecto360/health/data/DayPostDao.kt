package com.proyecto360.health.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayPostDao {
    @Query("SELECT * FROM day_posts ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DayPost>>

    @Query("SELECT * FROM day_posts WHERE id = :id")
    fun observeById(id: Long): Flow<DayPost?>

    @Query("SELECT * FROM day_posts WHERE id = :id")
    suspend fun getById(id: Long): DayPost?

    @Query(
        """
        SELECT * FROM day_posts
        WHERE createdAt >= :startOfDay AND createdAt < :endOfDay
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    fun observeToday(startOfDay: Long, endOfDay: Long): Flow<DayPost?>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(post: DayPost): Long

    @Update
    suspend fun update(post: DayPost)

    @Delete
    suspend fun delete(post: DayPost)
}
