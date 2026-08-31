package com.proyecto360.health.ui.common

import android.os.Build
import android.text.InputType
import android.view.Gravity
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.widget.doAfterTextChanged

@Composable
fun HandwritingDialog(
    title: String = "Escribir con lápiz",
    description: String,
    hint: String = "Escribe aquí con el lápiz…",
    confirmLabel: String = "Agregar",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var noteText by remember { mutableStateOf("") }
    val textColor = MaterialTheme.colorScheme.onSurface
    val hintColor = MaterialTheme.colorScheme.onSurfaceVariant
    val fieldBackground = MaterialTheme.colorScheme.surfaceVariant

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AndroidView(
                    factory = { context ->
                        EditText(context).apply {
                            this.hint = hint
                            setHintTextColor(hintColor.toArgb())
                            setTextColor(textColor.toArgb())
                            setBackgroundColor(fieldBackground.toArgb())
                            setPadding(28, 28, 28, 28)
                            minLines = 6
                            gravity = Gravity.TOP or Gravity.START
                            inputType = InputType.TYPE_CLASS_TEXT or
                                InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                                InputType.TYPE_TEXT_FLAG_CAP_SENTENCES
                            isFocusable = true
                            isFocusableInTouchMode = true
                            doAfterTextChanged { editable ->
                                noteText = editable?.toString().orEmpty()
                            }
                            post { startTabletHandwriting() }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(noteText) },
                enabled = noteText.isNotBlank()
            ) {
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

private fun EditText.startTabletHandwriting() {
    requestFocus()
    runCatching {
        javaClass.getMethod("semSetDirectWritingEnabled", Boolean::class.javaPrimitiveType)
            .invoke(this, true)
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
    isAutoHandwritingEnabled = true
    val imm = context.getSystemService(InputMethodManager::class.java) ?: return
    val start = {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE ||
            imm.isStylusHandwritingAvailable
        ) {
            imm.startStylusHandwriting(this)
        }
    }
    post(start)
    postDelayed(start, 280)
}
