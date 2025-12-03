package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.constants.AppRoutes
import com.example.points.constants.AppSpacing
import com.example.points.ui.components.ModernCard
import com.example.points.ui.theme.*
import com.example.points.viewmodel.PointOfInterestViewModel
import kotlinx.coroutines.delay

/**
 * Pantalla de Favoritos - Muestra los POIs guardados en la base de datos local (Room)
 * 
 * Esta pantalla demuestra el uso de Room Database para almacenar y recuperar datos localmente.
 * Los POIs favoritos se guardan en la tabla "favorite_pois" de SQLite.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar favoritos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadFavorites()
        viewModel.getFavoriteCount()
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo con gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF0F5),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header moderno
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B9D),
                                Color(0xFFFF8C8E)
                            ),
                            start = Offset.Zero,
                            end = Offset.Infinite
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            modifier = Modifier
                                .size(40.dp)
                                .clickable { navController.popBackStack() },
                            shape = CircleShape,
                            color = Color.White.copy(0.2f)
                        ) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = Color.White,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(16.dp))
                        
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Mis Favoritos",
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = "${uiState.favoriteCount} lugares guardados",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(0.9f)
                            )
                        }
                    }
                    
                    Surface(
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { navController.navigate(AppRoutes.DATABASE_DEMO) },
                        shape = CircleShape,
                        color = Color.White.copy(0.2f)
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = "Info de BD",
                            tint = Color.White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
            when {
                // Sin favoritos
                uiState.favorites.isEmpty() -> {
                    EmptyFavoritesView()
                }
                
                // Mostrar lista de favoritos
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(AppSpacing.STANDARD),
                        verticalArrangement = Arrangement.spacedBy(AppSpacing.STANDARD)
                    ) {
                        // Encabezado con información
                        item {
                            ModernCard(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .background(Color(0xFFFF6B9D).copy(0.15f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Storage,
                                            contentDescription = null,
                                            tint = Color(0xFFFF6B9D),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            "Almacenados en Room Database",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            "Base de datos SQLite local",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Lista de POIs favoritos
                        itemsIndexed(uiState.favorites) { index, poi ->
                            FavoritePOICard(
                                poi = poi,
                                onClick = {
                                    navController.navigate("${AppRoutes.POI_DETAIL}/${poi.id}")
                                },
                                showFavoriteIndicator = true
                            )
                        }
                        
                        // Espaciado al final
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
                }
            }
        }
    }
}

@Composable
fun EmptyFavoritesView() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        ModernCard {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
                modifier = Modifier.padding(40.dp)
            ) {
                // Icono animado
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(Color(0xFFFF6B9D).copy(0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = Color(0xFFFF6B9D).copy(0.6f)
                    )
                }
                
                Text(
                    "No tienes favoritos",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    "Agrega lugares a tus favoritos para acceder rápidamente desde aquí",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(0.5f)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                Icons.Default.Storage,
                                contentDescription = null,
                                tint = PointsPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                "Room Database (SQLite)",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            "Los favoritos se guardan localmente en tu dispositivo usando Room Database, " +
                            "lo que permite acceso offline y sincronización rápida.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoritePOICard(
    poi: com.example.points.models.PointOfInterest,
    onClick: () -> Unit,
    showFavoriteIndicator: Boolean
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(50)
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically { it / 2 }
    ) {
        ModernCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icono de categoría
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(getCategoryColorForPOI(poi).copy(0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = getCategoryEmoji(poi.categoria),
                        style = MaterialTheme.typography.headlineMedium
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = poi.nombre,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = poi.ubicacion.direccion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(0.6f),
                        maxLines = 1
                    )
                    
                    if (poi.calificacion > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFBE0B),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format("%.1f", poi.calificacion),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.8f)
                            )
                        }
                    }
                }
                
                if (showFavoriteIndicator) {
                    Icon(
                        Icons.Default.Favorite,
                        contentDescription = "Favorito",
                        tint = Color(0xFFFF6B9D),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

private fun getCategoryColorForPOI(poi: com.example.points.models.PointOfInterest): Color {
    return when (poi.categoria) {
        com.example.points.models.CategoriaPOI.COMIDA -> Color(0xFFFF6B6B)
        com.example.points.models.CategoriaPOI.ENTRETENIMIENTO -> Color(0xFFFFBE0B)
        com.example.points.models.CategoriaPOI.CULTURA -> Color(0xFF4ECDC4)
        com.example.points.models.CategoriaPOI.DEPORTE -> Color(0xFF4CAF50)
        com.example.points.models.CategoriaPOI.SALUD -> Color(0xFF38A3A5)
        com.example.points.models.CategoriaPOI.EDUCACION -> Color(0xFF95E1D3)
        com.example.points.models.CategoriaPOI.TRANSPORTE -> Color(0xFF5D5FEF)
        com.example.points.models.CategoriaPOI.SERVICIOS -> Color(0xFF9E9E9E)
        com.example.points.models.CategoriaPOI.TURISMO -> Color(0xFFE91E63)
        com.example.points.models.CategoriaPOI.RECARGA_ELECTRICA -> Color(0xFF00BCD4)
        com.example.points.models.CategoriaPOI.PARQUES -> Color(0xFF6BCF7F)
        com.example.points.models.CategoriaPOI.SHOPPING -> Color(0xFFFF8C42)
        com.example.points.models.CategoriaPOI.OTRO -> Color(0xFF9B59B6)
    }
}

private fun getCategoryEmoji(categoria: com.example.points.models.CategoriaPOI): String {
    return when (categoria) {
        com.example.points.models.CategoriaPOI.COMIDA -> "🍽️"
        com.example.points.models.CategoriaPOI.ENTRETENIMIENTO -> "🎭"
        com.example.points.models.CategoriaPOI.CULTURA -> "🎨"
        com.example.points.models.CategoriaPOI.DEPORTE -> "⚽"
        com.example.points.models.CategoriaPOI.SALUD -> "🏥"
        com.example.points.models.CategoriaPOI.EDUCACION -> "📚"
        com.example.points.models.CategoriaPOI.TRANSPORTE -> "🚌"
        com.example.points.models.CategoriaPOI.SERVICIOS -> "🔧"
        com.example.points.models.CategoriaPOI.TURISMO -> "🗺️"
        com.example.points.models.CategoriaPOI.RECARGA_ELECTRICA -> "⚡"
        com.example.points.models.CategoriaPOI.PARQUES -> "🌳"
        com.example.points.models.CategoriaPOI.SHOPPING -> "🛍️"
        com.example.points.models.CategoriaPOI.OTRO -> "📍"
    }
}

