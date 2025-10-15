package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.models.Event
import com.example.points.models.FrecuenciaRecurrencia
import com.example.points.viewmodel.EventViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventScheduleScreen(
    navController: NavController,
    viewModel: EventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estados locales
    var showCreateScheduleDialog by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showEventDetails by remember { mutableStateOf(false) }
    var selectedFrequency by remember { mutableStateOf<FrecuenciaRecurrencia?>(null) }
    
    // Cargar eventos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadAllEvents()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "📅 Programación de Eventos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gestiona eventos recurrentes y programados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de filtros por frecuencia
                IconButton(
                    onClick = { /* TODO: Mostrar filtros */ },
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = "Filtros"
                    )
                }
                
                // Botón de crear programación
                Button(
                    onClick = { showCreateScheduleDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Programar")
                }
            }
        }
        
        // Filtros de frecuencia
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            LazyRow(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        onClick = { selectedFrequency = null },
                        label = { Text("Todos") },
                        selected = selectedFrequency == null,
                        leadingIcon = {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
                
                items(FrecuenciaRecurrencia.values().toList()) { frequency ->
                    FilterChip(
                        onClick = { 
                            selectedFrequency = if (selectedFrequency == frequency) null else frequency
                        },
                        label = { Text(frequency.displayName) },
                        selected = selectedFrequency == frequency,
                        leadingIcon = {
                            Icon(
                                when (frequency) {
                                    FrecuenciaRecurrencia.DIARIO -> Icons.Default.Today
                                    FrecuenciaRecurrencia.SEMANAL -> Icons.Default.DateRange
                                    FrecuenciaRecurrencia.MENSUAL -> Icons.Default.CalendarMonth
                                    FrecuenciaRecurrencia.ANUAL -> Icons.Default.Event
                                },
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Contenido principal
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = "Cargando eventos programados...")
                }
            }
            
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsFeedback(
                        message = uiState.errorMessage ?: "Error desconocido",
                        type = "error",
                        onRetry = { viewModel.loadAllEvents() }
                    )
                }
            }
            
            else -> {
                val filteredEvents = if (selectedFrequency != null) {
                    uiState.events.filter { it.esRecurrente && it.frecuenciaRecurrencia == selectedFrequency }
                } else {
                    uiState.events.filter { it.esRecurrente }
                }
                
                if (filteredEvents.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay eventos programados",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "Crea un nuevo evento recurrente para comenzar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(filteredEvents) { event ->
                            ScheduledEventCard(
                                event = event,
                                onClick = {
                                    selectedEvent = event
                                    showEventDetails = true
                                },
                                onEditClick = {
                                    // TODO: Implementar edición de programación
                                },
                                onDeleteClick = {
                                    // TODO: Implementar eliminación de programación
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de detalles del evento programado
    if (showEventDetails && selectedEvent != null) {
        ScheduledEventDetailsDialog(
            event = selectedEvent!!,
            onDismiss = {
                showEventDetails = false
                selectedEvent = null
            },
            onEditClick = {
                // TODO: Implementar edición
            }
        )
    }
    
    // Diálogo de crear programación
    if (showCreateScheduleDialog) {
        CreateScheduleDialog(
            onDismiss = { showCreateScheduleDialog = false },
            onCreateSchedule = { event ->
                viewModel.createEvent(event)
                showCreateScheduleDialog = false
            }
        )
    }
}

@Composable
fun ScheduledEventCard(
    event: Event,
    onClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del evento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(event.categoria.icon),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.categoria.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Frecuencia de recurrencia
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
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
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.frecuenciaRecurrencia?.displayName ?: "Recurrente",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información del evento
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Próxima fecha
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Próxima: ${formatEventDateTime(event)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Ubicación
                if (event.direccion.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Fin de recurrencia
                if (event.fechaFinRecurrencia != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.EventBusy,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Hasta: ${formatDate(event.fechaFinRecurrencia!!)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Editar")
                }
                
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
            }
        }
    }
}

// Función auxiliar para formatear fecha y hora del evento
private fun formatEventDateTime(event: Event): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    val startDate = event.fechaInicio.toDate()
    val endDate = event.fechaFin.toDate()
    
    return if (dateFormat.format(startDate) == dateFormat.format(endDate)) {
        // Mismo día
        "${dateFormat.format(startDate)} ${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
    } else {
        // Diferentes días
        "${dateFormat.format(startDate)} ${timeFormat.format(startDate)} - ${dateFormat.format(endDate)} ${timeFormat.format(endDate)}"
    }
}

// Función auxiliar para formatear fecha
private fun formatDate(timestamp: com.google.firebase.Timestamp): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return dateFormat.format(timestamp.toDate())
}
