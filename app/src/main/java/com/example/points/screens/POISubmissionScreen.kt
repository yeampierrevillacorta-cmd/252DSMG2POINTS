package com.example.points.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.example.points.components.OptimizedAsyncImage
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.example.points.R
import com.example.points.models.CategoriaPOI
import com.example.points.models.CaracteristicaPOI
import com.example.points.models.DiaSemana
import com.example.points.models.Horario
import com.example.points.models.PointOfInterest
import com.example.points.models.RangoPrecio
import com.example.points.models.Ubicacion
import com.example.points.viewmodel.PointOfInterestViewModel
import com.example.points.components.PointsLoading
import com.example.points.components.PointsFeedback
import com.example.points.utils.getCategoryIcon
import com.example.points.services.LocationService
import com.example.points.ui.theme.PointsTheme
import com.example.points.constants.AppRoutes
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POISubmissionScreen(
    navController: NavController,
    initialUbicacion: Ubicacion? = null,
    onUbicacionSelected: (Ubicacion) -> Unit = {},
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<CategoriaPOI?>(null) }
    var direccion by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    var caracteristicas by remember { mutableStateOf<List<CaracteristicaPOI>>(emptyList()) }
    var rangoPrecio by remember { mutableStateOf<RangoPrecio?>(null) }
    var accesibilidad by remember { mutableStateOf(false) }
    var estacionamiento by remember { mutableStateOf(false) }
    var wifi by remember { mutableStateOf(false) }
    
    // Imágenes
    var imagenes by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // Ubicación del usuario
    var ubicacion by remember(initialUbicacion) { 
        mutableStateOf<Ubicacion?>(initialUbicacion) 
    }
    var isLoadingLocation by remember { mutableStateOf(true) }
    var shouldRequestLocation by remember { mutableStateOf(true) }
    
    // Observar cambios en la ubicación desde la navegación
    // SavedStateHandle solo acepta tipos primitivos, así que reconstruimos el objeto
    val savedLat = navController.currentBackStackEntry?.savedStateHandle?.get<Double>("selectedLat")
    val savedLon = navController.currentBackStackEntry?.savedStateHandle?.get<Double>("selectedLon")
    val savedDireccion = navController.currentBackStackEntry?.savedStateHandle?.get<String>("selectedDireccion")
    
    LaunchedEffect(savedLat, savedLon, savedDireccion) {
        if (savedLat != null && savedLon != null) {
            val nuevaUbicacion = Ubicacion(
                lat = savedLat,
                lon = savedLon,
                direccion = savedDireccion ?: ""
            )
            ubicacion = nuevaUbicacion
            direccion = nuevaUbicacion.direccion
            onUbicacionSelected(nuevaUbicacion)
            
            // Limpiar los valores guardados
            navController.currentBackStackEntry?.savedStateHandle?.remove<Double>("selectedLat")
            navController.currentBackStackEntry?.savedStateHandle?.remove<Double>("selectedLon")
            navController.currentBackStackEntry?.savedStateHandle?.remove<String>("selectedDireccion")
        }
    }
    
    // Horarios
    var horarios by remember { mutableStateOf<List<Horario>>(emptyList()) }
    
    // Obtener ubicación del usuario
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    
    
    LaunchedEffect(shouldRequestLocation) {
        if (shouldRequestLocation) {
            try {
                // Verificar permisos primero
                if (locationService.checkPermissions()) {
                    // Iniciar la obtención de ubicación usando el método público
                    locationService.startLocationUpdates().collect { locationState ->
                        if (locationState.latitude != null && locationState.longitude != null) {
                            ubicacion = Ubicacion(
                                lat = locationState.latitude!!,
                                lon = locationState.longitude!!,
                                direccion = "" // La dirección se obtendrá por geocodificación inversa si es necesario
                            )
                            isLoadingLocation = false
                            shouldRequestLocation = false
                        } else if (locationState.error != null) {
                            // Si hay error, detener la carga
                            isLoadingLocation = false
                            shouldRequestLocation = false
                        }
                    }
                } else {
                    // No hay permisos, detener la carga
                    isLoadingLocation = false
                    shouldRequestLocation = false
                }
            } catch (e: Exception) {
                // Si no se puede obtener la ubicación, se mantiene como null
                // El usuario deberá ingresar la ubicación manualmente
                isLoadingLocation = false
                shouldRequestLocation = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = { Text("Agregar Punto de Interés") },
            navigationIcon = {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                }
            }
        )
        
        // Pegar automáticamente la descripción generada en el campo
        LaunchedEffect(uiState.generatedDescription) {
            uiState.generatedDescription?.let { generatedDesc ->
                descripcion = generatedDesc
                viewModel.clearGeneratedDescription()
            }
        }
        
        if (uiState.submitSuccess) {
            PointsFeedback(
                message = "¡Punto de interés enviado exitosamente! Será revisado por nuestros moderadores antes de ser publicado.",
                type = "success",
                onRetry = { 
                    viewModel.clearSubmitSuccess()
                    navController.popBackStack()
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Información básica
                item {
                    BasicInfoSection(
                        nombre = nombre,
                        onNombreChange = { nombre = it },
                        descripcion = descripcion,
                        onDescripcionChange = { descripcion = it },
                        categoria = categoria,
                        onCategoriaChange = { categoria = it },
                        viewModel = viewModel,
                        direccion = direccion
                    )
                }
                
                // Ubicación
                item {
                    LocationSection(
                        direccion = direccion,
                        onDireccionChange = { direccion = it },
                        ubicacion = ubicacion,
                        onUbicacionChange = { ubicacion = it },
                        isLoadingLocation = isLoadingLocation,
                        onRequestLocation = {
                            if (locationService.checkPermissions()) {
                                isLoadingLocation = true
                                shouldRequestLocation = true
                            }
                        },
                        onSelectLocationClick = {
                            navController.navigate(AppRoutes.SELECT_LOCATION_MAP_POI)
                        }
                    )
                }
                
                // Información de contacto
                item {
                    ContactInfoSection(
                        telefono = telefono,
                        onTelefonoChange = { telefono = it },
                        email = email,
                        onEmailChange = { email = it },
                        sitioWeb = sitioWeb,
                        onSitioWebChange = { sitioWeb = it }
                    )
                }
                
                // Imágenes
                item {
                    ImagesSection(
                        imagenes = imagenes,
                        onImagenesChange = { imagenes = it },
                        context = context
                    )
                }
                
                // Características
                item {
                    FeaturesSection(
                        caracteristicas = caracteristicas,
                        onCaracteristicasChange = { caracteristicas = it },
                        rangoPrecio = rangoPrecio,
                        onRangoPrecioChange = { rangoPrecio = it },
                        accesibilidad = accesibilidad,
                        onAccesibilidadChange = { accesibilidad = it },
                        estacionamiento = estacionamiento,
                        onEstacionamientoChange = { estacionamiento = it },
                        wifi = wifi,
                        onWifiChange = { wifi = it }
                    )
                }
                
                // Horarios
                item {
                    ScheduleSection(
                        horarios = horarios,
                        onHorariosChange = { horarios = it }
                    )
                }
                
                // Botón de envío
                item {
                    SubmitButton(
                        isLoading = uiState.isSubmitting,
                        onSubmit = {
                            val poi = PointOfInterest(
                                nombre = nombre,
                                descripcion = descripcion,
                                categoria = categoria ?: CategoriaPOI.OTRO,
                                ubicacion = ubicacion?.copy(direccion = direccion) ?: Ubicacion(lat = 0.0, lon = 0.0, direccion = direccion),
                                direccion = direccion,
                                telefono = telefono.takeIf { it.isNotEmpty() },
                                email = email.takeIf { it.isNotEmpty() },
                                sitioWeb = sitioWeb.takeIf { it.isNotEmpty() },
                                horarios = horarios,
                                imagenes = imagenes,
                                caracteristicas = caracteristicas,
                                precio = rangoPrecio,
                                accesibilidad = accesibilidad,
                                estacionamiento = estacionamiento,
                                wifi = wifi,
                                fechaCreacion = Timestamp.now(),
                                fechaActualizacion = Timestamp.now()
                            )
                            viewModel.submitPOI(poi)
                        },
                        isEnabled = nombre.isNotEmpty() && descripcion.isNotEmpty() && 
                                   categoria != null && direccion.isNotEmpty() &&
                                   ubicacion != null && ubicacion!!.lat != 0.0 && ubicacion!!.lon != 0.0
                    )
                }
                
                // Error message
                uiState.errorMessage?.let { error ->
                    item {
                        PointsFeedback(
                            message = error,
                            type = "error",
                            onRetry = { viewModel.clearError() }
                        )
                    }
                }
                
                // Mostrar error de generación de descripción si existe
                uiState.descriptionGenerationError?.let { error ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { viewModel.clearDescriptionGenerationError() }
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BasicInfoSection(
    nombre: String,
    onNombreChange: (String) -> Unit,
    descripcion: String,
    onDescripcionChange: (String) -> Unit,
    categoria: CategoriaPOI?,
    onCategoriaChange: (CategoriaPOI?) -> Unit,
    viewModel: PointOfInterestViewModel,
    direccion: String = ""
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Información Básica",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = nombre,
                onValueChange = onNombreChange,
                label = { Text("Nombre del lugar *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = nombre.isEmpty()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Campo de descripción con botón para generar con IA
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Descripción *",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium
                        )
                    )
                    
                    // Botón para generar descripción con IA
                    TextButton(
                        onClick = {
                            if (nombre.isNotEmpty() && categoria != null) {
                                viewModel.generateDescription(
                                    nombre = nombre,
                                    categoria = categoria,
                                    direccion = direccion.takeIf { it.isNotEmpty() }
                                )
                            }
                        },
                        enabled = nombre.isNotEmpty() && 
                                 categoria != null && 
                                 !uiState.isGeneratingDescription
                    ) {
                        if (uiState.isGeneratingDescription) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Generando...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Icon(
                                Icons.Default.Create,
                                contentDescription = "Generar con IA",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Generar con IA",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                OutlinedTextField(
                    value = descripcion,
                    onValueChange = onDescripcionChange,
                    label = { Text("Descripción *") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5,
                    isError = descripcion.isEmpty(),
                    enabled = !uiState.isGeneratingDescription
                )
                
                // Mensaje de ayuda
                if (nombre.isNotEmpty() && categoria != null && descripcion.isEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "💡 Haz clic en 'Generar con IA' para crear una descripción automática",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Categoría *",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CategoriaPOI.values()) { cat ->
                    FilterChip(
                        onClick = { onCategoriaChange(if (categoria == cat) null else cat) },
                        label = { Text(cat.displayName, fontSize = 12.sp) },
                        selected = categoria == cat,
                        leadingIcon = {
                            Icon(
                                getCategoryIcon(cat.icon),
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun GeneratedDescriptionCard(
    description: String,
    onUseDescription: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Descripción Generada",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                
                IconButton(
                    onClick = onDismiss
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Descartar",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Descartar")
                }
                
                Button(
                    onClick = onUseDescription,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Usar esta descripción")
                }
            }
        }
    }
}

@Composable
fun LocationSection(
    direccion: String,
    onDireccionChange: (String) -> Unit,
    ubicacion: Ubicacion?,
    onUbicacionChange: (Ubicacion?) -> Unit,
    isLoadingLocation: Boolean,
    onRequestLocation: () -> Unit,
    onSelectLocationClick: () -> Unit = {}
) {
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Mostrar estado de carga de ubicación
            if (isLoadingLocation) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Obteniendo tu ubicación...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Mostrar dirección si hay ubicación seleccionada
            if (ubicacion != null && ubicacion.direccion.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = ubicacion.direccion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            OutlinedTextField(
                value = direccion,
                onValueChange = onDireccionChange,
                label = { Text("Dirección *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = direccion.isEmpty()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para seleccionar en el mapa
                OutlinedButton(
                    onClick = onSelectLocationClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Seleccionar en mapa")
                }
                
                // Botón para usar ubicación actual
                OutlinedButton(
                    onClick = onRequestLocation,
                    modifier = Modifier.weight(1f),
                    enabled = !isLoadingLocation
                ) {
                    if (isLoadingLocation) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Filled.MyLocation, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isLoadingLocation) "Obteniendo..." else "Mi ubicación")
                }
            }
        }
    }
}

@Composable
fun ContactInfoSection(
    telefono: String,
    onTelefonoChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    sitioWeb: String,
    onSitioWebChange: (String) -> Unit
) {
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = telefono,
                onValueChange = onTelefonoChange,
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = sitioWeb,
                onValueChange = onSitioWebChange,
                label = { Text("Sitio web") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )
        }
    }
}

@Composable
fun FeaturesSection(
    caracteristicas: List<CaracteristicaPOI>,
    onCaracteristicasChange: (List<CaracteristicaPOI>) -> Unit,
    rangoPrecio: RangoPrecio?,
    onRangoPrecioChange: (RangoPrecio?) -> Unit,
    accesibilidad: Boolean,
    onAccesibilidadChange: (Boolean) -> Unit,
    estacionamiento: Boolean,
    onEstacionamientoChange: (Boolean) -> Unit,
    wifi: Boolean,
    onWifiChange: (Boolean) -> Unit
) {
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rango de precio
            Text(
                text = "Rango de precio",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(RangoPrecio.values()) { precio ->
                    FilterChip(
                        onClick = { onRangoPrecioChange(if (rangoPrecio == precio) null else precio) },
                        label = { Text(precio.simbolo) },
                        selected = rangoPrecio == precio
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Características especiales
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = accesibilidad,
                        onCheckedChange = onAccesibilidadChange
                    )
                    Text("Accesible")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = estacionamiento,
                        onCheckedChange = onEstacionamientoChange
                    )
                    Text("Estacionamiento")
                }
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = wifi,
                        onCheckedChange = onWifiChange
                    )
                    Text("WiFi")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Características adicionales
            Text(
                text = "Características adicionales",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Medium
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(CaracteristicaPOI.values()) { caracteristica ->
                    FilterChip(
                        onClick = { 
                            val newList = if (caracteristicas.contains(caracteristica)) {
                                caracteristicas - caracteristica
                            } else {
                                caracteristicas + caracteristica
                            }
                            onCaracteristicasChange(newList)
                        },
                        label = { Text(caracteristica.displayName, fontSize = 10.sp) },
                        selected = caracteristicas.contains(caracteristica)
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleSection(
    horarios: List<Horario>,
    onHorariosChange: (List<Horario>) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Horarios",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
                
                TextButton(onClick = { 
                    // Agregar horario por defecto
                    val newHorario = Horario(
                        dia = DiaSemana.LUNES,
                        horaApertura = "09:00",
                        horaCierre = "18:00"
                    )
                    onHorariosChange(horarios + newHorario)
                }) {
                    Text("Agregar")
                }
            }
            
            if (horarios.isEmpty()) {
                Text(
                    text = "No se han agregado horarios",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                horarios.forEachIndexed { index, horario ->
                    ScheduleItem(
                        horario = horario,
                        onUpdate = { newHorario ->
                            val newList = horarios.toMutableList()
                            newList[index] = newHorario
                            onHorariosChange(newList)
                        },
                        onDelete = {
                            onHorariosChange(horarios - horario)
                        }
                    )
                    if (index < horarios.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleItem(
    horario: Horario,
    onUpdate: (Horario) -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Día
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = horario.dia.displayName,
                onValueChange = { },
                readOnly = true,
                label = { Text("Día") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .weight(1f)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DiaSemana.values().forEach { dia ->
                    DropdownMenuItem(
                        text = { Text(dia.displayName) },
                        onClick = {
                            onUpdate(horario.copy(dia = dia))
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Hora apertura
        OutlinedTextField(
            value = horario.horaApertura,
            onValueChange = { onUpdate(horario.copy(horaApertura = it)) },
            label = { Text("Apertura") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Hora cierre
        OutlinedTextField(
            value = horario.horaCierre,
            onValueChange = { onUpdate(horario.copy(horaCierre = it)) },
            label = { Text("Cierre") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        // Botón eliminar
        IconButton(onClick = onDelete) {
            Icon(Icons.Filled.Delete, contentDescription = "Eliminar")
        }
    }
}

@Composable
fun SubmitButton(
    isLoading: Boolean,
    onSubmit: () -> Unit,
    isEnabled: Boolean
) {
    Button(
        onClick = onSubmit,
        modifier = Modifier.fillMaxWidth(),
        enabled = isEnabled && !isLoading,
        shape = RoundedCornerShape(12.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = Color.White
            )
        } else {
            Icon(Icons.Filled.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Enviar Punto de Interés")
        }
    }
}

@Composable
fun ImagesSection(
    imagenes: List<String>,
    onImagenesChange: (List<String>) -> Unit,
    context: android.content.Context
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Imágenes de Referencia",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Agrega imágenes que ayuden a identificar el lugar (opcional)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estado para URI temporal de foto
            var photoUri by remember { mutableStateOf<Uri?>(null) }
            
            // Función helper para subir imagen y agregar a la lista
            fun uploadImageAndAddToList(uri: Uri) {
                val storage = FirebaseStorage.getInstance()
                val storageRef = storage.reference
                val imageRef = storageRef.child("pois/${System.currentTimeMillis()}.jpg")
                
                imageRef.putFile(uri)
                    .addOnSuccessListener {
                        imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                            onImagenesChange(imagenes + downloadUri.toString())
                        }
                    }
                    .addOnFailureListener {
                        // Manejar error
                    }
            }
            
            // Launcher para seleccionar imagen de galería
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                uri?.let {
                    // Subir imagen a Firebase Storage y agregar URL a la lista
                    uploadImageAndAddToList(it)
                }
            }
            
            // Launcher para tomar foto con la cámara
            val cameraLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.TakePicture()
            ) { success ->
                if (success && photoUri != null) {
                    photoUri?.let {
                        uploadImageAndAddToList(it)
                    }
                }
            }
            
            // Launcher para permisos de cámara
            val cameraPermissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // Crear URI para la foto
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "poi_photo_${System.currentTimeMillis()}.jpg")
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
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "poi_photo_${System.currentTimeMillis()}.jpg")
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
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botón para tomar foto
                OutlinedButton(
                    onClick = { takePhoto() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomar foto")
                }
                
                // Botón para seleccionar de galería
                OutlinedButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.Filled.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Galería")
                }
            }
            
            // Mostrar imágenes seleccionadas
            if (imagenes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(imagenes.size) { index ->
                        Card(
                            modifier = Modifier.size(100.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box {
                                OptimizedAsyncImage(
                                    imageUrl = imagenes[index],
                                    contentDescription = "Imagen ${index + 1}",
                                    modifier = Modifier.fillMaxSize()
                                )
                                
                                // Botón para eliminar imagen
                                IconButton(
                                    onClick = {
                                        onImagenesChange(imagenes.filterIndexed { i, _ -> i != index })
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(24.dp)
                                        .background(
                                            MaterialTheme.colorScheme.error,
                                            RoundedCornerShape(12.dp)
                                        )
                                ) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = "Eliminar imagen",
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onError
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

