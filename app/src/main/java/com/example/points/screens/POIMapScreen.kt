package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.example.points.models.CategoriaPOI
import com.example.points.models.PointOfInterest
import com.example.points.viewmodel.PointOfInterestViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.components.POIShareOptionsDialog
import com.example.points.services.LocationService
import com.example.points.constants.ButtonText
import com.example.points.constants.AppRoutes
import com.example.points.utils.getCategoryIcon
import com.example.points.utils.MapStyleUtils
import com.example.points.utils.POIIconUtils
import com.example.points.utils.IconToBitmapUtils
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.maps.android.compose.*
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.math.atan2
import kotlin.math.sin
import kotlin.math.cos
import kotlin.math.tan
import kotlin.math.asin
import kotlin.math.acos
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIMapScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(),
    targetLat: Double? = null,
    targetLon: Double? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    
    // Location service
    val locationService = remember { LocationService(context) }
    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    
    // Estado para el mapa con estilo sin POIs de Google
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
    
    var cameraPosition by remember {
        mutableStateOf(
            CameraPositionState(
                position = CameraPosition.fromLatLngZoom(
                    if (targetLat != null && targetLon != null) {
                        LatLng(targetLat, targetLon) // Usar coordenadas objetivo si están disponibles
                    } else {
                        LatLng(0.0, 0.0) // Posición neutral hasta obtener ubicación real
                    },
                    if (targetLat != null && targetLon != null) 16f else 2f
                )
            )
        )
    }
    
    // Filtros
    var selectedCategory by remember { mutableStateOf<CategoriaPOI?>(null) }
    var searchRadius by remember { mutableStateOf(5.0) } // km
    var showAllPOIs by remember { mutableStateOf(true) } // Mostrar todos los POIs por defecto
    var showFilters by remember { mutableStateOf(false) }
    
    // Estado para mensajes
    var showLocationMessage by remember { mutableStateOf(false) }
    var locationMessage by remember { mutableStateOf("") }
    var isLocationSuccess by remember { mutableStateOf(false) }
    
    // Estado para log
    var showLog by remember { mutableStateOf(false) }
    var logMessages by remember { mutableStateOf(listOf<String>()) }
    
    // Estado para navegación entre POIs
    var currentPOIIndex by remember { mutableStateOf(0) }
    var isNavigating by remember { mutableStateOf(false) }
    var showNavigationPanel by remember { mutableStateOf(false) }
    
    // Estado para detalles del POI
    var selectedPOI by remember { mutableStateOf<PointOfInterest?>(null) }
    var showPOIDetails by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    
    // Coroutine scope para animaciones
    val coroutineScope = rememberCoroutineScope()
    
    // Función para añadir mensaje al log
    fun addLogMessage(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        logMessages = logMessages + "[$timestamp] $message"
        if (logMessages.size > 10) {
            logMessages = logMessages.drop(1) // Mantener solo los últimos 10 mensajes
        }
    }
    
    // Manejar permisos de ubicación
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            mapProperties = mapProperties.copy(isMyLocationEnabled = true)
        }
    }
    
    // Obtener ubicación del usuario
    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
            try {
                // Intentar obtener ubicación con timeout
                val locationState = locationService.getCurrentLocation()
                if (locationState.latitude != null && locationState.longitude != null) {
                    userLocation = LatLng(locationState.latitude!!, locationState.longitude!!)
                    addLogMessage("Ubicación obtenida: ${String.format("%.4f", locationState.latitude!!)}, ${String.format("%.4f", locationState.longitude!!)}")
                    cameraPosition = CameraPositionState(
                        position = CameraPosition.fromLatLngZoom(
                            LatLng(locationState.latitude!!, locationState.longitude!!),
                            14f
                        )
                    )
                } else {
                    // Si no hay ubicación, mostrar mensaje y mantener la cámara en una posición neutral
                    userLocation = null
                    addLogMessage("Ubicación no disponible. Activa el GPS para ver tu ubicación actual.")
                    // No establecer una ubicación por defecto específica
                }
            } catch (e: Exception) {
                // En caso de error, mostrar mensaje sin ubicación por defecto
                userLocation = null
                addLogMessage("Error al obtener ubicación: ${e.message}")
                // No establecer una ubicación por defecto específica
            }
        } else {
            // Si no hay permisos, mostrar mensaje sin ubicación por defecto
            userLocation = null
            addLogMessage("Permisos de ubicación no otorgados. Otorga permisos para ver tu ubicación actual.")
            // No establecer una ubicación por defecto específica
        }
    }
    
    // Log para debug de POIs
    LaunchedEffect(uiState.pois) {
        addLogMessage("=== DEBUG POIs ===")
        addLogMessage("POIs cargados: ${uiState.pois.size}")
        addLogMessage("Estado de carga: ${uiState.isLoading}")
        addLogMessage("Error: ${uiState.errorMessage}")
        uiState.pois.forEach { poi ->
            addLogMessage("POI: ${poi.nombre} (${poi.categoria.displayName}) - Estado: ${poi.estado.displayName} - Coord: (${poi.ubicacion.lat}, ${poi.ubicacion.lon})")
        }
        addLogMessage("==================")
    }
    
    // Cargar POIs
    LaunchedEffect(Unit) {
        addLogMessage("Cargando POIs...")
        viewModel.loadAllPOIs()
    }
    
    // Filtrar POIs basado en los filtros seleccionados - Optimizado
    val filteredPOIs = remember(uiState.pois, selectedCategory, searchRadius, userLocation, showAllPOIs) {
        if (uiState.pois.isEmpty()) {
            emptyList()
        } else {
            var filtered = uiState.pois
            
            // Filtrar por categoría
            selectedCategory?.let { category ->
                filtered = filtered.filter { it.categoria == category }
            }
            
            // Filtrar por radio de búsqueda solo si no se muestran todos los POIs
            if (!showAllPOIs && userLocation != null) {
                val currentUserLocation = userLocation!!
                filtered = filtered.filter { poi ->
                    val distance = calculateDistance(
                        currentUserLocation.latitude, currentUserLocation.longitude,
                        poi.ubicacion.lat, poi.ubicacion.lon
                    )
                    distance <= searchRadius
                }
            }
            
            filtered
        }
    }
    
    // Log solo cuando cambian los POIs filtrados
    LaunchedEffect(filteredPOIs.size) {
        addLogMessage("POIs filtrados: ${filteredPOIs.size} puntos de interés")
        addLogMessage("Categoría seleccionada: ${selectedCategory?.displayName ?: "Todas"}")
        addLogMessage("Mostrar todos los POIs: $showAllPOIs")
        addLogMessage("Radio de búsqueda: $searchRadius km")
        if (filteredPOIs.isNotEmpty()) {
            filteredPOIs.forEach { poi ->
                addLogMessage("POI filtrado: ${poi.nombre} - Coord: (${poi.ubicacion.lat}, ${poi.ubicacion.lon})")
            }
        }
    }
    
    // Función para encontrar el POI más cercano
    fun findNearestPOI(): PointOfInterest? {
        if (userLocation == null || filteredPOIs.isEmpty()) return null
        
        val currentUserLocation = userLocation!!
        return filteredPOIs.minByOrNull { poi ->
            calculateDistance(
                currentUserLocation.latitude, currentUserLocation.longitude,
                poi.ubicacion.lat, poi.ubicacion.lon
            )
        }
    }
    
    // Funciones de navegación entre POIs
    fun navigateToPOI(index: Int) {
        val pois = filteredPOIs
        if (pois.isNotEmpty() && index in pois.indices) {
            val poi = pois[index]
            if (poi.ubicacion.lat != 0.0 && poi.ubicacion.lon != 0.0) {
                val position = LatLng(poi.ubicacion.lat, poi.ubicacion.lon)
                coroutineScope.launch {
                    cameraPosition.animate(
                        CameraUpdateFactory.newLatLngZoom(position, 16f),
                        durationMs = 1000
                    )
                }
                currentPOIIndex = index
                addLogMessage("Navegando a POI ${index + 1}/${pois.size}: ${poi.nombre}")
            }
        }
    }
    
    fun navigateToNextPOI() {
        val pois = filteredPOIs
        if (pois.isNotEmpty()) {
            val nextIndex = if (currentPOIIndex >= pois.size - 1) 0 else currentPOIIndex + 1
            navigateToPOI(nextIndex)
        }
    }
    
    fun navigateToPreviousPOI() {
        val pois = filteredPOIs
        if (pois.isNotEmpty()) {
            val prevIndex = if (currentPOIIndex <= 0) pois.size - 1 else currentPOIIndex - 1
            navigateToPOI(prevIndex)
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Mapa
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPosition,
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
            )
        ) {
            // Marcador de ubicación del usuario
            userLocation?.let { location ->
                Marker(
                    state = MarkerState(position = location),
                    title = "Mi ubicación",
                    snippet = "Tu ubicación actual",
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)
                )
            }
            
            // Marcadores de POIs - Optimizado para evitar renderizaciones constantes
            val stablePOIs = remember(filteredPOIs) { filteredPOIs }
            
            LaunchedEffect(stablePOIs) {
                addLogMessage("=== RENDERIZANDO MARCADORES ===")
                addLogMessage("POIs a renderizar: ${stablePOIs.size}")
                stablePOIs.forEach { poi ->
                    addLogMessage("POI: ${poi.nombre} - Coord: (${poi.ubicacion.lat}, ${poi.ubicacion.lon}) - Categoría: ${poi.categoria.displayName}")
                }
                addLogMessage("==============================")
            }
            
            stablePOIs.forEach { poi ->
                if (poi.ubicacion.lat != 0.0 && poi.ubicacion.lon != 0.0) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(poi.ubicacion.lat, poi.ubicacion.lon)
                        ),
                        title = poi.nombre,
                        snippet = "${poi.categoria.displayName} • ${poi.direccion}\n\nToca para ver detalles",
                        icon = IconToBitmapUtils.getPOIBitmapDescriptor(context, poi.categoria),
                        onClick = {
                            // Abrir diálogo de detalles directamente
                            addLogMessage("=== CLICK DETECTADO EN MARCADOR ===")
                            addLogMessage("POI clickeado: ${poi.nombre}")
                            addLogMessage("Estado ANTES: showPOIDetails=$showPOIDetails, selectedPOI=${selectedPOI?.nombre}")
                            addLogMessage("Abriendo diálogo de detalles...")
                            selectedPOI = poi
                            showPOIDetails = true
                            addLogMessage("Estado DESPUÉS: showPOIDetails=$showPOIDetails, selectedPOI=${selectedPOI?.nombre}")
                            addLogMessage("======================")
                            // Retornar true para consumir el evento y no mostrar info window
                            true
                        }
                    )
                } else {
                    addLogMessage("POI con coordenadas inválidas: ${poi.nombre} (${poi.ubicacion.lat}, ${poi.ubicacion.lon})")
                }
            }
        }
        
        // Botones flotantes reorganizados en la parte inferior derecha
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Botón de navegación entre POIs
            if (filteredPOIs.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { showNavigationPanel = !showNavigationPanel },
                    containerColor = if (showNavigationPanel) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navegar entre POIs"
                    )
                }
            }
            
            // Botón de filtros
            FloatingActionButton(
                onClick = { showFilters = !showFilters },
                containerColor = if (showFilters) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                    contentDescription = if (showFilters) "Cerrar filtros" else "Filtros"
                )
            }
            
            // Botón de zoom in
            FloatingActionButton(
                onClick = {
                    val currentZoom = cameraPosition.position.zoom
                    val newZoom = (currentZoom + 1).coerceAtMost(20f)
                    addLogMessage("Zoom in: ${String.format("%.1f", currentZoom)}x → ${String.format("%.1f", newZoom)}x")
                    cameraPosition = CameraPositionState(
                        position = CameraPosition.fromLatLngZoom(
                            cameraPosition.position.target,
                            newZoom
                        )
                    )
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Acercar",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Botón de zoom out
            FloatingActionButton(
                onClick = {
                    val currentZoom = cameraPosition.position.zoom
                    val newZoom = (currentZoom - 1).coerceAtLeast(1f)
                    addLogMessage("Zoom out: ${String.format("%.1f", currentZoom)}x → ${String.format("%.1f", newZoom)}x")
                    cameraPosition = CameraPositionState(
                        position = CameraPosition.fromLatLngZoom(
                            cameraPosition.position.target,
                            newZoom
                        )
                    )
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Alejar",
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // Botón para saltar al POI más cercano
            FloatingActionButton(
                onClick = {
                    val nearestPOI = findNearestPOI()
                    if (nearestPOI != null && userLocation != null) {
                        val currentUserLocation = userLocation!!
                        val distance = calculateDistance(
                            currentUserLocation.latitude, currentUserLocation.longitude,
                            nearestPOI.ubicacion.lat, nearestPOI.ubicacion.lon
                        )
                        addLogMessage("Saltando a POI más cercano: ${nearestPOI.nombre} (${String.format("%.1f", distance)} km)")
                        cameraPosition = CameraPositionState(
                            position = CameraPosition.fromLatLngZoom(
                                LatLng(nearestPOI.ubicacion.lat, nearestPOI.ubicacion.lon),
                                16f
                            )
                        )
                    } else {
                        addLogMessage("No se encontró POI cercano")
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = "POI más cercano"
                )
            }
            
            // Botón de configuración de ubicación
            FloatingActionButton(
                onClick = {
                    // Abrir configuración de ubicación del sistema
                    try {
                        val intent = android.content.Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        addLogMessage("Error al abrir configuración de ubicación: ${e.message}")
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurar ubicación"
                )
            }
            
            // Botón de centrar en mi ubicación
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        try {
                            if (userLocation != null) {
                                // Centrar en ubicación del usuario si está disponible
                                cameraPosition = CameraPositionState(
                                    position = CameraPosition.fromLatLngZoom(userLocation!!, 15f)
                                )
                            } else {
                                // Si no hay ubicación, intentar obtenerla usando Google Play Services
                                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                                try {
                                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                        location?.let {
                                            val currentPosition = LatLng(it.latitude, it.longitude)
                                            userLocation = currentPosition
                                            cameraPosition = CameraPositionState(
                                                position = CameraPosition.fromLatLngZoom(currentPosition, 15f)
                                            )
                                            addLogMessage("Ubicación obtenida via Google Play Services: ${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)}")
                                        }
                                    }
                                } catch (e: SecurityException) {
                                    addLogMessage("Error de seguridad al obtener ubicación: ${e.message}")
                                }
                            }
                        } catch (e: Exception) {
                            addLogMessage("Error al obtener ubicación: ${e.message}")
                        }
                    } else {
                        // Solicitar permisos de ubicación
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                modifier = Modifier.size(48.dp),
                containerColor = if (hasLocationPermission && userLocation != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.secondary
                }
            ) {
                Icon(
                    imageVector = if (hasLocationPermission && userLocation != null) {
                        Icons.Default.MyLocation
                    } else {
                        Icons.Default.LocationSearching
                    },
                    contentDescription = if (hasLocationPermission && userLocation != null) {
                        "Centrar en mi ubicación"
                    } else {
                        "Buscar mi ubicación"
                    }
                )
            }
        }
        
        // Panel de filtros
        if (showFilters) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtros del Mapa",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtro de categoría
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            FilterChip(
                                onClick = { selectedCategory = null },
                                label = { Text("Todas") },
                                selected = selectedCategory == null,
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Clear,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        
                        items(CategoriaPOI.values()) { category ->
                            FilterChip(
                                onClick = { 
                                    selectedCategory = if (selectedCategory == category) null else category
                                },
                                label = { Text(category.displayName) },
                                selected = selectedCategory == category,
                                leadingIcon = {
                                    Icon(
                                        getCategoryIcon(category.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtro de mostrar todos los POIs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Mostrar todos los POIs",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Switch(
                            checked = showAllPOIs,
                            onCheckedChange = { showAllPOIs = it }
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Filtro de radio de búsqueda (solo si no se muestran todos)
                    if (!showAllPOIs) {
                        Text(
                            text = "Radio de búsqueda: ${searchRadius.roundToInt()} km",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                        Slider(
                            value = searchRadius.toFloat(),
                            onValueChange = { searchRadius = it.toDouble() },
                            valueRange = 1f..20f,
                            steps = 18,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Información de resultados y ubicación
                    Column {
                        Text(
                            text = "${filteredPOIs.size} puntos de interés encontrados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (userLocation != null) {
                                    Icons.Default.LocationOn
                                } else {
                                    Icons.Default.LocationOff
                                },
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (userLocation != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (userLocation != null) {
                                    "Ubicación disponible"
                                } else {
                                    "Ubicación no disponible - Usa el botón de configuración"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = if (userLocation != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Panel de navegación entre POIs
        if (showNavigationPanel && filteredPOIs.isNotEmpty()) {
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
                            text = "🧭 Navegación POIs",
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
                        text = "POI ${currentPOIIndex + 1} de ${filteredPOIs.size}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Mostrar información del POI actual
                    if (currentPOIIndex < filteredPOIs.size) {
                        val currentPOI = filteredPOIs[currentPOIIndex]
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                            )
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Text(
                                    text = currentPOI.nombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = getCategoryIcon(currentPOI.categoria.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = currentPOI.categoria.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                if (currentPOI.direccion.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = currentPOI.direccion,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Botones de navegación
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { navigateToPreviousPOI() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary
                            )
                        ) {
                            Text("⬅️ Anterior")
                        }
                        
                        Button(
                            onClick = { navigateToNextPOI() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Siguiente ➡️")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Botón para ir al primer POI
                    OutlinedButton(
                        onClick = { 
                            navigateToPOI(0)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("🎯 Ir al primer POI")
                    }
                }
            }
        }
        
        // Ventana de log en la parte superior izquierda
        if (showLog) {
            Card(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
                    .width(300.dp)
                    .height(200.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Log de Actividad",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(
                            onClick = { showLog = false },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cerrar log",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(logMessages.reversed()) { message ->
                            Text(
                                text = message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
        
        // Botón para mostrar/ocultar log
        FloatingActionButton(
            onClick = { showLog = !showLog },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.secondary,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ) {
            Icon(
                imageVector = if (showLog) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                contentDescription = if (showLog) "Ocultar log" else "Mostrar log"
            )
        }
        
        
        
        // Estados de carga y error
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = "Cargando mapa...")
                }
            }
            
            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.8f)),
                    contentAlignment = Alignment.Center
                ) {
                    PointsFeedback(
                        message = uiState.errorMessage ?: "Error desconocido",
                        type = "error",
                        onRetry = { viewModel.loadAllPOIs() }
                    )
                }
            }
        }
    }
    
    // Monitorear cambios en el estado del diálogo
    LaunchedEffect(showPOIDetails, selectedPOI) {
        addLogMessage("ESTADO DIÁLOGO CAMBIÓ: showPOIDetails=$showPOIDetails, selectedPOI=${selectedPOI?.nombre}")
        addLogMessage("EVALUANDO CONDICIÓN DIÁLOGO: showPOIDetails=$showPOIDetails, selectedPOI=${selectedPOI?.nombre}")
        addLogMessage("Condición completa: ${showPOIDetails && selectedPOI != null}")
    }
    
    // Diálogo de detalles del POI - FUERA del Box del mapa para evitar problemas de z-index
    if (showPOIDetails && selectedPOI != null) {
        addLogMessage("ENTRANDO EN CONDICIÓN DEL DIÁLOGO - RENDERIZANDO")
        // Diálogo simple para debug
        AlertDialog(
            onDismissRequest = { 
                addLogMessage("Diálogo cerrado")
                showPOIDetails = false
                selectedPOI = null
            },
            title = {
                Text(
                    text = "POI: ${selectedPOI!!.nombre}",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "Categoría: ${selectedPOI!!.categoria.displayName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Dirección: ${selectedPOI!!.direccion}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    // Botones de acción
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                addLogMessage("Botón ver detalles presionado para: ${selectedPOI!!.nombre}")
                                // Navegar a la pantalla de detalles del POI
                                navController.navigate("${AppRoutes.POI_DETAIL}/${selectedPOI!!.id}")
                                showPOIDetails = false
                                selectedPOI = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(ButtonText.VER_DETALLES.value)
                        }
                        OutlinedButton(
                            onClick = {
                                addLogMessage("Botón ubicar presionado para: ${selectedPOI!!.nombre}")
                                coroutineScope.launch {
                                    cameraPosition.animate(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(selectedPOI!!.ubicacion.lat, selectedPOI!!.ubicacion.lon),
                                            16f
                                        )
                                    )
                                    addLogMessage("Mapa centrado en: ${selectedPOI!!.nombre}")
                                }
                                showPOIDetails = false
                                selectedPOI = null
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Ubicar")
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        addLogMessage("Botón cerrar presionado")
                        showPOIDetails = false
                        selectedPOI = null
                    }
                ) {
                    Text(ButtonText.CERRAR.value)
                }
            }
        )
    }
    
    // Diálogo de compartir POI
    if (showShareDialog && selectedPOI != null) {
        POIShareOptionsDialog(
            poi = selectedPOI!!,
            onDismiss = { 
                showShareDialog = false
                selectedPOI = null
            }
        )
    }
    
    // Snackbar para mensajes de ubicación
    if (showLocationMessage) {
        LaunchedEffect(showLocationMessage) {
            kotlinx.coroutines.delay(3000) // Mostrar por 3 segundos
            showLocationMessage = false
        }
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLocationSuccess) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (isLocationSuccess) {
                            Icons.Default.CheckCircle
                        } else {
                            Icons.Default.Warning
                        },
                        contentDescription = null,
                        tint = if (isLocationSuccess) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = locationMessage,
                        color = if (isLocationSuccess) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

// Función para calcular distancia entre dos puntos
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Radio de la Tierra en km
    
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    
    return earthRadius * c
}

// Función para obtener color del marcador según la categoría
private fun getCategoryColor(category: CategoriaPOI): Float {
    return when (category) {
        CategoriaPOI.COMIDA -> BitmapDescriptorFactory.HUE_RED
        CategoriaPOI.ENTRETENIMIENTO -> BitmapDescriptorFactory.HUE_ORANGE
        CategoriaPOI.CULTURA -> BitmapDescriptorFactory.HUE_VIOLET
        CategoriaPOI.DEPORTE -> BitmapDescriptorFactory.HUE_GREEN
        CategoriaPOI.SALUD -> BitmapDescriptorFactory.HUE_CYAN
        CategoriaPOI.EDUCACION -> BitmapDescriptorFactory.HUE_BLUE
        CategoriaPOI.TRANSPORTE -> BitmapDescriptorFactory.HUE_YELLOW
        CategoriaPOI.SERVICIOS -> BitmapDescriptorFactory.HUE_MAGENTA
        CategoriaPOI.TURISMO -> BitmapDescriptorFactory.HUE_AZURE
        CategoriaPOI.RECARGA_ELECTRICA -> BitmapDescriptorFactory.HUE_AZURE
        CategoriaPOI.PARQUES -> BitmapDescriptorFactory.HUE_GREEN
        CategoriaPOI.SHOPPING -> BitmapDescriptorFactory.HUE_ROSE
        CategoriaPOI.OTRO -> BitmapDescriptorFactory.HUE_RED
    }
}

@Composable
fun POIDetailsDialog(
    poi: PointOfInterest,
    onDismiss: () -> Unit,
    onLocatePOI: (PointOfInterest) -> Unit,
    onSharePOI: (PointOfInterest) -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = getCategoryIcon(poi.categoria.name),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = poi.nombre,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Categoría
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = poi.categoria.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Descripción
                if (poi.descripcion.isNotEmpty()) {
                    Text(
                        text = poi.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Dirección
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = poi.direccion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Coordenadas
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${String.format("%.6f", poi.ubicacion.lat)}, ${String.format("%.6f", poi.ubicacion.lon)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Información adicional si está disponible
                poi.telefono?.let { telefono ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Phone,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = telefono,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                poi.email?.let { email ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = email,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                poi.sitioWeb?.let { sitioWeb ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Language,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = sitioWeb,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para ver detalles completos
                OutlinedButton(
                    onClick = { 
                        onDismiss()
                        // Aquí podrías navegar a la pantalla de detalles completos
                        // navController.navigate("poi_detail/${poi.id}")
                    }
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver detalles")
                }
                
                // Botón para ubicar en el mapa
                Button(
                    onClick = { onLocatePOI(poi) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.MyLocation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ubicar")
                }
            }
        },
        dismissButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para compartir
                OutlinedButton(
                    onClick = { 
                        onSharePOI(poi)
                    }
                ) {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(ButtonText.COMPARTIR.value)
                }
                
                TextButton(onClick = onDismiss) {
                    Text(ButtonText.CERRAR.value)
                }
            }
        }
    )
}
