package com.example.points.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.points.models.Event
import com.example.points.models.EstadoEvento
import com.example.points.utils.getCategoryIcon

@Composable
fun ModerationDialog(
    event: Event,
    action: ModerationAction,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var comments by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (action) {
                                ModerationAction.APPROVE -> Icons.Default.CheckCircle
                                ModerationAction.REJECT -> Icons.Default.Cancel
                                ModerationAction.CANCEL -> Icons.Default.EventBusy
                            },
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = when (action) {
                                ModerationAction.APPROVE -> MaterialTheme.colorScheme.primary
                                ModerationAction.REJECT -> MaterialTheme.colorScheme.error
                                ModerationAction.CANCEL -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = when (action) {
                                ModerationAction.APPROVE -> "Aprobar Evento"
                                ModerationAction.REJECT -> "Rechazar Evento"
                                ModerationAction.CANCEL -> "Cancelar Evento"
                            },
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Información del evento
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(event.categoria.icon),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = event.nombre,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = event.categoria.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Mensaje de confirmación
                Text(
                    text = when (action) {
                        ModerationAction.APPROVE -> "¿Estás seguro de que quieres aprobar este evento? Una vez aprobado, será visible para todos los usuarios."
                        ModerationAction.REJECT -> "¿Estás seguro de que quieres rechazar este evento? El organizador será notificado del rechazo."
                        ModerationAction.CANCEL -> "¿Estás seguro de que quieres cancelar este evento? Los usuarios inscritos serán notificados."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Campo de comentarios
                OutlinedTextField(
                    value = comments,
                    onValueChange = { 
                        comments = it
                        showError = false
                    },
                    label = { 
                        Text(
                            when (action) {
                                ModerationAction.APPROVE -> "Comentarios (opcional)"
                                ModerationAction.REJECT -> "Motivo del rechazo *"
                                ModerationAction.CANCEL -> "Motivo de la cancelación *"
                            }
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences
                    ),
                    isError = showError && comments.isBlank() && action != ModerationAction.APPROVE,
                    supportingText = if (showError && comments.isBlank() && action != ModerationAction.APPROVE) {
                        { Text("Este campo es obligatorio") }
                    } else null
                )
                
                if (action != ModerationAction.APPROVE) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Los comentarios serán visibles para el organizador del evento.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            if (action == ModerationAction.APPROVE || comments.isNotBlank()) {
                                onConfirm(comments)
                            } else {
                                showError = true
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (action) {
                                ModerationAction.APPROVE -> MaterialTheme.colorScheme.primary
                                ModerationAction.REJECT -> MaterialTheme.colorScheme.error
                                ModerationAction.CANCEL -> MaterialTheme.colorScheme.tertiary
                            }
                        )
                    ) {
                        Text(
                            when (action) {
                                ModerationAction.APPROVE -> "Aprobar"
                                ModerationAction.REJECT -> "Rechazar"
                                ModerationAction.CANCEL -> "Cancelar"
                            }
                        )
                    }
                }
            }
        }
    }
}
