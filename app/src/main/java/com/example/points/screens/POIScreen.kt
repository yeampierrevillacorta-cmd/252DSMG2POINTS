package com.example.points.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
import com.example.points.ui.theme.ButtonSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Estados locales
    var showFilters by remember { mutableStateOf(false) }
    var selectedPOI by remember { mutableStateOf<PointOfInterest?>(null) }
    
    // Cargar POIs al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadAllPOIs()
    }
    
    // Usar los POIs filtrados del ViewModel
    val filteredPOIs = remember(uiState.filteredPOIs, uiState.selectedCategory) {
        uiState.filteredPOIs
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            val iconButtonSize = 48.dp

            // Header con título y botones
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.STANDARD),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    Text(
                        text = "📍 Puntos de Interés",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Descubre lugares en tu localidad",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.MEDIUM),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón de filtros
                    IconButton(
                        onClick = { showFilters = !showFilters },
                        modifier = Modifier
                            .size(iconButtonSize)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (showFilters) MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.surface,
                            ),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = if (showFilters) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                            contentDescription = ContentDescription.FILTROS.value
                        )
                    }
                    
                    // Botón de mapa
                    IconButton(
                        onClick = { navController.navigate(AppRoutes.POI_MAP) },
                        modifier = Modifier
                            .size(iconButtonSize)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                            ),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Ver mapa"
                        )
                    }
                }
            }
            
            // Panel de filtros con animación de expansión
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = AppSpacing.STANDARD),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(AppSpacing.STANDARD)
                        ) {
                            Text(
                                text = SectionTitle.FILTROS.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = AppSpacing.MEDIUM)
                            )
                            
                            // Filtro de categoría
                            Text(
                                text = SectionTitle.CATEGORIA.value,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = AppSpacing.SMALL)
                            )
                            
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.SMALL)
                            ) {
                                item {
                                    FilterChip(
                                        onClick = { viewModel.setCategoryFilter(null) },
                                        label = { Text(ButtonText.TODOS.value) },
                                        selected = uiState.selectedCategory == null
                                    )
                                }
                                
                                items(CategoriaPOI.values().toList()) { categoria ->
                                    FilterChip(
                                        onClick = { viewModel.setCategoryFilter(categoria) },
                                        label = { Text(categoria.displayName) },
                                        selected = uiState.selectedCategory == categoria
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(AppSpacing.STANDARD))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(AppSpacing.STANDARD))
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
                        contentPadding = PaddingValues(AppSpacing.STANDARD),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.STANDARD)
                    ) {
                        item {
                            Text(
                                text = if (uiState.selectedCategory != null) {
                                    "📋 ${uiState.selectedCategory!!.displayName}"
                                } else {
                                    SectionTitle.TODOS_LOS_POI.value
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(bottom = AppSpacing.SMALL)
                            )
                        }
                        
                        items(filteredPOIs) { poi ->
                            POICard(
                                poi = poi,
                                onClick = {
                                    navController.navigate("${AppRoutes.POI_DETAIL}/${poi.id}")
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Botón flotante para agregar POI
        FloatingActionButton(
            onClick = { navController.navigate(AppRoutes.POI_SUBMISSION) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar punto de interés",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
fun POICard(
    poi: PointOfInterest,
    onClick: () -> Unit,
    showFavoriteIndicator: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppSpacing.STANDARD),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.STANDARD)
            ) {
                // Imagen del POI
                if (poi.imagenes.isNotEmpty()) {
                    OptimizedRoundedImage(
                        imageUrl = poi.imagenes.first(),
                        contentDescription = poi.nombre,
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(poi.categoria.displayName),
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.LARGE),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
                
                // Información del POI
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.SMALL)
                ) {
                    Text(
                        text = poi.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    if (poi.descripcion.isNotEmpty()) {
                        Text(
                            text = poi.descripcion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.SMALL)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(poi.categoria.displayName),
                            contentDescription = null,
                            modifier = Modifier.size(IconSize.SMALL),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = poi.categoria.displayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    if (poi.direccion.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.SMALL)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.SMALL),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                text = poi.direccion,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Calificación si existe
                    if (poi.calificacion > 0.0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(AppSpacing.SMALL)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(IconSize.SMALL),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = String.format("%.1f", poi.calificacion),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                            if (poi.totalCalificaciones > 0) {
                                Text(
                                    text = "(${poi.totalCalificaciones})",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                )
                            }
                        }
                    }
                }
                
                // Icono de flecha
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier
                        .size(IconSize.STANDARD)
                        .align(Alignment.CenterVertically),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            
            if (showFavoriteIndicator) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(AppSpacing.SMALL),
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = CircleShape,
                    tonalElevation = 2.dp
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        modifier = Modifier
                            .size(20.dp)
                            .padding(AppSpacing.SMALL / 2),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

