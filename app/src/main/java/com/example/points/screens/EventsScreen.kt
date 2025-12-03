package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.models.CategoriaEvento
import com.example.points.models.Event
import com.example.points.models.EstadoEvento
import com.example.points.viewmodel.EventViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.constants.ReviewStatus
import com.example.points.constants.ButtonText
import com.example.points.constants.LoadingMessage
import com.example.points.constants.ErrorMessage
import com.example.points.constants.SectionTitle
import com.example.points.constants.ContentDescription
import com.example.points.constants.AppSpacing
import com.example.points.ui.theme.*
import com.example.points.ui.components.*
import com.example.points.utils.getCategoryIcon
import com.example.points.components.OptimizedRoundedImage
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(
    navController: NavController,
    viewModel: EventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var visible by remember { mutableStateOf(false) }
    
    // Estados locales
    var showFilters by remember { mutableStateOf(false) }
    var showSuccessMessage by remember { mutableStateOf(false) }
    var snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }
    
    // Cargar eventos al iniciar
    LaunchedEffect(Unit) {
        viewModel.loadAllEvents()
        viewModel.loadUpcomingEvents()
        viewModel.loadUserEvents()
        visible = true
    }
    
    // Observar cambios en el estado del ViewModel
    LaunchedEffect(uiState.eventCreated) {
        if (uiState.eventCreated) {
            viewModel.clearEventCreated()
            snackbarHostState.showSnackbar(
                message = "✅ Evento creado exitosamente y publicado.",
                duration = SnackbarDuration.Short
            )
        }
    }
    
    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { paddingValues ->
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        // Fondo animado
        AnimatedBackground()
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header moderno con gradiente
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { -40 })
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    withGradient = true,
                    gradientColors = listOf(
                        PointsAccent.copy(alpha = 0.8f),
                        Color(0xFFFF6B9D).copy(alpha = 0.8f)
                    ),
                    elevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "🎉 Eventos",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 24.sp
                                ),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Descubre eventos increíbles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Botón de filtros
                            Surface(
                                onClick = { showFilters = !showFilters },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = if (showFilters) Color.White else Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (showFilters) Icons.Default.Close else Icons.Default.FilterList,
                                        contentDescription = null,
                                        tint = if (showFilters) PointsAccent else Color.White
                                    )
                                }
                            }
                            
                            // Botón de crear evento
                            Surface(
                                onClick = { navController.navigate(com.example.points.constants.AppRoutes.CREATE_EVENT) },
                                modifier = Modifier.size(48.dp),
                                shape = CircleShape,
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = "Crear evento",
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Panel de filtros moderno
            AnimatedVisibility(
                visible = showFilters,
                enter = fadeIn() + expandVertically() + slideInVertically(initialOffsetY = { -20 }),
                exit = fadeOut() + shrinkVertically() + slideOutVertically(targetOffsetY = { -20 })
            ) {
                ModernCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    elevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.FilterList,
                                contentDescription = null,
                                tint = PointsAccent,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Filtros",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Categoría",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                FilterChip(
                                    onClick = { viewModel.clearSearch() },
                                    label = { 
                                        Text(
                                            "Todos",
                                            fontWeight = if (uiState.selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = uiState.selectedCategory == null,
                                    leadingIcon = if (uiState.selectedCategory == null) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                    } else null
                                )
                            }
                            
                            items(CategoriaEvento.values().toList()) { categoria ->
                                FilterChip(
                                    onClick = { 
                                        if (uiState.selectedCategory == categoria) {
                                            viewModel.clearSearch()
                                        } else {
                                            viewModel.loadEventsByCategory(categoria)
                                        }
                                    },
                                    label = { 
                                        Text(
                                            categoria.displayName,
                                            fontWeight = if (uiState.selectedCategory == categoria) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = uiState.selectedCategory == categoria,
                                    leadingIcon = if (uiState.selectedCategory == categoria) {
                                        { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }
            
            // Contenido principal
            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = PointsAccent,
                                strokeWidth = 4.dp
                            )
                            Text(
                                text = "Cargando eventos...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                uiState.errorMessage != null -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernCard(
                            modifier = Modifier.padding(24.dp),
                            elevation = 8.dp
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = PointsError
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = uiState.errorMessage ?: "Error desconocido",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                ModernButton(
                                    text = "Reintentar",
                                    onClick = { viewModel.loadAllEvents() },
                                    icon = Icons.Default.Refresh,
                                    variant = ButtonVariant.Primary
                                )
                            }
                        }
                    }
                }
                
                uiState.events.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.EventBusy,
                                contentDescription = null,
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay eventos disponibles",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Intenta cambiar los filtros o crea un nuevo evento",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            ModernButton(
                                text = "Crear Evento",
                                onClick = { navController.navigate(com.example.points.constants.AppRoutes.CREATE_EVENT) },
                                icon = Icons.Default.Add,
                                variant = ButtonVariant.Primary
                            )
                        }
                    }
                }
                
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            AnimatedVisibility(
                                visible = visible,
                                enter = fadeIn(animationSpec = tween(400, 200))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(PointsAccent.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Event,
                                            contentDescription = null,
                                            tint = PointsAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = if (uiState.selectedCategory != null) {
                                                uiState.selectedCategory!!.displayName
                                            } else {
                                                "Todos los eventos"
                                            },
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Bold
                                            )
                                        )
                                        Text(
                                            text = "${uiState.events.size} eventos encontrados",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        
                        itemsIndexed(uiState.events) { index, event ->
                            var itemVisible by remember { mutableStateOf(false) }
                            
                            LaunchedEffect(visible) {
                                if (visible) {
                                    delay(300L + (index * 80L))
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
                                ModernEventCard(
                                    event = event,
                                    onClick = {
                                        navController.navigate("${com.example.points.constants.AppRoutes.EVENT_DETAIL}/${event.id}")
                                    }
                                )
                            }
                        }
                        
                        // Espaciado inferior
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
        }
        
        // Botón flotante moderno
        var fabScale by remember { mutableStateOf(0f) }
        
        LaunchedEffect(visible) {
            if (visible) {
                delay(800)
                animate(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) { value, _ ->
                    fabScale = value
                }
            }
        }
        
        FloatingActionButton(
            onClick = { navController.navigate(com.example.points.constants.AppRoutes.CREATE_EVENT) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .scale(fabScale),
            containerColor = PointsAccent,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = "Crear Evento",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
    }
}

@Composable
private fun getEventCategoryColor(categoria: CategoriaEvento): Color {
    return when (categoria) {
        CategoriaEvento.CULTURAL -> Color(0xFF4ECDC4)
        CategoriaEvento.DEPORTIVO -> Color(0xFFFF6B6B)
        CategoriaEvento.MUSICAL -> Color(0xFFE91E63)
        CategoriaEvento.EDUCATIVO -> Color(0xFF95E1D3)
        CategoriaEvento.GASTRONOMICO -> Color(0xFFFF9800)
        CategoriaEvento.TECNOLOGICO -> Color(0xFF2196F3)
        CategoriaEvento.ARTISTICO -> Color(0xFF9C27B0)
        CategoriaEvento.COMERCIAL -> Color(0xFF4CAF50)
        CategoriaEvento.RELIGIOSO -> Color(0xFFFFC107)
        CategoriaEvento.COMUNITARIO -> Color(0xFF6BCF7F)
        CategoriaEvento.FESTIVAL -> Color(0xFFFF5722)
        CategoriaEvento.CONFERENCIA -> Color(0xFF3F51B5)
        CategoriaEvento.TALLER -> Color(0xFF009688)
        CategoriaEvento.EXPOSICION -> Color(0xFF795548)
        CategoriaEvento.FERIA -> Color(0xFFCDDC39)
        CategoriaEvento.OTRO -> Color(0xFF9B59B6)
    }
}

@Composable
private fun ModernEventCard(
    event: Event,
    onClick: () -> Unit
) {
    ModernCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Imagen del evento o icono si no hay imagen
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Imagen del evento
                if (event.imagenes.isNotEmpty() && event.imagenes.first().isNotEmpty()) {
                    OptimizedRoundedImage(
                        imageUrl = event.imagenes.first(),
                        contentDescription = "Imagen del evento ${event.nombre}",
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                } else {
                    // Icono por defecto si no hay imagen
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                getEventCategoryColor(event.categoria).copy(alpha = 0.15f),
                                RoundedCornerShape(16.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(event.categoria.displayName),
                            contentDescription = null,
                            tint = getEventCategoryColor(event.categoria),
                            modifier = Modifier.size(48.dp)
                        )
                    }
                }
                
                // Contenido del evento
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = event.nombre,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        // Estado del evento
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when (event.estado) {
                                EstadoEvento.PENDIENTE -> FeedbackWarning.copy(alpha = 0.15f)
                                EstadoEvento.EN_REVISION -> FeedbackInfo.copy(alpha = 0.15f)
                                EstadoEvento.APROBADO -> PointsSuccess.copy(alpha = 0.15f)
                                EstadoEvento.RECHAZADO -> PointsError.copy(alpha = 0.15f)
                                EstadoEvento.CANCELADO -> Color.Gray.copy(alpha = 0.15f)
                                EstadoEvento.FINALIZADO -> Color.DarkGray.copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = when (event.estado) {
                                    EstadoEvento.PENDIENTE -> "⏳"
                                    EstadoEvento.EN_REVISION -> "👀"
                                    EstadoEvento.APROBADO -> "✅"
                                    EstadoEvento.RECHAZADO -> "❌"
                                    EstadoEvento.CANCELADO -> "🚫"
                                    EstadoEvento.FINALIZADO -> "🏁"
                                },
                                modifier = Modifier.padding(8.dp),
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = event.descripcion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Categoría
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = getEventCategoryColor(event.categoria).copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(event.categoria.displayName),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = getEventCategoryColor(event.categoria)
                        )
                        Text(
                            text = event.categoria.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = getEventCategoryColor(event.categoria)
                        )
                    }
                }
                
                // Fecha
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = PointsPrimary.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = PointsPrimary
                        )
                        Text(
                            text = SimpleDateFormat("dd MMM", Locale("es")).format(event.fechaInicio.toDate()),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = PointsPrimary
                        )
                    }
                }
            }
            
            // Ubicación
            if (event.ubicacion.direccion.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = event.ubicacion.direccion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// Mantener la función original para compatibilidad
@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit
) {
    ModernEventCard(event = event, onClick = onClick)
}

@Composable
fun EventCard(
    event: Event,
    onClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header del evento
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = event.nombre,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = getCategoryIcon(event.categoria.icon),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = event.categoria.displayName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
                
                // Estado del evento
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = when (event.estado) {
                            com.example.points.models.EstadoEvento.APROBADO -> 
                                if (event.cancelado) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.primaryContainer
                            com.example.points.models.EstadoEvento.PENDIENTE -> 
                                MaterialTheme.colorScheme.secondaryContainer
                            com.example.points.models.EstadoEvento.EN_REVISION -> 
                                MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                ) {
                    Text(
                        text = getEventReviewStatusText(event),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = when (event.estado) {
                            EstadoEvento.APROBADO -> 
                                if (event.cancelado) MaterialTheme.colorScheme.onErrorContainer
                                else MaterialTheme.colorScheme.onPrimaryContainer
                            EstadoEvento.PENDIENTE -> 
                                MaterialTheme.colorScheme.onSecondaryContainer
                            EstadoEvento.EN_REVISION -> 
                                MaterialTheme.colorScheme.onTertiaryContainer
                            EstadoEvento.RECHAZADO -> 
                                MaterialTheme.colorScheme.onErrorContainer
                            EstadoEvento.CANCELADO -> 
                                MaterialTheme.colorScheme.onErrorContainer
                            EstadoEvento.FINALIZADO -> 
                                MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Descripción
            if (event.descripcion.isNotEmpty()) {
                Text(
                    text = event.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            // Información del evento
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fecha y hora
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatEventDateTime(event),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Ubicación
                if (event.direccion.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = event.direccion,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                
                // Precio
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            event.esGratuito -> "Gratuito"
                            event.precio.precioGeneral != null -> "$${String.format("%.0f", event.precio.precioGeneral)}"
                            else -> "Precio no especificado"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
                
                // Capacidad (si aplica)
                if (event.capacidad != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${event.inscripciones}/${event.capacidad} inscritos",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Botón de acción
            if (event.estado == com.example.points.models.EstadoEvento.APROBADO && !event.cancelado) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (event.requiereInscripcion && (event.capacidad == null || event.inscripciones < event.capacidad)) {
                        Button(
                            onClick = onRegisterClick,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(ButtonText.INSCRIBIRSE.value)
                        }
                    }
                    
                    OutlinedButton(
                        onClick = onClick,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(ButtonText.VER_DETALLES.value)
                    }
                }
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(ButtonText.VER_DETALLES.value)
                }
            }
        }
    }
}

// Función auxiliar para formatear fecha y hora del evento
private fun formatEventDateTime(event: Event): String {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    val startDate = event.fechaInicio.toDate()
    val endDate = event.fechaFin.toDate()
    
    return if (dateFormat.format(startDate) == dateFormat.format(endDate)) {
        // Mismo día
        "${dateFormat.format(startDate)} ${timeFormat.format(startDate)} - ${timeFormat.format(endDate)}"
    } else {
        // Diferentes días
        "${dateFormat.format(startDate)} ${timeFormat.format(startDate)} - ${dateFormat.format(endDate)} ${timeFormat.format(endDate)}"
    }
}

// Texto de estado usando ReviewStatus para consistencia visual
private fun getEventReviewStatusText(event: Event): String {
    val status = when (event.estado) {
        EstadoEvento.PENDIENTE -> ReviewStatus.PENDIENTE
        EstadoEvento.EN_REVISION -> ReviewStatus.EN_REVISION
        EstadoEvento.APROBADO -> if (event.cancelado) ReviewStatus.CANCELADO else ReviewStatus.APROBADO
        EstadoEvento.RECHAZADO -> ReviewStatus.RECHAZADO
        EstadoEvento.CANCELADO -> ReviewStatus.CANCELADO
        EstadoEvento.FINALIZADO -> ReviewStatus.FINALIZADO
    }
    return status.getFormattedText()
}
