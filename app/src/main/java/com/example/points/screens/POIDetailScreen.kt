package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.points.R
import com.example.points.models.PointOfInterest
import com.example.points.models.CaracteristicaPOI
import com.example.points.models.DiaSemana
import com.example.points.models.CategoriaPOI
import com.example.points.models.Ubicacion
import com.example.points.viewmodel.PointOfInterestViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.utils.getCategoryIcon
import com.example.points.ui.theme.PointsTheme
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POIDetailScreen(
    navController: NavController,
    poiId: String,
    viewModel: PointOfInterestViewModel = viewModel()
) {
    var poi by remember { mutableStateOf<PointOfInterest?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    LaunchedEffect(poiId) {
        try {
            isLoading = true
            errorMessage = null
            
            // Cargar el POI específico desde el repositorio
            val result = viewModel.getPOIById(poiId)
            result.fold(
                onSuccess = { loadedPOI ->
                    poi = loadedPOI
                    isLoading = false
                },
                onFailure = { error ->
                    errorMessage = "Error al cargar los detalles del punto de interés: ${error.message}"
                    isLoading = false
                }
            )
        } catch (e: Exception) {
            errorMessage = "Error al cargar los detalles del punto de interés: ${e.message}"
            isLoading = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Detalles") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
            },
            actions = {
                IconButton(onClick = { /* Compartir */ }) {
                    Icon(Icons.Filled.Share, contentDescription = "Compartir")
                }
                IconButton(onClick = { /* Favoritos */ }) {
                    Icon(Icons.Filled.FavoriteBorder, contentDescription = "Favorito")
                }
            }
        )
        
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    PointsLoading(message = "Cargando detalles...")
                }
            }
            
            errorMessage != null -> {
                PointsFeedback(
                    message = errorMessage ?: "Error desconocido",
                    type = "error",
                    onRetry = { /* Retry logic */ }
                )
            }
            
            poi != null -> {
                POIDetailContent(poi = poi!!, navController = navController)
            }
            
            else -> {
                // POI no encontrado
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Filled.LocationOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Punto de interés no encontrado",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "El punto de interés que buscas no existe o ha sido eliminado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Button(
                            onClick = { navController.popBackStack() }
                        ) {
                            Text("Volver")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun POIDetailContent(poi: PointOfInterest, navController: NavController) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Imágenes del POI
        if (poi.imagenes.isNotEmpty()) {
            item {
                POIImagesSection(imagenes = poi.imagenes)
            }
        } else {
            // Placeholder cuando no hay imágenes
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
                    text = poi.nombre,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        getCategoryIcon(poi.categoria.icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = poi.categoria.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Calificación
                if (poi.calificacion > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Star,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format("%.1f", poi.calificacion),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "(${poi.totalCalificaciones} reseñas)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        // Descripción
        if (poi.descripcion.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
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
                            text = poi.descripcion,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
        
        // Información de contacto
        item {
            ContactInfoCard(poi = poi)
        }
        
        // Horarios
        if (poi.horarios.isNotEmpty()) {
            item {
                ScheduleCard(horarios = poi.horarios)
            }
        }
        
        // Características
        if (poi.caracteristicas.isNotEmpty()) {
            item {
                FeaturesCard(caracteristicas = poi.caracteristicas)
            }
        }
        
        // Ubicación
        item {
            LocationCard(poi = poi, navController = navController)
        }
        
        // Botones de acción
        item {
            ActionButtons(poi = poi)
        }
    }
}

@Composable
fun ContactInfoCard(poi: PointOfInterest) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información de Contacto",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Dirección
            Row(
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = poi.direccion,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Teléfono
            poi.telefono?.let { telefono ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Phone,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = telefono,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Email
            poi.email?.let { email ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Email,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            // Sitio web
            poi.sitioWeb?.let { sitioWeb ->
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Language,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
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

@Composable
fun ScheduleCard(horarios: List<com.example.points.models.Horario>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Horarios",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            horarios.forEach { horario ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = horario.dia.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    
                    Text(
                        text = if (horario.cerrado) {
                            "Cerrado"
                        } else {
                            "${horario.horaApertura} - ${horario.horaCierre}"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (horario.cerrado) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                }
                
                if (horario != horarios.last()) {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
fun FeaturesCard(caracteristicas: List<CaracteristicaPOI>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Características",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(caracteristicas) { caracteristica ->
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = caracteristica.displayName,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun LocationCard(poi: PointOfInterest, navController: NavController) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Ubicación",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Mapa placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Filled.Map,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Mapa interactivo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { 
                        // Navegar al mapa con las coordenadas del POI
                        val lat = poi.ubicacion.lat
                        val lon = poi.ubicacion.lon
                        navController.navigate("poi_map/$lat/$lon")
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Navigation,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Cómo llegar")
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                OutlinedButton(
                    onClick = { /* Compartir ubicación */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Compartir")
                }
            }
        }
    }
}

@Composable
fun ActionButtons(poi: PointOfInterest) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { /* Llamar */ },
            modifier = Modifier.fillMaxWidth(),
            enabled = !poi.telefono.isNullOrEmpty()
        ) {
            Icon(
                Icons.Filled.Phone,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Llamar")
        }
        
        OutlinedButton(
            onClick = { /* Agregar a favoritos */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Agregar a favoritos")
        }
        
        OutlinedButton(
            onClick = { /* Reportar problema */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                Icons.Filled.Report,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reportar problema")
        }
    }
}

@Composable
fun POIImagesSection(imagenes: List<String>) {
    var selectedImageIndex by remember { mutableStateOf(0) }
    
    Column {
        // Imagen principal
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            AsyncImage(
                model = imagenes[selectedImageIndex],
                contentDescription = "Imagen del POI",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                error = painterResource(id = R.drawable.placeholder_poi),
                placeholder = painterResource(id = R.drawable.placeholder_poi)
            )
        }
        
        // Indicador de imagen actual si hay múltiples imágenes
        if (imagenes.size > 1) {
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(imagenes.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index == selectedImageIndex) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                                }
                            )
                    )
                    if (index < imagenes.size - 1) {
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Miniaturas de imágenes
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(imagenes.size) { index ->
                    Card(
                        modifier = Modifier
                            .size(60.dp)
                            .clickable { selectedImageIndex = index },
                        shape = RoundedCornerShape(8.dp),
                        border = if (index == selectedImageIndex) {
                            androidx.compose.foundation.BorderStroke(
                                2.dp, 
                                MaterialTheme.colorScheme.primary
                            )
                        } else null
                    ) {
                        AsyncImage(
                            model = imagenes[index],
                            contentDescription = "Miniatura ${index + 1}",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            error = painterResource(id = R.drawable.placeholder_poi),
                            placeholder = painterResource(id = R.drawable.placeholder_poi)
                        )
                    }
                }
            }
        }
    }
}

