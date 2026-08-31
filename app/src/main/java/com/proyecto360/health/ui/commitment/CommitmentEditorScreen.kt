package com.proyecto360.health.ui.commitment

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proyecto360.health.CommitmentEditorViewModel
import com.proyecto360.health.ui.common.EmojiBar
import com.proyecto360.health.ui.common.HandwritingDialog
import com.proyecto360.health.ui.theme.CommitmentColors

private val taskQuickEmojis = listOf(
    "✅" to "Hecho",
    "📌" to "Importante",
    "⏰" to "Hora",
    "💪" to "Ejercicio",
    "🛒" to "Compras",
    "📞" to "Llamada",
    "🏠" to "Casa"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommitmentEditorScreen(
    viewModel: CommitmentEditorViewModel,
    commitmentId: Long?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var focusedItemIndex by remember { mutableIntStateOf(0) }
    var showHandwritingDialog by remember { mutableStateOf(false) }

    LaunchedEffect(commitmentId) {
        viewModel.load(commitmentId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (commitmentId == null) "Compromiso del día" else "Editar compromiso"
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CommitmentColors.container,
                    titleContentColor = CommitmentColors.onContainer,
                    navigationIconContentColor = CommitmentColors.onContainer
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lista de pendientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = CommitmentColors.accent
                    )
                    IconButton(onClick = { showHandwritingDialog = true }) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Agregar pendiente con lápiz",
                            tint = CommitmentColors.accent
                        )
                    }
                }
                Text(
                    text = "Agrega lo que te comprometes a hacer hoy. Usa el lápiz o un emoji para el pendiente activo.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                EmojiBar(
                    emojis = taskQuickEmojis,
                    onSelect = { emoji ->
                        viewModel.insertItemEmoji(focusedItemIndex, emoji)
                    }
                )
            }

            state.items.forEachIndexed { index, item ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = item.isDone,
                        onCheckedChange = { viewModel.toggleDraftDone(index) }
                    )
                    OutlinedTextField(
                        value = item.text,
                        onValueChange = { viewModel.updateItemText(index, it) },
                        modifier = Modifier
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                if (focusState.isFocused) focusedItemIndex = index
                            },
                        placeholder = { Text("Ej. Caminar 30 minutos") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    IconButton(onClick = { viewModel.removeItem(index) }) {
                        Icon(Icons.Default.Close, contentDescription = "Quitar")
                    }
                }
            }

            OutlinedButton(
                onClick = viewModel::addItem,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.size(8.dp))
                Text("Agregar pendiente")
            }

            OutlinedTextField(
                value = state.note,
                onValueChange = viewModel::updateNote,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Nota (opcional)") },
                placeholder = { Text("¿Por qué estos compromisos importan hoy?") },
                minLines = 2,
                shape = RoundedCornerShape(12.dp)
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
                    Text("Guardar compromiso")
                }
            }
        }
    }

    if (showHandwritingDialog) {
        HandwritingDialog(
            description = "Usa el lápiz de la tablet para escribir el pendiente. El texto se agregará como una nueva tarea.",
            hint = "Escribe el pendiente con el lápiz…",
            confirmLabel = "Agregar pendiente",
            onDismiss = { showHandwritingDialog = false },
            onConfirm = { note ->
                focusedItemIndex = viewModel.addItemFromHandwriting(note)
                showHandwritingDialog = false
            }
        )
    }
}
