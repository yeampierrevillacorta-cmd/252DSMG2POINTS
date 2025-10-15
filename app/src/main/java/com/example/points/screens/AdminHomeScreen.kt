package com.example.points.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.points.R
import com.example.points.models.TipoUsuario
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.components.*

// Datos para las tarjetas de administración
data class AdminFeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun AdminHomeScreen(
    navController: NavHostController,
    viewModel: IncidentViewModel? = null
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) })
    
    // Características para administradores
    val adminFeatures = listOf(
        AdminFeatureCard(
            title = "Gestionar Incidentes",
            description = "Administra todos los reportes",
            icon = Icons.Default.Report,
            route = "admin_incidents"
        ),
        AdminFeatureCard(
            title = "Moderar Eventos",
            description = "Gestiona eventos pendientes",
            icon = Icons.Default.Event,
            route = "admin_events"
        ),
        AdminFeatureCard(
            title = "Gestionar POIs",
            description = "Administra puntos de interés",
            icon = Icons.Default.LocationOn,
            route = "admin_pois"
        ),
        AdminFeatureCard(
            title = "Usuarios",
            description = "Gestiona usuarios del sistema",
            icon = Icons.Default.People,
            route = "admin_users"
        ),
        AdminFeatureCard(
            title = "Analíticas",
            description = "Estadísticas y reportes",
            icon = Icons.Default.Analytics,
            route = "admin_analytics"
        ),
        AdminFeatureCard(
            title = "Configuración",
            description = "Ajustes del sistema",
            icon = Icons.Default.Settings,
            route = "admin_settings"
        )
    )
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Título principal
        item {
            SurfaceCard {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = "Logo POINTS",
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Panel de Administración",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestiona el sistema POINTS",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
        
        // Información del usuario si está disponible
        uiState?.currentUser?.let { user ->
            item {
                SurfaceCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Admin: ${user.nombre}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = user.email,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        
        // Estadísticas del sistema
        item {
            Text(
                text = "Estadísticas del Sistema",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SurfaceCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${uiState?.incidents?.size ?: "0"}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Incidentes",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                SurfaceCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "1,234",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Usuarios",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SurfaceCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "89%",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Resueltos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                SurfaceCard(modifier = Modifier.weight(1f)) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "2.5h",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tiempo Prom.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        // Funcionalidades principales
        item {
            Text(
                text = "Herramientas de Administración",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(adminFeatures) { feature ->
            SurfaceCard(
                onClick = {
                    when (feature.route) {
                        "admin_incidents" -> navController.navigate("admin_incidents")
                        "admin_events" -> navController.navigate("admin_events")
                        "admin_pois" -> navController.navigate("admin_pois")
                        "admin_users" -> navController.navigate("admin_users")
                        "admin_analytics" -> navController.navigate("admin_analytics")
                        "admin_settings" -> navController.navigate("admin_settings")
                    }
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = feature.title,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = feature.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = feature.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "Ir a ${feature.title}",
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        // Acceso rápido
        item {
            Text(
                text = "Acceso Rápido",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PrimaryButton(
                    onClick = { navController.navigate("admin_incidents") },
                    text = "Incidentes",
                    icon = Icons.Default.Report,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    onClick = { navController.navigate("admin_users") },
                    text = "Usuarios",
                    icon = Icons.Default.People,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}