package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.points.models.Event
import com.example.points.models.FrecuenciaRecurrencia
import com.example.points.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScheduledEventDetailsDialog(
    event: Event,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.9f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header con imagen y título
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    // Imagen del evento (placeholder por ahora)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(event.categoria.icon),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    
                    // Botón de cerrar
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    
                    // Indicador de evento recurrente
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = when (event.frecuenciaRecurrencia) {
                                    FrecuenciaRecurrencia.DIARIO -> Icons.Default.Today
                                    FrecuenciaRecurrencia.SEMANAL -> Icons.Default.DateRange
                                    FrecuenciaRecurrencia.MENSUAL -> Icons.Default.CalendarMonth
                                    FrecuenciaRecurrencia.ANUAL -> Icons.Default.Event
                                    null -> Icons.Default.Schedule
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = event.frecuenciaRecurrencia?.displayName ?: "Recurrente",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSecondary
                            )
                        }
                    }
                }
                
                // Contenido scrolleable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Título y categoría
                    Text(
                        text = event.nombre,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(event.categoria.icon),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = event.categoria.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Información de programación
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Información de Programación",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Frecuencia
                            ScheduledInfoRow(
                                icon = when (event.frecuenciaRecurrencia) {
                                    FrecuenciaRecurrencia.DIARIO -> Icons.Default.Today
                                    FrecuenciaRecurrencia.SEMANAL -> Icons.Default.DateRange
                                    FrecuenciaRecurrencia.MENSUAL -> Icons.Default.CalendarMonth
                                    FrecuenciaRecurrencia.ANUAL -> Icons.Default.Event
                                    null -> Icons.Default.Schedule
                                },
                                title = "Frecuencia",
                                content = { event.frecuenciaRecurrencia?.displayName ?: "No especificada" }
                            )
                            
                            // Fecha de inicio
                            ScheduledInfoRow(
                                icon = Icons.Default.PlayArrow,
                                title = "Fecha de inicio",
                                content = { formatDate(event.fechaInicio) }
                            )
                            
                            // Fecha de fin de recurrencia
                            if (event.fechaFinRecurrencia != null) {
                                ScheduledInfoRow(
                                    icon = Icons.Default.Stop,
                                    title = "Fecha de fin",
                                    content = { formatDate(event.fechaFinRecurrencia!!) }
                                )
                            } else {
                                ScheduledInfoRow(
                                    icon = Icons.Default.AllInclusive,
                                    title = "Duración",
                                    content = { "Sin fecha de fin (recurrencia infinita)" }
                                )
                            }
                            
                            // Horario
                            ScheduledInfoRow(
                                icon = Icons.Default.AccessTime,
                                title = "Horario",
                                content = { "${event.horaInicio} - ${event.horaFin}" }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Descripción
                    if (event.descripcion.isNotEmpty()) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = event.descripcion,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Información del evento
                    Text(
                        text = "Información del Evento",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Ubicación
                    if (event.direccion.isNotEmpty()) {
                        ScheduledInfoRow(
                            icon = Icons.Default.LocationOn,
                            title = "Ubicación",
                            content = { event.direccion }
                        )
                    }
                    
                    // Precio
                    ScheduledInfoRow(
                        icon = Icons.Default.AttachMoney,
                        title = "Precio",
                        content = {
                            when {
                                event.esGratuito -> "Gratuito"
                                event.precio.precioGeneral != null -> "$${String.format("%.0f", event.precio.precioGeneral)}"
                                else -> "Precio no especificado"
                            }
                        }
                    )
                    
                    // Duración
                    ScheduledInfoRow(
                        icon = Icons.Default.Timer,
                        title = "Duración",
                        content = {
                            val inicio = event.fechaInicio.toDate()
                            val fin = event.fechaFin.toDate()
                            val duracionMs = fin.time - inicio.time
                            val duracionHoras = duracionMs / (1000 * 60 * 60)
                            val duracionMinutos = (duracionMs % (1000 * 60 * 60)) / (1000 * 60)
                            
                            when {
                                duracionHoras > 0 -> "${duracionHoras}h ${duracionMinutos}min"
                                duracionMinutos > 0 -> "${duracionMinutos}min"
                                else -> "Duración no especificada"
                            }
                        }
                    )
                    
                    // Edad recomendada
                    if (event.edadMinima != null || event.edadMaxima != null) {
                        ScheduledInfoRow(
                            icon = Icons.Default.Person,
                            title = "Edad Recomendada",
                            content = {
                                when {
                                    event.edadMinima != null && event.edadMaxima != null -> "${event.edadMinima}-${event.edadMaxima} años"
                                    event.edadMinima != null -> "Desde ${event.edadMinima} años"
                                    event.edadMaxima != null -> "Hasta ${event.edadMaxima} años"
                                    else -> "Todas las edades"
                                }
                            }
                        )
                    }
                    
                    // Capacidad
                    if (event.capacidad != null) {
                        ScheduledInfoRow(
                            icon = Icons.Default.People,
                            title = "Capacidad",
                            content = { "${event.inscripciones}/${event.capacidad} inscritos" }
                        )
                    }
                    
                    // Organizador
                    if (event.organizador.isNotEmpty()) {
                        ScheduledInfoRow(
                            icon = Icons.Default.Business,
                            title = "Organizador",
                            content = { event.organizador }
                        )
                    }
                    
                    // Contacto
                    if (event.contacto.telefono != null || event.contacto.email != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Contacto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        event.contacto.telefono?.let { telefono ->
                            ScheduledInfoRow(
                                icon = Icons.Default.Phone,
                                title = "Teléfono",
                                content = { telefono }
                            )
                        }
                        
                        event.contacto.email?.let { email ->
                            ScheduledInfoRow(
                                icon = Icons.Default.Email,
                                title = "Email",
                                content = { email }
                            )
                        }
                    }
                    
                    // Características
                    if (event.caracteristicas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Características",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(event.caracteristicas) { caracteristica ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Text(
                                        text = caracteristica.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    // Etiquetas
                    if (event.etiquetas.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Etiquetas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(event.etiquetas) { etiqueta ->
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                                    )
                                ) {
                                    Text(
                                        text = "#$etiqueta",
                                        style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        color = MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onEditClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Editar Programación")
                    }
                    
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduledInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: @Composable () -> String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
            Text(
                text = content(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Función auxiliar para formatear fecha
private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    return dateFormat.format(timestamp.toDate())
}
