package com.example.points.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.points.constants.ButtonText
import com.example.points.constants.ErrorMessage
import com.example.points.constants.SuccessMessage
import com.example.points.models.*
import com.example.points.services.LocationService
import com.example.points.utils.getCategoryIcon
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CreateEventDialog(
    onDismiss: () -> Unit,
    onCreateEvent: (Event) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorShown: () -> Unit = {}
) {
    val context = LocalContext.current
    val locationService = remember { LocationService(context) }
    
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(CategoriaEvento.CULTURAL) }
    var direccion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf<Double?>(null) }
    var longitud by remember { mutableStateOf<Double?>(null) }
    var fechaInicio by remember { mutableStateOf<Date?>(null) }
    var fechaFin by remember { mutableStateOf<Date?>(null) }
    var horaInicio by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var horaFin by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var organizador by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var esGratuito by remember { mutableStateOf(true) }
    var precioGeneral by remember { mutableStateOf("") }
    var capacidad by remember { mutableStateOf("") }
    var requiereInscripcion by remember { mutableStateOf(false) }
    var edadMinima by remember { mutableStateOf("") }
    var edadMaxima by remember { mutableStateOf("") }
    var accesibilidad by remember { mutableStateOf(false) }
    var estacionamiento by remember { mutableStateOf(false) }
    var transportePublico by remember { mutableStateOf(false) }
    var etiquetas by remember { mutableStateOf("") }
    var sitioWeb by remember { mutableStateOf("") }
    
    var validationError by remember { mutableStateOf<String?>(null) }
    var isGettingLocation by remember { mutableStateOf(false) }
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    
    // Obtener ubicación del usuario si tiene permisos
    LaunchedEffect(Unit) {
        if (locationService.checkPermissions() && locationService.isLocationEnabled()) {
            isGettingLocation = true
            try {
                val locationState = locationService.getCurrentLocation()
                locationState.latitude?.let { lat ->
                    locationState.longitude?.let { lon ->
                        latitud = lat
                        longitud = lon
                    }
                }
            } catch (e: Exception) {
                // Error silencioso, el usuario puede ingresar manualmente
            } finally {
                isGettingLocation = false
            }
        }
    }
    
    // Mostrar error si existe
    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            validationError = errorMessage
            onErrorShown()
        }
    }
    
    Dialog(
        onDismissRequest = {
            if (!isLoading) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.95f),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Crear Evento",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar"
                        )
                    }
                }
                
                Divider()
                
                // Contenido scrolleable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Mensaje de error de validación
                    if (validationError != null) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = validationError ?: "",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                                IconButton(
                                    onClick = { validationError = null },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cerrar",
                                        tint = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }
                        }
                    }
                    
                    // Estado de carga
                    if (isLoading) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Creando evento...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Información básica
                    Text(
                        text = "Información Básica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it; validationError = null },
                        label = { Text("Nombre del evento *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        isError = validationError != null && nombre.isBlank()
                    )
                    
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it; validationError = null },
                        label = { Text("Descripción *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        enabled = !isLoading,
                        isError = validationError != null && descripcion.isBlank()
                    )
                    
                    // Categoría
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(CategoriaEvento.values()) { cat ->
                            FilterChip(
                                onClick = { if (!isLoading) categoria = cat },
                                label = { Text(cat.displayName) },
                                selected = categoria == cat,
                                enabled = !isLoading,
                                leadingIcon = {
                                    Icon(
                                        getCategoryIcon(cat.icon),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                    }
                    
                    // Ubicación
                    Text(
                        text = "Ubicación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = direccion,
                        onValueChange = { direccion = it; validationError = null },
                        label = { Text("Dirección *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        isError = validationError != null && direccion.isBlank()
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = latitud?.toString() ?: "",
                            onValueChange = { 
                                latitud = it.toDoubleOrNull()
                                validationError = null
                            },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = !isLoading,
                            placeholder = { Text("0.0") }
                        )
                        
                        OutlinedTextField(
                            value = longitud?.toString() ?: "",
                            onValueChange = { 
                                longitud = it.toDoubleOrNull()
                                validationError = null
                            },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = !isLoading,
                            placeholder = { Text("0.0") }
                        )
                        
                        // Botón para obtener ubicación actual
                        IconButton(
                            onClick = {
                                if (!isLoading && !isGettingLocation) {
                                    isGettingLocation = true
                                    try {
                                        if (locationService.checkPermissions() && locationService.isLocationEnabled()) {
                                            val locationState = locationService.forceLocationUpdate()
                                            locationState.latitude?.let { lat ->
                                                locationState.longitude?.let { lon ->
                                                    latitud = lat
                                                    longitud = lon
                                                }
                                            }
                                        }
                                    } catch (e: Exception) {
                                        validationError = "Error al obtener ubicación: ${e.message}"
                                    } finally {
                                        isGettingLocation = false
                                    }
                                }
                            },
                            enabled = !isLoading && !isGettingLocation
                        ) {
                            if (isGettingLocation) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                            } else {
                                Icon(
                                    imageVector = Icons.Default.MyLocation,
                                    contentDescription = "Usar mi ubicación"
                                )
                            }
                        }
                    }
                    
                    // Fecha y hora
                    Text(
                        text = "Fecha y Hora",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Fecha inicio
                        OutlinedTextField(
                            value = fechaInicio?.let { dateFormat.format(it) } ?: "",
                            onValueChange = { },
                            label = { Text("Fecha inicio *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            enabled = !isLoading,
                            isError = validationError != null && fechaInicio == null,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (!isLoading) {
                                            val calendar = Calendar.getInstance()
                                            fechaInicio?.let {
                                                calendar.time = it
                                            }
                                            DatePickerDialog(
                                                context,
                                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                                    val newDate = Calendar.getInstance().apply {
                                                        set(year, month, day)
                                                    }.time
                                                    fechaInicio = newDate
                                                    validationError = null
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                                }
                            }
                        )
                        
                        // Fecha fin
                        OutlinedTextField(
                            value = fechaFin?.let { dateFormat.format(it) } ?: "",
                            onValueChange = { },
                            label = { Text("Fecha fin *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            enabled = !isLoading,
                            isError = validationError != null && fechaFin == null,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (!isLoading) {
                                            val calendar = Calendar.getInstance()
                                            fechaFin?.let {
                                                calendar.time = it
                                            } ?: fechaInicio?.let {
                                                calendar.time = it
                                            }
                                            DatePickerDialog(
                                                context,
                                                { _: DatePicker, year: Int, month: Int, day: Int ->
                                                    val newDate = Calendar.getInstance().apply {
                                                        set(year, month, day)
                                                    }.time
                                                    fechaFin = newDate
                                                    validationError = null
                                                },
                                                calendar.get(Calendar.YEAR),
                                                calendar.get(Calendar.MONTH),
                                                calendar.get(Calendar.DAY_OF_MONTH)
                                            ).show()
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                                }
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Hora inicio
                        OutlinedTextField(
                            value = horaInicio?.let { 
                                String.format("%02d:%02d", it.first, it.second)
                            } ?: "",
                            onValueChange = { },
                            label = { Text("Hora inicio *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            enabled = !isLoading,
                            isError = validationError != null && horaInicio == null,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (!isLoading) {
                                            val calendar = Calendar.getInstance()
                                            horaInicio?.let {
                                                calendar.set(Calendar.HOUR_OF_DAY, it.first)
                                                calendar.set(Calendar.MINUTE, it.second)
                                            }
                                            TimePickerDialog(
                                                context,
                                                { _: TimePicker, hourOfDay: Int, minute: Int ->
                                                    horaInicio = Pair(hourOfDay, minute)
                                                    validationError = null
                                                },
                                                calendar.get(Calendar.HOUR_OF_DAY),
                                                calendar.get(Calendar.MINUTE),
                                                true
                                            ).show()
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
                                }
                            }
                        )
                        
                        // Hora fin
                        OutlinedTextField(
                            value = horaFin?.let { 
                                String.format("%02d:%02d", it.first, it.second)
                            } ?: "",
                            onValueChange = { },
                            label = { Text("Hora fin *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            enabled = !isLoading,
                            isError = validationError != null && horaFin == null,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (!isLoading) {
                                            val calendar = Calendar.getInstance()
                                            horaFin?.let {
                                                calendar.set(Calendar.HOUR_OF_DAY, it.first)
                                                calendar.set(Calendar.MINUTE, it.second)
                                            }
                                            TimePickerDialog(
                                                context,
                                                { _: TimePicker, hourOfDay: Int, minute: Int ->
                                                    horaFin = Pair(hourOfDay, minute)
                                                    validationError = null
                                                },
                                                calendar.get(Calendar.HOUR_OF_DAY),
                                                calendar.get(Calendar.MINUTE),
                                                true
                                            ).show()
                                        }
                                    },
                                    enabled = !isLoading
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
                                }
                            }
                        )
                    }
                    
                    // Organizador y contacto
                    Text(
                        text = "Organizador y Contacto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = organizador,
                        onValueChange = { organizador = it },
                        label = { Text("Organizador") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = telefono,
                            onValueChange = { telefono = it },
                            label = { Text("Teléfono") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            enabled = !isLoading
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            enabled = !isLoading
                        )
                    }
                    
                    // Precio y capacidad
                    Text(
                        text = "Precio y Capacidad",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evento gratuito",
                            style = MaterialTheme.typography.labelLarge
                        )
                        
                        Switch(
                            checked = esGratuito,
                            onCheckedChange = { if (!isLoading) esGratuito = it },
                            enabled = !isLoading
                        )
                    }
                    
                    if (!esGratuito) {
                        OutlinedTextField(
                            value = precioGeneral,
                            onValueChange = { precioGeneral = it },
                            label = { Text("Precio general") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            enabled = !isLoading,
                            leadingIcon = {
                                Text("$")
                            }
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = capacidad,
                            onValueChange = { capacidad = it },
                            label = { Text("Capacidad") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !isLoading
                        )
                        
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Requiere inscripción",
                                    style = MaterialTheme.typography.labelMedium
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Switch(
                                    checked = requiereInscripcion,
                                    onCheckedChange = { if (!isLoading) requiereInscripcion = it },
                                    enabled = !isLoading
                                )
                            }
                        }
                    }
                    
                    // Edad recomendada
                    Text(
                        text = "Edad Recomendada",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = edadMinima,
                            onValueChange = { edadMinima = it },
                            label = { Text("Edad mínima") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !isLoading
                        )
                        
                        OutlinedTextField(
                            value = edadMaxima,
                            onValueChange = { edadMaxima = it },
                            label = { Text("Edad máxima") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            enabled = !isLoading
                        )
                    }
                    
                    // Características
                    Text(
                        text = "Características",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Accesible en silla de ruedas")
                            Switch(
                                checked = accesibilidad,
                                onCheckedChange = { if (!isLoading) accesibilidad = it },
                                enabled = !isLoading
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Estacionamiento disponible")
                            Switch(
                                checked = estacionamiento,
                                onCheckedChange = { if (!isLoading) estacionamiento = it },
                                enabled = !isLoading
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Acceso por transporte público")
                            Switch(
                                checked = transportePublico,
                                onCheckedChange = { if (!isLoading) transportePublico = it },
                                enabled = !isLoading
                            )
                        }
                    }
                    
                    // Información adicional
                    Text(
                        text = "Información Adicional",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = etiquetas,
                        onValueChange = { etiquetas = it },
                        label = { Text("Etiquetas (separadas por comas)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        placeholder = { Text("ej: música, arte, familia") }
                    )
                    
                    OutlinedTextField(
                        value = sitioWeb,
                        onValueChange = { sitioWeb = it },
                        label = { Text("Sitio web") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isLoading,
                        placeholder = { Text("https://...") }
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                Divider()
                
                // Botones de acción
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Text(ButtonText.CANCELAR.value)
                    }
                    
                    Button(
                        onClick = {
                            // Validar campos requeridos
                            validationError = null
                            
                            when {
                                nombre.isBlank() -> {
                                    validationError = "El nombre del evento es requerido"
                                    return@Button
                                }
                                descripcion.isBlank() -> {
                                    validationError = "La descripción es requerida"
                                    return@Button
                                }
                                direccion.isBlank() -> {
                                    validationError = "La dirección es requerida"
                                    return@Button
                                }
                                fechaInicio == null -> {
                                    validationError = "La fecha de inicio es requerida"
                                    return@Button
                                }
                                fechaFin == null -> {
                                    validationError = "La fecha de fin es requerida"
                                    return@Button
                                }
                                horaInicio == null -> {
                                    validationError = "La hora de inicio es requerida"
                                    return@Button
                                }
                                horaFin == null -> {
                                    validationError = "La hora de fin es requerida"
                                    return@Button
                                }
                                !esGratuito && precioGeneral.isBlank() -> {
                                    validationError = "El precio es requerido para eventos de pago"
                                    return@Button
                                }
                                capacidad.isNotBlank() && capacidad.toIntOrNull() == null -> {
                                    validationError = "La capacidad debe ser un número válido"
                                    return@Button
                                }
                                capacidad.isNotBlank() && capacidad.toIntOrNull()!! < 1 -> {
                                    validationError = "La capacidad debe ser mayor a 0"
                                    return@Button
                                }
                            }
                            
                            // Validar fechas y horas
                            val calInicio = Calendar.getInstance().apply { time = fechaInicio!! }
                            val calFin = Calendar.getInstance().apply { time = fechaFin!! }
                            
                            if (calFin.before(calInicio)) {
                                validationError = "La fecha de fin debe ser posterior o igual a la fecha de inicio"
                                return@Button
                            }
                            
                            // Si son el mismo día, validar las horas
                            if (calInicio.get(Calendar.YEAR) == calFin.get(Calendar.YEAR) &&
                                calInicio.get(Calendar.DAY_OF_YEAR) == calFin.get(Calendar.DAY_OF_YEAR)) {
                                if (horaFin!!.first < horaInicio!!.first ||
                                    (horaFin!!.first == horaInicio!!.first && horaFin!!.second <= horaInicio!!.second)) {
                                    validationError = "La hora de fin debe ser posterior a la hora de inicio"
                                    return@Button
                                }
                            }
                            
                            // Crear el evento
                            val calendarInicio = Calendar.getInstance().apply {
                                time = fechaInicio!!
                                set(Calendar.HOUR_OF_DAY, horaInicio!!.first)
                                set(Calendar.MINUTE, horaInicio!!.second)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            
                            val calendarFin = Calendar.getInstance().apply {
                                time = fechaFin!!
                                set(Calendar.HOUR_OF_DAY, horaFin!!.first)
                                set(Calendar.MINUTE, horaFin!!.second)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }
                            
                            val event = Event(
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                categoria = categoria,
                                ubicacion = Ubicacion(
                                    lat = latitud ?: 0.0,
                                    lon = longitud ?: 0.0,
                                    direccion = direccion.trim()
                                ),
                                direccion = direccion.trim(),
                                fechaInicio = com.google.firebase.Timestamp(calendarInicio.time),
                                fechaFin = com.google.firebase.Timestamp(calendarFin.time),
                                horaInicio = String.format("%02d:%02d", horaInicio!!.first, horaInicio!!.second),
                                horaFin = String.format("%02d:%02d", horaFin!!.first, horaFin!!.second),
                                organizador = organizador.trim(),
                                contacto = ContactoEvento(
                                    telefono = telefono.takeIf { it.isNotBlank() },
                                    email = email.takeIf { it.isNotBlank() }
                                ),
                                precio = PrecioEvento(
                                    esGratuito = esGratuito,
                                    precioGeneral = if (!esGratuito) precioGeneral.toDoubleOrNull() else null
                                ),
                                capacidad = capacidad.toIntOrNull(),
                                requiereInscripcion = requiereInscripcion,
                                edadMinima = edadMinima.toIntOrNull(),
                                edadMaxima = edadMaxima.toIntOrNull(),
                                accesibilidad = accesibilidad,
                                estacionamiento = estacionamiento,
                                transportePublico = transportePublico,
                                etiquetas = etiquetas.split(",").map { it.trim() }.filter { it.isNotBlank() },
                                sitioWeb = sitioWeb.takeIf { it.isNotBlank() },
                                esGratuito = esGratuito,
                                estado = EstadoEvento.PENDIENTE
                            )
                            
                            onCreateEvent(event)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading && nombre.isNotBlank() && descripcion.isNotBlank() && 
                                 direccion.isNotBlank() && fechaInicio != null && 
                                 fechaFin != null && horaInicio != null && horaFin != null
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(if (isLoading) "Creando..." else "Crear Evento")
                    }
                }
            }
        }
    }
}
