package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.points.R
import com.example.points.models.CategoriaPOI
import com.example.points.models.PointOfInterest
import com.example.points.models.Ubicacion
import com.example.points.constants.AppRoutes
import com.example.points.viewmodel.PointOfInterestViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.services.LocationService
import com.example.points.utils.getCategoryIcon
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POISearchScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = navController.context
    
    // Location service
    val locationService = remember { LocationService(context) }
    var locationSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var showLocationSuggestions by remember { mutableStateOf(false) }
    
    // Obtener ubicación del usuario
    LaunchedEffect(Unit) {
        try {
            if (locationService.checkPermissions()) {
                locationService.startLocationUpdates().collect { locationState ->
                    if (locationState.latitude != null && locationState.longitude != null) {
                        val userLocation = Pair(locationState.latitude!!, locationState.longitude!!)
                        // Actualizar la ubicación en el ViewModel
                        viewModel.updateUserLocation(userLocation.first, userLocation.second)
                    }
                }
            }
        } catch (e: Exception) {
            // Manejar error de ubicación silenciosamente
        }
    }
    
    // Load location-based suggestions when user location is available
    LaunchedEffect(uiState.userLocation) {
        uiState.userLocation?.let { (lat, lon) ->
            locationSuggestions = locationService.getLocationBasedSuggestions(lat, lon)
            showLocationSuggestions = true
        }
    }
    
    Scaffold(
        floatingActionButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Botón de ver en mapa
                FloatingActionButton(
                    onClick = { navController.navigate(AppRoutes.POI_MAP) },
                    containerColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.Map, 
                        contentDescription = "Ver en mapa",
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Botón de añadir POI
                FloatingActionButton(
                    onClick = { navController.navigate(AppRoutes.POI_SUBMISSION) },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Agregar POI")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
        // Header con búsqueda
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Puntos de Interés",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Barra de búsqueda
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.searchPOIs(it) },
                    label = { Text("Buscar lugares...") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = "Buscar")
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.searchPOIs("") }) {
                                Icon(Icons.Filled.Clear, contentDescription = "Limpiar")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Filtros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Filtro de ubicación
                    FilterChip(
                        onClick = { viewModel.toggleNearbyFilter() },
                        label = { 
                            Text(
                                "Cercanos",
                                fontSize = 13.sp
                            ) 
                        },
                        selected = uiState.showOnlyNearby,
                        leadingIcon = {
                            Icon(
                                Icons.Filled.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.height(36.dp)
                    )
                    
                    // Botón para limpiar filtros
                    if (uiState.selectedCategory != null || uiState.showOnlyNearby) {
                        TextButton(
                            onClick = { viewModel.clearFilters() },
                            modifier = Modifier.height(36.dp)
                        ) {
                            Text(
                                "Limpiar filtros",
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
        
        // Categorías
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            items(CategoriaPOI.values()) { categoria ->
                CategoryChip(
                    categoria = categoria,
                    isSelected = uiState.selectedCategory == categoria,
                    onClick = { 
                        viewModel.setCategoryFilter(
                            if (uiState.selectedCategory == categoria) null else categoria
                        )
                    }
                )
            }
        }
        
        // Sugerencias basadas en ubicación
        if (showLocationSuggestions && locationSuggestions.isNotEmpty()) {
            LocationSuggestionsSection(
                suggestions = locationSuggestions,
                onSuggestionClick = { suggestion ->
                    viewModel.searchPOIs(suggestion)
                }
            )
        }
        
        // Lista de POIs
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = "Cargando puntos de interés...")
                }
            }
            
            uiState.errorMessage != null -> {
                PointsFeedback(
                    message = uiState.errorMessage ?: "Error desconocido",
                    type = "error",
                    onRetry = { viewModel.loadAllPOIs() }
                )
            }
            
            uiState.filteredPOIs.isEmpty() -> {
                PointsFeedback(
                    message = if (uiState.searchQuery.isNotEmpty() || uiState.selectedCategory != null) {
                        "No se encontraron puntos de interés con los filtros aplicados"
                    } else {
                        "No hay puntos de interés disponibles"
                    },
                    type = "empty",
                    onRetry = { viewModel.loadAllPOIs() }
                )
            }
            
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.filteredPOIs) { poi ->
                        POICard(
                            poi = poi,
                            userLocation = uiState.userLocation,
                            onClick = {
                                navController.navigate("${AppRoutes.POI_DETAIL}/${poi.id}")
                            }
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun LocationSuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Sugerencias para ti",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 2.dp)
            ) {
                items(suggestions) { suggestion ->
                    FilterChip(
                        onClick = { onSuggestionClick(suggestion) },
                        label = { 
                            Text(
                                text = suggestion,
                                fontSize = 12.sp
                            ) 
                        },
                        selected = false,
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                        },
                        modifier = Modifier.height(32.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChip(
    categoria: CategoriaPOI,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        onClick = onClick,
        label = { 
            Text(
                text = categoria.displayName,
                fontSize = 12.sp
            ) 
        },
        selected = isSelected,
        leadingIcon = {
            Icon(
                getCategoryIcon(categoria.icon),
                contentDescription = null,
                modifier = Modifier.size(14.dp)
            )
        },
        modifier = Modifier
            .height(36.dp)
            .clip(RoundedCornerShape(18.dp))
    )
}

@Composable
fun POICard(
    poi: PointOfInterest,
    userLocation: Pair<Double, Double>?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen de referencia del POI
            Card(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp)),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                if (poi.imagenes.isNotEmpty()) {
                    AsyncImage(
                        model = poi.imagenes.first(),
                        contentDescription = "Imagen de ${poi.nombre}",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        error = painterResource(id = R.drawable.placeholder_poi),
                        placeholder = painterResource(id = R.drawable.placeholder_poi)
                    )
                } else {
                    // Placeholder cuando no hay imagen
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.placeholder_poi),
                            contentDescription = "Sin imagen",
                            modifier = Modifier.size(32.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
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
                
                // Calificación
                if (poi.calificacion > 0) {
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Star,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = Color(0xFFFFD700)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = String.format("%.1f", poi.calificacion),
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                        Text(
                            text = "(${poi.totalCalificaciones})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Distancia si tenemos ubicación del usuario
            userLocation?.let { (userLat, userLon) ->
                val distance = calculateDistance(
                    userLat, userLon,
                    poi.ubicacion.lat, poi.ubicacion.lon
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (distance < 1) {
                            "${(distance * 1000).roundToInt()} m"
                        } else {
                            "${String.format("%.1f", distance)} km"
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            // Características destacadas
            if (poi.caracteristicas.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(poi.caracteristicas.take(3)) { caracteristica ->
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = caracteristica.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}


// Función para calcular distancia (simplificada)
private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371.0 // Radio de la Tierra en kilómetros
    
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
            kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
            kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
    
    val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
    
    return earthRadius * c
}
