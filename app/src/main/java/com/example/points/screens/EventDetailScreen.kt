package com.example.points.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.points.models.*
import com.example.points.ui.components.ModernCard
import com.example.points.ui.components.ModernButton
import com.example.points.ui.components.ButtonVariant
import com.example.points.ui.theme.*
import com.example.points.utils.getCategoryIcon
import com.example.points.components.OptimizedRoundedImage
import com.example.points.viewmodel.EventViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventDetailScreen(
    navController: NavController,
    eventId: String,
    viewModel: EventViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var event by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(eventId) {
        try {
            isLoading = true
            errorMessage = null
            
            // Cargar el evento específico
            val result = viewModel.getEventById(eventId)
            result.fold(
                onSuccess = { loadedEvent ->
                    event = loadedEvent
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = "Error al cargar los detalles del evento: ${error.message}"
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            errorMessage = "Error al cargar los detalles del evento: ${e.message}"
            isLoading = false
        }
    }
    
    Box(modifier = Modifier.fillMaxSize()) {
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
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Header moderno con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B9D),
                                Color(0xFFFF8E9B)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    
                    Text(
                        text = "Detalles del Evento",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color.White
                    )
                }
            }
            
            // Contenido principal
            when {
                isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(
                                color = PointsPrimary,
                                modifier = Modifier.size(64.dp),
                                strokeWidth = 6.dp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            Text(
                                text = "Cargando detalles...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }
                }
                
                errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(PointsError.copy(0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.Error,
                                        contentDescription = null,
                                        tint = PointsError,
                                        modifier = Modifier.size(40.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "Error al cargar el evento",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = errorMessage ?: "Error desconocido",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(0.7f),
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                ModernButton(
                                    text = "Volver",
                                    onClick = { navController.popBackStack() },
                                    variant = ButtonVariant.Primary,
                                    icon = Icons.Default.ArrowBack,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
                
                event != null -> {
                    EventDetailContent(
                        event = event!!,
                        navController = navController
                    )
                }
                
                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        ModernCard(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(16.dp),
                                modifier = Modifier.padding(32.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(80.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Filled.EventBusy,
                                        contentDescription = null,
                                        modifier = Modifier.size(40.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "Evento no encontrado",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "El evento que buscas no existe o ha sido eliminado.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                ModernButton(
                                    text = "Volver",
                                    onClick = { navController.popBackStack() },
                                    variant = ButtonVariant.Primary,
                                    icon = Icons.Default.ArrowBack,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventDetailContent(
    event: Event,
    navController: NavController
) {
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Imágenes del evento
        if (event.imagenes.isNotEmpty()) {
            item {
                EventImagesSection(imagenes = event.imagenes)
            }
        } else {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Filled.Image,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin imágenes disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Información principal
        item {
            Column {
                Text(
                    text = event.nombre,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                getCategoryIcon(event.categoria.displayName),
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
                            text = event.estado.displayName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = when (event.estado) {
                                EstadoEvento.PENDIENTE -> FeedbackWarning
                                EstadoEvento.EN_REVISION -> FeedbackInfo
                                EstadoEvento.APROBADO -> PointsSuccess
                                EstadoEvento.RECHAZADO -> PointsError
                                EstadoEvento.CANCELADO -> Color.Gray
                                EstadoEvento.FINALIZADO -> Color.DarkGray
                            }
                        )
                    }
                }
            }
        }
        
        // Descripción
        if (event.descripcion.isNotEmpty()) {
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Descripción",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = event.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
        
        // Fechas y horarios
        item {
            ModernCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Fecha y Hora",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = PointsPrimary
                                )
                                Text(
                                    text = "Inicio",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateFormat.format(event.fechaInicio.toDate()),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = event.horaInicio,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Event,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = PointsPrimary
                                )
                                Text(
                                    text = "Fin",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = dateFormat.format(event.fechaFin.toDate()),
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            Text(
                                text = event.horaFin,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
        
        // Ubicación
        if (event.ubicacion.direccion.isNotEmpty()) {
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = PointsError
                            )
                            Text(
                                text = "Ubicación",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = event.ubicacion.direccion,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
        
        // Organizador y contacto
        if (event.organizador.isNotEmpty() || event.contacto.telefono != null || event.contacto.email != null) {
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Información de Contacto",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        if (event.organizador.isNotEmpty()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = event.organizador,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        event.contacto.telefono?.let { telefono ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Phone,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = telefono,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                        
                        event.contacto.email?.let { email ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Email,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = email,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Precio
        item {
            ModernCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.AttachMoney,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = PointsSuccess
                        )
                        Text(
                            text = "Precio",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    if (event.precio.esGratuito) {
                        Text(
                            text = "Gratis",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = PointsSuccess
                            )
                        )
                    } else {
                        event.precio.precioGeneral?.let { precio ->
                            Text(
                                text = "S/ ${String.format("%.2f", precio)}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }
        
        // Características adicionales
        val caracteristicas = mutableListOf<String>()
        if (event.accesibilidad) caracteristicas.add("Accesible")
        if (event.estacionamiento) caracteristicas.add("Estacionamiento")
        if (event.transportePublico) caracteristicas.add("Transporte Público")
        if (event.requiereInscripcion) caracteristicas.add("Requiere Inscripción")
        if (event.capacidad != null) caracteristicas.add("Capacidad: ${event.capacidad}")
        if (event.edadMinima != null || event.edadMaxima != null) {
            val edadRange = when {
                event.edadMinima != null && event.edadMaxima != null -> 
                    "${event.edadMinima}-${event.edadMaxima} años"
                event.edadMinima != null -> "Desde ${event.edadMinima} años"
                else -> "Hasta ${event.edadMaxima} años"
            }
            caracteristicas.add(edadRange)
        }
        
        if (caracteristicas.isNotEmpty()) {
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Características",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        caracteristicas.forEach { caracteristica ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = PointsSuccess
                                )
                                Text(
                                    text = caracteristica,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Etiquetas
        if (event.etiquetas.isNotEmpty()) {
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Etiquetas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(event.etiquetas) { etiqueta ->
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = etiqueta,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Sitio web
        event.sitioWeb?.let { sitioWeb ->
            if (sitioWeb.isNotEmpty()) {
                item {
                    ModernCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Sitio Web",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = sitioWeb,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        // Espaciado inferior
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun EventImagesSection(imagenes: List<String>) {
    if (imagenes.isEmpty()) return
    
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (imagenes.size == 1) {
            // Una sola imagen - mostrar grande
            OptimizedRoundedImage(
                imageUrl = imagenes.first(),
                contentDescription = "Imagen del evento",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        } else {
            // Múltiples imágenes - carrusel
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imagenes) { imagenUrl ->
                    OptimizedRoundedImage(
                        imageUrl = imagenUrl,
                        contentDescription = "Imagen del evento",
                        modifier = Modifier
                            .size(200.dp)
                            .clip(RoundedCornerShape(12.dp))
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

