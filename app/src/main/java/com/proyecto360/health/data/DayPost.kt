package com.proyecto360.health.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

@Entity(tableName = "day_posts")
data class DayPost(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationLabel: String? = null,
    /** JSON array of file paths, or a single legacy path. */
    val photoPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun resolvedPhotoPaths(): List<String> = parsePhotoPaths(photoPath)
}

fun parsePhotoPaths(raw: String?): List<String> {
    val value = raw?.trim().orEmpty()
    if (value.isEmpty()) return emptyList()
    if (value.startsWith("[")) {
        return runCatching {
            val array = JSONArray(value)
            buildList {
                for (i in 0 until array.length()) {
                    array.optString(i).takeIf { it.isNotBlank() }?.let(::add)
                }
            }
        }.getOrElse { listOf(value) }
    }
    return listOf(value)
}

fun encodePhotoPaths(paths: List<String>): String? {
    val cleaned = paths.map { it.trim() }.filter { it.isNotEmpty() }
    if (cleaned.isEmpty()) return null
    return JSONArray(cleaned).toString()
}
