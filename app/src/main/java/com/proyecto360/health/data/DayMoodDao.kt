package com.proyecto360.health.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayMoodDao {
    @Query(
        """
        SELECT * FROM day_moods
        WHERE dayStart = :dayStart
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    fun observeLatestForDay(dayStart: Long): Flow<DayMood?>

    @Query(
        """
        SELECT * FROM day_moods
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getLatest(): DayMood?

    @Query(
        """
        SELECT * FROM day_moods
        WHERE dayStart >= :fromDayStart
        ORDER BY createdAt ASC
        """
    )
    fun observeSince(fromDayStart: Long): Flow<List<DayMood>>

    @Insert
    suspend fun insert(mood: DayMood): Long

    @Update
    suspend fun update(mood: DayMood)
}
