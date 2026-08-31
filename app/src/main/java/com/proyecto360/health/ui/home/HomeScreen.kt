package com.proyecto360.health.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proyecto360.health.HomeViewModel
import com.proyecto360.health.data.CommitmentTodo
import com.proyecto360.health.data.DayCommitmentWithTodos
import com.proyecto360.health.data.DayPost
import com.proyecto360.health.data.EveningExam
import com.proyecto360.health.data.MoodChartSummary
import com.proyecto360.health.data.MoodLevel
import com.proyecto360.health.ui.post.PostPhotoPreview
import com.proyecto360.health.ui.post.PostPhotoStrip
import com.proyecto360.health.ui.theme.CommitmentColors
import com.proyecto360.health.ui.theme.EveningExamColors
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onAddPost: () -> Unit,
    onEditToday: (Long) -> Unit,
    onOpenPost: (Long) -> Unit,
    onOpenHistory: () -> Unit,
    onAddCommitment: () -> Unit,
    onEditCommitment: (Long) -> Unit,
    onAddExam: () -> Unit,
    onEditExam: (Long) -> Unit
) {
    val phrase by viewModel.phrase.collectAsStateWithLifecycle()
    val todayPost by viewModel.todayPost.collectAsStateWithLifecycle()
    val todayCommitment by viewModel.todayCommitment.collectAsStateWithLifecycle()
    val todayExam by viewModel.todayExam.collectAsStateWithLifecycle()
    val yesterdayExam by viewModel.yesterdayExam.collectAsStateWithLifecycle()
    val todayMood by viewModel.todayMood.collectAsStateWithLifecycle()
    val moodChart by viewModel.moodChart.collectAsStateWithLifecycle()
    val recentPosts by viewModel.recentPosts.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "360 Me",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tu espacio personal",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onOpenHistory) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Historial")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                todayPost?.let { onEditToday(it.id) } ?: onAddPost()
            }) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo post del día")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PhraseCard(
                    phrase = phrase.phrase,
                    onShuffle = viewModel::shufflePhrase
                )
            }

            item {
                TodayCommitmentCard(
                    commitment = todayCommitment,
                    onOpen = {
                        todayCommitment?.let { onEditCommitment(it.commitment.id) }
                            ?: onAddCommitment()
                    },
                    onToggleTodo = viewModel::toggleCommitmentTodo
                )
            }

            item {
                TodayPostCard(
                    post = todayPost,
                    onClick = {
                        todayPost?.let { onEditToday(it.id) } ?: onAddPost()
                    }
                )
            }

            item {
                MoodScaleCard(
                    selected = todayMood,
                    onSelect = viewModel::setMood
                )
            }

            item {
                MoodAverageChartCard(summary = moodChart)
            }

            item {
                EveningExamCard(
                    exam = todayExam,
                    onOpen = {
                        todayExam?.let { onEditExam(it.id) } ?: onAddExam()
                    }
                )
            }

            if (recentPosts.isNotEmpty()) {
                item {
                    Text(
                        text = "Recientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                items(recentPosts.take(8), key = { it.id }) { post ->
                    PostListItem(
                        post = post,
                        onClick = { onOpenPost(post.id) }
                    )
                }
            }

            if (yesterdayExam != null && todayExam == null) {
                item {
                    YesterdayPurposeCard(purpose = yesterdayExam!!.courseCorrection)
                }
            }
        }
    }
}

@Composable
private fun MoodAverageChartCard(summary: MoodChartSummary) {
    val dayFormat = SimpleDateFormat("EEE", Locale("es", "ES"))
    val maxBarHeight = 120.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Promedio de ánimo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Últimos 7 días",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            if (summary.loggedCount == 0) {
                Text(
                    text = "Registra tu ánimo para ver el promedio aquí.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                val average = summary.averageScore ?: 0.0
                val averageLevel = summary.averageLevel

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "${averageLevel?.emoji ?: ""} ${averageLevel?.weather ?: ""}",
                        fontSize = 28.sp
                    )
                    Spacer(Modifier.size(12.dp))
                    Column {
                        Text(
                            text = "%.1f / 5".format(Locale.US, average),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = averageLevel?.label ?: "",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxBarHeight + 36.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    summary.points.forEach { point ->
                        val score = point.level?.score ?: 0
                        val fraction = if (score == 0) 0.08f else score / 5f
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Bottom,
                            modifier = Modifier.weight(1f)
                        ) {
                            if (point.level != null) {
                                Text(text = point.level.emoji, fontSize = 12.sp)
                                Spacer(Modifier.height(4.dp))
                            } else {
                                Spacer(Modifier.height(20.dp))
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.55f)
                                    .height(maxBarHeight * fraction)
                                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                    .background(
                                        if (point.level != null) {
                                            MaterialTheme.colorScheme.primary.copy(
                                                alpha = 0.35f + (score * 0.12f)
                                            )
                                        } else {
                                            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                                        }
                                    )
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = dayFormat.format(Date(point.dayStart))
                                    .replaceFirstChar { it.uppercase() }
                                    .take(3),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${summary.loggedCount} de 7 días · ${summary.totalEntries} registros",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MoodScaleCard(
    selected: MoodLevel?,
    onSelect: (MoodLevel) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "¿Cómo te sientes hoy?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
Text(
                    text = "Elige tu estado de ánimo. Si cambias en menos de 1 hora, se actualiza; después se guarda uno nuevo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MoodLevel.entries.forEach { level ->
                    val isSelected = selected == level
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { onSelect(level) }
                            .padding(horizontal = 2.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                                    }
                                )
                                .then(
                                    if (isSelected) {
                                        Modifier.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = CircleShape
                                        )
                                    } else {
                                        Modifier
                                    }
                                )
                        ) {
                            Text(
                                text = "${level.emoji}\n${level.weather}",
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            if (selected != null) {
                Text(
                    text = selected.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = selected.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Toca un estado para registrarlo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun YesterdayPurposeCard(purpose: String) {
    val dark = isSystemInDarkTheme()
    val container = if (dark) EveningExamColors.darkContainer else EveningExamColors.container
    val onContainer = if (dark) EveningExamColors.darkOnContainer else EveningExamColors.onContainer
    val accent = if (dark) EveningExamColors.darkAccent else EveningExamColors.accent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Propósito de ayer",
                style = MaterialTheme.typography.labelLarge,
                color = accent,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = purpose,
                style = MaterialTheme.typography.bodyLarge,
                color = onContainer
            )
        }
    }
}

@Composable
private fun EveningExamCard(exam: EveningExam?, onOpen: () -> Unit) {
    val dark = isSystemInDarkTheme()
    val container = if (dark) EveningExamColors.darkContainer else EveningExamColors.container
    val onContainer = if (dark) EveningExamColors.darkOnContainer else EveningExamColors.onContainer
    val accent = if (dark) EveningExamColors.darkAccent else EveningExamColors.accent

    Card(
        onClick = onOpen,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "Examen de conciencia nocturno",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Spacer(Modifier.height(8.dp))
            if (exam == null) {
                Text(
                    text = "Antes de dormir, revisa con honestidad qué hiciste bien, en qué fallaste y cómo actuar con virtud mañana.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer.copy(alpha = 0.85f)
                )
            } else {
                Text(
                    text = "Propósito para mañana",
                    style = MaterialTheme.typography.labelMedium,
                    color = accent,
                    fontWeight = FontWeight.Medium
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = exam.courseCorrection,
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Toca para revisar o editar tu examen",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun PhraseCard(phrase: String, onShuffle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Frase estoica del día",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onShuffle, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = "Otra frase",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = "“$phrase”",
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TodayCommitmentCard(
    commitment: DayCommitmentWithTodos?,
    onOpen: () -> Unit,
    onToggleTodo: (Long) -> Unit
) {
    val dark = isSystemInDarkTheme()
    val container = if (dark) CommitmentColors.darkContainer else CommitmentColors.container
    val onContainer = if (dark) CommitmentColors.darkOnContainer else CommitmentColors.onContainer
    val accent = if (dark) CommitmentColors.darkAccent else CommitmentColors.accent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (commitment == null) Modifier.clickable(onClick = onOpen)
                    else Modifier
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpen),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Compromiso del día",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
                if (commitment != null) {
                    val done = commitment.todos.count { it.isDone }
                    val total = commitment.todos.size
                    Text(
                        text = "$done/$total",
                        style = MaterialTheme.typography.labelLarge,
                        color = onContainer,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            if (commitment == null) {
                Text(
                    text = "Define tu lista de pendientes para hoy. Toca para agregar compromisos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onContainer.copy(alpha = 0.8f)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    commitment.todos
                        .sortedBy { it.sortOrder }
                        .forEach { todo ->
                            CommitmentTodoRow(
                                todo = todo,
                                accent = accent,
                                onContainer = onContainer,
                                onToggle = { onToggleTodo(todo.id) }
                            )
                        }
                }
                if (commitment.commitment.note.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = commitment.commitment.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = onContainer.copy(alpha = 0.75f),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.clickable(onClick = onOpen)
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Toca el título para editar",
                    style = MaterialTheme.typography.labelSmall,
                    color = accent.copy(alpha = 0.8f),
                    modifier = Modifier.clickable(onClick = onOpen)
                )
            }
        }
    }
}

@Composable
private fun CommitmentTodoRow(
    todo: CommitmentTodo,
    accent: androidx.compose.ui.graphics.Color,
    onContainer: androidx.compose.ui.graphics.Color,
    onToggle: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Checkbox(
            checked = todo.isDone,
            onCheckedChange = { onToggle() },
            colors = CheckboxDefaults.colors(
                checkedColor = accent,
                uncheckedColor = accent.copy(alpha = 0.6f),
                checkmarkColor = androidx.compose.ui.graphics.Color.White
            )
        )
        Text(
            text = todo.text,
            style = MaterialTheme.typography.bodyMedium,
            color = onContainer,
            textDecoration = if (todo.isDone) TextDecoration.LineThrough else null,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TodayPostCard(post: DayPost?, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = "Post de hoy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(8.dp))
                if (post == null) {
                    Text(
                        text = "Aún no has registrado tu día. Toca para escribir cómo te sientes, dónde estás y opcionalmente fotos.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!post.locationLabel.isNullOrBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = post.locationLabel,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            val photoPaths = post?.resolvedPhotoPaths().orEmpty()
            if (photoPaths.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                PostPhotoStrip(
                    photos = photoPaths.map { path ->
                        PostPhotoPreview(id = path, model = File(path))
                    }
                )
            }
        }
    }
}

@Composable
fun PostListItem(post: DayPost, onClick: () -> Unit) {
    val dateLabel = SimpleDateFormat("EEE d MMM · HH:mm", Locale("es", "ES"))
        .format(Date(post.createdAt))
    val photoPaths = post.resolvedPhotoPaths()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            ) {
                Text(
                    text = dateLabel.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!post.locationLabel.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = post.locationLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (photoPaths.isNotEmpty()) {
                Spacer(Modifier.height(10.dp))
                PostPhotoStrip(
                    photos = photoPaths.map { path ->
                        PostPhotoPreview(id = path, model = File(path))
                    }
                )
            }
        }
    }
}
