package com.proyecto360.health.widget

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.proyecto360.health.HealthApplication
import com.proyecto360.health.ui.theme.HealthTheme
import kotlinx.coroutines.launch

/**
 * Lightweight dialog activity launched from the widget to add a compromiso.
 */
class QuickAddTodoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFinishOnTouchOutside(true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        val app = application as HealthApplication

        setContent {
            HealthTheme {
                var text by remember { mutableStateOf("") }
                var error by remember { mutableStateOf<String?>(null) }
                var saving by remember { mutableStateOf(false) }
                val scope = rememberCoroutineScope()

                Dialog(onDismissRequest = { finish() }) {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        ) {
                            Text(
                                text = "Nuevo compromiso",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = text,
                                onValueChange = {
                                    text = it
                                    error = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Ej. Meditar 10 minutos") },
                                singleLine = true,
                                isError = error != null,
                                supportingText = error?.let { { Text(it) } },
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                TextButton(
                                    onClick = { finish() },
                                    enabled = !saving
                                ) {
                                    Text("Cancelar")
                                }
                                Spacer(Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        scope.launch {
                                            saving = true
                                            try {
                                                app.commitmentRepository.addTodoToToday(text)
                                                WidgetUpdater.refresh(app)
                                                finish()
                                            } catch (e: Exception) {
                                                error = e.message ?: "No se pudo guardar"
                                                saving = false
                                            }
                                        }
                                    },
                                    enabled = !saving && text.isNotBlank()
                                ) {
                                    Text("Agregar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
