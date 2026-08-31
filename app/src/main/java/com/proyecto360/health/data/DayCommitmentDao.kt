package com.proyecto360.health.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DayCommitmentDao {
    @Transaction
    @Query("SELECT * FROM day_commitments ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<DayCommitmentWithTodos>>

    @Transaction
    @Query("SELECT * FROM day_commitments WHERE id = :id")
    fun observeById(id: Long): Flow<DayCommitmentWithTodos?>

    @Transaction
    @Query("SELECT * FROM day_commitments WHERE id = :id")
    suspend fun getById(id: Long): DayCommitmentWithTodos?

    @Transaction
    @Query(
        """
        SELECT * FROM day_commitments
        WHERE createdAt >= :startOfDay AND createdAt < :endOfDay
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    fun observeToday(startOfDay: Long, endOfDay: Long): Flow<DayCommitmentWithTodos?>

    @Transaction
    @Query(
        """
        SELECT * FROM day_commitments
        WHERE createdAt >= :startOfDay AND createdAt < :endOfDay
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    suspend fun getToday(startOfDay: Long, endOfDay: Long): DayCommitmentWithTodos?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCommitment(commitment: DayCommitment): Long

    @Update
    suspend fun updateCommitment(commitment: DayCommitment)

    @Query("DELETE FROM day_commitments WHERE id = :id")
    suspend fun deleteCommitment(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTodos(todos: List<CommitmentTodo>)

    @Update
    suspend fun updateTodo(todo: CommitmentTodo)

    @Query("DELETE FROM commitment_todos WHERE commitmentId = :commitmentId")
    suspend fun deleteTodosForCommitment(commitmentId: Long)

    @Query("SELECT * FROM commitment_todos WHERE id = :id")
    suspend fun getTodoById(id: Long): CommitmentTodo?

    @Query(
        """
        SELECT t.* FROM commitment_todos t
        INNER JOIN day_commitments c ON c.id = t.commitmentId
        WHERE c.createdAt >= :startOfDay AND c.createdAt < :endOfDay
        ORDER BY t.sortOrder ASC, t.id ASC
        """
    )
    suspend fun getTodosForDay(startOfDay: Long, endOfDay: Long): List<CommitmentTodo>

    @Query(
        """
        SELECT COALESCE(MAX(sortOrder), -1) FROM commitment_todos
        WHERE commitmentId = :commitmentId
        """
    )
    suspend fun maxSortOrder(commitmentId: Long): Int
}
