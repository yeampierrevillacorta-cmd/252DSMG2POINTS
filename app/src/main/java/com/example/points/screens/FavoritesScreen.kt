package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.constants.AppRoutes
import com.example.points.constants.AppSpacing
import com.example.points.viewmodel.PointOfInterestViewModel

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
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Mis Favoritos")
                        Text(
                            text = "${uiState.favoriteCount} lugares guardados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        // Navegar a pantalla de demostración de BD
                        navController.navigate(AppRoutes.DATABASE_DEMO)
                    }) {
                        Icon(Icons.Filled.Info, contentDescription = "Info de BD")
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
                .background(MaterialTheme.colorScheme.background)
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
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        Icons.Filled.Favorite,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(32.dp)
                                    )
                                    Column {
                                        Text(
                                            "Almacenados en Room Database",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            "Base de datos SQLite local",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                        
                        // Lista de POIs favoritos
                        items(uiState.favorites) { poi ->
                            POICard(
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

@Composable
fun EmptyFavoritesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                Icons.Filled.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Text(
                "No tienes favoritos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Agrega lugares a tus favoritos para acceder rápidamente",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Filled.Storage,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            "Room Database (SQLite)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        "Los favoritos se guardan localmente en tu dispositivo usando Room Database, " +
                        "lo que permite acceso offline y sincronización rápida.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

