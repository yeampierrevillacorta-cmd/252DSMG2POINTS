package com.example.points.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.IconButtonDefaults
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
import com.example.points.components.OptimizedRoundedImage
import com.example.points.constants.ReviewStatus
import com.example.points.constants.ButtonText
import com.example.points.constants.LoadingMessage
import com.example.points.constants.ErrorMessage
import com.example.points.constants.SectionTitle
import com.example.points.constants.ContentDescription
import com.example.points.constants.AppSpacing
import com.example.points.constants.AppRoutes
import com.example.points.constants.IconSize
import com.example.points.ui.theme.ButtonSize
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
        val iconButtonSize = 48.dp

        // Header con título y botones
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f, fill = false)
            ) {
                Text(
                    text = "🚨 Incidentes",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Descubre el incidente de tu localidad",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de filtros
                IconButton(
                    onClick = { showFilters = !showFilters },
                    modifier = Modifier
                        .size(iconButtonSize)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (showFilters) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.surface,
                        ),
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (showFilters) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Icon(
                        imageVector = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                        contentDescription = ContentDescription.FILTROS.value
                    )
                }
                
                // Botón de crear incidente
                Button(
                    onClick = { navController.navigate(AppRoutes.INCIDENTS_MAP) },
                    modifier = Modifier.heightIn(min = ButtonSize.height),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(IconSize.STANDARD)
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.SMALL))
                    Text(
                        text = ButtonText.CREAR.value,
                        maxLines = 1
                    )
                }
            }
        }
        
        // Panel de filtros con animación de expansión
        AnimatedVisibility(
            visible = showFilters,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
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
                            text = SectionTitle.FILTROS.value,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.STANDARD))
                        
                        // Filtro de tipo de incidente
                        Text(
                            text = SectionTitle.TIPO_INCIDENTE.value,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.MEDIUM))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.filterByType(null) },
                                    label = { Text(ButtonText.TODOS.value) },
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
                            text = SectionTitle.ESTADO.value,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Spacer(modifier = Modifier.height(AppSpacing.MEDIUM))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.filterByStatus(null) },
                                    label = { Text(ButtonText.TODOS.value) },
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
        }
        
        // Contenido principal
        when {
            safeUiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = LoadingMessage.CARGANDO_INCIDENTES.value)
                }
            }
            
            safeUiState.errorMessage != null -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsFeedback(
                        message = safeUiState.errorMessage ?: ErrorMessage.ERROR_DESCONOCIDO.value,
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
                                    SectionTitle.INCIDENTES_FILTRADOS.value
                                } else {
                                    SectionTitle.TODOS_LOS_INCIDENTES.value
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
                                        text = ErrorMessage.NO_HAY_INCIDENTES.value,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                    Text(
                                        text = "${ErrorMessage.INTENTA_CAMBIAR_FILTROS.value} incidente",
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
                                    navController.navigate("${AppRoutes.INCIDENT_DETAIL}/${incident.id}")
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
                navController.navigate("${AppRoutes.INCIDENT_DETAIL}/${selectedIncident!!.id}")
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
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
        ) {
            // Header del incidente - siempre visible
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Imagen del incidente en círculo
                OptimizedRoundedImage(
                    imageUrl = incident.fotoUrl,
                    size = 56.dp,
                    contentDescription = "Foto del incidente",
                    placeholder = getIncidentTypeIconFromString(incident.tipo),
                    modifier = Modifier
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Título y fecha
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = incident.tipo,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatIncidentDateTime(incident),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Chevron de expandir/colapsar
                IconButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (expanded) ContentDescription.COLAPSAR.value else ContentDescription.EXPANDIR.value,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            
            // Contenido expandido - solo se muestra cuando expanded = true
            if (expanded) {
                Spacer(modifier = Modifier.height(16.dp))
                
                // Estado del incidente
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                            text = getIncidentReviewStatusText(incident),
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
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(ButtonText.VER_DETALLES.value)
                    }
                    
                    Button(
                        onClick = onViewDetailsClick,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(ButtonText.IR_A_MAPA.value)
                    }
                }
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

// Texto de estado usando ReviewStatus para consistencia visual
private fun getIncidentReviewStatusText(incident: Incident): String {
    val status = when (incident.estado) {
        EstadoIncidente.PENDIENTE -> ReviewStatus.PENDIENTE
        EstadoIncidente.EN_REVISION -> ReviewStatus.EN_REVISION
        EstadoIncidente.CONFIRMADO -> ReviewStatus.CONFIRMADO
        EstadoIncidente.RESUELTO -> ReviewStatus.RESUELTO
        EstadoIncidente.RECHAZADO -> ReviewStatus.RECHAZADO
    }
    return status.getFormattedText()
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
                Text(ButtonText.VER_DETALLES.value)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(ButtonText.CERRAR.value)
            }
        }
    )
}
