package com.example.points.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.points.models.TipoUsuario
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.components.*
import com.valentinilk.shimmer.shimmer
import kotlinx.coroutines.delay

// Datos para las tarjetas de administración
data class AdminFeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

// Datos para estadísticas de administración
data class AdminStats(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHomeScreen(
    navController: NavHostController,
    viewModel: IncidentViewModel? = null
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) })
    
    // Animación para el título principal
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )
    
    // Características de administración
    val adminFeatures = listOf(
        AdminFeatureCard(
            title = "Gestión de Incidentes",
            description = "Revisar, aprobar y gestionar reportes",
            icon = Icons.Default.Assignment,
            color = MaterialTheme.colorScheme.error,
            route = "admin_incidents"
        ),
        AdminFeatureCard(
            title = "Usuarios",
            description = "Gestionar usuarios y permisos",
            icon = Icons.Default.People,
            color = MaterialTheme.colorScheme.primary,
            route = "admin_users"
        ),
        AdminFeatureCard(
            title = "Estadísticas",
            description = "Analíticas y reportes del sistema",
            icon = Icons.Default.Analytics,
            color = MaterialTheme.colorScheme.secondary,
            route = "admin_analytics"
        ),
        AdminFeatureCard(
            title = "Configuración",
            description = "Configuración del sistema",
            icon = Icons.Default.Settings,
            color = MaterialTheme.colorScheme.tertiary,
            route = "admin_settings"
        )
    )
    
    // Estadísticas de administración
    val adminStats = listOf(
        AdminStats(
            title = "Incidentes Pendientes",
            value = "${uiState?.incidents?.count { it.estado != com.example.points.models.EstadoIncidente.RESUELTO } ?: "0"}",
            icon = Icons.Default.Pending,
            color = Color(0xFFFF6B6B)
        ),
        AdminStats(
            title = "Usuarios Activos",
            value = "1,234",
            icon = Icons.Default.People,
            color = Color(0xFF4ECDC4)
        ),
        AdminStats(
            title = "Tiempo Promedio",
            value = "2.5h",
            icon = Icons.Default.Schedule,
            color = Color(0xFF45B7D1)
        ),
        AdminStats(
            title = "Satisfacción",
            value = "94%",
            icon = Icons.Default.Star,
            color = Color(0xFF96CEB4)
        )
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.2f)
                    )
                )
            )
    ) {
        // Hero Section para Administrador
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo de administrador animado
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.error.copy(alpha = pulseAlpha),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Logo",
                        tint = MaterialTheme.colorScheme.onError,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Panel de Administración",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "SmartCity POINTS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Gestiona y supervisa la plataforma urbana",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                // Información del usuario administrador
                uiState?.currentUser?.let { user ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar del administrador
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.error,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onError,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Admin: ${user.nombre}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFFFFD700)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "👑",
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Admin",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.Black
                                            )
                                        }
                                    }
                                }
                                
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                                )
                                
                                Text(
                                    text = "Tipo: ${user.tipo.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Estadísticas de administración
        Text(
            text = "📊 Estadísticas del Sistema",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(adminStats) { stat ->
                if (uiState?.isLoading == true) {
                    ShimmerPanel(
                        modifier = Modifier.width(150.dp).height(120.dp)
                    ) {
                        // Placeholder content
                    }
                } else {
                    GradientCard(
                        colors = listOf(
                            stat.color.copy(alpha = 0.3f),
                            stat.color.copy(alpha = 0.1f)
                        )
                    ) {
                        AdminStatsCard(stat = stat)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Características de administración
        Text(
            text = "⚙️ Herramientas de Administración",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            adminFeatures.forEach { feature ->
                if (uiState?.isLoading == true) {
                    ShimmerPanel(
                        modifier = Modifier.height(84.dp)
                    ) {
                        // Placeholder content
                    }
                } else {
                    ModernMenuItem(
                        title = feature.title,
                        subtitle = feature.description,
                        icon = feature.icon,
                        containerColor = when (feature.route) {
                            "admin_incidents" -> MaterialTheme.colorScheme.errorContainer
                            "admin_users" -> MaterialTheme.colorScheme.primaryContainer
                            "admin_analytics" -> MaterialTheme.colorScheme.secondaryContainer
                            "admin_settings" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        iconColor = when (feature.route) {
                            "admin_incidents" -> MaterialTheme.colorScheme.onErrorContainer
                            "admin_users" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "admin_analytics" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "admin_settings" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        titleColor = when (feature.route) {
                            "admin_incidents" -> MaterialTheme.colorScheme.onErrorContainer
                            "admin_users" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "admin_analytics" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "admin_settings" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        subtitleColor = when (feature.route) {
                            "admin_incidents" -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            "admin_users" -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            "admin_analytics" -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            "admin_settings" -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        },
                        onClick = {
                            when (feature.route) {
                                "admin_incidents" -> navController.navigate("admin_incidents")
                                "admin_users" -> navController.navigate("admin_users")
                                "admin_analytics" -> navController.navigate("admin_analytics")
                                "admin_settings" -> navController.navigate("admin_settings")
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Acceso rápido para administradores
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Speed,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Acceso Rápido",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                
                Text(
                    text = "Herramientas esenciales para administradores",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón de Gestión de Incidentes
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("admin_incidents") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Incidentes",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Botón de Usuarios
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("admin_users") },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.People,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Usuarios",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Footer con información adicional
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🔧 Panel de Control",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.error
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Como administrador, tienes acceso completo a todas las funcionalidades del sistema para gestionar la plataforma urbana.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Espaciado final
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun AdminStatsCard(
    stat: AdminStats,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(150.dp),
        colors = CardDefaults.cardColors(
            containerColor = stat.color.copy(alpha = 0.08f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            stat.color.copy(alpha = 0.1f),
                            stat.color.copy(alpha = 0.05f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icono con fondo circular
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            stat.color.copy(alpha = 0.2f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        stat.icon,
                        contentDescription = null,
                        tint = stat.color,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stat.value,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = stat.color
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = stat.title,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    lineHeight = 13.sp
                )
            }
        }
    }
}
