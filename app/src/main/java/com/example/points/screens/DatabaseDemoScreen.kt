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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.constants.AppSpacing
import com.example.points.viewmodel.PointOfInterestViewModel

/**
 * Pantalla de Demostración de Room Database
 * 
 * Esta pantalla muestra información sobre la implementación de Room Database
 * y permite al usuario ver estadísticas y detalles técnicos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatabaseDemoScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar estadísticas
    LaunchedEffect(Unit) {
        viewModel.getFavoriteCount()
        viewModel.loadFavorites()
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Demostración Room Database") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(AppSpacing.STANDARD),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.STANDARD)
        ) {
            // Encabezado con ícono
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                "Room Database",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "Base de datos SQLite local",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
            
            // Estadísticas
            item {
                Text(
                    "📊 Estadísticas",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "Favoritos",
                        value = "${uiState.favoriteCount}",
                        icon = Icons.Filled.Favorite,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Tablas",
                        value = "3",
                        icon = Icons.Filled.TableChart,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Arquitectura
            item {
                Text(
                    "🏗️ Arquitectura",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            item {
                ArchitectureCard(
                    title = "PointsDatabase.kt",
                    description = "Clase principal de la base de datos con patrón Singleton",
                    icon = Icons.Filled.Storage,
                    details = listOf(
                        "• Versión: 1",
                        "• Nombre: points_database",
                        "• Tipo: RoomDatabase (SQLite)"
                    )
                )
            }
            
            // Entidades (Tablas)
            item {
                Text(
                    "📋 Tablas (Entidades)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            items(
                listOf(
                    TableInfo("favorite_pois", "POIs Favoritos", "FavoritePOI.kt", 
                        "Almacena los POIs marcados como favoritos por el usuario"),
                    TableInfo("cached_pois", "Caché de POIs", "CachedPOI.kt",
                        "Guarda POIs vistos recientemente para acceso offline"),
                    TableInfo("search_history", "Historial", "SearchHistory.kt",
                        "Registra las búsquedas realizadas por el usuario")
                )
            ) { table ->
                TableCard(table)
            }
            
            // DAOs
            item {
                Text(
                    "🔧 DAOs (Data Access Objects)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            item {
                ArchitectureCard(
                    title = "FavoritePOIDao",
                    description = "Interfaz para operaciones CRUD de favoritos",
                    icon = Icons.Filled.Code,
                    details = listOf(
                        "• getAllFavorites(): Flow<List<FavoritePOI>>",
                        "• insertFavorite(favorite)",
                        "• deleteFavorite(poiId)",
                        "• isFavorite(poiId): Boolean"
                    )
                )
            }
            
            item {
                ArchitectureCard(
                    title = "CachedPOIDao",
                    description = "Interfaz para operaciones de caché",
                    icon = Icons.Filled.Code,
                    details = listOf(
                        "• getCachedPOIs(limit): Flow<List>",
                        "• insertCachedPOI(poi)",
                        "• deleteOldCachedPOIs(timestamp)"
                    )
                )
            }
            
            // Repositorios
            item {
                Text(
                    "📦 Repositorio",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            item {
                ArchitectureCard(
                    title = "LocalPOIRepository",
                    description = "Capa de abstracción sobre Room Database",
                    icon = Icons.Filled.FolderOpen,
                    details = listOf(
                        "• getAllFavorites(): Flow<List<POI>>",
                        "• addToFavorites(poi)",
                        "• removeFromFavorites(poiId)",
                        "• cachePOI(poi)",
                        "• getCachedPOIs(limit)"
                    )
                )
            }
            
            // Ruta de la Base de Datos
            item {
                Text(
                    "📂 Ubicación",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            "Ruta del archivo SQLite:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "/data/data/com.example.points/databases/points_database",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
            
            // Beneficios
            item {
                Text(
                    "✅ Beneficios",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                )
            }
            
            items(
                listOf(
                    "Acceso offline a datos",
                    "Sincronización automática con UI (Flow)",
                    "Type-safe (compilador verifica consultas)",
                    "Rendimiento optimizado",
                    "Persistencia entre sesiones"
                )
            ) { benefit ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        benefit,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Espaciado final
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
            Text(
                value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun ArchitectureCard(
    title: String,
    description: String,
    icon: ImageVector,
    details: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Divider(modifier = Modifier.padding(vertical = 4.dp))
            
            details.forEach { detail ->
                Text(
                    detail,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }
        }
    }
}

data class TableInfo(
    val name: String,
    val displayName: String,
    val entityFile: String,
    val description: String
)

@Composable
fun TableCard(table: TableInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
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
                Icons.Filled.TableChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    table.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    table.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    table.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

