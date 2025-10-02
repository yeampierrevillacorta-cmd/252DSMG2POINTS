package com.example.points.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import android.util.Log
import com.example.points.models.TipoIncidente
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.utils.TestDataCreator
import com.example.points.utils.TestUserCreator
import com.example.points.utils.MarkerUtils
import com.example.points.utils.MapStyleUtils
import com.example.points.utils.ShareUtils
import com.example.points.components.ShareOptionsDialog
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsMapScreen(
    onCreateIncidentClick: () -> Unit,
    onIncidentDetailClick: (String) -> Unit,
    viewModel: IncidentViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()
    
    // Log para debug
    LaunchedEffect(uiState.incidents) {
        Log.d("IncidentsMap", "=== DEBUG INCIDENTES ===")
        Log.d("IncidentsMap", "Incidentes cargados: ${uiState.incidents.size}")
        Log.d("IncidentsMap", "Estado de carga: ${uiState.isLoading}")
        Log.d("IncidentsMap", "Error: ${uiState.errorMessage}")
        uiState.incidents.forEach { incident ->
            Log.d("IncidentsMap", "Incidente: ${incident.tipo} en (${incident.ubicacion.lat}, ${incident.ubicacion.lon})")
        }
        Log.d("IncidentsMap", "========================")
    }
    
    // Estado para el mapa
    val mapStyle = remember { MapStyleUtils.getMapStyleWithoutPOI(context) }
    var mapProperties by remember { 
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = false,
                isTrafficEnabled = false,
                isBuildingEnabled = false,
                isIndoorEnabled = false,
                mapType = MapType.NORMAL,
                mapStyleOptions = mapStyle
            )
        ) 
    }
    var cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 12f) // Lima, Perú
    }
    
    // Estado para permisos de ubicación
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Launcher para solicitar permisos
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (hasLocationPermission) {
            mapProperties = mapProperties.copy(isMyLocationEnabled = true)
        }
    }
    
    // Estado para filtros
    var showFilters by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf<TipoIncidente?>(null) }
    var showPOI by remember { mutableStateOf(false) }
    
    // Estado para navegación entre incidentes
    var currentIncidentIndex by remember { mutableStateOf(0) }
    var isNavigating by remember { mutableStateOf(false) }
    var showNavigationPanel by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            mapProperties = mapProperties.copy(
                isMyLocationEnabled = true,
                mapStyleOptions = if (showPOI) null else mapStyle
            )
        }
    }
    
    LaunchedEffect(showPOI) {
        mapProperties = mapProperties.copy(
            mapStyleOptions = if (showPOI) null else mapStyle
        )
    }
    
    // Funciones de navegación
    fun navigateToIncident(index: Int) {
        val incidents = uiState.incidents
        if (incidents.isNotEmpty() && index in incidents.indices) {
            val incident = incidents[index]
            if (incident.ubicacion.lat != 0.0 && incident.ubicacion.lon != 0.0) {
                val position = LatLng(incident.ubicacion.lat, incident.ubicacion.lon)
                coroutineScope.launch {
                    cameraPositionState.animate(
                        CameraUpdateFactory.newLatLngZoom(position, 16f),
                        durationMs = 1000
                    )
                }
                viewModel.selectIncident(incident)
                currentIncidentIndex = index
                Log.d("IncidentsMap", "Navegando a incidente ${index + 1}/${incidents.size}: ${incident.tipo}")
            }
        }
    }
    
    fun navigateToNext() {
        val incidents = uiState.incidents
        if (incidents.isNotEmpty()) {
            val nextIndex = if (currentIncidentIndex >= incidents.size - 1) 0 else currentIncidentIndex + 1
            navigateToIncident(nextIndex)
        }
    }
    
    fun navigateToPrevious() {
        val incidents = uiState.incidents
        if (incidents.isNotEmpty()) {
            val prevIndex = if (currentIncidentIndex <= 0) incidents.size - 1 else currentIncidentIndex - 1
            navigateToIncident(prevIndex)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = false,
                mapToolbarEnabled = false,
                compassEnabled = true,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                tiltGesturesEnabled = false,
                zoomGesturesEnabled = true
            ),
            onMapLoaded = {
                Log.d("IncidentsMap", "Mapa de Google cargado correctamente")
            }
        ) {
            // Marcadores de incidentes con iconos personalizados
            Log.d("IncidentsMap", "Renderizando ${uiState.incidents.size} marcadores")
            uiState.incidents.forEach { incident ->
                if (incident.ubicacion.lat != 0.0 && incident.ubicacion.lon != 0.0) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(incident.ubicacion.lat, incident.ubicacion.lon)
                        ),
                        title = incident.tipo,
                        snippet = "${incident.descripcion.take(50)}${if (incident.descripcion.length > 50) "..." else ""}",
                        icon = MarkerUtils.createCustomMarkerIcon(
                            context = context,
                            tipo = incident.tipo,
                            estado = incident.estado.displayName
                        ),
                        onClick = {
                            viewModel.selectIncident(incident)
                            Log.d("IncidentsMap", "Marcador seleccionado: ${incident.tipo}")
                            true
                        }
                    )
                }
            }
        }
        
        // Panel de debug (temporal)
        Card(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    text = "🐛 DEBUG PANEL",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "📍 Incidentes: ${uiState.incidents.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "🗺️ Mapa: ${if (context != null) "Cargado ✅" else "Error ❌"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "👤 Usuario: ${uiState.currentUser?.tipo?.displayName ?: "Sin autenticar"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                Text(
                    text = "🔐 Admin: ${if (uiState.isUserAdmin) "Sí ✅" else "No ❌"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(12.dp),
                            strokeWidth = 1.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Cargando Firebase...",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                
                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "❌ Error: $error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                if (uiState.incidents.isEmpty() && !uiState.isLoading) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { 
                            Log.d("IncidentsMap", "Creando datos de prueba...")
                            TestDataCreator.createTestIncidents()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "🧪 Crear datos de prueba",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Button(
                        onClick = { 
                            Log.d("IncidentsMap", "Recargando incidentes...")
                            viewModel.loadAllIncidents()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Text(
                            text = "🔄 Recargar datos",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Button(
                        onClick = { 
                            showPOI = !showPOI
                            Log.d("IncidentsMap", "POI ${if (showPOI) "habilitado" else "deshabilitado"}")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showPOI) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline
                        )
                    ) {
                        Text(
                            text = if (showPOI) "🏢 Ocultar POI" else "🏢 Mostrar POI",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Botones para cambiar tipo de usuario (solo para testing)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Button(
                            onClick = { 
                                coroutineScope.launch {
                                    val success = TestUserCreator.makeCurrentUserAdmin()
                                    if (success) {
                                        viewModel.loadCurrentUser() // Recargar usuario
                                        Log.d("IncidentsMap", "Usuario convertido a administrador")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text(
                                text = "👑 Admin",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                        
                        Button(
                            onClick = { 
                                coroutineScope.launch {
                                    val success = TestUserCreator.makeCurrentUserCitizen()
                                    if (success) {
                                        viewModel.loadCurrentUser() // Recargar usuario
                                        Log.d("IncidentsMap", "Usuario convertido a ciudadano")
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text(
                                text = "👤 Ciudadano",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                // Mostrar ubicación de incidentes si los hay
                if (uiState.incidents.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📍 Ubicaciones:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    uiState.incidents.take(3).forEach { incident ->
                        Text(
                            text = "• ${incident.tipo} (${String.format("%.3f", incident.ubicacion.lat)}, ${String.format("%.3f", incident.ubicacion.lon)})",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    if (uiState.incidents.size > 3) {
                        Text(
                            text = "... y ${uiState.incidents.size - 3} más",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
        
        // Panel de navegación entre incidentes
        if (showNavigationPanel && uiState.incidents.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🧭 Navegación",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        IconButton(
                            onClick = { showNavigationPanel = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Incidente ${currentIncidentIndex + 1} de ${uiState.incidents.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Mostrar información del incidente actual
                    if (uiState.incidents.isNotEmpty() && currentIncidentIndex in uiState.incidents.indices) {
                        val currentIncident = uiState.incidents[currentIncidentIndex]
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MarkerUtils.getColorForIncidentType(currentIncident.tipo).copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Icono del incidente actual
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            MarkerUtils.getColorForIncidentType(currentIncident.tipo).copy(alpha = 0.2f),
                                            RoundedCornerShape(12.dp)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = when (currentIncident.tipo) {
                                            "Inseguridad" -> "⚠️"
                                            "Accidente de Tránsito" -> "🚗"
                                            "Incendio" -> "🔥"
                                            "Inundación" -> "💧"
                                            "Vandalismo" -> "⚡"
                                            "Servicio Público" -> "🔧"
                                            else -> "📍"
                                        },
                                        fontSize = 12.sp
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentIncident.tipo,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MarkerUtils.getColorForIncidentType(currentIncident.tipo)
                                    )
                                    Text(
                                        text = currentIncident.descripcion.take(30) + if (currentIncident.descripcion.length > 30) "..." else "",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Controles de navegación
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { navigateToPrevious() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("⬅️ Anterior")
                        }
                        
                        Button(
                            onClick = { navigateToNext() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Siguiente ➡️")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón para ir al primer incidente
                    OutlinedButton(
                        onClick = { 
                            navigateToIncident(0)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎯 Ir al primer incidente")
                    }
                }
            }
        }
        
        // Filtros en la parte superior
        if (showFilters) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtrar por tipo de incidente",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = {
                                    selectedFilter = null
                                    viewModel.filterByType(null)
                                },
                                label = { Text("Todos") },
                                selected = selectedFilter == null
                            )
                        }
                        
                        items(TipoIncidente.values()) { tipo ->
                            FilterChip(
                                onClick = {
                                    selectedFilter = tipo
                                    viewModel.filterByType(tipo)
                                },
                                label = { Text(tipo.displayName) },
                                selected = selectedFilter == tipo
                            )
                        }
                    }
                }
            }
        }
        
        // Botones flotantes
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón de navegación entre incidentes
            if (uiState.incidents.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showNavigationPanel = !showNavigationPanel },
                    containerColor = if (showNavigationPanel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navegar entre incidentes"
                    )
                }
            }
            
            // Botón de filtros
            FloatingActionButton(
                onClick = { showFilters = !showFilters },
                containerColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = "Filtros"
                )
            }
            
            // Botón de mi ubicación
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        // Obtener ubicación actual y centrar mapa
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val currentPosition = LatLng(it.latitude, it.longitude)
                                    cameraPositionState.move(
                                        CameraUpdateFactory.newLatLngZoom(currentPosition, 15f)
                                    )
                                }
                            }
                        } catch (e: SecurityException) {
                            // Manejar excepción de seguridad
                        }
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Mi ubicación"
                )
            }
            
            // Botón para crear incidente
            FloatingActionButton(
                onClick = onCreateIncidentClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Reportar incidente"
                )
            }
        }
        
        // Información del incidente seleccionado
        uiState.selectedIncident?.let { incident ->
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth(0.85f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Header con tipo e icono
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
                            
                            Text(
                                text = incident.tipo,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MarkerUtils.getColorForIncidentType(incident.tipo)
                            )
                        }
                        
                        IconButton(
                            onClick = { viewModel.selectIncident(null) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Cerrar",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Descripción
                    Text(
                        text = incident.descripcion.take(80) + if (incident.descripcion.length > 80) "..." else "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Estado y botones
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Estado (solo visible para administradores)
                        if (uiState.isUserAdmin) {
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
                        } else {
                            // Spacer para mantener el layout cuando no es admin
                            Spacer(modifier = Modifier.width(1.dp))
                        }
                        
                        // Botones de acción
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón Compartir
                            OutlinedButton(
                                onClick = { 
                                    showShareDialog = true
                                },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Share,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Compartir",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            
                            // Botón Ver detalles
                            Button(
                                onClick = { 
                                    onIncidentDetailClick(incident.id)
                                    viewModel.selectIncident(null)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(
                                    Icons.Default.Visibility,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Ver detalles",
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Indicador de carga
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Diálogo de compartir
        if (showShareDialog && uiState.selectedIncident != null) {
            ShareOptionsDialog(
                incident = uiState.selectedIncident!!,
                onDismiss = { showShareDialog = false }
            )
        }
    }
}
