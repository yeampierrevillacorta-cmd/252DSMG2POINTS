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
import com.example.points.constants.AppRoutes
import com.example.points.R
import com.example.points.models.TipoUsuario
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.components.*

// Datos para las tarjetas de características del cliente
data class ClientFeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun ClientHomeScreen(
    navController: NavHostController,
    viewModel: IncidentViewModel? = null
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) })
    
    // Características para clientes
    val clientFeatures = listOf(
        ClientFeatureCard(
            title = "Reportar Problema",
            description = "Informa incidentes urbanos",
            icon = Icons.Default.Report,
            route = "create_incident"
        ),
        ClientFeatureCard(
            title = "Ver Mapa",
            description = "Explora incidentes cercanos",
            icon = Icons.Default.Map,
            route = "incidents"
        ),
        ClientFeatureCard(
            title = "Eventos",
            description = "Descubre eventos en tu localidad",
            icon = Icons.Default.Event,
            route = "events"
        ),
        ClientFeatureCard(
            title = "Lugares de Interés",
            description = "Explora POIs cercanos",
            icon = Icons.Default.LocationOn,
            route = AppRoutes.POI_LIST
        ),
        ClientFeatureCard(
            title = "Mis Reportes",
            description = "Historial de mis reportes",
            icon = Icons.Default.History,
            route = "my_reports"
        ),
        ClientFeatureCard(
            title = "Notificaciones",
            description = "Alertas y actualizaciones",
            icon = Icons.Default.Notifications,
            route = "notifications"
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
                            text = "Panel de Cliente",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Gestiona tus reportes y consultas",
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
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hola, ${user.nombre}",
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
        
        // Estadísticas básicas
        item {
            Text(
                text = "Mis Estadísticas",
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
                            text = "5",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Reportes",
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
                            text = "3",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Resueltos",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
        
        // Funcionalidades principales
        item {
            Text(
                text = "Funcionalidades",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        
        items(clientFeatures) { feature ->
            SurfaceCard(
                onClick = {
                    when (feature.route) {
                        "create_incident" -> navController.navigate(AppRoutes.CREATE_INCIDENT)
                        "incidents" -> navController.navigate(AppRoutes.INCIDENTS)
                        "events" -> navController.navigate(AppRoutes.EVENTS)
                        AppRoutes.POI_LIST -> navController.navigate(AppRoutes.POI_LIST)
                        "my_reports" -> navController.navigate("my_reports")
                        "notifications" -> navController.navigate("notifications")
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
                    onClick = { navController.navigate("create_incident") },
                    text = "Reportar",
                    icon = Icons.Default.Report,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    onClick = { navController.navigate("incidents") },
                    text = "Ver Mapa",
                    icon = Icons.Default.Map,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}