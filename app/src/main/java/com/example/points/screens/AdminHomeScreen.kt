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
import com.example.points.R
import com.example.points.models.TipoUsuario
import com.example.points.viewmodel.IncidentViewModel
import com.example.points.ui.components.*
import com.example.points.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await

// Importaciones para gráficos
import com.example.points.ui.components.ModernBarChart
import com.example.points.ui.components.HorizontalBarChart
import com.example.points.ui.components.BarChartData

// Datos para las tarjetas de administración
data class AdminFeatureCard(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@Composable
fun AdminHomeScreen(
    navController: NavHostController,
    viewModel: IncidentViewModel? = null
) {
    val uiState by (viewModel?.uiState?.collectAsState() ?: remember { mutableStateOf(null) })
    var visible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        visible = true
    }
    
    // Características para administradores con colores vibrantes
    val adminFeatures = listOf(
        AdminFeatureCard(
            title = "Gestionar Incidentes",
            description = "Administra todos los reportes",
            icon = Icons.Default.Report,
            route = "admin_incidents",
            color = PriorityHigh
        ),
        AdminFeatureCard(
            title = "Mapa de Calor",
            description = "Visualiza densidad de incidentes",
            icon = Icons.Default.Whatshot,
            route = "incident_heatmap",
            color = Color(0xFFFF5722)
        ),
        AdminFeatureCard(
            title = "Moderar Eventos",
            description = "Gestiona eventos pendientes",
            icon = Icons.Default.Event,
            route = "admin_events",
            color = PointsAccent
        ),
        AdminFeatureCard(
            title = "Gestionar POIs",
            description = "Administra puntos de interés",
            icon = Icons.Default.LocationOn,
            route = "admin_pois",
            color = CategoryEnvironment
        ),
        AdminFeatureCard(
            title = "Usuarios",
            description = "Gestiona usuarios del sistema",
            icon = Icons.Default.People,
            route = "admin_users",
            color = PointsPrimary
        ),
        AdminFeatureCard(
            title = "Analíticas",
            description = "Estadísticas y reportes",
            icon = Icons.Default.Analytics,
            route = "admin_analytics",
            color = PointsSecondary
        ),
        AdminFeatureCard(
            title = "Configuración",
            description = "Ajustes del sistema",
            icon = Icons.Default.Settings,
            route = "admin_settings",
            color = PointsSubtle
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
                            Color(0xFF6200EE),
                            Color(0xFF3700B3)
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
                            
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .scale(logoScale)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color.White
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Panel de Administración",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Gestiona el sistema POINTS",
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
                                                    Color(0xFF6200EE),
                                                    Color(0xFF03DAC5)
                                                )
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.AdminPanelSettings,
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
                                    color = when (user.tipo) {
                                        TipoUsuario.ADMINISTRADOR -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                        TipoUsuario.MODERADOR -> Color(0xFF2196F3).copy(alpha = 0.15f)
                                        else -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                    }
                                ) {
                                    Text(
                                        text = when (user.tipo) {
                                            TipoUsuario.ADMINISTRADOR -> "ADMIN"
                                            TipoUsuario.MODERADOR -> "MOD"
                                            else -> "USER"
                                        },
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = when (user.tipo) {
                                            TipoUsuario.ADMINISTRADOR -> Color(0xFF4CAF50)
                                            TipoUsuario.MODERADOR -> Color(0xFF2196F3)
                                            else -> Color(0xFFFF9800)
                                        },
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Estadísticas del sistema con animación
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
                            text = "📊 Estadísticas del Sistema",
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
                            AdminStatCard(
                                value = uiState?.incidents?.size ?: 0,
                                label = "Incidentes",
                                icon = Icons.Default.Report,
                                color = PriorityHigh,
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                value = 1234,
                                label = "Usuarios",
                                icon = Icons.Default.People,
                                color = PointsPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AdminStatCard(
                                value = 89,
                                label = "% Resueltos",
                                icon = Icons.Default.CheckCircle,
                                color = PointsSuccess,
                                suffix = "%",
                                modifier = Modifier.weight(1f)
                            )
                            AdminStatCard(
                                value = 2,
                                label = "Hrs. Promedio",
                                icon = Icons.Default.Timer,
                                color = FeedbackWarning,
                                suffix = "h",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Herramientas de administración
            item {
                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn(animationSpec = tween(600, 450)) +
                            slideInVertically(
                                initialOffsetY = { 40 },
                                animationSpec = tween(600, 450, easing = EaseOutCubic)
                            )
                ) {
                    Text(
                        text = "⚙️ Herramientas de Administración",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        )
                    )
                }
            }
            
            itemsIndexed(adminFeatures) { index, feature ->
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
                                "admin_incidents" -> navController.navigate("admin_incidents")
                                "incident_heatmap" -> navController.navigate("incident_heatmap")
                                "admin_events" -> navController.navigate("admin_events")
                                "admin_pois" -> navController.navigate("admin_pois")
                                "admin_users" -> navController.navigate("admin_users")
                                "admin_analytics" -> navController.navigate("admin_analytics")
                                "admin_settings" -> navController.navigate("admin_settings")
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
                        
                        val auth = com.google.firebase.auth.FirebaseAuth.getInstance()
                        val currentUser = auth.currentUser
                        var userRole by remember { mutableStateOf<TipoUsuario?>(null) }
                        var isLoading by remember { mutableStateOf(true) }
                        
                        LaunchedEffect(currentUser?.uid) {
                            if (currentUser?.uid != null) {
                                try {
                                    val userDoc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                                        .collection("users")
                                        .document(currentUser.uid)
                                        .get()
                                        .await()
                                    
                                    if (userDoc.exists()) {
                                        val tipo = userDoc.getString("tipo")
                                        userRole = when (tipo) {
                                            "ADMINISTRADOR" -> TipoUsuario.ADMINISTRADOR
                                            "MODERADOR" -> TipoUsuario.MODERADOR
                                            "CIUDADANO" -> TipoUsuario.CIUDADANO
                                            else -> null
                                        }
                                    }
                                } catch (e: Exception) {
                                    // Error
                                }
                            }
                            isLoading = false
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            ModernButton(
                                text = "Incidentes",
                                onClick = { navController.navigate("admin_incidents") },
                                icon = Icons.Default.Report,
                                modifier = Modifier.weight(1f),
                                variant = ButtonVariant.Primary
                            )
                            
                            if (userRole == TipoUsuario.ADMINISTRADOR) {
                                ModernButton(
                                    text = "Usuarios",
                                    onClick = { navController.navigate("admin_users") },
                                    icon = Icons.Default.People,
                                    modifier = Modifier.weight(1f),
                                    variant = ButtonVariant.Secondary
                                )
                            }
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
private fun AdminStatCard(
    value: Int,
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    suffix: String = ""
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
                text = "$animatedValue$suffix",
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
