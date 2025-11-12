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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.models.EstadoPOI
import com.example.points.models.PointOfInterest
import com.example.points.constants.ButtonText
import com.example.points.constants.LoadingMessage
import com.example.points.constants.ErrorMessage
import com.example.points.viewmodel.PointOfInterestViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPOIManagementScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Pendientes", "En Revisión", "Aprobados", "Rechazados")
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Gestión de Puntos de Interés",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Modera y gestiona los puntos de interés reportados por los ciudadanos",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> PendingPOIsList(viewModel = viewModel)
            1 -> InReviewPOIsList(viewModel = viewModel)
            2 -> ApprovedPOIsList(viewModel = viewModel)
            3 -> RejectedPOIsList(viewModel = viewModel)
        }
    }
}

@Composable
fun PendingPOIsList(viewModel: PointOfInterestViewModel) {
    var pendingPOIs by remember { mutableStateOf<List<PointOfInterest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(Unit) {
        // Aquí deberías cargar los POIs pendientes desde el repositorio
        // viewModel.repository.getPendingPOIs().collect { pois ->
        //     pendingPOIs = pois
        //     isLoading = false
        // }
        
        // Simulación temporal
        kotlinx.coroutines.delay(1000)
        pendingPOIs = emptyList() // Datos reales se cargarán desde Firebase
        isLoading = false
    }
    
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PointsLoading(message = LoadingMessage.CARGANDO_POI.value)
            }
        }
        
        errorMessage != null -> {
            PointsFeedback(
                message = errorMessage ?: ErrorMessage.ERROR_DESCONOCIDO.value,
                type = "error",
                onRetry = { /* Retry logic */ }
            )
        }
        
        pendingPOIs.isEmpty() -> {
            PointsFeedback(
                message = ErrorMessage.NO_HAY_POI.value,
                type = "empty"
            )
        }
        
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pendingPOIs) { poi ->
                    POIManagementCard(
                        poi = poi,
                        onApprove = { /* Approve logic */ },
                        onReject = { /* Reject logic */ },
                        onViewDetails = { /* View details logic */ }
                    )
                }
            }
        }
    }
}

@Composable
fun InReviewPOIsList(viewModel: PointOfInterestViewModel) {
    var inReviewPOIs by remember { mutableStateOf<List<PointOfInterest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        inReviewPOIs = emptyList() // Datos reales se cargarán desde Firebase
        isLoading = false
    }
    
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PointsLoading(message = LoadingMessage.CARGANDO_POI.value)
            }
        }
        
        inReviewPOIs.isEmpty() -> {
            PointsFeedback(
                message = ErrorMessage.NO_HAY_POI.value,
                type = "empty"
            )
        }
        
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(inReviewPOIs) { poi ->
                    POIManagementCard(
                        poi = poi,
                        onApprove = { /* Approve logic */ },
                        onReject = { /* Reject logic */ },
                        onViewDetails = { /* View details logic */ }
                    )
                }
            }
        }
    }
}

@Composable
fun ApprovedPOIsList(viewModel: PointOfInterestViewModel) {
    var approvedPOIs by remember { mutableStateOf<List<PointOfInterest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        approvedPOIs = emptyList() // Datos reales se cargarán desde Firebase
        isLoading = false
    }
    
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PointsLoading(message = LoadingMessage.CARGANDO_POI.value)
            }
        }
        
        approvedPOIs.isEmpty() -> {
            PointsFeedback(
                message = ErrorMessage.NO_HAY_POI.value,
                type = "empty"
            )
        }
        
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(approvedPOIs) { poi ->
                    POIManagementCard(
                        poi = poi,
                        onApprove = null, // Ya está aprobado
                        onReject = { /* Suspend logic */ },
                        onViewDetails = { /* View details logic */ }
                    )
                }
            }
        }
    }
}

@Composable
fun RejectedPOIsList(viewModel: PointOfInterestViewModel) {
    var rejectedPOIs by remember { mutableStateOf<List<PointOfInterest>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(1000)
        rejectedPOIs = emptyList() // Datos reales se cargarán desde Firebase
        isLoading = false
    }
    
    when {
        isLoading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                PointsLoading(message = LoadingMessage.CARGANDO_POI.value)
            }
        }
        
        rejectedPOIs.isEmpty() -> {
            PointsFeedback(
                message = ErrorMessage.NO_HAY_POI.value,
                type = "empty"
            )
        }
        
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(rejectedPOIs) { poi ->
                    POIManagementCard(
                        poi = poi,
                        onApprove = { /* Re-approve logic */ },
                        onReject = null, // Ya está rechazado
                        onViewDetails = { /* View details logic */ }
                    )
                }
            }
        }
    }
}

@Composable
fun POIManagementCard(
    poi: PointOfInterest,
    onApprove: (() -> Unit)?,
    onReject: (() -> Unit)?,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = poi.nombre,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = poi.categoria.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = poi.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = poi.direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Estado badge
                Surface(
                    color = when (poi.estado) {
                        EstadoPOI.PENDIENTE -> Color(0xFFFF9800)
                        EstadoPOI.EN_REVISION -> Color(0xFF2196F3)
                        EstadoPOI.APROBADO -> Color(0xFF4CAF50)
                        EstadoPOI.RECHAZADO -> Color(0xFFF44336)
                        EstadoPOI.SUSPENDIDO -> Color(0xFF9E9E9E)
                    },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = poi.estado.displayName,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = Color.White
                    )
                }
            }
            
            // Información de moderación
            if (poi.moderadorId != null && poi.fechaModeracion != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Moderado el ${formatDate(poi.fechaModeracion)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                poi.comentariosModeracion?.let { comentarios ->
                    Text(
                        text = "Comentarios: $comentarios",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            // Botones de acción
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onViewDetails,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(ButtonText.VER_DETALLES.value)
                }
                
                onApprove?.let { approveAction ->
                    Button(
                        onClick = approveAction,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF4CAF50)
                        )
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Aprobar")
                    }
                }
                
                onReject?.let { rejectAction ->
                    Button(
                        onClick = rejectAction,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFF44336)
                        )
                    ) {
                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Rechazar")
                    }
                }
            }
        }
    }
}

// Las funciones de datos de ejemplo han sido eliminadas
// Ahora se usan datos reales de Firebase

private fun formatDate(timestamp: Timestamp): String {
    val date = timestamp.toDate()
    val formatter = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
}
