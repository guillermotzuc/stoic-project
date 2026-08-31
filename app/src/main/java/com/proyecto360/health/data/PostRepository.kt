package com.proyecto360.health.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class PostRepository(
    private val dao: DayPostDao,
    private val context: Context
) {
    fun observeAll(): Flow<List<DayPost>> = dao.observeAll()

    fun observeById(id: Long): Flow<DayPost?> = dao.observeById(id)

    fun observeToday(): Flow<DayPost?> {
        val (start, end) = todayBounds()
        return dao.observeToday(start, end)
    }

    suspend fun getById(id: Long): DayPost? = dao.getById(id)

    suspend fun save(
        content: String,
        latitude: Double?,
        longitude: Double?,
        locationLabel: String?,
        newPhotoUris: List<Uri> = emptyList(),
        keptPhotoPaths: List<String> = emptyList(),
        existingId: Long? = null
    ): Long = withContext(Dispatchers.IO) {
        val existing = existingId?.let { dao.getById(it) }
        val existingPaths = parsePhotoPaths(existing?.photoPath)
        val kept = keptPhotoPaths.filter { path ->
            path in existingPaths || File(path).exists()
        }
        existingPaths.filter { it !in kept }.forEach { path ->
            File(path).takeIf { it.exists() }?.delete()
        }
        val persistedNew = newPhotoUris.map { persistPhoto(it) }
        val photoPath = encodePhotoPaths(kept + persistedNew)

        val normalizedLabel = locationLabel?.trim()?.takeIf { it.isNotEmpty() }
        val hasLocation = latitude != null && longitude != null

        val post = DayPost(
            id = existing?.id ?: 0,
            content = content.trim(),
            latitude = if (hasLocation) latitude else null,
            longitude = if (hasLocation) longitude else null,
            locationLabel = if (hasLocation) {
                normalizedLabel
                    ?: "%.5f, %.5f".format(Locale.US, latitude, longitude)
            } else {
                null
            },
            photoPath = photoPath,
            createdAt = existing?.createdAt ?: System.currentTimeMillis()
        )

        if (existing != null) {
            dao.update(post)
            existing.id
        } else {
            dao.insert(post)
        }
    }

    suspend fun delete(post: DayPost) = withContext(Dispatchers.IO) {
        post.resolvedPhotoPaths().forEach { path ->
            File(path).takeIf { it.exists() }?.delete()
        }
        dao.delete(post)
    }

    private fun persistPhoto(source: Uri): String {
        val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
        val dest = File(photosDir, "${UUID.randomUUID()}.jpg")
        context.contentResolver.openInputStream(source)?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: error("No se pudo leer la imagen")
        return dest.absolutePath
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
