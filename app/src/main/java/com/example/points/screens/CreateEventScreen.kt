package com.example.points.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.net.Uri
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.points.components.OptimizedRoundedImage
import com.example.points.constants.AppRoutes
import com.example.points.models.*
import com.example.points.models.gemini.Content
import com.example.points.models.gemini.GeminiRequest
import com.example.points.models.gemini.Part
import com.example.points.network.GeminiApiService
import com.example.points.utils.EnvironmentConfig
import com.example.points.ui.components.ModernButton
import com.example.points.ui.components.ModernCard
import com.example.points.ui.components.ModernTextField
import com.example.points.ui.components.ButtonVariant
import com.example.points.ui.theme.PointsPrimary
import com.example.points.ui.theme.PointsSecondary
import com.example.points.utils.getCategoryIcon
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@Composable
fun CreateEventScreen(
    navController: NavController,
    onCreateEvent: (Event) -> Unit,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onErrorShown: () -> Unit = {},
    onEventCreated: () -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    
    // Configurar Gemini API
    val geminiApiService = remember {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiApiService::class.java)
    }
    
    // Estados del formulario con rememberSaveable para persistir en navegación
    var nombre by rememberSaveable { mutableStateOf("") }
    var descripcion by rememberSaveable { mutableStateOf("") }
    var categoria by rememberSaveable { mutableStateOf(CategoriaEvento.CULTURAL) }
    var ubicacionLat by rememberSaveable { mutableStateOf<Double?>(null) }
    var ubicacionLon by rememberSaveable { mutableStateOf<Double?>(null) }
    var ubicacionDireccion by rememberSaveable { mutableStateOf("") }
    var fechaInicioMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var fechaFinMillis by rememberSaveable { mutableStateOf<Long?>(null) }
    var horaInicioHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var horaInicioMinute by rememberSaveable { mutableStateOf<Int?>(null) }
    var horaFinHour by rememberSaveable { mutableStateOf<Int?>(null) }
    var horaFinMinute by rememberSaveable { mutableStateOf<Int?>(null) }
    var organizador by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var esGratuito by rememberSaveable { mutableStateOf(true) }
    var precioGeneral by rememberSaveable { mutableStateOf("") }
    var capacidad by rememberSaveable { mutableStateOf("") }
    var requiereInscripcion by rememberSaveable { mutableStateOf(false) }
    var edadMinima by rememberSaveable { mutableStateOf("") }
    var edadMaxima by rememberSaveable { mutableStateOf("") }
    var accesibilidad by rememberSaveable { mutableStateOf(false) }
    var estacionamiento by rememberSaveable { mutableStateOf(false) }
    var transportePublico by rememberSaveable { mutableStateOf(false) }
    var etiquetas by rememberSaveable { mutableStateOf("") }
    var sitioWeb by rememberSaveable { mutableStateOf("") }
    
    // Estados de imágenes (no persistibles con rememberSaveable)
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var uploadedImageUrls by remember { mutableStateOf<List<String>>(emptyList()) }
    var isUploadingImage by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    
    // Estados de IA
    var isGeneratingDescription by remember { mutableStateOf(false) }
    
    var validationError by remember { mutableStateOf<String?>(null) }
    
    // Convertir valores saveable a objetos
    val ubicacion = if (ubicacionLat != null && ubicacionLon != null && ubicacionDireccion.isNotEmpty()) {
        Ubicacion(ubicacionLat!!, ubicacionLon!!, ubicacionDireccion)
    } else null
    
    val fechaInicio = fechaInicioMillis?.let { Date(it) }
    val fechaFin = fechaFinMillis?.let { Date(it) }
    val horaInicio = if (horaInicioHour != null && horaInicioMinute != null) {
        Pair(horaInicioHour!!, horaInicioMinute!!)
    } else null
    val horaFin = if (horaFinHour != null && horaFinMinute != null) {
        Pair(horaFinHour!!, horaFinMinute!!)
    } else null
    
    val dateFormat = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    
    // Función para subir imagen
    suspend fun uploadImage(uri: Uri): String {
        val storage = Firebase.storage
        val storageRef = storage.reference
        val fileName = "events/${UUID.randomUUID()}.jpg"
        val imageRef = storageRef.child(fileName)
        
        imageRef.putFile(uri).await()
        return imageRef.downloadUrl.await().toString()
    }
    
    // Función para subir imagen y agregar a la lista
    fun uploadImageAndAddToList(uri: Uri) {
        scope.launch {
            isUploadingImage = true
            try {
                val downloadUrl = uploadImage(uri)
                uploadedImageUrls = uploadedImageUrls + downloadUrl
            } catch (e: Exception) {
                validationError = "Error al subir imagen: ${e.message}"
            } finally {
                isUploadingImage = false
            }
        }
    }
    
    // Escuchar ubicación del mapa usando currentBackStackEntryAsState (más compatible con Compose)
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    
    // Verificar ubicación cuando cambia el back stack entry
    LaunchedEffect(navBackStackEntry?.id) {
        val savedStateHandle = navBackStackEntry?.savedStateHandle
        savedStateHandle?.let { handle ->
            val lat = handle.get<Double>("selectedLat")
            val lon = handle.get<Double>("selectedLon")
            val direccion = handle.get<String>("selectedDireccion")
            
            if (lat != null && lon != null && direccion != null && direccion.isNotEmpty()) {
                ubicacionLat = lat
                ubicacionLon = lon
                ubicacionDireccion = direccion
                
                // Limpiar después de leer
                handle.remove<Double>("selectedLat")
                handle.remove<Double>("selectedLon")
                handle.remove<String>("selectedDireccion")
            }
        }
    }
    
    // También verificar periódicamente el savedStateHandle por si acaso
    LaunchedEffect(Unit) {
        // Verificar cada vez que el composable se recompone
        val savedStateHandle = navController.currentBackStackEntry?.savedStateHandle
        savedStateHandle?.let { handle ->
            val lat = handle.get<Double>("selectedLat")
            val lon = handle.get<Double>("selectedLon")
            val direccion = handle.get<String>("selectedDireccion")
            
            if (lat != null && lon != null && direccion != null && direccion.isNotEmpty()) {
                ubicacionLat = lat
                ubicacionLon = lon
                ubicacionDireccion = direccion
                
                // Limpiar
                handle.remove<Double>("selectedLat")
                handle.remove<Double>("selectedLon")
                handle.remove<String>("selectedDireccion")
            }
        }
    }
    
    // Launcher para cámara
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && photoUri != null) {
            selectedImages = selectedImages + photoUri!!
            // Subir automáticamente
            uploadImageAndAddToList(photoUri!!)
        }
    }
    
    // Launcher para galería
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImages = selectedImages + it
            // Subir automáticamente
            uploadImageAndAddToList(it)
        }
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header moderno con gradiente
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(PointsPrimary, PointsSecondary)
                        )
                    )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { navController.navigateUp() },
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Volver",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "🎉 Crear Evento",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color.White
                    )
                    
                    IconButton(
                        onClick = { /* Info */ },
                        enabled = false
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = Color.Transparent
                        )
                    }
                }
            }
            
            // Contenido scrolleable
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Error message
                AnimatedVisibility(visible = validationError != null) {
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
                                text = validationError ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { validationError = null },
                                modifier = Modifier.size(24.dp)
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
                
                // Información Básica
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📋 Información Básica",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        OutlinedTextField(
                            value = nombre,
                            onValueChange = { nombre = it; validationError = null },
                            label = { Text("Nombre del evento *") },
                            placeholder = { Text("Ej: Festival de Música 2024") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                        )
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = descripcion,
                                onValueChange = { descripcion = it; validationError = null },
                                label = { Text("Descripción *") },
                                placeholder = { Text("Describe tu evento...") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && !isGeneratingDescription,
                                minLines = 4,
                                maxLines = 6,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() })
                            )
                            
                            // Botón para generar descripción con IA
                            OutlinedButton(
                                onClick = {
                                    if (nombre.isNotBlank()) {
                                        scope.launch {
                                            isGeneratingDescription = true
                                            try {
                                                val apiKey = EnvironmentConfig.GEMINI_API_KEY
                                                if (apiKey.isNotEmpty()) {
                                                    val prompt = """
                                                        Genera una descripción atractiva y profesional en español para un evento con las siguientes características:
                                                        
                                                        - Nombre: $nombre
                                                        - Categoría: ${categoria.displayName}
                                                        
                                                        La descripción debe:
                                                        - Tener entre 2-3 párrafos (máximo 200 palabras)
                                                        - Ser motivadora y entusiasta
                                                        - Mencionar aspectos relevantes del tipo de evento
                                                        - Estar escrita en español
                                                        - No incluir precios, fechas ni horarios específicos
                                                        - No usar asteriscos ni formato markdown
                                                        
                                                        Solo genera la descripción, sin títulos ni encabezados.
                                                    """.trimIndent()
                                                    
                                                    val request = GeminiRequest(
                                                        contents = listOf(
                                                            Content(parts = listOf(Part(text = prompt)))
                                                        )
                                                    )
                                                    
                                                    val response = geminiApiService.generateContent(apiKey, request)
                                                    val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                                                    
                                                    if (generatedText != null) {
                                                        descripcion = generatedText.trim()
                                                    } else {
                                                        throw Exception("Respuesta vacía de Gemini")
                                                    }
                                                } else {
                                                    // Descripción predeterminada
                                                    descripcion = generateDefaultEventDescription(nombre, categoria)
                                                }
                                            } catch (e: Exception) {
                                                // En caso de error, usar descripción predeterminada
                                                descripcion = generateDefaultEventDescription(nombre, categoria)
                                            } finally {
                                                isGeneratingDescription = false
                                            }
                                        }
                                    } else {
                                        validationError = "Ingresa el nombre del evento primero"
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading && !isGeneratingDescription && nombre.isNotBlank()
                            ) {
                                if (isGeneratingDescription) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generando con IA...")
                                } else {
                                    Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("✨ Generar con IA")
                                }
                            }
                        }
                        
                        // Categoría
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "Categoría",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Medium
                                )
                            )
                            
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        }
                    }
                }
                
                // Imágenes
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📸 Imágenes del Evento",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        // Botones para agregar imágenes
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val photoFile = File(context.cacheDir, "event_${System.currentTimeMillis()}.jpg")
                                    photoUri = FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        photoFile
                                    )
                                    cameraLauncher.launch(photoUri!!)
                                },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading && !isUploadingImage
                            ) {
                                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tomar Foto")
                            }
                            
                            OutlinedButton(
                                onClick = { galleryLauncher.launch("image/*") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading && !isUploadingImage
                            ) {
                                Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Galería")
                            }
                        }
                        
                        // Mostrar imágenes seleccionadas
                        if (selectedImages.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(selectedImages) { uri ->
                                    Box(
                                        modifier = Modifier.size(100.dp)
                                    ) {
                                        OptimizedRoundedImage(
                                            imageUrl = uri.toString(),
                                            contentDescription = "Imagen seleccionada",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                        
                                        // Botón para subir
                                        if (!uploadedImageUrls.contains(uri.toString())) {
                                            IconButton(
                                                onClick = { uploadImageAndAddToList(uri) },
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(32.dp)
                                                    .background(
                                                        MaterialTheme.colorScheme.primaryContainer,
                                                        RoundedCornerShape(50)
                                                    )
                                            ) {
                                                if (isUploadingImage) {
                                                    CircularProgressIndicator(
                                                        modifier = Modifier.size(16.dp),
                                                        strokeWidth = 2.dp
                                                    )
                                                } else {
                                                    Icon(
                                                        Icons.Default.CloudUpload,
                                                        contentDescription = "Subir",
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        } else {
                                            Icon(
                                                Icons.Default.CheckCircle,
                                                contentDescription = "Subida",
                                                tint = Color.Green,
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .size(24.dp)
                                            )
                                        }
                                        
                                        // Botón para eliminar
                                        IconButton(
                                            onClick = {
                                                selectedImages = selectedImages.filter { it != uri }
                                            },
                                            modifier = Modifier
                                                .align(Alignment.BottomEnd)
                                                .size(32.dp)
                                                .background(
                                                    MaterialTheme.colorScheme.errorContainer,
                                                    RoundedCornerShape(50)
                                                )
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Eliminar",
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Ubicación
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📍 Ubicación",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        if (ubicacion != null) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = PointsPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = ubicacion!!.direccion,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            ubicacionLat = null
                                            ubicacionLon = null
                                            ubicacionDireccion = ""
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Eliminar ubicación"
                                        )
                                    }
                                }
                            }
                        }
                        
                        OutlinedButton(
                            onClick = {
                                navController.navigate(AppRoutes.SELECT_LOCATION_MAP_POI)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading
                        ) {
                            Icon(Icons.Default.Map, null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(if (ubicacion == null) "Seleccionar en Mapa" else "Cambiar Ubicación")
                        }
                    }
                }
                
                // Fecha y Hora
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "📅 Fecha y Hora",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = fechaInicio?.let { dateFormat.format(it) } ?: "",
                                onValueChange = { },
                                label = { Text("Fecha inicio *") },
                                modifier = Modifier.weight(1f),
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (!isLoading) {
                                                val calendar = Calendar.getInstance()
                                                fechaInicio?.let { calendar.time = it }
                                                DatePickerDialog(
                                                    context,
                                                    { _: DatePicker, year: Int, month: Int, day: Int ->
                                                        fechaInicioMillis = Calendar.getInstance().apply {
                                                            set(year, month, day)
                                                        }.timeInMillis
                                                        validationError = null
                                                    },
                                                    calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH)
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.DateRange, "Fecha")
                                    }
                                }
                            )
                            
                            OutlinedTextField(
                                value = horaInicio?.let { String.format("%02d:%02d", it.first, it.second) } ?: "",
                                onValueChange = { },
                                label = { Text("Hora *") },
                                modifier = Modifier.weight(1f),
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (!isLoading) {
                                                val calendar = Calendar.getInstance()
                                                TimePickerDialog(
                                                    context,
                                                    { _: TimePicker, hourOfDay: Int, minute: Int ->
                                                        horaInicioHour = hourOfDay
                                                        horaInicioMinute = minute
                                                        validationError = null
                                                    },
                                                    calendar.get(Calendar.HOUR_OF_DAY),
                                                    calendar.get(Calendar.MINUTE),
                                                    true
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Schedule, "Hora")
                                    }
                                }
                            )
                        }
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = fechaFin?.let { dateFormat.format(it) } ?: "",
                                onValueChange = { },
                                label = { Text("Fecha fin *") },
                                modifier = Modifier.weight(1f),
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (!isLoading) {
                                                val calendar = Calendar.getInstance()
                                                fechaFin?.let { calendar.time = it } ?: fechaInicio?.let { calendar.time = it }
                                                DatePickerDialog(
                                                    context,
                                                    { _: DatePicker, year: Int, month: Int, day: Int ->
                                                        fechaFinMillis = Calendar.getInstance().apply {
                                                            set(year, month, day)
                                                        }.timeInMillis
                                                        validationError = null
                                                    },
                                                    calendar.get(Calendar.YEAR),
                                                    calendar.get(Calendar.MONTH),
                                                    calendar.get(Calendar.DAY_OF_MONTH)
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.DateRange, "Fecha")
                                    }
                                }
                            )
                            
                            OutlinedTextField(
                                value = horaFin?.let { String.format("%02d:%02d", it.first, it.second) } ?: "",
                                onValueChange = { },
                                label = { Text("Hora *") },
                                modifier = Modifier.weight(1f),
                                readOnly = true,
                                enabled = !isLoading,
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            if (!isLoading) {
                                                val calendar = Calendar.getInstance()
                                                TimePickerDialog(
                                                    context,
                                                    { _: TimePicker, hourOfDay: Int, minute: Int ->
                                                        horaFinHour = hourOfDay
                                                        horaFinMinute = minute
                                                        validationError = null
                                                    },
                                                    calendar.get(Calendar.HOUR_OF_DAY),
                                                    calendar.get(Calendar.MINUTE),
                                                    true
                                                ).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Schedule, "Hora")
                                    }
                                }
                            )
                        }
                    }
                }
                
                // Organizador y Detalles
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "👥 Organizador y Contacto",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        OutlinedTextField(
                            value = organizador,
                            onValueChange = { organizador = it },
                            label = { Text("Organizador") },
                            placeholder = { Text("Nombre del organizador") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
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
                                placeholder = { Text("+51 999 999 999") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                            )
                            
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email") },
                                placeholder = { Text("contacto@evento.com") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                            )
                        }
                    }
                }
                
                // Precio y Capacidad
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "💰 Precio y Capacidad",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Evento gratuito",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Switch(
                                checked = esGratuito,
                                onCheckedChange = { if (!isLoading) esGratuito = it }
                            )
                        }
                        
                        AnimatedVisibility(visible = !esGratuito) {
                            OutlinedTextField(
                                value = precioGeneral,
                                onValueChange = { precioGeneral = it },
                                label = { Text("Precio general") },
                                placeholder = { Text("0.00") },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                leadingIcon = { Text("S/") }
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
                                placeholder = { Text("100") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Requiere inscripción",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Switch(
                                        checked = requiereInscripcion,
                                        onCheckedChange = { if (!isLoading) requiereInscripcion = it }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Características
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "✨ Características",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Accessible, null, modifier = Modifier.size(20.dp))
                                Text("Accesible en silla de ruedas")
                            }
                            Switch(
                                checked = accesibilidad,
                                onCheckedChange = { if (!isLoading) accesibilidad = it }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.LocalParking, null, modifier = Modifier.size(20.dp))
                                Text("Estacionamiento disponible")
                            }
                            Switch(
                                checked = estacionamiento,
                                onCheckedChange = { if (!isLoading) estacionamiento = it }
                            )
                        }
                        
                        HorizontalDivider()
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.DirectionsBus, null, modifier = Modifier.size(20.dp))
                                Text("Transporte público cercano")
                            }
                            Switch(
                                checked = transportePublico,
                                onCheckedChange = { if (!isLoading) transportePublico = it }
                            )
                        }
                    }
                }
                
                // Información Adicional
                ModernCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "ℹ️ Información Adicional",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = edadMinima,
                                onValueChange = { edadMinima = it },
                                label = { Text("Edad mínima") },
                                placeholder = { Text("0") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            
                            OutlinedTextField(
                                value = edadMaxima,
                                onValueChange = { edadMaxima = it },
                                label = { Text("Edad máxima") },
                                placeholder = { Text("99") },
                                modifier = Modifier.weight(1f),
                                enabled = !isLoading,
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                        }
                        
                        OutlinedTextField(
                            value = etiquetas,
                            onValueChange = { etiquetas = it },
                            label = { Text("Etiquetas") },
                            placeholder = { Text("música, arte, familia") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = true
                        )
                        
                        OutlinedTextField(
                            value = sitioWeb,
                            onValueChange = { sitioWeb = it },
                            label = { Text("Sitio web") },
                            placeholder = { Text("https://...") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isLoading,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                        )
                    }
                }
                
                // Botón Crear Evento
                ModernButton(
                    text = if (isLoading) "Creando Evento..." else "🎉 Crear Evento",
                    onClick = {
                        keyboardController?.hide()
                        validationError = null
                        
                        // Validaciones
                        when {
                            nombre.isBlank() -> {
                                validationError = "El nombre del evento es requerido"
                                return@ModernButton
                            }
                            descripcion.isBlank() -> {
                                validationError = "La descripción es requerida"
                                return@ModernButton
                            }
                            ubicacion == null -> {
                                validationError = "La ubicación es requerida"
                                return@ModernButton
                            }
                            fechaInicio == null -> {
                                validationError = "La fecha de inicio es requerida"
                                return@ModernButton
                            }
                            fechaFin == null -> {
                                validationError = "La fecha de fin es requerida"
                                return@ModernButton
                            }
                            horaInicio == null -> {
                                validationError = "La hora de inicio es requerida"
                                return@ModernButton
                            }
                            horaFin == null -> {
                                validationError = "La hora de fin es requerida"
                                return@ModernButton
                            }
                            !esGratuito && precioGeneral.isBlank() -> {
                                validationError = "El precio es requerido para eventos de pago"
                                return@ModernButton
                            }
                        }
                        
                        // Validar fechas
                        val calInicio = Calendar.getInstance().apply { time = fechaInicio!! }
                        val calFin = Calendar.getInstance().apply { time = fechaFin!! }
                        
                        if (calFin.before(calInicio)) {
                            validationError = "La fecha de fin debe ser posterior o igual a la fecha de inicio"
                            return@ModernButton
                        }
                        
                        if (calInicio.get(Calendar.YEAR) == calFin.get(Calendar.YEAR) &&
                            calInicio.get(Calendar.DAY_OF_YEAR) == calFin.get(Calendar.DAY_OF_YEAR)) {
                            if (horaFin!!.first < horaInicio!!.first ||
                                (horaFin!!.first == horaInicio!!.first && horaFin!!.second <= horaInicio!!.second)) {
                                validationError = "La hora de fin debe ser posterior a la hora de inicio"
                                return@ModernButton
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
                            ubicacion = ubicacion!!,
                            direccion = ubicacion!!.direccion,
                            fechaInicio = com.google.firebase.Timestamp(calendarInicio.time),
                            fechaFin = com.google.firebase.Timestamp(calendarFin.time),
                            horaInicio = String.format("%02d:%02d", horaInicio!!.first, horaInicio!!.second),
                            horaFin = String.format("%02d:%02d", horaFin!!.first, horaFin!!.second),
                            organizador = organizador.trim().ifEmpty { "Sin organizador" },
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
                            imagenes = uploadedImageUrls,
                            estado = EstadoEvento.APROBADO // APROBADO directamente, sin verificación
                        )
                        
                        onCreateEvent(event)
                    },
                    variant = ButtonVariant.Primary,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && nombre.isNotBlank() && descripcion.isNotBlank() &&
                            ubicacion != null && fechaInicio != null && fechaFin != null &&
                            horaInicio != null && horaFin != null
                )
                
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
        
        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                ModernCard {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            color = PointsPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "Creando evento...",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Genera una descripción predeterminada para un evento
 */
private fun generateDefaultEventDescription(nombre: String, categoria: CategoriaEvento): String {
    val categoriaDesc = when (categoria) {
        CategoriaEvento.CULTURAL -> "un evento cultural que enriquece la vida de la comunidad con arte, tradición y expresiones culturales únicas"
        CategoriaEvento.DEPORTIVO -> "un evento deportivo lleno de emoción, competencia y espíritu deportivo que reúne a atletas y aficionados"
        CategoriaEvento.MUSICAL -> "un evento musical que promete emocionar con ritmos vibrantes y las mejores presentaciones en vivo"
        CategoriaEvento.GASTRONOMICO -> "un evento gastronómico que celebra la diversidad culinaria con los mejores sabores y experiencias"
        CategoriaEvento.TECNOLOGICO -> "un evento tecnológico que conecta a innovadores, emprendedores y entusiastas de la tecnología"
        CategoriaEvento.ARTISTICO -> "un evento artístico que celebra la creatividad y el talento de artistas locales e internacionales"
        CategoriaEvento.EDUCATIVO -> "un evento educativo que ofrece conocimiento, aprendizaje y desarrollo personal y profesional"
        CategoriaEvento.COMERCIAL -> "un evento comercial que conecta negocios, emprendedores y consumidores en un espacio dinámico"
        CategoriaEvento.RELIGIOSO -> "un evento religioso que reúne a la comunidad en fe, reflexión y celebración espiritual"
        CategoriaEvento.COMUNITARIO -> "un evento comunitario que fortalece los lazos vecinales y promueve la participación ciudadana"
        CategoriaEvento.FESTIVAL -> "un festival lleno de color, alegría y celebración que promete momentos inolvidables para toda la familia"
        CategoriaEvento.CONFERENCIA -> "una conferencia que reúne a expertos y profesionales para compartir conocimientos e ideas innovadoras"
        CategoriaEvento.TALLER -> "un taller práctico donde los participantes aprenderán nuevas habilidades de forma interactiva"
        CategoriaEvento.EXPOSICION -> "una exposición que presenta obras, productos o proyectos de manera profesional y atractiva"
        CategoriaEvento.FERIA -> "una feria vibrante con múltiples actividades, stands y opciones para toda la familia"
        CategoriaEvento.OTRO -> "un evento especial que promete ser una experiencia única y memorable"
    }
    
    return "$nombre es $categoriaDesc. Un evento imperdible que no te puedes perder. " +
            "Te invitamos a ser parte de esta experiencia única que promete dejar huella en todos los asistentes. " +
            "¡Ven y disfruta de todo lo que tenemos preparado para ti!"
}
