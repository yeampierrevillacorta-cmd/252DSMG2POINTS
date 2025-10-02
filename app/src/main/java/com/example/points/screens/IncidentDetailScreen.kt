package com.example.points.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.points.components.ShareOptionsDialog
import com.example.points.models.Incident
import com.example.points.utils.MarkerUtils
import com.example.points.utils.ShareUtils
import com.example.points.viewmodel.IncidentViewModel
import java.text.SimpleDateFormat
import java.util.*

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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Incidente") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { 
                        if (incident != null) {
                            showShareDialog = true
                        }
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Compartir")
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                }
            )
        }
    ) { paddingValues ->
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Cargando detalles...")
                    }
                }
            }
            
            error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Error al cargar el incidente",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = error ?: "Error desconocido",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onBackClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Volver")
                            }
                        }
                    }
                }
            }
            
            incident != null -> {
                IncidentDetailContent(
                    incident = incident!!,
                    isUserAdmin = uiState.isUserAdmin,
                    modifier = Modifier.padding(paddingValues)
                )
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
        // Encabezado con tipo e icono
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MarkerUtils.getColorForIncidentType(incident.tipo).copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icono del tipo de incidente
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .background(
                                MarkerUtils.getColorForIncidentType(incident.tipo),
                                RoundedCornerShape(30.dp)
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
                            fontSize = 24.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = incident.tipo,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MarkerUtils.getColorForIncidentType(incident.tipo)
                        )
                        
                        // Estado (solo visible para administradores)
                        if (isUserAdmin) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MarkerUtils.getColorForIncidentStatus(incident.estado.displayName).copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = incident.estado.displayName,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MarkerUtils.getColorForIncidentStatus(incident.estado.displayName),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Descripción
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = incident.descripcion.ifEmpty { "Sin descripción disponible" },
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 24.sp
                    )
                }
            }
        }
        
        // Imagen si existe
        incident.fotoUrl?.let { imageUrl ->
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Evidencia Fotográfica",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Image(
                            painter = rememberAsyncImagePainter(imageUrl),
                            contentDescription = "Foto del incidente",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }
        
        // Información de ubicación
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ubicación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (incident.ubicacion.direccion.isNotEmpty()) {
                        Text(
                            text = incident.ubicacion.direccion,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    
                    Text(
                        text = "Coordenadas: ${String.format("%.6f", incident.ubicacion.lat)}, ${String.format("%.6f", incident.ubicacion.lon)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = { 
                            ShareUtils.openInGoogleMaps(context, incident)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Ver en Google Maps")
                    }
                }
            }
        }
        
        // Información temporal
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Información Temporal",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    val reportDate = incident.fechaHora.toDate()
                    
                    Text(
                        text = "Reportado: ${dateFormat.format(reportDate)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    
                    val timeAgo = getTimeAgo(reportDate)
                    Text(
                        text = "Hace $timeAgo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Información del reportero
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Información del Reporte",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "ID del incidente: ${incident.id}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Reportado por: Usuario ${incident.usuarioId.take(8)}...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        // Acciones
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { /* TODO: Reportar problema */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reportar")
                }
                
                Button(
                    onClick = { /* TODO: Seguir incidente */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Bookmark, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seguir")
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
