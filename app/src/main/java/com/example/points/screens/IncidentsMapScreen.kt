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
import androidx.compose.material.icons.filled.FilterList
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
import androidx.lifecycle.viewmodel.compose.viewModel
import android.util.Log
import com.example.points.models.TipoIncidente
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.utils.TestDataCreator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentsMapScreen(
    onCreateIncidentClick: () -> Unit,
    viewModel: IncidentViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    // Log para debug
    LaunchedEffect(uiState.incidents) {
        Log.d("IncidentsMap", "Incidentes cargados: ${uiState.incidents.size}")
        uiState.incidents.forEach { incident ->
            Log.d("IncidentsMap", "Incidente: ${incident.tipo} en (${incident.ubicacion.lat}, ${incident.ubicacion.lon})")
        }
    }
    
    // Estado para el mapa
    var mapProperties by remember { mutableStateOf(MapProperties(isMyLocationEnabled = false)) }
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
    
    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission) {
            mapProperties = mapProperties.copy(isMyLocationEnabled = true)
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
                mapToolbarEnabled = false
            ),
            onMapLoaded = {
                Log.d("IncidentsMap", "Mapa de Google cargado correctamente")
            }
        ) {
            // Marcadores de incidentes
            Log.d("IncidentsMap", "Renderizando ${uiState.incidents.size} marcadores")
            uiState.incidents.forEach { incident ->
                if (incident.ubicacion.lat != 0.0 && incident.ubicacion.lon != 0.0) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(incident.ubicacion.lat, incident.ubicacion.lon)
                        ),
                        title = incident.tipo,
                        snippet = incident.descripcion,
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
                    text = "Debug: Incidentes: ${uiState.incidents.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                
                if (uiState.incidents.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { 
                            TestDataCreator.createTestIncidents()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Text(
                            text = "Crear datos de prueba",
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
                
                if (uiState.isLoading) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cargando...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                uiState.errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Error: $error",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
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
                    .fillMaxWidth(0.7f),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = incident.tipo,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        TextButton(
                            onClick = { viewModel.selectIncident(null) }
                        ) {
                            Text("✕")
                        }
                    }
                    
                    Text(
                        text = incident.descripcion,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = when (incident.estado.displayName) {
                                "Pendiente" -> Color(0xFFFFF3CD)
                                "Confirmado" -> Color(0xFFD1ECF1)
                                "Resuelto" -> Color(0xFFD4EDDA)
                                else -> Color(0xFFF8D7DA)
                            }
                        ) {
                            Text(
                                text = incident.estado.displayName,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = when (incident.estado.displayName) {
                                    "Pendiente" -> Color(0xFF856404)
                                    "Confirmado" -> Color(0xFF0C5460)
                                    "Resuelto" -> Color(0xFF155724)
                                    else -> Color(0xFF721C24)
                                }
                            )
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
    }
}
