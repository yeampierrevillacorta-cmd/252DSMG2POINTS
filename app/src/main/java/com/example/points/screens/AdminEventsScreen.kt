package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.example.points.models.EstadoEvento
import com.example.points.viewmodel.EventViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.constants.LoadingMessage
import com.example.points.constants.ErrorMessage
import com.example.points.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminEventsScreen(
    navController: NavController,
    viewModel: EventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estados locales
    var selectedTab by remember { mutableStateOf(0) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showEventDetails by remember { mutableStateOf(false) }
    var showModerationDialog by remember { mutableStateOf(false) }
    var moderationAction by remember { mutableStateOf<ModerationAction?>(null) }
    var moderationComments by remember { mutableStateOf("") }
    
    // Cargar eventos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadPendingEvents()
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
                    text = "🎉 Moderación de Eventos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Gestiona eventos pendientes y aprobados",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            IconButton(
                onClick = { navController.popBackStack() }
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver"
                )
            }
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth()
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Pendientes (${uiState.pendingEvents.size})") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Pending,
                        contentDescription = null
                    )
                }
            )
            
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Todos (${uiState.events.size})") },
                icon = {
                    Icon(
                        imageVector = Icons.Default.List,
                        contentDescription = null
                    )
                }
            )
        }
        
        // Contenido de las tabs
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = LoadingMessage.CARGANDO_EVENTOS.value)
                }
            }
            
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsFeedback(
                        message = uiState.errorMessage ?: ErrorMessage.ERROR_DESCONOCIDO.value,
                        type = "error",
                        onRetry = { 
                            if (selectedTab == 0) {
                                viewModel.loadPendingEvents()
                            } else {
                                viewModel.loadAllEvents()
                            }
                        }
                    )
                }
            }
            
            else -> {
                val eventsToShow = if (selectedTab == 0) uiState.pendingEvents else uiState.events
                
                if (eventsToShow.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = if (selectedTab == 0) Icons.Default.Pending else Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = if (selectedTab == 0) "No hay eventos pendientes" else "No hay eventos",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(eventsToShow) { event ->
                            AdminEventCard(
                                event = event,
                                onClick = {
                                    selectedEvent = event
                                    showEventDetails = true
                                },
                                onApproveClick = {
                                    selectedEvent = event
                                    moderationAction = ModerationAction.APPROVE
                                    showModerationDialog = true
                                },
                                onRejectClick = {
                                    selectedEvent = event
                                    moderationAction = ModerationAction.REJECT
                                    showModerationDialog = true
                                },
                                onCancelClick = {
                                    selectedEvent = event
                                    moderationAction = ModerationAction.CANCEL
                                    showModerationDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de detalles del evento
    if (showEventDetails && selectedEvent != null) {
        AdminEventDetailsDialog(
            event = selectedEvent!!,
            onDismiss = {
                showEventDetails = false
                selectedEvent = null
            },
            onApproveClick = {
                selectedEvent = selectedEvent
                moderationAction = ModerationAction.APPROVE
                showEventDetails = false
                showModerationDialog = true
            },
            onRejectClick = {
                selectedEvent = selectedEvent
                moderationAction = ModerationAction.REJECT
                showEventDetails = false
                showModerationDialog = true
            },
            onCancelClick = {
                selectedEvent = selectedEvent
                moderationAction = ModerationAction.CANCEL
                showEventDetails = false
                showModerationDialog = true
            }
        )
    }
    
    // Diálogo de moderación
    if (showModerationDialog && selectedEvent != null && moderationAction != null) {
        ModerationDialog(
            event = selectedEvent!!,
            action = moderationAction!!,
            onDismiss = {
                showModerationDialog = false
                selectedEvent = null
                moderationAction = null
                moderationComments = ""
            },
            onConfirm = { comments ->
                when (moderationAction) {
                    ModerationAction.APPROVE -> {
                        viewModel.approveEvent(selectedEvent!!.id, comments)
                    }
                    ModerationAction.REJECT -> {
                        viewModel.rejectEvent(selectedEvent!!.id, comments)
                    }
                    ModerationAction.CANCEL -> {
                        viewModel.cancelEvent(selectedEvent!!.id, comments)
                    }
                    null -> {
                        // No action
                    }
                }
                showModerationDialog = false
                selectedEvent = null
                moderationAction = null
                moderationComments = ""
            }
        )
    }
}

@Composable
fun AdminEventCard(
    event: Event,
    onClick: () -> Unit,
    onApproveClick: () -> Unit,
    onRejectClick: () -> Unit,
    onCancelClick: () -> Unit
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
                
                // Estado del evento
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (event.estado) {
                            EstadoEvento.APROBADO -> 
                                if (event.cancelado) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            EstadoEvento.PENDIENTE -> 
                                MaterialTheme.colorScheme.secondaryContainer
                            EstadoEvento.EN_REVISION -> 
                                MaterialTheme.colorScheme.tertiaryContainer
                            EstadoEvento.RECHAZADO -> 
                                MaterialTheme.colorScheme.errorContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = when (event.estado) {
                            EstadoEvento.PENDIENTE -> "⏳ ${event.estado.displayName}"
                            EstadoEvento.EN_REVISION -> "🔍 ${event.estado.displayName}"
                            EstadoEvento.APROBADO -> if (event.cancelado) "❌ Cancelado" else "✅ ${event.estado.displayName}"
                            EstadoEvento.RECHAZADO -> "❌ ${event.estado.displayName}"
                            EstadoEvento.CANCELADO -> "❌ ${event.estado.displayName}"
                            EstadoEvento.FINALIZADO -> "🏁 ${event.estado.displayName}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when (event.estado) {
                            EstadoEvento.APROBADO -> 
                                if (event.cancelado) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            EstadoEvento.PENDIENTE -> 
                                MaterialTheme.colorScheme.onSecondaryContainer
                            EstadoEvento.EN_REVISION -> 
                                MaterialTheme.colorScheme.onTertiaryContainer
                            EstadoEvento.RECHAZADO -> 
                                MaterialTheme.colorScheme.onErrorContainer
                            EstadoEvento.CANCELADO -> 
                                MaterialTheme.colorScheme.onErrorContainer
                            EstadoEvento.FINALIZADO -> 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Información del evento
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Fecha
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
                        text = formatEventDateTime(event),
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
                
                // Organizador
                if (event.organizador.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.organizador,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Fecha de creación
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Creado: ${formatDate(event.fechaCreacion)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botones de acción
            if (event.estado == EstadoEvento.PENDIENTE || event.estado == EstadoEvento.EN_REVISION) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onApproveClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar")
                    }
                    
                    OutlinedButton(
                        onClick = onRejectClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }
            } else if (event.estado == EstadoEvento.APROBADO && !event.cancelado) {
                OutlinedButton(
                    onClick = onCancelClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Cancel,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cancelar Evento")
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
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return dateFormat.format(timestamp.toDate())
}

// Enum para acciones de moderación
enum class ModerationAction {
    APPROVE, REJECT, CANCEL
}
