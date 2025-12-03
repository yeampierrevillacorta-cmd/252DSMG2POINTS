package com.example.points.screens

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.components.OptimizedAsyncImage
import com.example.points.models.TipoIncidente
import com.example.points.models.Ubicacion
import com.example.points.ui.components.ModernCard
import com.example.points.ui.components.ModernTextField
import com.example.points.ui.theme.*
import com.example.points.utils.GeocodingUtils
import com.example.points.viewmodel.IncidentViewModel
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateIncidentScreen(
    onBackClick: () -> Unit,
    onIncidentCreated: () -> Unit,
    onSelectLocationClick: () -> Unit = {},
    viewModel: IncidentViewModel = viewModel()
) {
    val context = LocalContext.current
    val createState by viewModel.createIncidentState.collectAsState()
    
    // Log para debugging - observar cambios en la ubicación
    LaunchedEffect(createState.ubicacion) {
        android.util.Log.d("CreateIncidentScreen", "Ubicación en el estado: lat=${createState.ubicacion.lat}, lon=${createState.ubicacion.lon}, direccion=${createState.ubicacion.direccion}")
    }
    
    // URI temporal para la foto tomada
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Launcher para seleccionar imagen de galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        viewModel.updateSelectedImage(uri)
    }
    
    // Launcher para tomar foto con la cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            viewModel.updateSelectedImage(photoUri)
        }
    }
    
    // Launcher para permisos de cámara
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Crear URI para la foto
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "incident_photo_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Points")
                }
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            photoUri = uri
            uri?.let { cameraLauncher.launch(it) }
        }
    }
    
    // Función para tomar foto
    fun takePhoto() {
        val hasCameraPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        
        if (hasCameraPermission) {
            // Crear URI para la foto
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "incident_photo_${System.currentTimeMillis()}.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/Points")
                }
            }
            
            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            photoUri = uri
            uri?.let { cameraLauncher.launch(it) }
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Launcher para permisos de ubicación
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        
        if (granted) {
            getCurrentLocation(context) { ubicacion ->
                viewModel.updateLocation(ubicacion)
            }
        }
    }
    
    // Efecto para manejar el éxito de la creación
    LaunchedEffect(createState.submitSuccess) {
        if (createState.submitSuccess) {
            onIncidentCreated()
            viewModel.resetCreateIncidentState()
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Fondo con gradiente
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFFF8F0),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        )
        
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header moderno
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFF6B6B),
                                Color(0xFFFF8E53)
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
                            .clickable { onBackClick() },
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
                    
                    Column {
                        Text(
                            text = "Reportar Incidente",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Ayuda a tu comunidad",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(0.9f)
                        )
                    }
                }
            }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
            // Selector de tipo de incidente
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PointsPrimary.copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = PointsPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Tipo de Incidente",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "*",
                                color = Color.Red,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(TipoIncidente.values()) { tipo ->
                                FilterChip(
                                    onClick = { viewModel.updateIncidentType(tipo) },
                                    label = { 
                                        Text(
                                            tipo.displayName,
                                            fontWeight = if (createState.tipo == tipo) FontWeight.Bold else FontWeight.Normal
                                        ) 
                                    },
                                    selected = createState.tipo == tipo,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PointsPrimary,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Descripción
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF4ECDC4).copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF4ECDC4),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Descripción",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "*",
                                color = Color.Red,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        OutlinedTextField(
                            value = createState.descripcion,
                            onValueChange = viewModel::updateDescription,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Describe el incidente en detalle...") },
                            minLines = 4,
                            maxLines = 6,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PointsPrimary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            
            // Ubicación
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF6BCF7F).copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF6BCF7F),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Ubicación",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "*",
                                color = Color.Red,
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AnimatedVisibility(
                            visible = createState.ubicacion.lat != 0.0 && createState.ubicacion.lon != 0.0,
                            enter = fadeIn() + expandVertically()
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFF6BCF7F).copy(0.1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF6BCF7F),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (createState.ubicacion.direccion.isNotEmpty()) {
                                                createState.ubicacion.direccion
                                            } else {
                                                "Coordenadas: ${String.format("%.4f", createState.ubicacion.lat)}, ${String.format("%.4f", createState.ubicacion.lon)}"
                                            },
                                            style = MaterialTheme.typography.bodyLarge,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botón para seleccionar en el mapa
                            OutlinedButton(
                                onClick = onSelectLocationClick,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("En Mapa", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            }
                            
                            // Botón para usar ubicación actual
                            Button(
                                onClick = {
                                    val hasLocationPermission = ContextCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED
                                    
                                    if (hasLocationPermission) {
                                        getCurrentLocation(context) { ubicacion ->
                                            viewModel.updateLocation(ubicacion)
                                        }
                                    } else {
                                        locationPermissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF6BCF7F)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mi Ubicación", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            }
                        }
                    }
                }
            }
            
            // Imagen
            item {
                ModernCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFFFFBE0B).copy(0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.PhotoCamera,
                                    contentDescription = null,
                                    tint = Color(0xFFFFBE0B),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Text(
                                text = "Fotografía",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "(Opcional)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        AnimatedVisibility(
                            visible = createState.selectedImageUri != null,
                            enter = fadeIn() + expandVertically()
                        ) {
                            OptimizedAsyncImage(
                                imageUrl = createState.selectedImageUri?.toString(),
                                contentDescription = "Imagen seleccionada",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(220.dp)
                                    .clip(RoundedCornerShape(16.dp))
                            )
                        }
                        
                        if (createState.selectedImageUri != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Botón para tomar foto
                            OutlinedButton(
                                onClick = { takePhoto() },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Cámara", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            }
                            
                            // Botón para seleccionar de galería
                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFBE0B)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Galería", fontSize = MaterialTheme.typography.bodyMedium.fontSize)
                            }
                        }
                    }
                }
            }
            
            // Error message
            createState.errorMessage?.let { error ->
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + expandVertically()
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFF6B6B).copy(0.15f)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = Color(0xFFFF6B6B),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = error,
                                    color = Color(0xFFFF6B6B),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
            
            // Botón de envío
            item {
                Button(
                    onClick = viewModel::createIncident,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !createState.isSubmitting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PointsPrimary,
                        disabledContainerColor = PointsPrimary.copy(0.5f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (createState.isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Enviando...", style = MaterialTheme.typography.titleMedium)
                    } else {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Reportar Incidente", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            
                // Spacer para el bottom padding
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

private fun getCurrentLocation(
    context: android.content.Context,
    onLocationReceived: (Ubicacion) -> Unit
) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    
    try {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                // Obtener dirección mediante geocodificación inversa
                CoroutineScope(Dispatchers.IO).launch {
                    val address = GeocodingUtils.getAddressFromCoordinates(
                        context,
                        it.latitude,
                        it.longitude
                    ) ?: "Ubicación actual"
                    
                    val ubicacion = Ubicacion(
                        lat = it.latitude,
                        lon = it.longitude,
                        direccion = address
                    )
                    CoroutineScope(Dispatchers.Main).launch {
                        onLocationReceived(ubicacion)
                    }
                }
            }
        }
    } catch (e: SecurityException) {
        // Manejar excepción de seguridad
    }
}
