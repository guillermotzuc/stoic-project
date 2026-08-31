package com.proyecto360.health.ui.exam

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proyecto360.health.EveningExamEditorViewModel
import com.proyecto360.health.data.EveningExamPrompts
import com.proyecto360.health.ui.theme.EveningExamColors

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EveningExamEditorScreen(
    viewModel: EveningExamEditorViewModel,
    examId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(examId) {
        viewModel.load(examId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (examId == null) "Examen nocturno" else "Editar examen"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = EveningExamColors.container,
                    titleContentColor = EveningExamColors.onContainer,
                    navigationIconContentColor = EveningExamColors.onContainer
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text(
                text = "Evalúa con honestidad qué hiciste bien, en qué fallaste y cómo actuar con virtud mañana. Sin culpa: el error es dato para aprender.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            ExamSection(
                title = "1. Qué hice bien",
                subtitle = "El cultivo de la virtud",
                chips = EveningExamPrompts.didWell,
                selectedIds = state.selectedTags,
                onToggleChip = viewModel::toggleTag,
                value = state.didWell,
                onValueChange = viewModel::updateDidWell,
                placeholder = "Acciones justas, dominio propio, enfoque en lo que depende de ti…"
            )

            ExamSection(
                title = "2. Qué no hice bien",
                subtitle = "Los errores y pendientes",
                chips = EveningExamPrompts.didNotWell,
                selectedIds = state.selectedTags,
                onToggleChip = viewModel::toggleTag,
                value = state.didNotWell,
                onValueChange = viewModel::updateDidNotWell,
                placeholder = "Juicios erróneos, falta de acción, exceso de apego…"
            )

            ExamSection(
                title = "3. Cómo corregir el rumbo",
                subtitle = "El propósito — una sola actitud o hábito para mañana",
                chips = EveningExamPrompts.courseCorrection,
                selectedIds = state.selectedTags,
                onToggleChip = viewModel::toggleTag,
                value = state.courseCorrection,
                onValueChange = viewModel::updateCourseCorrection,
                placeholder = "Mañana por la mañana voy a…",
                minLines = 2
            )

            state.error?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Button(
                onClick = { viewModel.save(onSaved) },
                enabled = !state.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Guardar examen")
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ExamSection(
    title: String,
    subtitle: String,
    chips: List<EveningExamPrompts.PromptChip>,
    selectedIds: Set<String>,
    onToggleChip: (String) -> Unit,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 3
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = EveningExamColors.accent
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            chips.forEach { chip ->
                FilterChip(
                    selected = chip.id in selectedIds,
                    onClick = { onToggleChip(chip.id) },
                    label = { Text(chip.label) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EveningExamColors.accent,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }

        val activeHints = chips.filter { it.id in selectedIds }.map { it.hint }
        if (activeHints.isNotEmpty()) {
            activeHints.forEach { hint ->
                Text(
                    text = "• $hint",
                    style = MaterialTheme.typography.bodySmall,
                    color = EveningExamColors.onContainer.copy(alpha = 0.75f)
                )
            }
        }

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            minLines = minLines,
            shape = RoundedCornerShape(14.dp)
        )
    }
}
