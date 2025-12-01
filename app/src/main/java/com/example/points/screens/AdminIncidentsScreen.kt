package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.components.OptimizedAsyncImage
import com.example.points.constants.ButtonText
import com.example.points.models.EstadoIncidente
import com.example.points.models.TipoIncidente
import com.example.points.utils.MarkerUtils
import com.example.points.viewmodel.IncidentViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminIncidentsScreen(
    onBackClick: () -> Unit,
    onIncidentDetailClick: (String) -> Unit,
    viewModel: IncidentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedStatusFilter by remember { mutableStateOf<EstadoIncidente?>(null) }
    var selectedTypeFilter by remember { mutableStateOf<TipoIncidente?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var showEvaluationDialog by remember { mutableStateOf(false) }
    var selectedIncidentForEvaluation by remember { mutableStateOf<com.example.points.models.Incident?>(null) }

    // Filtrar y ordenar incidentes según los filtros seleccionados
    val filteredIncidents = remember(uiState.incidents, selectedStatusFilter, selectedTypeFilter) {
        val filtered = uiState.incidents.filter { incident ->
            val statusMatch = selectedStatusFilter?.let { incident.estado == it } ?: true
            val typeMatch = selectedTypeFilter?.let { incident.tipo == it.displayName } ?: true
            statusMatch && typeMatch
        }
        
        // Ordenar por prioridad (ALTA primero) y luego por fecha
        filtered.sortedWith(
            compareByDescending<com.example.points.models.Incident> { incident ->
                when (incident.prioridad?.uppercase()) {
                    "ALTA" -> 3
                    "MEDIA" -> 2
                    "BAJA" -> 1
                    else -> 0
                }
            }.thenByDescending { it.fechaHora.toDate().time }
        )
    }

    // Estadísticas
    val stats = remember(uiState.incidents) {
        val total = uiState.incidents.size
        val pendientes = uiState.incidents.count { it.estado == EstadoIncidente.PENDIENTE }
        val confirmados = uiState.incidents.count { it.estado == EstadoIncidente.CONFIRMADO }
        val resueltos = uiState.incidents.count { it.estado == EstadoIncidente.RESUELTO }
        Triple(pendientes, confirmados, resueltos)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Gestión de Incidentes")
                        Text(
                            text = "${filteredIncidents.size} incidentes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filtros")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Estadísticas rápidas
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard(
                        title = "Pendientes",
                        count = stats.first,
                        color = MaterialTheme.colorScheme.error,
                        icon = Icons.Default.Pending
                    )
                    StatCard(
                        title = "Confirmados",
                        count = stats.second,
                        color = MaterialTheme.colorScheme.primary,
                        icon = Icons.Default.CheckCircle
                    )
                    StatCard(
                        title = "Resueltos",
                        count = stats.third,
                        color = MaterialTheme.colorScheme.tertiary,
                        icon = Icons.Default.Done
                    )
                }
            }

            // Filtros
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Filtros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Filtro por estado
                        Text(
                            text = "Estado:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { selectedStatusFilter = null },
                                    label = { Text(ButtonText.TODOS.value) },
                                    selected = selectedStatusFilter == null
                                )
                            }
                            items(EstadoIncidente.values()) { estado ->
                                FilterChip(
                                    onClick = { selectedStatusFilter = estado },
                                    label = { Text(estado.displayName) },
                                    selected = selectedStatusFilter == estado
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Filtro por tipo
                        Text(
                            text = "Tipo:",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { selectedTypeFilter = null },
                                    label = { Text(ButtonText.TODOS.value) },
                                    selected = selectedTypeFilter == null
                                )
                            }
                            items(TipoIncidente.values()) { tipo ->
                                FilterChip(
                                    onClick = { selectedTypeFilter = tipo },
                                    label = { Text(tipo.displayName) },
                                    selected = selectedTypeFilter == tipo
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Lista de incidentes
            if (filteredIncidents.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Report,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No hay incidentes",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Los incidentes aparecerán aquí cuando los usuarios los reporten",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredIncidents) { incident ->
                        IncidentAdminCard(
                            incident = incident,
                            onEvaluateClick = {
                                selectedIncidentForEvaluation = incident
                                showEvaluationDialog = true
                            },
                            onDetailClick = {
                                onIncidentDetailClick(incident.id)
                            }
                        )
                    }
                }
            }
        }
    }

    // Diálogo de evaluación
    if (showEvaluationDialog && selectedIncidentForEvaluation != null) {
        IncidentEvaluationDialog(
            incident = selectedIncidentForEvaluation!!,
            onDismiss = {
                showEvaluationDialog = false
                selectedIncidentForEvaluation = null
            },
            onStatusChange = { newStatus ->
                viewModel.updateIncidentStatus(selectedIncidentForEvaluation!!.id, newStatus)
                showEvaluationDialog = false
                selectedIncidentForEvaluation = null
            }
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: Int,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color.copy(alpha = 0.1f),
                    RoundedCornerShape(24.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun IncidentAdminCard(
    incident: com.example.points.models.Incident,
    onEvaluateClick: () -> Unit,
    onDetailClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val reportDate = incident.fechaHora.toDate()
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDetailClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header con tipo y estado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono del tipo
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                MarkerUtils.getColorForIncidentType(incident.tipo).copy(alpha = 0.2f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (incident.tipo) {
                                "Inseguridad" -> "⚠️"
                                "Accidente de Tránsito" -> "🚗"
                                "Incendio" -> "🔥"
                                "Inundación" -> "💧"
                                "Vandalismo" -> "⚡"
                                "Servicio Público" -> "🔧"
                                else -> "📍"
                            },
                            fontSize = 14.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = incident.tipo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MarkerUtils.getColorForIncidentType(incident.tipo)
                        )
                        Text(
                            text = dateFormat.format(reportDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                // Estado
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MarkerUtils.getColorForIncidentStatus(incident.estado.displayName).copy(alpha = 0.2f)
                ) {
                    Text(
                        text = incident.estado.displayName,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MarkerUtils.getColorForIncidentStatus(incident.estado.displayName),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Prioridad y etiqueta IA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge de prioridad
                incident.prioridad?.let { prioridad ->
                    val (priorityColor, priorityText) = when (prioridad.uppercase()) {
                        "ALTA" -> Pair(Color(0xFFFF5252), "ALTA")
                        "MEDIA" -> Pair(Color(0xFFFFA726), "MEDIA")
                        "BAJA" -> Pair(Color(0xFF66BB6A), "BAJA")
                        else -> Pair(MaterialTheme.colorScheme.onSurfaceVariant, prioridad)
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = priorityColor.copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, priorityColor)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.PriorityHigh,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = priorityColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Prioridad: $priorityText",
                                style = MaterialTheme.typography.labelSmall,
                                color = priorityColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                
                // Etiqueta IA si existe
                incident.etiqueta_ia?.let { etiqueta ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "IA: $etiqueta",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Descripción
            Text(
                text = incident.descripcion,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Ubicación
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (incident.ubicacion.direccion.isNotEmpty()) {
                        incident.ubicacion.direccion
                    } else {
                        "Lat: ${String.format("%.4f", incident.ubicacion.lat)}, Lon: ${String.format("%.4f", incident.ubicacion.lon)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Imagen si existe
            incident.fotoUrl?.let { imageUrl ->
                Spacer(modifier = Modifier.height(8.dp))
                OptimizedAsyncImage(
                    imageUrl = imageUrl,
                    contentDescription = "Imagen del incidente",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Botones de acción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDetailClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(ButtonText.VER_DETALLES.value)
                }
                
                if (incident.estado == EstadoIncidente.PENDIENTE || incident.estado == EstadoIncidente.EN_REVISION) {
                    Button(
                        onClick = onEvaluateClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Evaluar")
                    }
                }
            }
        }
    }
}

@Composable
private fun IncidentEvaluationDialog(
    incident: com.example.points.models.Incident,
    onDismiss: () -> Unit,
    onStatusChange: (EstadoIncidente) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Evaluar Incidente")
        },
        text = {
            Column {
                Text(
                    text = "Tipo: ${incident.tipo}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Descripción: ${incident.descripcion}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Selecciona el nuevo estado:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onStatusChange(EstadoIncidente.RECHAZADO) }
                ) {
                    Text("Rechazar")
                }
                Button(
                    onClick = { onStatusChange(EstadoIncidente.EN_REVISION) }
                ) {
                    Text("En Revisión")
                }
                Button(
                    onClick = { onStatusChange(EstadoIncidente.CONFIRMADO) }
                ) {
                    Text(ButtonText.CONFIRMAR.value)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(ButtonText.CANCELAR.value)
            }
        }
    )
}



