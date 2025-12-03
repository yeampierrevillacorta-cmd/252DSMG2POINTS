package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.components.OptimizedIncidentImage
import com.example.points.components.ShareOptionsDialog
import com.example.points.constants.ButtonText
import com.example.points.models.Incident
import com.example.points.ui.components.ModernCard
import com.example.points.ui.components.ModernButton
import com.example.points.ui.components.ButtonVariant
import com.example.points.ui.theme.*
import com.example.points.utils.MarkerUtils
import com.example.points.utils.ShareUtils
import com.example.points.viewmodel.IncidentViewModel
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDetailScreen(
    incidentId: String,
    onBackClick: () -> Unit,
    onEditClick: () -> Unit = {},
    viewModel: IncidentViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var incident by remember { mutableStateOf<Incident?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    // Cargar el incidente por ID
    LaunchedEffect(incidentId) {
        viewModel.repository.getIncidentById(incidentId).fold(
            onSuccess = { 
                incident = it
                isLoading = false
            },
            onFailure = { 
                error = it.message
                isLoading = false
            }
        )
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo con gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF8F0),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header moderno
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
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { onBackClick() },
                        shape = CircleShape,
                        color = Color.White.copy(0.2f)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Detalles del Incidente",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Información completa",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(0.9f)
                        )
                    }
                    
                    // Botones de acción
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { 
                                if (incident != null) {
                                    showShareDialog = true
                                }
                            },
                        shape = CircleShape,
                        color = Color.White.copy(0.2f)
                    ) {
                        Icon(
                            Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    
                    if (uiState.isUserAdmin) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { onEditClick() },
                            shape = CircleShape,
                            color = Color.White.copy(0.2f)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Editar",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
            
            // Contenido principal
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = PointsPrimary,
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 6.dp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Cargando detalles...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                
                error != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(PointsError.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = PointsError,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Error al cargar el incidente",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = error ?: "Error desconocido",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                ModernButton(
                                    text = "Volver",
                                    onClick = onBackClick,
                                    variant = ButtonVariant.Primary,
                                    icon = Icons.Default.ArrowBack,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                incident != null -> {
                    IncidentDetailContent(
                        incident = incident!!,
                        isUserAdmin = uiState.isUserAdmin,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Diálogo de compartir
        if (showShareDialog && incident != null) {
            ShareOptionsDialog(
                incident = incident!!,
                onDismiss = { showShareDialog = false }
            )
        }
    }
}

@Composable
private fun IncidentDetailContent(
    incident: Incident,
    isUserAdmin: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Encabezado con tipo e icono (con animación de entrada)
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(100)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { -it / 2 }
            ) {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        MarkerUtils.getColorForIncidentType(incident.tipo).copy(alpha = 0.15f),
                                        MarkerUtils.getColorForIncidentType(incident.tipo).copy(alpha = 0.05f)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono del tipo de incidente con sombra
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .shadow(8.dp, CircleShape)
                                    .background(
                                        MarkerUtils.getColorForIncidentType(incident.tipo),
                                        CircleShape
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
                                    fontSize = 32.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(20.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = incident.tipo,
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                // Estado con badge mejorado
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MarkerUtils.getColorForIncidentStatus(incident.estado.displayName).copy(alpha = 0.2f),
                                    modifier = Modifier.shadow(2.dp, RoundedCornerShape(12.dp))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = when (incident.estado.displayName) {
                                                "Pendiente" -> Icons.Default.Schedule
                                                "En Revisión" -> Icons.Default.Search
                                                "Confirmado" -> Icons.Default.CheckCircle
                                                "Resuelto" -> Icons.Default.Done
                                                else -> Icons.Default.Cancel
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = MarkerUtils.getColorForIncidentStatus(incident.estado.displayName)
                                        )
                                        Text(
                                            text = incident.estado.displayName,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = MarkerUtils.getColorForIncidentStatus(incident.estado.displayName),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Descripción
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(200)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(PointsPrimary.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = PointsPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = incident.descripcion.ifEmpty { "Sin descripción disponible" },
                            style = MaterialTheme.typography.bodyLarge,
                            lineHeight = 26.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.85f)
                        )
                    }
                }
            }
        }
        
        // Imagen si existe
        incident.fotoUrl?.let { imageUrl ->
            item {
                var visible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    delay(300)
                    visible = true
                }
                
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { it / 2 }
                ) {
                    ModernCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(Color(0xFF4ECDC4).copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = Color(0xFF4ECDC4),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Evidencia Fotográfica",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            OptimizedIncidentImage(
                                imageUrl = imageUrl,
                                contentDescription = "Foto del incidente",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                    }
                }
            }
        }
        
        // Información de ubicación
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(400)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFF6B6B).copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Ubicación",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        if (incident.ubicacion.direccion.isNotEmpty()) {
                            Text(
                                text = incident.ubicacion.direccion,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        Text(
                            text = "📍 ${String.format("%.6f", incident.ubicacion.lat)}, ${String.format("%.6f", incident.ubicacion.lon)}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        ModernButton(
                            text = "Ver en Google Maps",
                            onClick = { 
                                ShareUtils.openInGoogleMaps(context, incident)
                            },
                            variant = ButtonVariant.Secondary,
                            icon = Icons.Default.Map,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        
        // Información temporal
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(500)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFFFBE0B).copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = Color(0xFFFFBE0B),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Información Temporal",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        val reportDate = incident.fechaHora.toDate()
                        
                        Text(
                            text = "📅 ${dateFormat.format(reportDate)}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        val timeAgo = getTimeAgo(reportDate)
                        Text(
                            text = "⏱️ Hace $timeAgo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
            }
        }
        
        // Información del reportero
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(600)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFF9B59B6).copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color(0xFF9B59B6),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Información del Reporte",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "🆔 ${incident.id}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "👤 Usuario ${incident.usuarioId.take(8)}...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                        )
                    }
                }
            }
        }
        
        // Acciones
        item {
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(700)
                visible = true
            }
            
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically { it / 2 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ModernButton(
                        text = "Reportar",
                        onClick = { /* TODO: Reportar problema */ },
                        variant = ButtonVariant.Secondary,
                        icon = Icons.Default.Flag,
                        modifier = Modifier.weight(1f)
                    )
                    
                    ModernButton(
                        text = "Seguir",
                        onClick = { /* TODO: Seguir incidente */ },
                        variant = ButtonVariant.Primary,
                        icon = Icons.Default.Bookmark,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
        
        // Spacer final
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun getTimeAgo(date: Date): String {
    val now = Date()
    val diffInMillis = now.time - date.time
    
    return when {
        diffInMillis < 60 * 1000 -> "menos de un minuto"
        diffInMillis < 60 * 60 * 1000 -> "${diffInMillis / (60 * 1000)} minutos"
        diffInMillis < 24 * 60 * 60 * 1000 -> "${diffInMillis / (60 * 60 * 1000)} horas"
        diffInMillis < 7 * 24 * 60 * 60 * 1000 -> "${diffInMillis / (24 * 60 * 60 * 1000)} días"
        else -> "más de una semana"
    }
}
