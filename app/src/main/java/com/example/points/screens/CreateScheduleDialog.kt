package com.example.points.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.points.models.*
import com.example.points.utils.getCategoryIcon
import java.util.*

@Composable
fun CreateScheduleDialog(
    onDismiss: () -> Unit,
    onCreateSchedule: (Event) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf(CategoriaEvento.CULTURAL) }
    var direccion by remember { mutableStateOf("") }
    var latitud by remember { mutableStateOf("") }
    var longitud by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    var horaInicio by remember { mutableStateOf("") }
    var horaFin by remember { mutableStateOf("") }
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
    
    // Campos específicos de programación
    var esRecurrente by remember { mutableStateOf(true) }
    var frecuenciaRecurrencia by remember { mutableStateOf(FrecuenciaRecurrencia.SEMANAL) }
    var fechaFinRecurrencia by remember { mutableStateOf("") }
    var tieneFechaFin by remember { mutableStateOf(false) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isStartDate by remember { mutableStateOf(true) }
    var isStartTime by remember { mutableStateOf(true) }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
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
                        text = "Crear Evento Programado",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    IconButton(onClick = onDismiss) {
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
                    // Información básica
                    Text(
                        text = "Información Básica",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del evento *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    OutlinedTextField(
                        value = descripcion,
                        onValueChange = { descripcion = it },
                        label = { Text("Descripción *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5
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
                        items(CategoriaEvento.values().toList()) { cat ->
                            FilterChip(
                                onClick = { categoria = cat },
                                label = { Text(cat.displayName) },
                                selected = categoria == cat,
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
                        onValueChange = { direccion = it },
                        label = { Text("Dirección *") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = latitud,
                            onValueChange = { latitud = it },
                            label = { Text("Latitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        OutlinedTextField(
                            value = longitud,
                            onValueChange = { longitud = it },
                            label = { Text("Longitud") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    
                    // Programación
                    Text(
                        text = "Programación",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Evento recurrente
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Evento recurrente",
                            style = MaterialTheme.typography.labelLarge
                        )
                        
                        Switch(
                            checked = esRecurrente,
                            onCheckedChange = { esRecurrente = it }
                        )
                    }
                    
                    if (esRecurrente) {
                        // Frecuencia de recurrencia
                        Text(
                            text = "Frecuencia",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Medium
                        )
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                        items(FrecuenciaRecurrencia.values().toList()) { freq ->
                            FilterChip(
                                onClick = { frecuenciaRecurrencia = freq },
                                label = { Text(freq.displayName) },
                                selected = frecuenciaRecurrencia == freq,
                                leadingIcon = {
                                    Icon(
                                        when (freq) {
                                            FrecuenciaRecurrencia.DIARIO -> Icons.Default.Today
                                            FrecuenciaRecurrencia.SEMANAL -> Icons.Default.DateRange
                                            FrecuenciaRecurrencia.MENSUAL -> Icons.Default.CalendarMonth
                                            FrecuenciaRecurrencia.ANUAL -> Icons.Default.Event
                                        },
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            )
                        }
                        }
                        
                        // Fecha de fin de recurrencia
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Tiene fecha de fin",
                                style = MaterialTheme.typography.labelLarge
                            )
                            
                            Switch(
                                checked = tieneFechaFin,
                                onCheckedChange = { tieneFechaFin = it }
                            )
                        }
                        
                        if (tieneFechaFin) {
                            OutlinedTextField(
                                value = fechaFinRecurrencia,
                                onValueChange = { },
                                label = { Text("Fecha de fin de recurrencia *") },
                                modifier = Modifier.fillMaxWidth(),
                                readOnly = true,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            isStartDate = false
                                            showDatePicker = true
                                        }
                                    ) {
                                        Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                                    }
                                }
                            )
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
                        OutlinedTextField(
                            value = fechaInicio,
                            onValueChange = { },
                            label = { Text("Fecha inicio *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isStartDate = true
                                        showDatePicker = true
                                    }
                                ) {
                                    Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha")
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = fechaFin,
                            onValueChange = { },
                            label = { Text("Fecha fin *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isStartDate = false
                                        showDatePicker = true
                                    }
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
                        OutlinedTextField(
                            value = horaInicio,
                            onValueChange = { },
                            label = { Text("Hora inicio *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isStartTime = true
                                        showTimePicker = true
                                    }
                                ) {
                                    Icon(Icons.Default.Schedule, contentDescription = "Seleccionar hora")
                                }
                            }
                        )
                        
                        OutlinedTextField(
                            value = horaFin,
                            onValueChange = { },
                            label = { Text("Hora fin *") },
                            modifier = Modifier.weight(1f),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        isStartTime = false
                                        showTimePicker = true
                                    }
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
                        singleLine = true
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                        )
                        
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
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
                            onCheckedChange = { esGratuito = it }
                        )
                    }
                    
                    if (!esGratuito) {
                        OutlinedTextField(
                            value = precioGeneral,
                            onValueChange = { precioGeneral = it },
                            label = { Text("Precio general") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                                    onCheckedChange = { requiereInscripcion = it }
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
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        
                        OutlinedTextField(
                            value = edadMaxima,
                            onValueChange = { edadMaxima = it },
                            label = { Text("Edad máxima") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                                onCheckedChange = { accesibilidad = it }
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
                                onCheckedChange = { estacionamiento = it }
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
                                onCheckedChange = { transportePublico = it }
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
                        placeholder = { Text("ej: música, arte, familia") }
                    )
                    
                    OutlinedTextField(
                        value = sitioWeb,
                        onValueChange = { sitioWeb = it },
                        label = { Text("Sitio web") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
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
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancelar")
                    }
                    
                    Button(
                        onClick = {
                            // Validar campos requeridos
                            if (nombre.isBlank() || descripcion.isBlank() || direccion.isBlank() || 
                                fechaInicio.isBlank() || fechaFin.isBlank() || horaInicio.isBlank() || horaFin.isBlank()) {
                                // TODO: Mostrar error
                                return@Button
                            }
                            
                            if (esRecurrente && tieneFechaFin && fechaFinRecurrencia.isBlank()) {
                                // TODO: Mostrar error
                                return@Button
                            }
                            
                            // Crear el evento programado
                            val event = Event(
                                nombre = nombre.trim(),
                                descripcion = descripcion.trim(),
                                categoria = categoria,
                                ubicacion = Ubicacion(
                                    lat = latitud.toDoubleOrNull() ?: 0.0,
                                    lon = longitud.toDoubleOrNull() ?: 0.0
                                ),
                                direccion = direccion.trim(),
                                fechaInicio = parseDateTime(fechaInicio, horaInicio),
                                fechaFin = parseDateTime(fechaFin, horaFin),
                                horaInicio = horaInicio,
                                horaFin = horaFin,
                                esRecurrente = esRecurrente,
                                frecuenciaRecurrencia = if (esRecurrente) frecuenciaRecurrencia else null,
                                fechaFinRecurrencia = if (esRecurrente && tieneFechaFin) parseDate(fechaFinRecurrencia) else null,
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
                            
                            onCreateSchedule(event)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = nombre.isNotBlank() && descripcion.isNotBlank() && 
                                 direccion.isNotBlank() && fechaInicio.isNotBlank() && 
                                 fechaFin.isNotBlank() && horaInicio.isNotBlank() && horaFin.isNotBlank() &&
                                 (!esRecurrente || !tieneFechaFin || fechaFinRecurrencia.isNotBlank())
                    ) {
                        Text("Crear Programación")
                    }
                }
            }
        }
    }
    
    // Date picker
    if (showDatePicker) {
        // TODO: Implementar DatePicker
        // Por ahora, simular selección de fecha
        LaunchedEffect(showDatePicker) {
            val today = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
            if (isStartDate) {
                fechaInicio = today
            } else if (tieneFechaFin) {
                fechaFinRecurrencia = today
            } else {
                fechaFin = today
            }
            showDatePicker = false
        }
    }
    
    // Time picker
    if (showTimePicker) {
        // TODO: Implementar TimePicker
        // Por ahora, simular selección de hora
        LaunchedEffect(showTimePicker) {
            val now = java.text.SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            if (isStartTime) {
                horaInicio = now
            } else {
                horaFin = now
            }
            showTimePicker = false
        }
    }
}

// Función auxiliar para parsear fecha y hora
private fun parseDateTime(fecha: String, hora: String): com.google.firebase.Timestamp {
    return try {
        val dateTimeString = "$fecha $hora"
        val format = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val date = format.parse(dateTimeString)
        com.google.firebase.Timestamp(date ?: Date())
    } catch (e: Exception) {
        com.google.firebase.Timestamp.now()
    }
}

// Función auxiliar para parsear solo fecha
private fun parseDate(fecha: String): com.google.firebase.Timestamp {
    return try {
        val format = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val date = format.parse(fecha)
        com.google.firebase.Timestamp(date ?: Date())
    } catch (e: Exception) {
        com.google.firebase.Timestamp.now()
    }
}
