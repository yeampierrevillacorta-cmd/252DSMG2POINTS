package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.models.Notification
import com.example.points.models.TipoNotificacion
import com.example.points.viewmodel.AlertsViewModel
import com.example.points.ui.components.ModernCard
import com.example.points.ui.components.ModernButton
import com.example.points.ui.components.ButtonVariant
import com.example.points.ui.components.AnimatedBackground
import com.example.points.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(
    navController: NavController? = null
) {
    val context = LocalContext.current
    val viewModel: AlertsViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AlertsViewModel(context) as T
            }
        }
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var showSettingsDialog by remember { mutableStateOf(false) }
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Fondo animado
        AnimatedBackground()
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header moderno con gradiente
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    withGradient = true,
                    gradientColors = listOf(
                        Color(0xFFFF6B9D),
                        Color(0xFFFF8E9B)
                    ),
                    elevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "🔔 Alertas y Notificaciones",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${uiState.notifications.size} notificaciones",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        IconButton(
                            onClick = { showSettingsDialog = true }
                        ) {
                            Icon(
                                Icons.Default.Settings,
                                contentDescription = "Configurar alertas",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
            
            // Filtros
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInHorizontally(initialOffsetX = { 100 })
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    elevation = 4.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Filtro por tipo
                        FilterChip(
                            selected = uiState.selectedFilter == null,
                            onClick = { viewModel.setFilter(null) },
                            label = { Text("Todos") }
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == TipoNotificacion.INCIDENTE,
                            onClick = { viewModel.setFilter(TipoNotificacion.INCIDENTE) },
                            label = { Text("Incidentes") }
                        )
                        FilterChip(
                            selected = uiState.selectedFilter == TipoNotificacion.EVENTO,
                            onClick = { viewModel.setFilter(TipoNotificacion.EVENTO) },
                            label = { Text("Eventos") }
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Toggle solo no leídas
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Solo no leídas",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Switch(
                                checked = uiState.showOnlyUnread,
                                onCheckedChange = { viewModel.toggleShowOnlyUnread() }
                            )
                        }
                    }
                }
            }
            
            // Lista de notificaciones
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PointsPrimary)
                    }
                }
                
                uiState.notifications.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.NotificationsOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "No hay notificaciones",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = if (uiState.alertsEnabled) {
                                    "Las alertas están activas. Recibirás notificaciones cuando haya incidentes o eventos cercanos."
                                } else {
                                    "⚠️ IMPORTANTE: Para recibir notificaciones, debes activar las alertas.\n\n" +
                                    "1. Toca el botón de configuración (⚙️) arriba\n" +
                                    "2. Configura el radio de búsqueda\n" +
                                    "3. Selecciona los tipos de alertas (Incidentes/Eventos)\n" +
                                    "4. Presiona 'Activar'\n\n" +
                                    "También necesitas:\n" +
                                    "• Permisos de ubicación concedidos\n" +
                                    "• Permisos de notificaciones (Android 13+)\n" +
                                    "• GPS activado"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 32.dp)
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.notifications,
                            key = { it.id }
                        ) { notification ->
                            var itemVisible by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(Unit) {
                                delay(100)
                                itemVisible = true
                            }
                            
                            AnimatedVisibility(
                                visible = itemVisible,
                                enter = fadeIn() + slideInHorizontally(initialOffsetX = { 100 })
                            ) {
                                NotificationCard(
                                    notification = notification,
                                    onRead = { viewModel.markAsRead(notification.id) },
                                    onDelete = { viewModel.deleteNotification(notification.id) },
                                    onClick = {
                                        viewModel.markAsRead(notification.id)
                                        // Navegar al detalle según el tipo
                                        when {
                                            notification.incidenteId != null && navController != null -> {
                                                navController.navigate("${com.example.points.constants.AppRoutes.INCIDENT_DETAIL}/${notification.incidenteId}")
                                            }
                                            notification.eventoId != null && navController != null -> {
                                                navController.navigate("${com.example.points.constants.AppRoutes.EVENT_DETAIL}/${notification.eventoId}")
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        
                        // Botón para marcar todas como leídas
                        if (uiState.notifications.any { !it.leida }) {
                            item {
                                OutlinedButton(
                                    onClick = { viewModel.markAllAsRead() },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        Icons.Default.DoneAll,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Marcar todas como leídas")
                                }
                            }
                        }
                        
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }
            }
        }
        
        // Dialog de configuración
        if (showSettingsDialog) {
            AlertSettingsDialog(
                uiState = uiState,
                onDismiss = { showSettingsDialog = false },
                onEnableAlerts = { radiusKm, enableIncidents, enableEvents ->
                    viewModel.enableAlerts(radiusKm, enableIncidents, enableEvents)
                    showSettingsDialog = false
                },
                onDisableAlerts = {
                    viewModel.disableAlerts()
                    showSettingsDialog = false
                },
                onCheckNow = {
                    viewModel.checkAlertsNow()
                }
            )
        }
    }
}

@Composable
fun NotificationCard(
    notification: Notification,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val icon = when (notification.tipo) {
        TipoNotificacion.INCIDENTE -> Icons.Default.Report
        TipoNotificacion.EVENTO -> Icons.Default.Event
        TipoNotificacion.SISTEMA -> Icons.Default.Info
        TipoNotificacion.CONFIRMACION -> Icons.Default.CheckCircle
    }
    
    val iconColor = when (notification.tipo) {
        TipoNotificacion.INCIDENTE -> PointsError
        TipoNotificacion.EVENTO -> PointsAccent
        TipoNotificacion.SISTEMA -> PointsPrimary
        TipoNotificacion.CONFIRMACION -> PointsSuccess
    }
    
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = if (notification.leida) 2.dp else 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        iconColor.copy(alpha = 0.15f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Contenido
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = notification.mensaje,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = if (notification.leida) FontWeight.Normal else FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (!notification.leida) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(PointsPrimary, CircleShape)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = dateFormat.format(notification.fechaHora.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Botón eliminar
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun AlertSettingsDialog(
    uiState: com.example.points.viewmodel.AlertsUiState,
    onDismiss: () -> Unit,
    onEnableAlerts: (Double, Boolean, Boolean) -> Unit,
    onDisableAlerts: () -> Unit,
    onCheckNow: () -> Unit
) {
    var radiusKm by remember { mutableStateOf(uiState.radiusKm) }
    var enableIncidents by remember { mutableStateOf(uiState.enableIncidents) }
    var enableEvents by remember { mutableStateOf(uiState.enableEvents) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Configurar Alertas",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Radio de búsqueda
                Column {
                    Text(
                        text = "Radio de búsqueda: ${String.format("%.1f", radiusKm)} km",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Slider(
                        value = radiusKm.toFloat(),
                        onValueChange = { radiusKm = it.toDouble() },
                        valueRange = 1f..50f,
                        steps = 49
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("1 km", style = MaterialTheme.typography.bodySmall)
                        Text("50 km", style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                HorizontalDivider()
                
                // Tipos de alertas
                Text(
                    text = "Tipos de alertas:",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = enableIncidents,
                            onCheckedChange = { enableIncidents = it }
                        )
                        Text("Incidentes")
                    }
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = enableEvents,
                            onCheckedChange = { enableEvents = it }
                        )
                        Text("Eventos")
                    }
                }
                
                HorizontalDivider()
                
                // Estado actual
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Estado:",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    Text(
                        text = if (uiState.alertsEnabled) "✅ Activado" else "❌ Desactivado",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (uiState.alertsEnabled) PointsSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.alertsEnabled) {
                    TextButton(onClick = onDisableAlerts) {
                        Text("Desactivar")
                    }
                }
                OutlinedButton(
                    onClick = onCheckNow
                ) {
                    Text("Verificar ahora")
                }
                Button(
                    onClick = {
                        onEnableAlerts(radiusKm, enableIncidents, enableEvents)
                    }
                ) {
                    Text(if (uiState.alertsEnabled) "Actualizar" else "Activar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

