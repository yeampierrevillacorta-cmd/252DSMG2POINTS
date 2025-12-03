package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.models.CategoriaPOI
import com.example.points.models.PointOfInterest
import com.example.points.viewmodel.PointOfInterestViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.components.OptimizedRoundedImage
import com.example.points.constants.ButtonText
import com.example.points.constants.LoadingMessage
import com.example.points.constants.ErrorMessage
import com.example.points.constants.SectionTitle
import com.example.points.constants.ContentDescription
import com.example.points.constants.AppSpacing
import com.example.points.constants.AppRoutes
import com.example.points.constants.IconSize
import com.example.points.utils.getCategoryIcon
import com.example.points.ui.theme.*
import com.example.points.ui.components.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    
    // Estados locales
    var showFilters by remember { mutableStateOf(false) }
    var selectedPOI by remember { mutableStateOf<PointOfInterest?>(null) }
    
    // Cargar POIs al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadAllPOIs()
        visible = true
    }
    
    // Usar los POIs filtrados del ViewModel
    val filteredPOIs = remember(uiState.filteredPOIs, uiState.selectedCategory) {
        uiState.filteredPOIs
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo animado sutil
        AnimatedBackground()
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
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
                        CategoryEnvironment.copy(alpha = 0.8f),
                        PointsAccent.copy(alpha = 0.8f)
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
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "📍 Puntos de Interés",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Descubre lugares increíbles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón de filtros con animación
                            Surface(
                                onClick = { showFilters = !showFilters },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = if (showFilters) Color.White else Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = if (showFilters) CategoryEnvironment else Color.White
                                    )
                                }
                            }
                            
                            // Botón de mapa
                            Surface(
                                onClick = { navController.navigate(AppRoutes.POI_MAP) },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Map,
                                        contentDescription = "Ver mapa",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Panel de filtros moderno con animación
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically() + slideInVertically(initialOffsetY = { -20 }),
                exit = fadeOut() + shrinkVertically() + slideOutVertically(targetOffsetY = { -20 })
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = PointsPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Filtros",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Categoría",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.setCategoryFilter(null) },
                                    label = { 
                                        Text(
                                            "Todos",
                                            fontWeight = if (uiState.selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = uiState.selectedCategory == null,
                                    leadingIcon = if (uiState.selectedCategory == null) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                    } else null
                                )
                            }
                            
                            items(CategoriaPOI.values().toList()) { categoria ->
                                FilterChip(
                                    onClick = { viewModel.setCategoryFilter(categoria) },
                                    label = { 
                                        Text(
                                            categoria.displayName,
                                            fontWeight = if (uiState.selectedCategory == categoria) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = uiState.selectedCategory == categoria,
                                    leadingIcon = if (uiState.selectedCategory == categoria) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
            
            // Contenido principal
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        PointsLoading(message = LoadingMessage.CARGANDO_POI.value)
                    }
                }
                
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        PointsFeedback(
                            message = uiState.errorMessage ?: ErrorMessage.ERROR_DESCONOCIDO.value,
                            type = "error",
                            onRetry = { viewModel.loadAllPOIs() }
                        )
                    }
                }
                
                filteredPOIs.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(AppSpacing.EXTRA_LARGE * 2),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(AppSpacing.STANDARD))
                            Text(
                                text = ErrorMessage.NO_HAY_POI.value,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "${ErrorMessage.INTENTA_CAMBIAR_FILTROS.value} punto de interés",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(400, 200))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (uiState.selectedCategory != null) 
                                                    getCategoryColor(uiState.selectedCategory!!).copy(alpha = 0.2f)
                                                else PointsPrimary.copy(alpha = 0.2f)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (uiState.selectedCategory != null) 
                                                getCategoryIcon(uiState.selectedCategory!!.displayName)
                                            else Icons.Default.Apps,
                                            contentDescription = null,
                                            tint = if (uiState.selectedCategory != null) 
                                                getCategoryColor(uiState.selectedCategory!!)
                                            else PointsPrimary,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (uiState.selectedCategory != null) {
                                                uiState.selectedCategory!!.displayName
                                            } else {
                                                "Todos los lugares"
                                            },
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${filteredPOIs.size} lugares encontrados",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        
                        itemsIndexed(filteredPOIs) { index, poi ->
                            var itemVisible by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(visible) {
                                if (visible) {
                                    delay(300L + (index * 80L))
                                    itemVisible = true
                                }
                            }
                            
                            AnimatedVisibility(
                                visible = itemVisible,
                                enter = fadeIn() + slideInHorizontally(
                                    initialOffsetX = { 100 },
                                    animationSpec = tween(400, easing = EaseOutCubic)
                                ) + expandVertically()
                            ) {
                                ModernPOICard(
                                    poi = poi,
                                    onClick = {
                                        navController.navigate("${AppRoutes.POI_DETAIL}/${poi.id}")
                                    }
                                )
                            }
                        }
                        
                        // Espaciado inferior
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
        
        // Botón flotante moderno con animación
        var fabScale by remember { mutableStateOf(0f) }
        
        LaunchedEffect(visible) {
            if (visible) {
                delay(800)
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) { value, _ ->
                    fabScale = value
                }
            }
        }
        
        FloatingActionButton(
            onClick = { navController.navigate(AppRoutes.POI_SUBMISSION) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .scale(fabScale),
            containerColor = CategoryEnvironment,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = "Agregar POI",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun getCategoryColor(categoria: CategoriaPOI): Color {
    return when (categoria) {
        CategoriaPOI.COMIDA -> Color(0xFFFF6B6B)
        CategoriaPOI.ENTRETENIMIENTO -> Color(0xFFFFBE0B)
        CategoriaPOI.CULTURA -> Color(0xFF4ECDC4)
        CategoriaPOI.DEPORTE -> Color(0xFF4CAF50)
        CategoriaPOI.SALUD -> Color(0xFF38A3A5)
        CategoriaPOI.EDUCACION -> Color(0xFF95E1D3)
        CategoriaPOI.TRANSPORTE -> Color(0xFF5D5FEF)
        CategoriaPOI.SERVICIOS -> Color(0xFF9E9E9E)
        CategoriaPOI.TURISMO -> Color(0xFFE91E63)
        CategoriaPOI.RECARGA_ELECTRICA -> Color(0xFF00BCD4)
        CategoriaPOI.PARQUES -> Color(0xFF6BCF7F)
        CategoriaPOI.SHOPPING -> Color(0xFFFF8C42)
        CategoriaPOI.OTRO -> Color(0xFF9B59B6)
    }
}

@Composable
private fun ModernPOICard(
    poi: PointOfInterest,
    onClick: () -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen o placeholder con gradiente
            if (poi.imagenes.isNotEmpty()) {
                OptimizedRoundedImage(
                    imageUrl = poi.imagenes.first(),
                    contentDescription = poi.nombre,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    getCategoryColor(poi.categoria).copy(alpha = 0.3f),
                                    getCategoryColor(poi.categoria).copy(alpha = 0.1f)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCategoryIcon(poi.categoria.displayName),
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = getCategoryColor(poi.categoria)
                    )
                }
            }
            
            // Información del POI
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = poi.nombre,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                if (poi.descripcion.isNotEmpty()) {
                    Text(
                        text = poi.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // Categoría con chip colorido
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getCategoryColor(poi.categoria).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(poi.categoria.displayName),
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = getCategoryColor(poi.categoria)
                        )
                        Text(
                            text = poi.categoria.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = getCategoryColor(poi.categoria)
                        )
                    }
                }
                
                // Calificación con estrellas
                if (poi.calificacion > 0.0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = if (index < poi.calificacion.toInt()) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = if (index < poi.calificacion.toInt()) Color(0xFFFFC107) else Color.Gray
                            )
                        }
                        Text(
                            text = String.format("%.1f", poi.calificacion),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        if (poi.totalCalificaciones > 0) {
                            Text(
                                text = "(${poi.totalCalificaciones})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun POICard(
    poi: PointOfInterest,
    onClick: () -> Unit,
    showFavoriteIndicator: Boolean = false
) {
    ModernPOICard(poi = poi, onClick = onClick)
}

