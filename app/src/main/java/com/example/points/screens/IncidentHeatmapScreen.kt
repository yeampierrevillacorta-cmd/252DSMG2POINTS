package com.example.points.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MyLocation
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
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import com.example.points.models.TipoUsuario
import com.example.points.repository.UserRepository
import com.example.points.utils.MapStyleUtils
import com.example.points.viewmodel.IncidentViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.Gradient
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.TileOverlay
import com.google.android.gms.maps.model.TileOverlayOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.DisposableEffect

/**
 * Pantalla de mapa de calor para incidentes
 * Solo visible para administradores y moderadores
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentHeatmapScreen(
    onBackClick: () -> Unit,
    viewModel: IncidentViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val userRepository = remember { 
        UserRepository(
            FirebaseFirestore.getInstance(),
            FirebaseAuth.getInstance()
        )
    }
    
    // Estado para verificar permisos
    var hasPermission by remember { mutableStateOf(false) }
    var isLoadingPermission by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Verificar si el usuario es admin o moderador
    LaunchedEffect(Unit) {
        try {
            val isAdminOrModerator = userRepository.isCurrentUserAdminOrModerator()
            hasPermission = isAdminOrModerator
            isLoadingPermission = false
            
            if (!isAdminOrModerator) {
                errorMessage = "Solo administradores y moderadores pueden ver el mapa de calor"
            }
        } catch (e: Exception) {
            Log.e("IncidentHeatmap", "Error verificando permisos", e)
            errorMessage = "Error al verificar permisos: ${e.message}"
            isLoadingPermission = false
        }
    }
    
    // Estado para permisos de ubicación (declarado temprano)
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    // Estado para la ubicación inicial del usuario
    var userInitialLocation by remember { mutableStateOf<LatLng?>(null) }
    
    // Cargar incidentes si tiene permisos
    LaunchedEffect(hasPermission) {
        if (hasPermission) {
            viewModel.loadAllIncidents()
            
            // Intentar obtener ubicación del usuario si tiene permisos
            if (hasLocationPermission) {
                try {
                    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            userInitialLocation = LatLng(it.latitude, it.longitude)
                            Log.d("IncidentHeatmap", "Ubicación inicial obtenida: ${it.latitude}, ${it.longitude}")
                        }
                    }
                } catch (e: SecurityException) {
                    Log.e("IncidentHeatmap", "Error obteniendo ubicación inicial", e)
                }
            }
        }
    }
    
    // Estado para el overlay del heatmap y referencia al mapa (declarado temprano para uso en callbacks)
    var heatmapOverlay by remember { mutableStateOf<TileOverlay?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    
    // Launcher para solicitar permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        // La habilitación de ubicación se manejará en el LaunchedEffect
    }
    
    // Preparar datos para el heatmap
    val heatmapPoints = remember(uiState.incidents) {
        if (uiState.incidents.isEmpty()) {
            emptyList()
        } else {
            // Agrupar incidentes por área geográfica y crear puntos de calor
            val points = mutableListOf<LatLng>()
            
            uiState.incidents.forEach { incident ->
                if (incident.ubicacion.lat != 0.0 && incident.ubicacion.lon != 0.0) {
                    // Agregar múltiples puntos cercanos para crear densidad
                    // Esto simula agrupación por área
                    val baseLat = incident.ubicacion.lat
                    val baseLon = incident.ubicacion.lon
                    
                    // Agregar el punto principal
                    points.add(LatLng(baseLat, baseLon))
                    
                    // Agregar puntos cercanos para crear densidad visual
                    // La cantidad de puntos adicionales depende de la densidad deseada
                    repeat(3) {
                        val offsetLat = (Math.random() - 0.5) * 0.001 // ~100m de variación
                        val offsetLon = (Math.random() - 0.5) * 0.001
                        points.add(LatLng(baseLat + offsetLat, baseLon + offsetLon))
                    }
                }
            }
            
            Log.d("IncidentHeatmap", "Puntos de calor generados: ${points.size} de ${uiState.incidents.size} incidentes")
            points
        }
    }
    
    // Configuración del mapa
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
    
    // Calcular posición inicial del mapa basada en los incidentes, ubicación del usuario, o posición por defecto
    val initialCameraPosition = remember(uiState.incidents, userInitialLocation) {
        if (uiState.incidents.isNotEmpty()) {
            val validIncidents = uiState.incidents.filter { 
                it.ubicacion.lat != 0.0 && it.ubicacion.lon != 0.0 
            }
            
            if (validIncidents.isNotEmpty()) {
                val avgLat = validIncidents.map { it.ubicacion.lat }.average()
                val avgLon = validIncidents.map { it.ubicacion.lon }.average()
                CameraPosition.fromLatLngZoom(LatLng(avgLat, avgLon), 12f)
            } else if (userInitialLocation != null) {
                // Si no hay incidentes pero tenemos ubicación del usuario, usar esa
                CameraPosition.fromLatLngZoom(userInitialLocation!!, 12f)
            } else {
                // Si no hay incidentes ni ubicación, usar una posición por defecto (Lima, Perú)
                // Puedes cambiar esto a la ubicación por defecto que prefieras
                CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 10f)
            }
        } else if (userInitialLocation != null) {
            // Si no hay incidentes pero tenemos ubicación del usuario, usar esa
            CameraPosition.fromLatLngZoom(userInitialLocation!!, 12f)
        } else {
            // Si no hay incidentes ni ubicación, usar una posición por defecto
            CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 10f)
        }
    }
    
    var cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }
    
    // Gradiente de colores para el heatmap
    val heatmapGradient = remember {
        intArrayOf(
            android.graphics.Color.argb(0, 255, 0, 0),      // Transparente
            android.graphics.Color.argb(51, 255, 0, 0),     // Rojo claro (20% opacidad)
            android.graphics.Color.argb(102, 255, 0, 0),    // Rojo medio (40% opacidad)
            android.graphics.Color.argb(153, 255, 0, 0),    // Rojo intenso (60% opacidad)
            android.graphics.Color.argb(204, 255, 0, 0)     // Rojo muy intenso (80% opacidad)
        )
    }
    
    // Actualizar ubicación en el mapa cuando se otorguen permisos
    LaunchedEffect(hasLocationPermission, googleMap) {
        googleMap?.let { map ->
            if (hasLocationPermission) {
                try {
                    map.isMyLocationEnabled = true
                    Log.d("IncidentHeatmap", "Ubicación habilitada en el mapa")
                } catch (e: SecurityException) {
                    Log.e("IncidentHeatmap", "Error habilitando ubicación", e)
                }
            } else {
                map.isMyLocationEnabled = false
            }
        }
    }
    
    // Actualizar heatmap y centrar cámara cuando cambien los puntos o se carguen incidentes
    LaunchedEffect(heatmapPoints, googleMap, uiState.incidents) {
        googleMap?.let { map ->
            // Limpiar overlay anterior
            heatmapOverlay?.remove()
            
            // Si hay incidentes válidos, centrar la cámara en ellos
            if (uiState.incidents.isNotEmpty() && !uiState.isLoading) {
                val validIncidents = uiState.incidents.filter { 
                    it.ubicacion.lat != 0.0 && it.ubicacion.lon != 0.0 
                }
                if (validIncidents.isNotEmpty()) {
                    val avgLat = validIncidents.map { it.ubicacion.lat }.average()
                    val avgLon = validIncidents.map { it.ubicacion.lon }.average()
                    val newPosition = CameraPosition.fromLatLngZoom(LatLng(avgLat, avgLon), 12f)
                    
                    map.moveCamera(
                        com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(newPosition)
                    )
                    Log.d("IncidentHeatmap", "Cámara centrada en incidentes: $avgLat, $avgLon")
                }
            }
            
            // Agregar nuevo heatmap si hay puntos
            if (heatmapPoints.isNotEmpty()) {
                try {
                    val gradient = Gradient(
                        heatmapGradient,
                        floatArrayOf(0.0f, 0.2f, 0.4f, 0.6f, 1.0f)
                    )
                    
                    val provider = HeatmapTileProvider.Builder()
                        .data(heatmapPoints)
                        .radius(20)
                        .gradient(gradient)
                        .opacity(0.7)
                        .build()
                    
                    val overlayOptions = TileOverlayOptions()
                        .tileProvider(provider)
                    
                    heatmapOverlay = map.addTileOverlay(overlayOptions)
                    Log.d("IncidentHeatmap", "Heatmap actualizado con ${heatmapPoints.size} puntos")
                } catch (e: Exception) {
                    Log.e("IncidentHeatmap", "Error al actualizar heatmap", e)
                }
            }
        }
    }
    
    // Mostrar pantalla de carga o error
    if (isLoadingPermission) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text("Verificando permisos...")
            }
        }
        return
    }
    
    if (!hasPermission || errorMessage != null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .padding(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Acceso Restringido",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = errorMessage ?: "No tienes permisos para ver esta pantalla",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = onBackClick,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Volver")
                    }
                }
            }
        }
        return
    }
    
    // Pantalla principal con mapa de calor
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Mapa de Calor de Incidentes",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Mapa con heatmap usando MapView para tener acceso directo a GoogleMap
            var mapView by remember { mutableStateOf<MapView?>(null) }
            
            AndroidView(
                factory = { ctx ->
                    MapView(ctx).apply {
                        mapView = this
                        onCreate(null)
                        onResume()
                        getMapAsync { map ->
                            googleMap = map
                            map.uiSettings.isZoomControlsEnabled = true
                            map.uiSettings.isMyLocationButtonEnabled = false
                            map.uiSettings.isMapToolbarEnabled = false
                            map.uiSettings.isCompassEnabled = true
                            map.uiSettings.isRotateGesturesEnabled = true
                            map.uiSettings.isScrollGesturesEnabled = true
                            map.uiSettings.isTiltGesturesEnabled = false
                            map.uiSettings.isZoomGesturesEnabled = true
                            
                            // Aplicar estilo del mapa
                            mapStyle?.let { style ->
                                map.setMapStyle(style)
                            }
                            
                            // Habilitar ubicación si se tienen permisos
                            if (hasLocationPermission) {
                                try {
                                    map.isMyLocationEnabled = true
                                } catch (e: SecurityException) {
                                    Log.e("IncidentHeatmap", "Error habilitando ubicación", e)
                                }
                            }
                            
                            // Configurar cámara inicial basada en incidentes o ubicación
                            val cameraPosition = if (uiState.incidents.isNotEmpty()) {
                                val validIncidents = uiState.incidents.filter { 
                                    it.ubicacion.lat != 0.0 && it.ubicacion.lon != 0.0 
                                }
                                if (validIncidents.isNotEmpty()) {
                                    val avgLat = validIncidents.map { it.ubicacion.lat }.average()
                                    val avgLon = validIncidents.map { it.ubicacion.lon }.average()
                                    CameraPosition.fromLatLngZoom(LatLng(avgLat, avgLon), 12f)
                                } else {
                                    initialCameraPosition
                                }
                            } else {
                                initialCameraPosition
                            }
                            
                            map.moveCamera(
                                com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
                                    cameraPosition
                                )
                            )
                            
                            Log.d("IncidentHeatmap", "Mapa cargado y centrado en: ${cameraPosition.target.latitude}, ${cameraPosition.target.longitude}")
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            // Manejar ciclo de vida del MapView
            DisposableEffect(Unit) {
                onDispose {
                    mapView?.onPause()
                    mapView?.onDestroy()
                }
            }
            
            // Panel de información
            Card(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Estadísticas",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                    
                    Divider()
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Total de incidentes:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${uiState.incidents.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Puntos de calor:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "${heatmapPoints.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "💡 Las áreas más intensas indican mayor concentración de incidentes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Botón flotante para ir a mi ubicación
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        // Obtener ubicación actual y centrar mapa
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val currentPosition = LatLng(it.latitude, it.longitude)
                                    googleMap?.moveCamera(
                                        com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(
                                            currentPosition, 
                                            15f
                                        )
                                    )
                                    Log.d("IncidentHeatmap", "Centrando mapa en ubicación: ${it.latitude}, ${it.longitude}")
                                } ?: run {
                                    Log.w("IncidentHeatmap", "No se pudo obtener la ubicación")
                                }
                            }.addOnFailureListener { e ->
                                Log.e("IncidentHeatmap", "Error al obtener ubicación", e)
                            }
                        } catch (e: SecurityException) {
                            Log.e("IncidentHeatmap", "Error de seguridad al obtener ubicación", e)
                        }
                    } else {
                        // Solicitar permisos si no se tienen
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(56.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Mi ubicación",
                    modifier = Modifier.size(24.dp)
                )
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
        }
    }
}

