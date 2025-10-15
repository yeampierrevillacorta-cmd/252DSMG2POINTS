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
import com.example.points.models.CategoriaEvento
import com.example.points.models.Event
import com.example.points.models.EstadoEvento
import com.example.points.viewmodel.EventViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estados locales
    var showCreateEventDialog by remember { mutableStateOf(false) }
    var showFilters by remember { mutableStateOf(false) }
    var selectedEvent by remember { mutableStateOf<Event?>(null) }
    var showEventDetails by remember { mutableStateOf(false) }
    
    // Cargar eventos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadAllEvents()
        viewModel.loadUpcomingEvents()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header con título y botón de crear evento
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🎉 Eventos",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Descubre eventos en tu localidad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                )
            }
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón de filtros
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .background(
                            if (showFilters) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                            RoundedCornerShape(8.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                        contentDescription = "Filtros",
                        tint = if (showFilters) MaterialTheme.colorScheme.onPrimary 
                               else MaterialTheme.colorScheme.onSurface
                    )
                }
                
                // Botón de crear evento
                Button(
                    onClick = { showCreateEventDialog = true },
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
                    Text("Crear")
                }
            }
        }
        
        // Panel de filtros
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtro de categoría
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { viewModel.clearSearch() },
                                label = { Text("Todas") },
                                selected = uiState.selectedCategory == null,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        
                        items(CategoriaEvento.values()) { category ->
                            FilterChip(
                                onClick = { 
                                    if (uiState.selectedCategory == category) {
                                        viewModel.clearSearch()
                                    } else {
                                        viewModel.loadEventsByCategory(category)
                                    }
                                },
                                label = { Text(category.displayName) },
                                selected = uiState.selectedCategory == category,
                                leadingIcon = {
                                    Icon(
                                        getCategoryIcon(category.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtro de mostrar solo próximos
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Solo eventos próximos",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Switch(
                            checked = uiState.showOnlyUpcoming,
                            onCheckedChange = { viewModel.toggleShowOnlyUpcoming() }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Contenido principal
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = "Cargando eventos...")
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
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección de eventos próximos
                    if (uiState.upcomingEvents.isNotEmpty() && uiState.showOnlyUpcoming) {
                        item {
                            Text(
                                text = "📅 Próximos Eventos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        items(uiState.upcomingEvents) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    selectedEvent = event
                                    showEventDetails = true
                                },
                                onRegisterClick = {
                                    viewModel.registerToEvent(event.id)
                                }
                            )
                        }
                    }
                    
                    // Sección de todos los eventos
                    if (!uiState.showOnlyUpcoming || uiState.upcomingEvents.isEmpty()) {
                        item {
                            Text(
                                text = if (uiState.selectedCategory != null) {
                                    "📋 ${uiState.selectedCategory!!.displayName}"
                                } else {
                                    "🎪 Todos los Eventos"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        
                        if (uiState.events.isEmpty()) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.EventBusy,
                                            contentDescription = null,
                                            modifier = Modifier.size(64.dp),
                                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "No hay eventos disponibles",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                        )
                                        Text(
                                            text = "Intenta cambiar los filtros o crear un nuevo evento",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                        )
                                    }
                                }
                            }
                        } else {
                            items(uiState.events) { event ->
                                EventCard(
                                    event = event,
                                    onClick = {
                                        selectedEvent = event
                                        showEventDetails = true
                                    },
                                    onRegisterClick = {
                                        viewModel.registerToEvent(event.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de detalles del evento
    if (showEventDetails && selectedEvent != null) {
        EventDetailsDialog(
            event = selectedEvent!!,
            onDismiss = {
                showEventDetails = false
                selectedEvent = null
            },
            onRegisterClick = {
                viewModel.registerToEvent(selectedEvent!!.id)
                showEventDetails = false
                selectedEvent = null
            },
            onShareClick = {
                // TODO: Implementar compartir evento
            }
        )
    }
    
    // Diálogo de crear evento
    if (showCreateEventDialog) {
        CreateEventDialog(
            onDismiss = { showCreateEventDialog = false },
            onCreateEvent = { event ->
                viewModel.createEvent(event)
                showCreateEventDialog = false
            }
        )
    }
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onRegisterClick: () -> Unit
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
                            com.example.points.models.EstadoEvento.APROBADO -> 
                                if (event.cancelado) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            com.example.points.models.EstadoEvento.PENDIENTE -> 
                                MaterialTheme.colorScheme.secondaryContainer
                            com.example.points.models.EstadoEvento.EN_REVISION -> 
                                MaterialTheme.colorScheme.tertiaryContainer
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
            
            // Descripción
            if (event.descripcion.isNotEmpty()) {
                Text(
                    text = event.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Información del evento
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fecha y hora
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
                
                // Precio
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            event.esGratuito -> "Gratuito"
                            event.precio.precioGeneral != null -> "$${String.format("%.0f", event.precio.precioGeneral)}"
                            else -> "Precio no especificado"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Capacidad (si aplica)
                if (event.capacidad != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${event.inscripciones}/${event.capacidad} inscritos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de acción
            if (event.estado == com.example.points.models.EstadoEvento.APROBADO && !event.cancelado) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
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
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Inscribirse")
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Ver detalles")
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver detalles")
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
