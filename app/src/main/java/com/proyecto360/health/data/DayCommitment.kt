package com.proyecto360.health.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "day_commitments")
data class DayCommitment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "commitment_todos",
    foreignKeys = [
        ForeignKey(
            entity = DayCommitment::class,
            parentColumns = ["id"],
            childColumns = ["commitmentId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("commitmentId")]
)
data class CommitmentTodo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val commitmentId: Long,
    val text: String,
    val isDone: Boolean = false,
    val sortOrder: Int = 0
)

data class DayCommitmentWithTodos(
    @Embedded val commitment: DayCommitment,
    @Relation(
        parentColumn = "id",
        entityColumn = "commitmentId"
    )
    val todos: List<CommitmentTodo>
)
