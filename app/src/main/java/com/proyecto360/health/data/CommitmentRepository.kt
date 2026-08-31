package com.proyecto360.health.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.Calendar
import android.content.Context
import com.proyecto360.health.widget.WidgetUpdater

class CommitmentRepository(
    private val dao: DayCommitmentDao,
    private val appContext: Context? = null
) {
    fun observeToday(): Flow<DayCommitmentWithTodos?> {
        val (start, end) = todayBounds()
        return dao.observeToday(start, end)
    }

    suspend fun getToday(): DayCommitmentWithTodos? {
        val (start, end) = todayBounds()
        return dao.getToday(start, end)
    }

    suspend fun getTodayTodos(): List<CommitmentTodo> {
        val (start, end) = todayBounds()
        return dao.getTodosForDay(start, end)
    }

    fun observeById(id: Long): Flow<DayCommitmentWithTodos?> = dao.observeById(id)

    suspend fun getById(id: Long): DayCommitmentWithTodos? = dao.getById(id)

    suspend fun save(
        note: String,
        todoTexts: List<String>,
        existingDoneByText: Map<String, Boolean> = emptyMap(),
        existingId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val cleaned = todoTexts.map { it.trim() }.filter { it.isNotEmpty() }
        require(cleaned.isNotEmpty()) { "Agrega al menos un compromiso" }

        val commitmentId = if (existingId != null) {
            val existing = dao.getById(existingId)?.commitment
                ?: error("Compromiso no encontrado")
            dao.updateCommitment(existing.copy(note = note.trim()))
            dao.deleteTodosForCommitment(existingId)
            existingId
        } else {
            dao.insertCommitment(DayCommitment(note = note.trim()))
        }

        val todos = cleaned.mapIndexed { index, text ->
            CommitmentTodo(
                commitmentId = commitmentId,
                text = text,
                isDone = existingDoneByText[text] == true,
                sortOrder = index
            )
        }
        dao.insertTodos(todos)
        appContext?.let { WidgetUpdater.refresh(it) }
        commitmentId
    }

    suspend fun toggleTodo(todoId: Long, refreshWidget: Boolean = true) = withContext(Dispatchers.IO) {
        val todo = dao.getTodoById(todoId) ?: return@withContext
        dao.updateTodo(todo.copy(isDone = !todo.isDone))
        if (refreshWidget) {
            appContext?.let { WidgetUpdater.refresh(it) }
        }
    }

    /** Append one todo to today's commitment, creating the commitment if needed. */
    suspend fun addTodoToToday(text: String): Long = withContext(Dispatchers.IO) {
        val cleaned = text.trim()
        require(cleaned.isNotEmpty()) { "Escribe un compromiso" }

        val today = getToday()
        val commitmentId = today?.commitment?.id
            ?: dao.insertCommitment(DayCommitment())

        val nextOrder = dao.maxSortOrder(commitmentId) + 1
        dao.insertTodos(
            listOf(
                CommitmentTodo(
                    commitmentId = commitmentId,
                    text = cleaned,
                    isDone = false,
                    sortOrder = nextOrder
                )
            )
        )
        appContext?.let { WidgetUpdater.refresh(it) }
        commitmentId
    }

    suspend fun delete(commitmentId: Long) = withContext(Dispatchers.IO) {
        dao.deleteCommitment(commitmentId)
        appContext?.let { WidgetUpdater.refresh(it) }
    }

    private fun todayBounds(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
        return start.timeInMillis to end.timeInMillis
    }
}
