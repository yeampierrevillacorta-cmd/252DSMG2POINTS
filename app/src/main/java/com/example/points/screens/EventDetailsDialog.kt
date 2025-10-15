package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.points.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventDetailsDialog(
    event: Event,
    onDismiss: () -> Unit,
    onRegisterClick: () -> Unit,
    onShareClick: () -> Unit
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
                    
                    // Estado del evento
                    Card(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when (event.estado) {
                                com.example.points.models.EstadoEvento.APROBADO -> 
                                    if (event.cancelado) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.primary
                                com.example.points.models.EstadoEvento.PENDIENTE -> 
                                    MaterialTheme.colorScheme.secondary
                                com.example.points.models.EstadoEvento.EN_REVISION -> 
                                    MaterialTheme.colorScheme.tertiary
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        Text(
                            text = when (event.estado) {
                            com.example.points.models.EstadoEvento.PENDIENTE -> "⏳ ${event.estado.displayName}"
                            com.example.points.models.EstadoEvento.EN_REVISION -> "🔍 ${event.estado.displayName}"
                            com.example.points.models.EstadoEvento.APROBADO -> if (event.cancelado) "❌ Cancelado" else "✅ ${event.estado.displayName}"
                            com.example.points.models.EstadoEvento.RECHAZADO -> "❌ ${event.estado.displayName}"
                            com.example.points.models.EstadoEvento.CANCELADO -> "❌ ${event.estado.displayName}"
                            com.example.points.models.EstadoEvento.FINALIZADO -> "🏁 ${event.estado.displayName}"
                        },
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = when (event.estado) {
                                com.example.points.models.EstadoEvento.APROBADO -> 
                                    if (event.cancelado) MaterialTheme.colorScheme.onErrorContainer
                                    else MaterialTheme.colorScheme.onPrimary
                                com.example.points.models.EstadoEvento.PENDIENTE -> 
                                    MaterialTheme.colorScheme.onSecondary
                                com.example.points.models.EstadoEvento.EN_REVISION -> 
                                    MaterialTheme.colorScheme.onTertiary
                                com.example.points.models.EstadoEvento.RECHAZADO -> 
                                    MaterialTheme.colorScheme.onError
                                com.example.points.models.EstadoEvento.CANCELADO -> 
                                    MaterialTheme.colorScheme.onError
                                com.example.points.models.EstadoEvento.FINALIZADO -> 
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                // Contenido scrolleable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
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
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.categoria.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
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
                    
                    // Fecha y hora
                    InfoRow(
                        icon = Icons.Default.Schedule,
                        title = "Fecha y Hora",
                        content = { formatEventDateTime(event) }
                    )
                    
                    // Ubicación
                    if (event.direccion.isNotEmpty()) {
                        InfoRow(
                            icon = Icons.Default.LocationOn,
                            title = "Ubicación",
                            content = { event.direccion }
                        )
                    }
                    
                    // Precio
                    InfoRow(
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
                    InfoRow(
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
                        InfoRow(
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
                        InfoRow(
                            icon = Icons.Default.People,
                            title = "Capacidad",
                            content = { "${event.inscripciones}/${event.capacidad} inscritos" }
                        )
                    }
                    
                    // Organizador
                    if (event.organizador.isNotEmpty()) {
                        InfoRow(
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
                            InfoRow(
                                icon = Icons.Default.Phone,
                                title = "Teléfono",
                                content = { telefono }
                            )
                        }
                        
                        event.contacto.email?.let { email ->
                            InfoRow(
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
                    
                    // Motivo de cancelación
                    if (event.cancelado && event.motivoCancelacion != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Evento Cancelado",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = event.motivoCancelacion!!,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }
                
                // Botones de acción
                if (event.estado == com.example.points.models.EstadoEvento.APROBADO && !event.cancelado) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (event.requiereInscripcion && (event.capacidad == null || event.inscripciones < event.capacidad)) {
                            Button(
                                onClick = onRegisterClick,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Inscribirse")
                            }
                        }
                        
                        OutlinedButton(
                            onClick = onShareClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Compartir")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
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

// Función auxiliar para formatear fecha y hora del evento
private fun formatEventDateTime(event: Event): String {
    val dateFormat = SimpleDateFormat("EEEE, dd 'de' MMMM 'de' yyyy", Locale("es", "ES"))
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    val startDate = event.fechaInicio.toDate()
    val endDate = event.fechaFin.toDate()
    
    return if (dateFormat.format(startDate) == dateFormat.format(endDate)) {
        // Mismo día
        "${dateFormat.format(startDate)}\n${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
    } else {
        // Diferentes días
        "${dateFormat.format(startDate)} ${timeFormat.format(startDate)}\n${dateFormat.format(endDate)} ${timeFormat.format(endDate)}"
    }
}
