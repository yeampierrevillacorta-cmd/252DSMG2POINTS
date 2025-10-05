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

// Datos para las tarjetas de características del cliente
data class ClientFeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val route: String
)

// Datos para estadísticas del cliente
data class ClientStats(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientHomeScreen(
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
    
    // Características para clientes
    val clientFeatures = listOf(
        ClientFeatureCard(
            title = "Reportar Problema",
            description = "Informa incidentes urbanos",
            icon = Icons.Default.Report,
            color = MaterialTheme.colorScheme.error,
            route = "create_incident"
        ),
        ClientFeatureCard(
            title = "Ver Mapa",
            description = "Explora incidentes cercanos",
            icon = Icons.Default.Map,
            color = MaterialTheme.colorScheme.primary,
            route = "incidents"
        ),
        ClientFeatureCard(
            title = "Mis Reportes",
            description = "Historial de mis reportes",
            icon = Icons.Default.History,
            color = MaterialTheme.colorScheme.secondary,
            route = "my_reports"
        ),
        ClientFeatureCard(
            title = "Notificaciones",
            description = "Alertas y actualizaciones",
            icon = Icons.Default.Notifications,
            color = MaterialTheme.colorScheme.tertiary,
            route = "notifications"
        )
    )
    
    // Estadísticas para clientes
    val clientStats = listOf(
        ClientStats(
            title = "Mis Reportes",
            value = "${uiState?.incidents?.count { it.usuarioId == uiState?.currentUser?.id } ?: "0"}",
            icon = Icons.Default.Assignment,
            color = Color(0xFF4ECDC4)
        ),
        ClientStats(
            title = "Resueltos",
            value = "${uiState?.incidents?.count { it.usuarioId == uiState?.currentUser?.id && it.estado == com.example.points.models.EstadoIncidente.RESUELTO } ?: "0"}",
            icon = Icons.Default.CheckCircle,
            color = Color(0xFF45B7D1)
        ),
        ClientStats(
            title = "Pendientes",
            value = "${uiState?.incidents?.count { it.usuarioId == uiState?.currentUser?.id && it.estado != com.example.points.models.EstadoIncidente.RESUELTO } ?: "0"}",
            icon = Icons.Default.Pending,
            color = Color(0xFFFF6B6B)
        ),
        ClientStats(
            title = "Puntos",
            value = "1,250",
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
        // Hero Section para Cliente
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo animado
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(alpha = pulseAlpha),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.LocationCity,
                        contentDescription = "SmartCity Logo",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Bienvenido a SmartCity",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Text(
                    text = "POINTS",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Tu plataforma de monitoreo urbano inteligente",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                // Información del usuario cliente
                uiState?.currentUser?.let { user ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Avatar del usuario
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary,
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (user.photoUrl != null) {
                                    // TODO: Cargar imagen real del usuario
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                } else {
                                    Text(
                                        text = user.nombre.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Hola, ${user.nombre}",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color(0xFF4CAF50)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "👤",
                                                fontSize = 12.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "Ciudadano",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                                
                                Text(
                                    text = user.email,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                                
                                Text(
                                    text = "Tipo: ${user.tipo.displayName}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Estadísticas del cliente
        Text(
            text = "📊 Mis Estadísticas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(clientStats) { stat ->
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
                        ClientStatsCard(stat = stat)
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Características principales para clientes
        Text(
            text = "🚀 Funcionalidades Principales",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            clientFeatures.forEach { feature ->
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
                            "create_incident" -> MaterialTheme.colorScheme.errorContainer
                            "incidents" -> MaterialTheme.colorScheme.primaryContainer
                            "my_reports" -> MaterialTheme.colorScheme.secondaryContainer
                            "notifications" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        },
                        iconColor = when (feature.route) {
                            "create_incident" -> MaterialTheme.colorScheme.onErrorContainer
                            "incidents" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "my_reports" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "notifications" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        titleColor = when (feature.route) {
                            "create_incident" -> MaterialTheme.colorScheme.onErrorContainer
                            "incidents" -> MaterialTheme.colorScheme.onPrimaryContainer
                            "my_reports" -> MaterialTheme.colorScheme.onSecondaryContainer
                            "notifications" -> MaterialTheme.colorScheme.onTertiaryContainer
                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                        },
                        subtitleColor = when (feature.route) {
                            "create_incident" -> MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                            "incidents" -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            "my_reports" -> MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                            "notifications" -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
                            else -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        },
                        onClick = {
                            when (feature.route) {
                                "create_incident" -> navController.navigate("create_incident")
                                "incidents" -> navController.navigate("incidents")
                                "my_reports" -> navController.navigate("my_reports")
                                "notifications" -> navController.navigate("notifications")
                            }
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sección de menú adicional
        Text(
            text = "⚙️ Configuración y Herramientas",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Perfil de usuario
            ModernMenuItem(
                title = "Perfil de Usuario",
                subtitle = "Gestiona tu información personal y preferencias",
                icon = Icons.Default.Person,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                titleColor = MaterialTheme.colorScheme.onSecondaryContainer,
                subtitleColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                onClick = { navController.navigate("profile") }
            )
            
            // Configuración
            ModernMenuItem(
                title = "Configuración",
                subtitle = "Ajustes de la aplicación y notificaciones",
                icon = Icons.Default.Settings,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                titleColor = MaterialTheme.colorScheme.onTertiaryContainer,
                subtitleColor = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f),
                onClick = { navController.navigate("settings") }
            )
            
            // Ayuda y soporte
            ModernMenuItem(
                title = "Ayuda y Soporte",
                subtitle = "Centro de ayuda, FAQ y contacto",
                icon = Icons.Default.Help,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                subtitleColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                onClick = { navController.navigate("help") }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Sección de acceso rápido
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.TouchApp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Acceso Rápido",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                
                Text(
                    text = "¿Necesitas reportar algo urgente?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Botón de Emergencia
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("create_incident") },
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
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Emergencia",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White
                            )
                        }
                    }
                    
                    // Botón Ver Mapa
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { navController.navigate("incidents") },
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
                                    Icons.Default.Map,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ver Mapa",
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
                    text = "💡 ¿Sabías que?",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "SmartCity POINTS utiliza inteligencia artificial para optimizar la gestión urbana y mejorar la calidad de vida de todos los ciudadanos.",
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
private fun ClientStatsCard(
    stat: ClientStats,
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
