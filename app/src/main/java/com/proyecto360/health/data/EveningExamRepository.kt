package com.proyecto360.health.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar

class EveningExamRepository(
    private val dao: EveningExamDao
) {
    fun observeToday(): Flow<EveningExam?> {
        val (start, end) = dayBounds(0)
        return dao.observeForDay(start, end)
    }

    fun observeYesterday(): Flow<EveningExam?> {
        val (start, end) = dayBounds(-1)
        return dao.observeForDay(start, end)
    }

    fun observeById(id: Long): Flow<EveningExam?> = dao.observeById(id)

    suspend fun getById(id: Long): EveningExam? = dao.getById(id)

    suspend fun getYesterday(): EveningExam? = withContext(Dispatchers.IO) {
        val (start, end) = dayBounds(-1)
        dao.getForDay(start, end)
    }

    suspend fun save(
        didWell: String,
        didNotWell: String,
        courseCorrection: String,
        focusTags: List<String>,
        existingId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        require(didWell.isNotBlank() || didNotWell.isNotBlank() || courseCorrection.isNotBlank()) {
            "Escribe al menos una sección del examen"
        }
        require(courseCorrection.isNotBlank()) {
            "Define un propósito concreto para mañana"
        }

        val tags = focusTags.joinToString(",")
        if (existingId != null) {
            val existing = dao.getById(existingId) ?: error("Examen no encontrado")
            dao.update(
                existing.copy(
                    didWell = didWell.trim(),
                    didNotWell = didNotWell.trim(),
                    courseCorrection = courseCorrection.trim(),
                    focusTags = tags
                )
            )
            existingId
        } else {
            dao.insert(
                EveningExam(
                    didWell = didWell.trim(),
                    didNotWell = didNotWell.trim(),
                    courseCorrection = courseCorrection.trim(),
                    focusTags = tags
                )
            )
        }
    }

    suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.delete(id)
    }

    /** @param dayOffset 0 = today, -1 = yesterday */
    private fun dayBounds(dayOffset: Int): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, dayOffset)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        return start.timeInMillis to end.timeInMillis
    }
}
