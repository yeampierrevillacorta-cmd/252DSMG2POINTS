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
import com.example.points.models.EstadoIncidente
import com.example.points.models.Incident
import com.example.points.models.TipoIncidente
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsScreen(
    navController: NavController,
    viewModel: IncidentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Protección contra estados nulos o vacíos
    val safeUiState = remember(uiState) {
        uiState.copy(
            incidents = uiState.incidents ?: emptyList(),
            filteredIncidents = uiState.filteredIncidents ?: emptyList()
        )
    }
    
    // Estados locales
    var showFilters by remember { mutableStateOf(false) }
    var selectedIncident by remember { mutableStateOf<Incident?>(null) }
    var showIncidentDetails by remember { mutableStateOf(false) }
    
    // Cargar incidentes al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadAllIncidents()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header con título y botones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "🚨 Incidentes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Descubre el incidente de tu localidad",
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
                
                // Botón de crear incidente
                Button(
                    onClick = { navController.navigate("incidents_map") },
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
                    
                    // Filtro de tipo de incidente
                    Text(
                        text = "Tipo de Incidente",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { viewModel.filterByType(null) },
                                label = { Text("Todos") },
                                selected = safeUiState.selectedType == null,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        
                        items(TipoIncidente.values()) { tipo ->
                            FilterChip(
                                onClick = { 
                                    if (uiState.selectedType == tipo) {
                                        viewModel.filterByType(null)
                                    } else {
                                        viewModel.filterByType(tipo)
                                    }
                                },
                                label = { Text(tipo.displayName) },
                                selected = safeUiState.selectedType == tipo,
                                leadingIcon = {
                                    Icon(
                                        getIncidentTypeIcon(tipo),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtro de estado
                    Text(
                        text = "Estado",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { viewModel.filterByStatus(null) },
                                label = { Text("Todos") },
                                selected = safeUiState.selectedStatus == null,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        
                        items(EstadoIncidente.values()) { estado ->
                            FilterChip(
                                onClick = { 
                                    if (uiState.selectedStatus == estado) {
                                        viewModel.filterByStatus(null)
                                    } else {
                                        viewModel.filterByStatus(estado)
                                    }
                                },
                                label = { Text(estado.displayName) },
                                selected = safeUiState.selectedStatus == estado,
                                leadingIcon = {
                                    Icon(
                                        getIncidentStatusIcon(estado),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Contenido principal
        when {
            safeUiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = "Cargando incidentes...")
                }
            }
            
            safeUiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsFeedback(
                        message = safeUiState.errorMessage ?: "Error desconocido",
                        type = "error",
                        onRetry = { viewModel.loadAllIncidents() }
                    )
                }
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Sección de todos los incidentes
                    item {
                        Text(
                            text = if (safeUiState.selectedType != null || safeUiState.selectedStatus != null) {
                                "📋 Incidentes Filtrados"
                            } else {
                                "🚨 Todos los Incidentes"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    if (safeUiState.filteredIncidents.isEmpty()) {
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
                                        imageVector = Icons.Default.ReportProblem,
                                        contentDescription = null,
                                        modifier = Modifier.size(64.dp),
                                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay incidentes disponibles",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "Intenta cambiar los filtros o crear un nuevo incidente",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    } else {
                        items(safeUiState.filteredIncidents) { incident ->
                            IncidentCard(
                                incident = incident,
                                onClick = {
                                    selectedIncident = incident
                                    showIncidentDetails = true
                                },
                                onViewDetailsClick = {
                                    navController.navigate("incident_detail/${incident.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // Diálogo de detalles del incidente
    if (showIncidentDetails && selectedIncident != null) {
        IncidentDetailsDialog(
            incident = selectedIncident!!,
            onDismiss = {
                showIncidentDetails = false
                selectedIncident = null
            },
            onViewDetailsClick = {
                navController.navigate("incident_detail/${selectedIncident!!.id}")
                showIncidentDetails = false
                selectedIncident = null
            }
        )
    }
}

@Composable
fun IncidentCard(
    incident: Incident,
    onClick: () -> Unit,
    onViewDetailsClick: () -> Unit
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
            // Header del incidente
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = incident.tipo,
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
                            imageVector = getIncidentTypeIconFromString(incident.tipo),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = getIncidentTypeColor(incident.tipo)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = incident.tipo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Estado del incidente
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (incident.estado) {
                            EstadoIncidente.CONFIRMADO -> MaterialTheme.colorScheme.primaryContainer
                            EstadoIncidente.PENDIENTE -> MaterialTheme.colorScheme.secondaryContainer
                            EstadoIncidente.EN_REVISION -> MaterialTheme.colorScheme.tertiaryContainer
                            EstadoIncidente.RESUELTO -> MaterialTheme.colorScheme.surfaceVariant
                            EstadoIncidente.RECHAZADO -> MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Text(
                        text = when (incident.estado) {
                            EstadoIncidente.PENDIENTE -> "⏳ ${incident.estado.displayName}"
                            EstadoIncidente.EN_REVISION -> "🔍 ${incident.estado.displayName}"
                            EstadoIncidente.CONFIRMADO -> "✅ ${incident.estado.displayName}"
                            EstadoIncidente.RESUELTO -> "✅ ${incident.estado.displayName}"
                            EstadoIncidente.RECHAZADO -> "❌ ${incident.estado.displayName}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when (incident.estado) {
                            EstadoIncidente.CONFIRMADO -> MaterialTheme.colorScheme.onPrimaryContainer
                            EstadoIncidente.PENDIENTE -> MaterialTheme.colorScheme.onSecondaryContainer
                            EstadoIncidente.EN_REVISION -> MaterialTheme.colorScheme.onTertiaryContainer
                            EstadoIncidente.RESUELTO -> MaterialTheme.colorScheme.onSurfaceVariant
                            EstadoIncidente.RECHAZADO -> MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Descripción
            if (incident.descripcion.isNotEmpty()) {
                Text(
                    text = incident.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Información del incidente
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
                        text = formatIncidentDateTime(incident),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Ubicación
                if (incident.ubicacion.direccion.isNotEmpty()) {
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
                            text = incident.ubicacion.direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de acción
            OutlinedButton(
                onClick = onViewDetailsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver detalles")
            }
        }
    }
}

// Función auxiliar para formatear fecha y hora del incidente
private fun formatIncidentDateTime(incident: Incident): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    val date = incident.fechaHora.toDate()
    return "${dateFormat.format(date)} ${timeFormat.format(date)}"
}

// Función para obtener el icono del tipo de incidente
private fun getIncidentTypeIcon(tipo: TipoIncidente): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tipo) {
        TipoIncidente.INSEGURIDAD -> Icons.Default.Warning
        TipoIncidente.ACCIDENTE_TRANSITO -> Icons.Default.CarCrash
        TipoIncidente.INCENDIO -> Icons.Default.LocalFireDepartment
        TipoIncidente.INUNDACION -> Icons.Default.Water
        TipoIncidente.VANDALISMO -> Icons.Default.Build
        TipoIncidente.SERVICIO_PUBLICO -> Icons.Default.Settings
        TipoIncidente.OTRO -> Icons.Default.ReportProblem
    }
}

// Función segura para obtener el icono del tipo de incidente desde string
private fun getIncidentTypeIconFromString(tipoString: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (tipoString.lowercase()) {
        "inseguridad" -> Icons.Default.Warning
        "accidente de tránsito", "accidente de transito" -> Icons.Default.CarCrash
        "incendio" -> Icons.Default.LocalFireDepartment
        "inundación", "inundacion" -> Icons.Default.Water
        "vandalismo" -> Icons.Default.Build
        "servicio público", "servicio publico" -> Icons.Default.Settings
        else -> Icons.Default.ReportProblem
    }
}

// Función para obtener el icono del estado del incidente
private fun getIncidentStatusIcon(estado: EstadoIncidente): androidx.compose.ui.graphics.vector.ImageVector {
    return when (estado) {
        EstadoIncidente.PENDIENTE -> Icons.Default.Schedule
        EstadoIncidente.EN_REVISION -> Icons.Default.Search
        EstadoIncidente.CONFIRMADO -> Icons.Default.CheckCircle
        EstadoIncidente.RESUELTO -> Icons.Default.Done
        EstadoIncidente.RECHAZADO -> Icons.Default.Cancel
    }
}

// Función para obtener el color del tipo de incidente
private fun getIncidentTypeColor(tipo: String): Color {
    return when (tipo) {
        "Inseguridad" -> Color(0xFFFF9800)
        "Accidente de Tránsito" -> Color(0xFFF44336)
        "Incendio" -> Color(0xFFFF5722)
        "Inundación" -> Color(0xFF2196F3)
        "Vandalismo" -> Color(0xFF9C27B0)
        "Servicio Público" -> Color(0xFF607D8B)
        else -> Color(0xFF6200EE) // Color primario por defecto
    }
}

// Diálogo simple de detalles del incidente (placeholder)
@Composable
fun IncidentDetailsDialog(
    incident: Incident,
    onDismiss: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(incident.tipo) },
        text = { Text(incident.descripcion) },
        confirmButton = {
            TextButton(onClick = onViewDetailsClick) {
                Text("Ver detalles")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}
