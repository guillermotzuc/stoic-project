package com.proyecto360.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyecto360.health.data.CommitmentRepository
import com.proyecto360.health.data.DayCommitmentWithTodos
import com.proyecto360.health.data.DayPost
import com.proyecto360.health.data.EveningExam
import com.proyecto360.health.data.EveningExamRepository
import com.proyecto360.health.data.MoodChartSummary
import com.proyecto360.health.data.MoodLevel
import com.proyecto360.health.data.MoodRepository
import com.proyecto360.health.data.PhraseRepository
import com.proyecto360.health.data.PostRepository
import com.proyecto360.health.data.StoicPhrase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID

class HomeViewModel(
    phraseRepository: PhraseRepository,
    postRepository: PostRepository,
    private val commitmentRepository: CommitmentRepository,
    eveningExamRepository: EveningExamRepository,
    private val moodRepository: MoodRepository
) : ViewModel() {
    private val _phrase = MutableStateFlow(phraseRepository.phraseOfTheDay())
    val phrase: StateFlow<StoicPhrase> = _phrase.asStateFlow()

    private val phraseRepo = phraseRepository

    val todayPost: StateFlow<DayPost?> = postRepository.observeToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val todayCommitment: StateFlow<DayCommitmentWithTodos?> =
        commitmentRepository.observeToday()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val todayExam: StateFlow<EveningExam?> = eveningExamRepository.observeToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val yesterdayExam: StateFlow<EveningExam?> = eveningExamRepository.observeYesterday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val todayMood: StateFlow<MoodLevel?> = moodRepository.observeToday()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val moodChart: StateFlow<MoodChartSummary> = moodRepository.observeLastDaysChart(7)
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            MoodChartSummary(emptyList(), null, null, 0)
        )

    val recentPosts: StateFlow<List<DayPost>> = postRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun shufflePhrase() {
        _phrase.value = phraseRepo.randomPhrase(_phrase.value.phrase)
    }

    fun setMood(level: MoodLevel) {
        viewModelScope.launch {
            moodRepository.setTodayMood(level)
        }
    }

    fun toggleCommitmentTodo(todoId: Long) {
        viewModelScope.launch {
            commitmentRepository.toggleTodo(todoId)
        }
    }

    companion object {
        fun factory(app: HealthApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(
                    app.phraseRepository,
                    app.postRepository,
                    app.commitmentRepository,
                    app.eveningExamRepository,
                    app.moodRepository
                )
            }
        }
    }
}

class PostEditorViewModel(
    private val postRepository: PostRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(PostEditorUiState())
    val uiState: StateFlow<PostEditorUiState> = _uiState.asStateFlow()

    fun load(postId: Long?) {
        if (postId == null) {
            _uiState.value = PostEditorUiState(loaded = true)
            return
        }
        // Keep postId immediately so save updates the same row even if load is slow.
        _uiState.value = _uiState.value.copy(postId = postId)
        viewModelScope.launch {
            val post = postRepository.getById(postId) ?: return@launch
            val current = _uiState.value
            // Avoid overwriting location/content the user already changed while loading.
            if (current.loaded && current.postId == postId) return@launch
            val keepLocalLocation = current.latitude != null && current.longitude != null
            _uiState.value = PostEditorUiState(
                postId = post.id,
                content = current.content.ifBlank { post.content },
                latitude = if (keepLocalLocation) current.latitude else post.latitude,
                longitude = if (keepLocalLocation) current.longitude else post.longitude,
                locationLabel = if (keepLocalLocation) current.locationLabel else post.locationLabel,
                photos = current.photos.ifEmpty {
                    post.resolvedPhotoPaths().map { PhotoDraft(path = it) }
                },
                loaded = true
            )
        }
    }

    fun updateContent(value: String) {
        _uiState.value = _uiState.value.copy(content = value, contentError = null)
    }

    fun insertHourAndLineBreak() {
        appendAtBottom(currentTimeStamp() + "\n")
    }

    fun insertTimedNote(text: String) {
        val note = text.trim()
        if (note.isEmpty()) return
        appendAtBottom("${currentTimeStamp()} : $note\n")
    }

    fun insertEmoji(emoji: String) {
        val current = _uiState.value.content
        val insertion = if (current.isEmpty() || current.endsWith("\n") || current.endsWith(" ")) {
            emoji
        } else {
            " $emoji"
        }
        _uiState.value = _uiState.value.copy(
            content = current + insertion,
            contentError = null
        )
    }

    private fun currentTimeStamp(): String {
        return java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
            .format(java.util.Date())
    }

    private fun appendAtBottom(line: String) {
        val current = _uiState.value.content
        val insertion = buildString {
            if (current.isNotEmpty() && !current.endsWith("\n")) append('\n')
            append(line)
        }
        _uiState.value = _uiState.value.copy(
            content = current + insertion,
            contentError = null
        )
    }

    fun setLocation(latitude: Double, longitude: Double, label: String) {
        _uiState.value = _uiState.value.copy(
            latitude = latitude,
            longitude = longitude,
            locationLabel = label.ifBlank {
                "%.5f, %.5f".format(java.util.Locale.US, latitude, longitude)
            },
            locationError = null,
            isLocating = false
        )
    }

    fun clearLocation() {
        _uiState.value = _uiState.value.copy(
            latitude = null,
            longitude = null,
            locationLabel = null
        )
    }

    fun addPhotoUris(uris: List<android.net.Uri>) {
        val current = _uiState.value.photos
        val remaining = (MAX_PHOTOS - current.size).coerceAtLeast(0)
        if (remaining == 0 || uris.isEmpty()) return
        _uiState.value = _uiState.value.copy(
            photos = current + uris.take(remaining).map { PhotoDraft(uri = it) }
        )
    }

    fun addPhotoUri(uri: android.net.Uri) {
        addPhotoUris(listOf(uri))
    }

    fun removePhoto(id: String) {
        _uiState.value = _uiState.value.copy(
            photos = _uiState.value.photos.filter { it.id != id }
        )
    }

    fun setLocationLoading(loading: Boolean) {
        _uiState.value = _uiState.value.copy(isLocating = loading)
    }

    fun setLocationError(message: String) {
        _uiState.value = _uiState.value.copy(locationError = message, isLocating = false)
    }

    fun save(onSaved: (Long) -> Unit) {
        val state = _uiState.value
        if (state.postId != null && !state.loaded) {
            _uiState.value = state.copy(contentError = "Cargando el post, espera un momento…")
            return
        }
        if (state.content.isBlank()) {
            _uiState.value = state.copy(contentError = "Escribe algo sobre tu día")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, contentError = null)
            try {
                val id = postRepository.save(
                    content = state.content,
                    latitude = state.latitude,
                    longitude = state.longitude,
                    locationLabel = state.locationLabel,
                    newPhotoUris = state.photos.mapNotNull { it.uri },
                    keptPhotoPaths = state.photos.mapNotNull { it.path },
                    existingId = state.postId
                )
                onSaved(id)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    contentError = e.message ?: "No se pudo guardar"
                )
            }
        }
    }

    companion object {
        const val MAX_PHOTOS = 10

        fun factory(app: HealthApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                PostEditorViewModel(app.postRepository)
            }
        }
    }
}

data class PhotoDraft(
    val id: String = UUID.randomUUID().toString(),
    val uri: android.net.Uri? = null,
    val path: String? = null
) {
    val previewModel: Any
        get() = uri ?: File(requireNotNull(path) { "PhotoDraft needs a uri or path" })
}

data class PostEditorUiState(
    val postId: Long? = null,
    val content: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationLabel: String? = null,
    val photos: List<PhotoDraft> = emptyList(),
    val isLocating: Boolean = false,
    val isSaving: Boolean = false,
    val contentError: String? = null,
    val locationError: String? = null,
    val loaded: Boolean = false
)

class PostDetailViewModel(
    private val postRepository: PostRepository,
    postId: Long
) : ViewModel() {
    val post: StateFlow<DayPost?> = postRepository.observeById(postId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun delete(onDeleted: () -> Unit) {
        viewModelScope.launch {
            val current = post.value ?: return@launch
            postRepository.delete(current)
            onDeleted()
        }
    }

    companion object {
        fun factory(app: HealthApplication, postId: Long): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    PostDetailViewModel(app.postRepository, postId)
                }
            }
    }
}

class HistoryViewModel(
    postRepository: PostRepository
) : ViewModel() {
    val posts: StateFlow<List<DayPost>> = postRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    companion object {
        fun factory(app: HealthApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HistoryViewModel(app.postRepository)
            }
        }
    }
}

data class CommitmentDraftItem(
    val localId: String = UUID.randomUUID().toString(),
    val text: String = "",
    val isDone: Boolean = false
)

data class CommitmentEditorUiState(
    val commitmentId: Long? = null,
    val note: String = "",
    val items: List<CommitmentDraftItem> = listOf(CommitmentDraftItem()),
    val isSaving: Boolean = false,
    val error: String? = null,
    val loaded: Boolean = false
)

class CommitmentEditorViewModel(
    private val commitmentRepository: CommitmentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommitmentEditorUiState())
    val uiState: StateFlow<CommitmentEditorUiState> = _uiState.asStateFlow()

    fun load(commitmentId: Long?) {
        if (commitmentId == null) return
        viewModelScope.launch {
            val data = commitmentRepository.getById(commitmentId) ?: return@launch
            _uiState.value = CommitmentEditorUiState(
                commitmentId = data.commitment.id,
                note = data.commitment.note,
                items = data.todos
                    .sortedBy { it.sortOrder }
                    .map {
                        CommitmentDraftItem(
                            text = it.text,
                            isDone = it.isDone
                        )
                    }
                    .ifEmpty { listOf(CommitmentDraftItem()) },
                loaded = true
            )
        }
    }

    fun updateNote(value: String) {
        _uiState.value = _uiState.value.copy(note = value, error = null)
    }

    fun updateItemText(index: Int, value: String) {
        val items = _uiState.value.items.toMutableList()
        if (index !in items.indices) return
        items[index] = items[index].copy(text = value)
        _uiState.value = _uiState.value.copy(items = items, error = null)
    }

    fun toggleDraftDone(index: Int) {
        val items = _uiState.value.items.toMutableList()
        if (index !in items.indices) return
        items[index] = items[index].copy(isDone = !items[index].isDone)
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun addItem() {
        _uiState.value = _uiState.value.copy(
            items = _uiState.value.items + CommitmentDraftItem()
        )
    }

    fun addItemFromHandwriting(text: String): Int {
        val note = text.trim()
        if (note.isEmpty()) return _uiState.value.items.lastIndex.coerceAtLeast(0)
        val items = _uiState.value.items.toMutableList()
        val emptyIndex = items.indexOfLast { it.text.isBlank() }
        val target = if (emptyIndex >= 0) {
            items[emptyIndex] = items[emptyIndex].copy(text = note)
            emptyIndex
        } else {
            items += CommitmentDraftItem(text = note)
            items.lastIndex
        }
        _uiState.value = _uiState.value.copy(items = items, error = null)
        return target
    }

    fun insertItemEmoji(index: Int, emoji: String) {
        val items = _uiState.value.items.toMutableList()
        val target = index.takeIf { it in items.indices } ?: items.lastIndex
        if (target !in items.indices) return
        val current = items[target].text
        val insertion = if (current.isEmpty() || current.endsWith(" ")) {
            emoji
        } else {
            " $emoji"
        }
        items[target] = items[target].copy(text = current + insertion)
        _uiState.value = _uiState.value.copy(items = items, error = null)
    }

    fun removeItem(index: Int) {
        val items = _uiState.value.items.toMutableList()
        if (items.size <= 1 || index !in items.indices) {
            if (items.size == 1 && index == 0) {
                items[0] = CommitmentDraftItem()
                _uiState.value = _uiState.value.copy(items = items)
            }
            return
        }
        items.removeAt(index)
        _uiState.value = _uiState.value.copy(items = items)
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        val texts = state.items.map { it.text.trim() }.filter { it.isNotEmpty() }
        if (texts.isEmpty()) {
            _uiState.value = state.copy(error = "Agrega al menos un pendiente")
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            try {
                val doneMap = state.items
                    .filter { it.text.isNotBlank() }
                    .associate { it.text.trim() to it.isDone }
                commitmentRepository.save(
                    note = state.note,
                    todoTexts = texts,
                    existingDoneByText = doneMap,
                    existingId = state.commitmentId
                )
                onSaved()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "No se pudo guardar"
                )
            }
        }
    }

    companion object {
        fun factory(app: HealthApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                CommitmentEditorViewModel(app.commitmentRepository)
            }
        }
    }
}

data class EveningExamEditorUiState(
    val examId: Long? = null,
    val didWell: String = "",
    val didNotWell: String = "",
    val courseCorrection: String = "",
    val selectedTags: Set<String> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null,
    val loaded: Boolean = false
)

class EveningExamEditorViewModel(
    private val eveningExamRepository: EveningExamRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(EveningExamEditorUiState())
    val uiState: StateFlow<EveningExamEditorUiState> = _uiState.asStateFlow()

    fun load(examId: Long?) {
        if (examId == null) return
        viewModelScope.launch {
            val exam = eveningExamRepository.getById(examId) ?: return@launch
            _uiState.value = EveningExamEditorUiState(
                examId = exam.id,
                didWell = exam.didWell,
                didNotWell = exam.didNotWell,
                courseCorrection = exam.courseCorrection,
                selectedTags = exam.focusTags
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet(),
                loaded = true
            )
        }
    }

    fun updateDidWell(value: String) {
        _uiState.value = _uiState.value.copy(didWell = value, error = null)
    }

    fun updateDidNotWell(value: String) {
        _uiState.value = _uiState.value.copy(didNotWell = value, error = null)
    }

    fun updateCourseCorrection(value: String) {
        _uiState.value = _uiState.value.copy(courseCorrection = value, error = null)
    }

    fun toggleTag(tagId: String) {
        val current = _uiState.value.selectedTags.toMutableSet()
        if (!current.add(tagId)) current.remove(tagId)
        _uiState.value = _uiState.value.copy(selectedTags = current)
    }

    fun save(onSaved: () -> Unit) {
        val state = _uiState.value
        if (state.courseCorrection.isBlank()) {
            _uiState.value = state.copy(
                error = "Define un propósito concreto para mañana"
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, error = null)
            try {
                eveningExamRepository.save(
                    didWell = state.didWell,
                    didNotWell = state.didNotWell,
                    courseCorrection = state.courseCorrection,
                    focusTags = state.selectedTags.toList(),
                    existingId = state.examId
                )
                onSaved()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = e.message ?: "No se pudo guardar"
                )
            }
        }
    }

    companion object {
        fun factory(app: HealthApplication): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                EveningExamEditorViewModel(app.eveningExamRepository)
            }
        }
    }
}
