package com.proyecto360.health.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

data class MoodDayPoint(
    val dayStart: Long,
    val level: MoodLevel?,
    val entryCount: Int = 0
)

data class MoodChartSummary(
    val points: List<MoodDayPoint>,
    val averageScore: Double?,
    val averageLevel: MoodLevel?,
    val loggedCount: Int,
    val totalEntries: Int = 0
)

class MoodRepository(
    private val dao: DayMoodDao
) {
    companion object {
        private val ONE_HOUR_MS = TimeUnit.HOURS.toMillis(1)
    }

    /** Latest mood registered today (selected state in the scale). */
    fun observeToday(): Flow<MoodLevel?> {
        return dao.observeLatestForDay(todayStart()).map { entry ->
            entry?.let { MoodLevel.fromId(it.moodId) }
        }
    }

    fun observeLastDaysChart(days: Int = 7): Flow<MoodChartSummary> {
        val start = dayStartOffset(-(days - 1))
        val dayStarts = (0 until days).map { dayStartOffset(-(days - 1 - it)) }

        return dao.observeSince(start).map { moods ->
            val byDay = moods.groupBy { it.dayStart }
            val points = dayStarts.map { day ->
                val dayMoods = byDay[day].orEmpty()
                val scores = dayMoods.mapNotNull { MoodLevel.fromId(it.moodId)?.score }
                val avgLevel = if (scores.isEmpty()) {
                    null
                } else {
                    MoodLevel.nearestByScore(scores.average())
                }
                MoodDayPoint(
                    dayStart = day,
                    level = avgLevel,
                    entryCount = dayMoods.size
                )
            }
            val allScores = moods.mapNotNull { MoodLevel.fromId(it.moodId)?.score }
            val average = if (allScores.isEmpty()) null else allScores.average()
            val daysWithData = points.count { it.level != null }
            MoodChartSummary(
                points = points,
                averageScore = average,
                averageLevel = average?.let { MoodLevel.nearestByScore(it) },
                loggedCount = daysWithData,
                totalEntries = allScores.size
            )
        }
    }

    /**
     * If the last mood was registered less than 1 hour ago, update it.
     * Otherwise insert a new mood entry (multiple per day allowed).
     */
    suspend fun setTodayMood(level: MoodLevel) = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val last = dao.getLatest()
        if (last != null && now - last.updatedAt < ONE_HOUR_MS) {
            dao.update(
                last.copy(
                    moodId = level.id,
                    updatedAt = now
                )
            )
        } else {
            dao.insert(
                DayMood(
                    moodId = level.id,
                    dayStart = todayStart(),
                    createdAt = now,
                    updatedAt = now
                )
            )
        }
    }

    private fun todayStart(): Long = dayStartOffset(0)

    private fun dayStartOffset(dayOffset: Int): Long {
        return Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}
