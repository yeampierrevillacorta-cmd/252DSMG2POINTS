package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.points.ui.theme.*
import com.example.points.ui.components.ModernCard
import com.example.points.ui.components.ModernButton
import com.example.points.ui.components.ButtonVariant
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

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
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo moderno con gradiente sutil
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF8F0),
                            Color(0xFFFFFBF5),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header moderno con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B),
                                Color(0xFFFF8E53)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Título con animación
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color.White.copy(0.2f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "🚨",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Incidentes",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Reporta y descubre incidentes en tu localidad",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        // Botón de filtros moderno
                        Surface(
                            modifier = Modifier
                                .size(48.dp)
                                .shadow(if (showFilters) 8.dp else 4.dp, CircleShape)
                                .clickable { showFilters = !showFilters },
                            shape = CircleShape,
                            color = if (showFilters) Color.White else Color.White.copy(0.3f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                                    contentDescription = ContentDescription.FILTROS.value,
                                    tint = if (showFilters) Color(0xFFFF6B6B) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botón de crear incidente moderno
                    ModernButton(
                        text = "Reportar Incidente",
                        onClick = { navController.navigate(AppRoutes.INCIDENTS_MAP) },
                        variant = ButtonVariant.Secondary,
                        icon = Icons.Default.Add,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            // Panel de filtros con animación de expansión
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = PointsPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Filtros",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Filtro de tipo de incidente
                        Text(
                            text = "Tipo de Incidente",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.filterByType(null) },
                                    label = { 
                                        Text(
                                            "Todos",
                                            fontWeight = if (safeUiState.selectedType == null) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = safeUiState.selectedType == null,
                                    leadingIcon = if (safeUiState.selectedType == null) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PointsPrimary,
                                        selectedLabelColor = Color.White
                                    )
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
                                    label = { 
                                        Text(
                                            tipo.displayName,
                                            fontWeight = if (safeUiState.selectedType == tipo) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = safeUiState.selectedType == tipo,
                                    leadingIcon = {
                                        Icon(
                                            getIncidentTypeIcon(tipo),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = getIncidentTypeColor(tipo.displayName),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // Filtro de estado
                        Text(
                            text = "Estado",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.filterByStatus(null) },
                                    label = { 
                                        Text(
                                            "Todos",
                                            fontWeight = if (safeUiState.selectedStatus == null) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = safeUiState.selectedStatus == null,
                                    leadingIcon = if (safeUiState.selectedStatus == null) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PointsPrimary,
                                        selectedLabelColor = Color.White
                                    )
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
                                    label = { 
                                        Text(
                                            estado.displayName,
                                            fontWeight = if (safeUiState.selectedStatus == estado) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = safeUiState.selectedStatus == estado,
                                    leadingIcon = {
                                        Icon(
                                            getIncidentStatusIcon(estado),
                                            contentDescription = null,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = getIncidentStatusColorModern(estado),
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Sección de estadísticas
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatsCard(
                                title = "Total",
                                value = safeUiState.filteredIncidents.size.toString(),
                                icon = Icons.Default.Warning,
                                color = Color(0xFFFF6B6B),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = "Activos",
                                value = safeUiState.filteredIncidents.count { 
                                    it.estado != EstadoIncidente.RESUELTO && it.estado != EstadoIncidente.RECHAZADO 
                                }.toString(),
                                icon = Icons.Default.Schedule,
                                color = Color(0xFFFFBE0B),
                                modifier = Modifier.weight(1f)
                            )
                            StatsCard(
                                title = "Resueltos",
                                value = safeUiState.filteredIncidents.count { 
                                    it.estado == EstadoIncidente.RESUELTO 
                                }.toString(),
                                icon = Icons.Default.CheckCircle,
                                color = Color(0xFF6BCF7F),
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    
                    // Título de sección
                    item {
                        Text(
                            text = if (safeUiState.selectedType != null || safeUiState.selectedStatus != null) {
                                "Incidentes Filtrados"
                            } else {
                                "Todos los Incidentes"
                            },
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    
                    if (safeUiState.filteredIncidents.isEmpty()) {
                        item {
                            ModernCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(48.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        modifier = Modifier.size(72.dp),
                                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = "No hay incidentes",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Prueba ajustando los filtros o reporta uno nuevo",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    } else {
                        itemsIndexed(safeUiState.filteredIncidents) { index, incident ->
                            var visible by remember { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                delay(index * 50L)
                                visible = true
                            }
                            
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn() + slideInVertically { it / 2 }
                            ) {
                                ModernIncidentCard(
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

// Componente de tarjeta de estadísticas
@Composable
private fun StatsCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    ModernCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color.copy(alpha = 0.15f), CircleShape),
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
            
            var animatedValue by remember { mutableStateOf(0) }
            val targetValue = value.toIntOrNull() ?: 0
            LaunchedEffect(targetValue) {
                val step = if (targetValue > 0) targetValue / 20 else 0
                repeat(20) {
                    animatedValue = (animatedValue + step).coerceAtMost(targetValue)
                    delay(30)
                }
                animatedValue = targetValue
            }
            
            Text(
                text = animatedValue.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
            )
        }
    }
}

@Composable
fun ModernIncidentCard(
    incident: Incident,
    onClick: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Foto del incidente o icono si no hay foto
                if (incident.fotoUrl != null && incident.fotoUrl!!.isNotEmpty()) {
                    OptimizedRoundedImage(
                        imageUrl = incident.fotoUrl!!,
                        contentDescription = "Foto del incidente",
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                getIncidentTypeColor(incident.tipo).copy(0.15f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getIncidentTypeIconFromString(incident.tipo),
                            contentDescription = null,
                            tint = getIncidentTypeColor(incident.tipo),
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Tipo de incidente
                    Text(
                        text = incident.tipo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Fecha y hora
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatIncidentDateTime(incident),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Estado con badge moderno
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = getIncidentStatusColorModern(incident.estado).copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = getIncidentStatusIcon(incident.estado),
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = getIncidentStatusColorModern(incident.estado)
                            )
                            Text(
                                text = incident.estado.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = getIncidentStatusColorModern(incident.estado)
                            )
                        }
                    }
                }
            }
            
            // Contenido expandido
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Divider decorativo
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(0.1f))
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Descripción
                    if (incident.descripcion.isNotEmpty()) {
                        Text(
                            text = incident.descripcion,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Ubicación
                    if (incident.ubicacion.direccion.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant.copy(0.5f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PointsPrimary.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = PointsPrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = incident.ubicacion.direccion,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Botones de acción
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onClick,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ver Detalles")
                        }
                        
                        Button(
                            onClick = onViewDetailsClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PointsPrimary
                            )
                        ) {
                            Icon(Icons.Default.Map, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Ver en Mapa")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun IncidentCard(
    incident: Incident,
    onClick: () -> Unit,
    onViewDetailsClick: () -> Unit
) {
    ModernIncidentCard(
        incident = incident,
        onClick = onClick,
        onViewDetailsClick = onViewDetailsClick
    )
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
    return when (tipo.lowercase()) {
        "inseguridad" -> Color(0xFFFF9800)
        "accidente de tránsito", "accidente de transito" -> Color(0xFFF44336)
        "incendio" -> Color(0xFFFF5722)
        "inundación", "inundacion" -> Color(0xFF2196F3)
        "vandalismo" -> Color(0xFF9C27B0)
        "servicio público", "servicio publico" -> Color(0xFF607D8B)
        else -> Color(0xFFFF6B6B)
    }
}

// Función para obtener el color del estado del incidente (moderna)
private fun getIncidentStatusColorModern(estado: EstadoIncidente): Color {
    return when (estado) {
        EstadoIncidente.PENDIENTE -> Color(0xFFFFBE0B)
        EstadoIncidente.EN_REVISION -> Color(0xFF4ECDC4)
        EstadoIncidente.CONFIRMADO -> Color(0xFF6BCF7F)
        EstadoIncidente.RESUELTO -> Color(0xFF38A3A5)
        EstadoIncidente.RECHAZADO -> Color(0xFFFF6B6B)
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
