package com.proyecto360.health.ui.post

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.proyecto360.health.PostEditorViewModel
import com.proyecto360.health.ui.common.EmojiBar
import com.proyecto360.health.ui.common.HandwritingDialog
import com.proyecto360.health.util.LocationHelper
import com.proyecto360.health.util.PhotoHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostEditorScreen(
    viewModel: PostEditorViewModel,
    postId: Long?,
    onBack: () -> Unit,
    onSaved: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
    var showTimedNoteDialog by remember { mutableStateOf(false) }
    var showHandwritingNoteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(postId) {
        viewModel.load(postId)
    }

    val pickImages = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(PostEditorViewModel.MAX_PHOTOS)
    ) { uris ->
        if (uris.isNotEmpty()) viewModel.addPhotoUris(uris)
    }

    val takePicture = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            pendingCameraUri?.let { viewModel.addPhotoUri(it) }
        }
    }

    val locationPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.any { it }) {
            scope.launch {
                viewModel.setLocationLoading(true)
                val captured = LocationHelper.capture(context)
                if (captured != null) {
                    viewModel.setLocation(captured.latitude, captured.longitude, captured.label)
                } else {
                    viewModel.setLocationError("No se pudo obtener la ubicación")
                }
                viewModel.setLocationLoading(false)
            }
        } else {
            viewModel.setLocationError("Permiso de ubicación denegado")
        }
    }

    val cameraPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val (uri, _) = PhotoHelper.createCameraUri(context)
            pendingCameraUri = uri
            takePicture.launch(uri)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (postId == null) "Post del día" else "Editar post")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
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
                        text = "¿Cómo va tu día?",
                        style = MaterialTheme.typography.titleSmall
                    )
                    Row {
                        IconButton(onClick = viewModel::insertHourAndLineBreak) {
                            Icon(
                                Icons.Default.AccessTime,
                                contentDescription = "Insertar hora y salto de línea",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showTimedNoteDialog = true }) {
                            Icon(
                                Icons.Default.AddComment,
                                contentDescription = "Agregar nota con hora",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { showHandwritingNoteDialog = true }) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Escribir con lápiz",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
                EmojiBar(
                    emojis = postQuickEmojis,
                    onSelect = viewModel::insertEmoji
                )
            }

            OutlinedTextField(
                value = state.content,
                onValueChange = viewModel::updateContent,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                placeholder = { Text("Escribe lo que quieras recordar…") },
                isError = state.contentError != null,
                supportingText = {
                    Text(
                        state.contentError
                            ?: "El reloj inserta la hora. El comentario y el lápiz agregan una línea con hora y texto."
                    )
                },
                shape = RoundedCornerShape(16.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Ubicación",
                    style = MaterialTheme.typography.titleSmall
                )
                if (state.locationLabel != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Text(
                            text = state.locationLabel.orEmpty(),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        IconButton(onClick = viewModel::clearLocation) {
                            Icon(Icons.Default.Close, contentDescription = "Quitar ubicación")
                        }
                    }
                }
                OutlinedButton(
                    onClick = {
                        if (LocationHelper.hasPermission(context)) {
                            scope.launch {
                                viewModel.setLocationLoading(true)
                                val captured = LocationHelper.capture(context)
                                if (captured != null) {
                                    viewModel.setLocation(
                                        captured.latitude,
                                        captured.longitude,
                                        captured.label
                                    )
                                } else {
                                    viewModel.setLocationError("No se pudo obtener la ubicación")
                                }
                                viewModel.setLocationLoading(false)
                            }
                        } else {
                            locationPermission.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    },
                    enabled = !state.isLocating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (state.isLocating) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.size(8.dp))
                    } else {
                        Icon(Icons.Default.MyLocation, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                    }
                    Text(if (state.locationLabel == null) "Usar ubicación actual" else "Actualizar ubicación")
                }
                state.locationError?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Fotos (opcional)",
                    style = MaterialTheme.typography.titleSmall
                )

                PostPhotoStrip(
                    photos = state.photos.map { PostPhotoPreview(it.id, it.previewModel) },
                    onRemove = viewModel::removePhoto
                )

                val canAddMore = state.photos.size < PostEditorViewModel.MAX_PHOTOS
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = {
                            pickImages.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        enabled = canAddMore,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Galería")
                    }
                    OutlinedButton(
                        onClick = {
                            cameraPermission.launch(Manifest.permission.CAMERA)
                        },
                        enabled = canAddMore,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(Modifier.size(8.dp))
                        Text("Cámara")
                    }
                }
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
                    Text("Guardar")
                }
            }
        }
    }

    if (showTimedNoteDialog) {
        TimedNoteDialog(
            onDismiss = { showTimedNoteDialog = false },
            onConfirm = { note ->
                viewModel.insertTimedNote(note)
                showTimedNoteDialog = false
            }
        )
    }

    if (showHandwritingNoteDialog) {
        HandwritingDialog(
            description = "Usa el lápiz de la tablet para escribir. El texto reconocido se agregará al post.",
            onDismiss = { showHandwritingNoteDialog = false },
            onConfirm = { note ->
                viewModel.insertTimedNote(note)
                showHandwritingNoteDialog = false
            }
        )
    }
}

private val postQuickEmojis = listOf(
    "⭐" to "Estrella",
    "❤️" to "Corazón",
    "😊" to "Feliz",
    "😢" to "Triste",
    "🔥" to "Fuego",
    "👍" to "Me gusta",
    "✨" to "Brillo"
)

@Composable
private fun TimedNoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nota con hora") },
        text = {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("¿Qué está pasando?") },
                minLines = 2
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(noteText) },
                enabled = noteText.isNotBlank()
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
