package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.points.constants.AppRoutes
import com.example.points.R
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.ui.components.*
import com.example.points.ui.theme.*
import kotlinx.coroutines.delay

// Datos para las tarjetas de características del cliente
data class ClientFeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun ClientHomeScreen(
    navController: NavHostController,
    viewModel: IncidentViewModel? = null
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) })
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Características para clientes con colores vibrantes
    val clientFeatures = listOf(
        ClientFeatureCard(
            title = "Reportar Problema",
            description = "Informa incidentes urbanos",
            icon = Icons.Default.Report,
            route = "create_incident",
            color = PriorityHigh
        ),
        ClientFeatureCard(
            title = "Ver Mapa",
            description = "Explora incidentes cercanos",
            icon = Icons.Default.Map,
            route = "incidents",
            color = MapUserLocation
        ),
        ClientFeatureCard(
            title = "Eventos",
            description = "Descubre eventos en tu localidad",
            icon = Icons.Default.Event,
            route = "events",
            color = PointsAccent
        ),
        ClientFeatureCard(
            title = "Lugares de Interés",
            description = "Explora POIs cercanos",
            icon = Icons.Default.LocationOn,
            route = AppRoutes.POI_LIST,
            color = CategoryEnvironment
        ),
        ClientFeatureCard(
            title = "Mis Reportes",
            description = "Historial de mis reportes",
            icon = Icons.Default.History,
            route = "my_reports",
            color = PointsSecondary
        ),
        ClientFeatureCard(
            title = "Notificaciones",
            description = "Alertas y actualizaciones",
            icon = Icons.Default.Notifications,
            route = "notifications",
            color = FeedbackWarning
        )
    )
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo animado
        AnimatedBackground()
        
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Espaciado superior
            item {
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Título principal con animación
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically(
                        initialOffsetY = { -40 },
                        animationSpec = tween(600, easing = EaseOutCubic)
                    )
                ) {
                    ModernCard(
                        modifier = Modifier.fillMaxWidth(),
                        withGradient = true,
                        gradientColors = listOf(
                            PointsSecondary.copy(alpha = 0.9f),
                            PointsAccent.copy(alpha = 0.9f)
                        ),
                        elevation = 12.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Logo animado
                            var logoScale by remember { mutableStateOf(0.5f) }
                            LaunchedEffect(Unit) {
                                animate(
                                    initialValue = 0.5f,
                                    targetValue = 1f,
                                    animationSpec = spring(
                                        dampingRatio = Spring.DampingRatioMediumBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                ) { value, _ ->
                                    logoScale = value
                                }
                            }
                            
                            Image(
                                painter = painterResource(id = R.drawable.logo),
                                contentDescription = "Logo POINTS",
                                modifier = Modifier
                                    .size(64.dp)
                                    .scale(logoScale)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Panel de Cliente",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Gestiona tus reportes y consultas",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontSize = 16.sp
                                ),
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
            }
            
            // Información del usuario con animación
            uiState?.currentUser?.let { user ->
                item {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(animationSpec = tween(600, 200)) +
                                slideInVertically(
                                    initialOffsetY = { 40 },
                                    animationSpec = tween(600, 200, easing = EaseOutCubic)
                                )
                    ) {
                        ModernCard(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Avatar circular con gradiente
                                Box(
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.linearGradient(
                                                colors = listOf(
                                                    PointsSecondary,
                                                    PointsAccent
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        modifier = Modifier.size(32.dp),
                                        tint = Color.White
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Hola, ${user.nombre} 👋",
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = user.email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = PointsSecondary.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "CLIENTE",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = PointsSecondary,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Estadísticas personales con contadores animados
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, 300)) +
                            slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = tween(600, 300, easing = EaseOutCubic)
                            )
                ) {
                    Column {
                        Text(
                            text = "📊 Mis Estadísticas",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            StatCard(
                                value = 5,
                                label = "Reportes",
                                icon = Icons.Default.Report,
                                color = PriorityMedium,
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                value = 3,
                                label = "Resueltos",
                                icon = Icons.Default.CheckCircle,
                                color = PointsSuccess,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Funcionalidades principales
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, 400)) +
                            slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = tween(600, 400, easing = EaseOutCubic)
                            )
                ) {
                    Text(
                        text = "✨ Funcionalidades",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                }
            }
            
            itemsIndexed(clientFeatures) { index, feature ->
                var itemVisible by remember { mutableStateOf(false) }
                
                LaunchedEffect(visible) {
                    if (visible) {
                        delay(500L + (index * 100L))
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
                    ModernCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            when (feature.route) {
                                "create_incident" -> navController.navigate(AppRoutes.CREATE_INCIDENT)
                                "incidents" -> navController.navigate(AppRoutes.INCIDENTS)
                                "events" -> navController.navigate(AppRoutes.EVENTS)
                                AppRoutes.POI_LIST -> navController.navigate(AppRoutes.POI_LIST)
                                "my_reports" -> navController.navigate("my_reports")
                                "notifications" -> navController.navigate("notifications")
                            }
                        },
                        elevation = 6.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Icono con fondo circular de color
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(feature.color.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = feature.icon,
                                    contentDescription = feature.title,
                                    modifier = Modifier.size(28.dp),
                                    tint = feature.color
                                )
                            }
                            
                            Spacer(modifier = Modifier.width(16.dp))
                            
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = feature.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = feature.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            // Acceso rápido
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, 700))
                ) {
                    Column {
                        Text(
                            text = "🚀 Acceso Rápido",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernButton(
                                text = "Reportar",
                                onClick = { navController.navigate("create_incident") },
                                icon = Icons.Default.Report,
                                modifier = Modifier.weight(1f),
                                variant = ButtonVariant.Primary
                            )
                            ModernButton(
                                text = "Ver Mapa",
                                onClick = { navController.navigate("incidents") },
                                icon = Icons.Default.Map,
                                modifier = Modifier.weight(1f),
                                variant = ButtonVariant.Secondary
                            )
                        }
                    }
                }
            }
            
            // Espaciado inferior
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    var animatedValue by remember { mutableStateOf(0) }
    
    LaunchedEffect(value) {
        animate(
            initialValue = 0f,
            targetValue = value.toFloat(),
            animationSpec = tween(1000, easing = EaseOutCubic)
        ) { animValue, _ ->
            animatedValue = animValue.toInt()
        }
    }
    
    ModernCard(
        modifier = modifier,
        elevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = color
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "$animatedValue",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                ),
                color = color
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
