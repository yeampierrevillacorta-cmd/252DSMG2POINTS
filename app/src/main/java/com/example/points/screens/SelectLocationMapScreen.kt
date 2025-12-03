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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.points.models.Ubicacion
import com.example.points.utils.GeocodingUtils
import com.example.points.utils.MapStyleUtils
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

/**
 * Pantalla para seleccionar una ubicación en el mapa
 * Permite al usuario tocar en el mapa para seleccionar un punto específico
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectLocationMapScreen(
    initialLocation: Ubicacion? = null,
    onLocationSelected: (Ubicacion) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Estado para la ubicación seleccionada
    var selectedLocation by remember { 
        mutableStateOf<LatLng?>(
            initialLocation?.let { 
                if (it.lat != 0.0 && it.lon != 0.0) {
                    LatLng(it.lat, it.lon)
                } else null
            }
        )
    }
    
    // Estado para la dirección obtenida por geocodificación
    var addressText by remember { mutableStateOf<String?>(null) }
    var isLoadingAddress by remember { mutableStateOf(false) }
    
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
    }
    
    // Configuración del mapa
    val mapStyle = remember { MapStyleUtils.getMapStyleWithoutPOI(context) }
    var mapProperties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = hasLocationPermission,
                isTrafficEnabled = false,
                isBuildingEnabled = false,
                isIndoorEnabled = false,
                mapType = MapType.NORMAL,
                mapStyleOptions = mapStyle
            )
        )
    }
    
    // Posición inicial de la cámara
    val initialCameraPosition = remember(selectedLocation) {
        selectedLocation?.let {
            CameraPosition.fromLatLngZoom(it, 15f)
        } ?: CameraPosition.fromLatLngZoom(LatLng(-12.0464, -77.0428), 12f) // Lima por defecto
    }
    
    var cameraPositionState = rememberCameraPositionState {
        position = initialCameraPosition
    }
    
    // Actualizar propiedades del mapa cuando cambien los permisos
    LaunchedEffect(hasLocationPermission) {
        mapProperties = mapProperties.copy(isMyLocationEnabled = hasLocationPermission)
    }
    
    // Obtener ubicación actual si se tienen permisos y no hay ubicación inicial
    LaunchedEffect(hasLocationPermission, Unit) {
        if (hasLocationPermission && selectedLocation == null) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        val currentPosition = LatLng(it.latitude, it.longitude)
                        selectedLocation = currentPosition
                        isLoadingAddress = true
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(currentPosition, 15f),
                                durationMs = 1000
                            )
                            // Obtener dirección para la ubicación inicial
                            val address = GeocodingUtils.getAddressFromCoordinates(
                                context,
                                currentPosition.latitude,
                                currentPosition.longitude
                            )
                            addressText = address
                            isLoadingAddress = false
                        }
                    }
                }
            } catch (e: SecurityException) {
                Log.e("SelectLocationMap", "Error obteniendo ubicación", e)
            }
        }
    }
    
    // Obtener dirección para la ubicación inicial si existe
    LaunchedEffect(initialLocation) {
        initialLocation?.let { ubicacion ->
            if (ubicacion.lat != 0.0 && ubicacion.lon != 0.0 && addressText == null) {
                isLoadingAddress = true
                val address = GeocodingUtils.getAddressFromCoordinates(
                    context,
                    ubicacion.lat,
                    ubicacion.lon
                )
                addressText = address ?: ubicacion.direccion
                isLoadingAddress = false
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Seleccionar Ubicación",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancelar"
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
            // Mapa interactivo
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = true,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    compassEnabled = true,
                    rotationGesturesEnabled = true,
                    scrollGesturesEnabled = true,
                    tiltGesturesEnabled = false,
                    zoomGesturesEnabled = true
                ),
                onMapClick = { latLng ->
                    // Cuando el usuario toca el mapa, seleccionar ese punto
                    selectedLocation = latLng
                    addressText = null // Resetear dirección
                    isLoadingAddress = true
                    Log.d("SelectLocationMap", "Ubicación seleccionada tocando el mapa: ${latLng.latitude}, ${latLng.longitude}")
                    
                    // Obtener dirección mediante geocodificación inversa
                    coroutineScope.launch {
                        val address = GeocodingUtils.getAddressFromCoordinates(
                            context,
                            latLng.latitude,
                            latLng.longitude
                        )
                        addressText = address
                        isLoadingAddress = false
                    }
                }
            ) {
                // Mostrar marcador en la ubicación seleccionada
                selectedLocation?.let { location ->
                    var markerState by remember(location) { mutableStateOf(MarkerState(position = location)) }
                    
                    // Actualizar markerState cuando cambie selectedLocation (excepto cuando se arrastra)
                    LaunchedEffect(location) {
                        // Solo actualizar si la posición es diferente
                        if (kotlin.math.abs(markerState.position.latitude - location.latitude) > 0.00001 ||
                            kotlin.math.abs(markerState.position.longitude - location.longitude) > 0.00001) {
                            markerState = MarkerState(position = location)
                        }
                    }
                    
                    Marker(
                        state = markerState,
                        title = "Ubicación seleccionada",
                        snippet = "Lat: ${String.format("%.6f", markerState.position.latitude)}, Lon: ${String.format("%.6f", markerState.position.longitude)}",
                        draggable = true,
                        onClick = {
                            // Permitir arrastrar el marcador
                            true
                        }
                    )
                    
                    // Observar cambios en la posición del marcador cuando se arrastra
                    // Usar un efecto que observe la posición actual del marcador
                    LaunchedEffect(markerState.position) {
                        val currentPos = markerState.position
                        // Solo actualizar si es diferente de selectedLocation
                        if (selectedLocation == null || 
                            kotlin.math.abs(currentPos.latitude - selectedLocation!!.latitude) > 0.00001 ||
                            kotlin.math.abs(currentPos.longitude - selectedLocation!!.longitude) > 0.00001) {
                            selectedLocation = currentPos
                            addressText = null // Resetear dirección
                            isLoadingAddress = true
                            Log.d("SelectLocationMap", "Marcador arrastrado a: ${currentPos.latitude}, ${currentPos.longitude}")
                            
                            // Obtener dirección mediante geocodificación inversa
                            val address = GeocodingUtils.getAddressFromCoordinates(
                                context,
                                currentPos.latitude,
                                currentPos.longitude
                            )
                            addressText = address
                            isLoadingAddress = false
                        }
                    }
                }
            }
            
            // Instrucciones en la parte superior
            Card(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .fillMaxWidth(0.9f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "📍 Toca el mapa para seleccionar una ubicación o arrastra el marcador",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            
            // Panel de información de la ubicación seleccionada
            if (selectedLocation != null) {
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
                        Text(
                            text = "Ubicación seleccionada",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (isLoadingAddress) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp))
                                Text(
                                    text = "Obteniendo dirección...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                )
                            }
                        } else if (addressText != null) {
                            Text(
                                text = addressText!!,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = "Lat: ${String.format("%.6f", selectedLocation!!.latitude)}, Lon: ${String.format("%.6f", selectedLocation!!.longitude)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Botón flotante para ir a mi ubicación
            FloatingActionButton(
                onClick = {
                    if (hasLocationPermission) {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                        try {
                            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                                location?.let {
                                    val currentPosition = LatLng(it.latitude, it.longitude)
                                    selectedLocation = currentPosition
                                    addressText = null
                                    isLoadingAddress = true
                                    coroutineScope.launch {
                                        cameraPositionState.animate(
                                            CameraUpdateFactory.newLatLngZoom(currentPosition, 15f),
                                            durationMs = 1000
                                        )
                                        // Obtener dirección
                                        val address = GeocodingUtils.getAddressFromCoordinates(
                                            context,
                                            currentPosition.latitude,
                                            currentPosition.longitude
                                        )
                                        addressText = address
                                        isLoadingAddress = false
                                    }
                                }
                            }
                        } catch (e: SecurityException) {
                            Log.e("SelectLocationMap", "Error obteniendo ubicación", e)
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
                containerColor = MaterialTheme.colorScheme.secondary,
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
            
            // Botón flotante para confirmar selección
            if (selectedLocation != null) {
                FloatingActionButton(
                    onClick = {
                        selectedLocation?.let { location ->
                            // Usar la dirección obtenida o una por defecto
                            val direccion = addressText ?: "Lat: ${String.format("%.6f", location.latitude)}, Lon: ${String.format("%.6f", location.longitude)}"
                            
                            val ubicacion = Ubicacion(
                                lat = location.latitude,
                                lon = location.longitude,
                                direccion = direccion
                            )
                            Log.d("SelectLocationMap", "Guardando ubicación: ${ubicacion.lat}, ${ubicacion.lon}, ${ubicacion.direccion}")
                            Log.d("SelectLocationMap", "Llamando a onLocationSelected con ubicación: lat=${ubicacion.lat}, lon=${ubicacion.lon}")
                            onLocationSelected(ubicacion)
                            Log.d("SelectLocationMap", "onLocationSelected llamado exitosamente")
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(bottom = 80.dp, end = 16.dp)
                        .size(56.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Confirmar ubicación",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

