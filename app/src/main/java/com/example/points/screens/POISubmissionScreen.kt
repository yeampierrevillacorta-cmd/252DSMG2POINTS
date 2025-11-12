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
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun POISubmissionScreen(
    navController: NavController,
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
    var ubicacion by remember { mutableStateOf<Ubicacion?>(null) }
    var isLoadingLocation by remember { mutableStateOf(true) }
    var shouldRequestLocation by remember { mutableStateOf(true) }
    
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
                        onCategoriaChange = { categoria = it }
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
                        onImagenesChange = { imagenes = it }
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
    onCategoriaChange: (CategoriaPOI?) -> Unit
) {
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
            
            OutlinedTextField(
                value = descripcion,
                onValueChange = onDescripcionChange,
                label = { Text("Descripción *") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 5,
                isError = descripcion.isEmpty()
            )
            
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
fun LocationSection(
    direccion: String,
    onDireccionChange: (String) -> Unit,
    ubicacion: Ubicacion?,
    onUbicacionChange: (Ubicacion?) -> Unit,
    isLoadingLocation: Boolean,
    onRequestLocation: () -> Unit
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
            
            OutlinedTextField(
                value = direccion,
                onValueChange = onDireccionChange,
                label = { Text("Dirección *") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = direccion.isEmpty()
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Coordenadas (solo si se obtuvo la ubicación)
            if (ubicacion != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = ubicacion.lat.toString(),
                        onValueChange = { 
                            it.toDoubleOrNull()?.let { lat ->
                                onUbicacionChange(ubicacion.copy(lat = lat))
                            }
                        },
                        label = { Text("Latitud") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    
                    OutlinedTextField(
                        value = ubicacion.lon.toString(),
                        onValueChange = { 
                            it.toDoubleOrNull()?.let { lon ->
                                onUbicacionChange(ubicacion.copy(lon = lon))
                            }
                        },
                        label = { Text("Longitud") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "✅ Ubicación obtenida automáticamente",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else if (!isLoadingLocation) {
                // Si no se pudo obtener la ubicación, mostrar campos vacíos
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = "",
                        onValueChange = { 
                            it.toDoubleOrNull()?.let { lat ->
                                onUbicacionChange(Ubicacion(lat = lat, lon = 0.0, direccion = ""))
                            }
                        },
                        label = { Text("Latitud") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("Ej: -12.0464") }
                    )
                    
                    OutlinedTextField(
                        value = "",
                        onValueChange = { 
                            it.toDoubleOrNull()?.let { lon ->
                                onUbicacionChange(Ubicacion(lat = 0.0, lon = lon, direccion = ""))
                            }
                        },
                        label = { Text("Longitud") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        placeholder = { Text("Ej: -77.0428") }
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "⚠️ No se pudo obtener tu ubicación automáticamente. Verifica que el GPS esté activado y los permisos de ubicación otorgados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedButton(
                onClick = onRequestLocation,
                modifier = Modifier.fillMaxWidth(),
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
                Text(if (isLoadingLocation) "Obteniendo ubicación..." else "Usar ubicación actual")
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
    onImagenesChange: (List<String>) -> Unit
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
            
            // Botón para agregar imagen
            OutlinedButton(
                        onClick = {
                            // TODO: Implementar selector de imágenes real
                            // Por ahora, no se pueden agregar imágenes hasta implementar el selector
                        },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.Filled.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar Imagen")
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
                                AsyncImage(
                                    model = imagenes[index],
                                    contentDescription = "Imagen ${index + 1}",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    error = painterResource(id = R.drawable.placeholder_poi),
                                    placeholder = painterResource(id = R.drawable.placeholder_poi)
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

