# 📚 Arquitectura MVVM - Documentación Completa

## 📋 Tabla de Contenidos

1. [Introducción a MVVM](#introducción-a-mvvm)
2. [Estructura del Proyecto](#estructura-del-proyecto)
3. [Componentes Principales](#componentes-principales)
4. [Flujo de Datos](#flujo-de-datos)
5. [Inyección de Dependencias](#inyección-de-dependencias)
6. [Ejemplos Prácticos](#ejemplos-prácticos)
7. [Patrones y Conceptos Clave](#patrones-y-conceptos-clave)

---

## 🎯 Introducción a MVVM

### ¿Qué es MVVM?

**MVVM (Model-View-ViewModel)** es un patrón arquitectónico que separa la lógica de presentación de la lógica de negocio y los datos.

```
┌─────────────┐
│    VIEW     │  ← Interfaz de Usuario (Compose)
│  (Pantalla) │
└──────┬──────┘
       │ Observa
       ↓
┌─────────────┐
│  VIEWMODEL  │  ← Lógica de Presentación
│  (Estado)   │
└──────┬──────┘
       │ Usa
       ↓
┌─────────────┐
│ REPOSITORY  │  ← Acceso a Datos
└──────┬──────┘
       │
       ├──→ Firebase Firestore
       ├──→ Firebase Storage
       ├──→ Retrofit (APIs)
       └──→ Room Database
```

### Ventajas de MVVM

✅ **Separación de responsabilidades**: Cada componente tiene una función específica  
✅ **Testabilidad**: Fácil de probar cada capa por separado  
✅ **Mantenibilidad**: Código organizado y fácil de mantener  
✅ **Reutilización**: ViewModels pueden ser reutilizados en diferentes Views  
✅ **Reactividad**: Los cambios en el estado se reflejan automáticamente en la UI

---

## 📁 Estructura del Proyecto

```
app/src/main/java/com/example/points/
│
├── 📂 models/              # MODEL - Clases de datos
│   ├── PointOfInterest.kt
│   ├── Incident.kt
│   ├── Event.kt
│   ├── User.kt
│   └── weather/
│       └── WeatherResponse.kt
│
├── 📂 repository/          # REPOSITORY - Acceso a datos
│   ├── PointOfInterestRepository.kt
│   ├── IncidentRepository.kt
│   ├── EventRepository.kt
│   ├── UserRepository.kt
│   ├── WeatherRepository.kt
│   ├── GeminiRepository.kt
│   └── DefaultWeatherRepository.kt
│
├── 📂 viewmodel/           # VIEWMODEL - Lógica de presentación
│   ├── PointOfInterestViewModel.kt
│   ├── IncidentViewModel.kt
│   ├── EventViewModel.kt
│   ├── UserManagementViewModel.kt
│   └── DashboardViewModel.kt
│
├── 📂 screens/             # VIEW - Pantallas (UI)
│   ├── POISubmissionScreen.kt
│   ├── POIDetailScreen.kt
│   ├── IncidentsScreen.kt
│   └── ...
│
├── 📂 data/                # Configuración de dependencias
│   ├── AppContainer.kt
│   └── DefaultAppContainer.kt
│
├── 📂 network/             # Servicios de red
│   ├── WeatherApiService.kt
│   └── GeminiApiService.kt
│
├── 📂 utils/                # Utilidades
│   └── EnvironmentConfig.kt
│
└── PointsApplication.kt     # Clase Application
```

---

## 🧩 Componentes Principales

### 1. MODEL (Modelo de Datos)

Los **Models** son clases de datos que representan las entidades del dominio.

#### Ejemplo: `PointOfInterest.kt`

```kotlin
data class PointOfInterest(
    val id: String = "",
    val nombre: String = "",
    val descripcion: String = "",
    val categoria: CategoriaPOI = CategoriaPOI.COMIDA,
    val ubicacion: Ubicacion = Ubicacion(),
    val direccion: String = "",
    val estado: EstadoPOI = EstadoPOI.PENDIENTE,
    val fechaCreacion: Timestamp = Timestamp.now(),
    // ... más propiedades
)
```

**Características:**
- ✅ Clase `data class` (genera automáticamente `equals()`, `hashCode()`, `toString()`, `copy()`)
- ✅ Propiedades inmutables (`val`)
- ✅ Valores por defecto
- ✅ Compatible con Firebase Firestore (serialización automática)

---

### 2. REPOSITORY (Repositorio)

El **Repository** es la capa de acceso a datos. Actúa como intermediario entre el ViewModel y las fuentes de datos.

#### Ejemplo: `PointOfInterestRepository.kt`

```kotlin
class PointOfInterestRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val poiCollection = firestore.collection("puntos_interes")
    
    // Retorna un Flow para actualizaciones en tiempo real
    fun getAllApprovedPOIs(): Flow<List<PointOfInterest>> = callbackFlow {
        val listener = poiCollection
            .whereEqualTo("estado", EstadoPOI.APROBADO.name)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                
                val pois = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PointOfInterest::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                
                trySend(pois)
            }
        
        awaitClose { listener.remove() }
    }.flowOn(Dispatchers.IO)
    
    // Operación suspend para crear un POI
    suspend fun createPOI(poi: PointOfInterest): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val docRef = poiCollection.add(poi).await()
                Result.success(docRef.id)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
```

**Características del Repository:**
- ✅ **Abstracción**: Oculta la implementación de Firebase/API
- ✅ **Flows**: Retorna `Flow<T>` para datos reactivos en tiempo real
- ✅ **Result**: Usa `Result<T>` para manejar éxito/error
- ✅ **Coroutines**: Usa `suspend fun` para operaciones asíncronas
- ✅ **Dispatchers**: Ejecuta operaciones de red en `Dispatchers.IO`

---

### 3. VIEWMODEL (Modelo de Vista)

El **ViewModel** contiene la lógica de presentación y el estado de la UI.

#### Ejemplo: `PointOfInterestViewModel.kt`

```kotlin
class PointOfInterestViewModel(
    private val poiRepository: PointOfInterestRepository,
    private val weatherRepository: WeatherRepository,
    private val geminiRepository: GeminiRepository? = null
) : ViewModel() {
    
    // Estado privado mutable
    private val _uiState = MutableStateFlow(POIUIState())
    
    // Estado público inmutable (solo lectura)
    val uiState: StateFlow<POIUIState> = _uiState.asStateFlow()
    
    init {
        loadAllPOIs()
    }
    
    // Función para cargar POIs
    fun loadAllPOIs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                poiRepository.getAllApprovedPOIs().collect { pois ->
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        filteredPOIs = applyFilters(pois),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
    
    // Función para crear un POI
    fun submitPOI(poi: PointOfInterest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true)
            
            try {
                val result = poiRepository.createPOI(poi)
                result.fold(
                    onSuccess = { poiId ->
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            submitSuccess = true
                        )
                        loadAllPOIs() // Recargar lista
                    },
                    onFailure = { error ->
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = "Error: ${error.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
}
```

**Características del ViewModel:**
- ✅ **StateFlow**: Maneja el estado de la UI de forma reactiva
- ✅ **viewModelScope**: Coroutine scope que se cancela cuando el ViewModel se destruye
- ✅ **Inmutabilidad**: El estado público es de solo lectura (`asStateFlow()`)
- ✅ **Separación**: No conoce la UI, solo maneja lógica de negocio

#### UIState (Estado de la UI)

```kotlin
data class POIUIState(
    val isLoading: Boolean = false,
    val pois: List<PointOfInterest> = emptyList(),
    val filteredPOIs: List<PointOfInterest> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: CategoriaPOI? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    // ... más estados
)
```

**Ventajas de UIState:**
- ✅ **Estado centralizado**: Todo el estado de la UI en un solo lugar
- ✅ **Inmutable**: Usa `data class` con `copy()` para actualizaciones
- ✅ **Type-safe**: El compilador verifica que todos los estados estén definidos

---

### 4. VIEW (Vista - Pantalla)

La **View** es la interfaz de usuario construida con Jetpack Compose.

#### Ejemplo: `POISubmissionScreen.kt`

```kotlin
@Composable
fun POISubmissionScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    // Observar el estado del ViewModel
    val uiState by viewModel.uiState.collectAsState()
    
    // Estado local de la UI
    var nombre by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var categoria by remember { mutableStateOf<CategoriaPOI?>(null) }
    
    // Efecto para pegar descripción generada automáticamente
    LaunchedEffect(uiState.generatedDescription) {
        uiState.generatedDescription?.let { generatedDesc ->
            descripcion = generatedDesc
            viewModel.clearGeneratedDescription()
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Agregar POI") })
        
        // Mostrar loading
        if (uiState.isSubmitting) {
            CircularProgressIndicator()
        }
        
        // Mostrar error
        uiState.errorMessage?.let { error ->
            Text(text = error, color = MaterialTheme.colorScheme.error)
        }
        
        // Formulario
        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre") }
        )
        
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") }
        )
        
        // Botón para generar descripción con IA
        TextButton(
            onClick = {
                viewModel.generateDescription(nombre, categoria ?: CategoriaPOI.OTRO, direccion)
            }
        ) {
            Text("Generar con IA")
        }
        
        // Botón para enviar
        Button(
            onClick = {
                val poi = PointOfInterest(
                    nombre = nombre,
                    descripcion = descripcion,
                    categoria = categoria ?: CategoriaPOI.OTRO,
                    // ... más campos
                )
                viewModel.submitPOI(poi)
            },
            enabled = !uiState.isSubmitting
        ) {
            Text("Enviar")
        }
        
        // Mostrar éxito
        if (uiState.submitSuccess) {
            Text("¡POI enviado exitosamente!")
        }
    }
}
```

**Características de la View:**
- ✅ **@Composable**: Función que construye la UI
- ✅ **collectAsState()**: Observa cambios en el StateFlow del ViewModel
- ✅ **remember**: Mantiene estado local durante recomposiciones
- ✅ **LaunchedEffect**: Ejecuta efectos secundarios cuando cambian las keys
- ✅ **Reactive**: Se recompone automáticamente cuando cambia el estado

---

## 🔄 Flujo de Datos

### Flujo Completo: Crear un POI

```
1. USUARIO INTERACTÚA
   └─> Usuario llena formulario y presiona "Enviar"
       │
       ↓
2. VIEW (Pantalla)
   └─> onClick { viewModel.submitPOI(poi) }
       │
       ↓
3. VIEWMODEL
   └─> viewModelScope.launch {
           _uiState.value = _uiState.value.copy(isSubmitting = true)
           val result = poiRepository.createPOI(poi)
           // Actualizar estado según resultado
       }
       │
       ↓
4. REPOSITORY
   └─> suspend fun createPOI(poi: PointOfInterest): Result<String> {
           return withContext(Dispatchers.IO) {
               try {
                   val docRef = firestore.collection("puntos_interes")
                       .add(poi).await()
                   Result.success(docRef.id)
               } catch (e: Exception) {
                   Result.failure(e)
               }
           }
       }
       │
       ↓
5. FIREBASE FIRESTORE
   └─> Guarda el documento en la base de datos
       │
       ↓
6. REPOSITORY RETORNA
   └─> Result.success(poiId) o Result.failure(error)
       │
       ↓
7. VIEWMODEL ACTUALIZA ESTADO
   └─> _uiState.value = _uiState.value.copy(
           isSubmitting = false,
           submitSuccess = true
       )
       │
       ↓
8. VIEW SE RECOMPONE
   └─> La UI muestra el mensaje de éxito automáticamente
       (porque observa uiState con collectAsState())
```

### Flujo de Datos Reactivo: Cargar POIs

```
1. VIEWMODEL INICIA
   └─> init { loadAllPOIs() }
       │
       ↓
2. REPOSITORY RETORNA FLOW
   └─> fun getAllApprovedPOIs(): Flow<List<PointOfInterest>> {
           return callbackFlow {
               firestore.collection("puntos_interes")
                   .whereEqualTo("estado", "APROBADO")
                   .addSnapshotListener { snapshot, error ->
                       // Emite nuevos datos cuando cambian
                       trySend(pois)
                   }
           }
       }
       │
       ↓
3. VIEWMODEL COLECTA EL FLOW
   └─> poiRepository.getAllApprovedPOIs().collect { pois ->
           _uiState.value = _uiState.value.copy(pois = pois)
       }
       │
       ↓
4. ESTADO SE ACTUALIZA
   └─> _uiState.value cambia → StateFlow emite nuevo valor
       │
       ↓
5. VIEW OBSERVA Y SE RECOMPONE
   └─> val uiState by viewModel.uiState.collectAsState()
       → La UI muestra los nuevos POIs automáticamente
```

**Ventajas del flujo reactivo:**
- ✅ **Tiempo real**: Los cambios en Firebase se reflejan automáticamente
- ✅ **Sin polling**: No necesitas recargar manualmente
- ✅ **Eficiente**: Solo se actualiza cuando hay cambios reales

---

## 💉 Inyección de Dependencias

### ¿Qué es la Inyección de Dependencias?

La **Inyección de Dependencias (DI)** es un patrón donde las dependencias se proporcionan desde fuera, en lugar de crearlas dentro de la clase.

### Estructura de DI en el Proyecto

#### 1. `AppContainer.kt` (Interfaz)

```kotlin
interface AppContainer {
    val weatherRepository: WeatherRepository
    val dashboardRepository: DashboardRepository
    val geminiRepository: GeminiRepository?
    // ... más repositorios
}
```

#### 2. `DefaultAppContainer.kt` (Implementación)

```kotlin
class DefaultAppContainer(private val context: Context) : AppContainer {
    
    // Configuración de Retrofit para APIs
    private val weatherRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    private val geminiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    // Servicios de API
    private val weatherApiService: WeatherApiService by lazy {
        weatherRetrofit.create(WeatherApiService::class.java)
    }
    
    private val geminiApiService: GeminiApiService by lazy {
        geminiRetrofit.create(GeminiApiService::class.java)
    }
    
    // Repositorios (inyectados)
    override val weatherRepository: WeatherRepository by lazy {
        DefaultWeatherRepository(weatherApiService)
    }
    
    override val geminiRepository: GeminiRepository? by lazy {
        val apiKey = EnvironmentConfig.GEMINI_API_KEY
        if (apiKey.isEmpty()) {
            null // Si no hay API key, retorna null
        } else {
            DefaultGeminiRepository(geminiApiService)
        }
    }
    
    // ... más repositorios
}
```

#### 3. `PointsApplication.kt` (Inicialización)

```kotlin
class PointsApplication : Application() {
    lateinit var container: AppContainer
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar variables de entorno
        EnvironmentConfig.initialize(this)
        
        // Crear contenedor de dependencias
        container = DefaultAppContainer(this)
    }
}
```

#### 4. `ViewModelFactory` (Crear ViewModels con DI)

```kotlin
class PointOfInterestViewModel(
    private val poiRepository: PointOfInterestRepository,
    private val weatherRepository: WeatherRepository,
    private val geminiRepository: GeminiRepository? = null
) : ViewModel() {
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                // Obtener la aplicación
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] 
                    as PointsApplication
                
                // Obtener repositorios del contenedor
                val poiRepository = PointOfInterestRepository()
                val weatherRepository = application.container.weatherRepository
                val geminiRepository = application.container.geminiRepository
                
                // Crear ViewModel con dependencias inyectadas
                PointOfInterestViewModel(poiRepository, weatherRepository, geminiRepository)
            }
        }
    }
}
```

#### 5. Uso en la Pantalla

```kotlin
@Composable
fun POISubmissionScreen(
    navController: NavController,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    // El ViewModel se crea automáticamente con todas sus dependencias
    val uiState by viewModel.uiState.collectAsState()
    // ...
}
```

**Ventajas de la Inyección de Dependencias:**
- ✅ **Testabilidad**: Puedes inyectar mocks para pruebas
- ✅ **Flexibilidad**: Fácil cambiar implementaciones
- ✅ **Mantenibilidad**: Dependencias centralizadas
- ✅ **Reutilización**: Mismo repositorio usado en múltiples ViewModels

---

## 📝 Ejemplos Prácticos

### Ejemplo 1: Cargar y Mostrar POIs

#### ViewModel

```kotlin
class PointOfInterestViewModel(
    private val poiRepository: PointOfInterestRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(POIUIState())
    val uiState: StateFlow<POIUIState> = _uiState.asStateFlow()
    
    fun loadAllPOIs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                poiRepository.getAllApprovedPOIs().collect { pois ->
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.message}"
                )
            }
        }
    }
}
```

#### View

```kotlin
@Composable
fun POIScreen(
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(Unit) {
        viewModel.loadAllPOIs()
    }
    
    when {
        uiState.isLoading -> {
            CircularProgressIndicator()
        }
        uiState.errorMessage != null -> {
            Text("Error: ${uiState.errorMessage}")
        }
        else -> {
            LazyColumn {
                items(uiState.pois) { poi ->
                    POICard(poi = poi)
                }
            }
        }
    }
}
```

---

### Ejemplo 2: Generar Descripción con Gemini API

#### Repository

```kotlin
interface GeminiRepository {
    suspend fun generatePOIDescription(
        nombre: String,
        categoria: CategoriaPOI,
        direccion: String? = null
    ): Result<String>
}

class DefaultGeminiRepository(
    private val geminiApiService: GeminiApiService
) : GeminiRepository {
    
    override suspend fun generatePOIDescription(
        nombre: String,
        categoria: CategoriaPOI,
        direccion: String?
    ): Result<String> {
        return try {
            val prompt = buildPrompt(nombre, categoria, direccion)
            val request = GeminiRequest(
                contents = listOf(Content(parts = listOf(Part(text = prompt))))
            )
            
            val response = geminiApiService.generateContent(
                apiKey = EnvironmentConfig.GEMINI_API_KEY,
                request = request
            )
            
            val description = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("No se pudo generar descripción")
            
            Result.success(description)
        } catch (e: Exception) {
            // Si falla, generar descripción predeterminada
            Result.success(generateDefaultDescription(nombre, categoria, direccion))
        }
    }
}
```

#### ViewModel

```kotlin
fun generateDescription(nombre: String, categoria: CategoriaPOI, direccion: String? = null) {
    viewModelScope.launch {
        _uiState.value = _uiState.value.copy(
            isGeneratingDescription = true,
            descriptionGenerationError = null
        )
        
        val geminiRepo = geminiRepository
        if (geminiRepo != null) {
            try {
                val result = geminiRepo.generatePOIDescription(nombre, categoria, direccion)
                result.fold(
                    onSuccess = { description ->
                        _uiState.value = _uiState.value.copy(
                            isGeneratingDescription = false,
                            generatedDescription = description
                        )
                    },
                    onFailure = { exception ->
                        // Usar descripción predeterminada
                        val defaultDescription = generateDefaultDescription(nombre, categoria, direccion)
                        _uiState.value = _uiState.value.copy(
                            isGeneratingDescription = false,
                            generatedDescription = defaultDescription
                        )
                    }
                )
            } catch (e: Exception) {
                // Usar descripción predeterminada
                val defaultDescription = generateDefaultDescription(nombre, categoria, direccion)
                _uiState.value = _uiState.value.copy(
                    isGeneratingDescription = false,
                    generatedDescription = defaultDescription
                )
            }
        } else {
            // Si no hay Gemini, usar descripción predeterminada
            val defaultDescription = generateDefaultDescription(nombre, categoria, direccion)
            _uiState.value = _uiState.value.copy(
                isGeneratingDescription = false,
                generatedDescription = defaultDescription
            )
        }
    }
}
```

#### View

```kotlin
@Composable
fun POISubmissionScreen(
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    var descripcion by remember { mutableStateOf("") }
    val uiState by viewModel.uiState.collectAsState()
    
    // Pegar descripción generada automáticamente
    LaunchedEffect(uiState.generatedDescription) {
        uiState.generatedDescription?.let { generatedDesc ->
            descripcion = generatedDesc
            viewModel.clearGeneratedDescription()
        }
    }
    
    Column {
        OutlinedTextField(
            value = descripcion,
            onValueChange = { descripcion = it },
            label = { Text("Descripción") }
        )
        
        TextButton(
            onClick = {
                viewModel.generateDescription(nombre, categoria, direccion)
            },
            enabled = !uiState.isGeneratingDescription
        ) {
            if (uiState.isGeneratingDescription) {
                CircularProgressIndicator()
            } else {
                Text("Generar con IA")
            }
        }
    }
}
```

---

### Ejemplo 3: Cargar Clima para un POI

#### Repository

```kotlin
interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse
}

class DefaultWeatherRepository(
    private val weatherApiService: WeatherApiService
) : WeatherRepository {
    
    override suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        val apiKey = EnvironmentConfig.OPENWEATHER_API_KEY
        if (apiKey.isEmpty()) {
            throw IllegalStateException("OpenWeatherMap API key no configurada")
        }
        
        return weatherApiService.getCurrentWeather(
            lat = lat,
            lon = lon,
            appid = apiKey,
            units = "metric",
            lang = "es"
        )
    }
}
```

#### ViewModel

```kotlin
fun loadWeatherForPOI(ubicacion: Ubicacion) {
    viewModelScope.launch {
        val apiKey = EnvironmentConfig.OPENWEATHER_API_KEY
        if (apiKey.isEmpty()) {
            // No mostrar error, solo no cargar
            return@launch
        }
        
        _uiState.value = _uiState.value.copy(
            isLoadingWeather = true,
            weatherError = null
        )
        
        try {
            val weather = weatherRepository.getWeather(ubicacion.lat, ubicacion.lon)
            _uiState.value = _uiState.value.copy(
                weatherResponse = weather,
                isLoadingWeather = false
            )
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                weatherError = "Error al cargar el clima",
                isLoadingWeather = false
            )
        }
    }
}
```

#### View

```kotlin
@Composable
fun POIDetailScreen(
    poi: PointOfInterest,
    viewModel: PointOfInterestViewModel = viewModel(factory = PointOfInterestViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Cargar clima cuando se muestra el POI
    LaunchedEffect(poi) {
        viewModel.loadWeatherForPOI(poi.ubicacion)
    }
    
    // Limpiar estado cuando se sale de la pantalla
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearWeatherState()
        }
    }
    
    Column {
        // Información del POI
        Text(poi.nombre)
        Text(poi.descripcion)
        
        // Sección de clima
        if (uiState.isLoadingWeather) {
            CircularProgressIndicator()
        } else if (uiState.weatherResponse != null) {
            val current = uiState.weatherResponse.current
            Text("Temperatura: ${current.temperature}°C")
            Text("Sensación: ${current.feelsLike}°C")
            Text("Condición: ${current.weather.firstOrNull()?.description}")
        } else if (uiState.weatherError != null) {
            Text("Error: ${uiState.weatherError}")
        }
    }
}
```

---

## 🎓 Patrones y Conceptos Clave

### 1. StateFlow vs MutableStateFlow

```kotlin
// Estado privado mutable (solo el ViewModel puede modificarlo)
private val _uiState = MutableStateFlow(POIUIState())

// Estado público inmutable (la View solo puede leerlo)
val uiState: StateFlow<POIUIState> = _uiState.asStateFlow()
```

**¿Por qué?**
- ✅ **Encapsulación**: Solo el ViewModel puede modificar el estado
- ✅ **Type-safety**: La View no puede modificar accidentalmente el estado
- ✅ **Reactividad**: Los cambios se propagan automáticamente

---

### 2. Result<T> para Manejo de Errores

```kotlin
suspend fun createPOI(poi: PointOfInterest): Result<String> {
    return try {
        val docRef = poiCollection.add(poi).await()
        Result.success(docRef.id)
    } catch (e: Exception) {
        Result.failure(e)
    }
}

// Uso:
result.fold(
    onSuccess = { poiId -> /* éxito */ },
    onFailure = { error -> /* error */ }
)
```

**Ventajas:**
- ✅ **Type-safe**: El compilador fuerza el manejo de ambos casos
- ✅ **Funcional**: Usa `fold()` para manejar ambos casos
- ✅ **Sin excepciones**: No necesitas try-catch en cada llamada

---

### 3. Flow para Datos Reactivos

```kotlin
fun getAllApprovedPOIs(): Flow<List<PointOfInterest>> = callbackFlow {
    val listener = poiCollection
        .whereEqualTo("estado", EstadoPOI.APROBADO.name)
        .addSnapshotListener { snapshot, error ->
            // Emite nuevos datos cuando cambian en Firebase
            trySend(pois)
        }
    
    awaitClose { listener.remove() }
}

// Uso en ViewModel:
poiRepository.getAllApprovedPOIs().collect { pois ->
    _uiState.value = _uiState.value.copy(pois = pois)
}
```

**Ventajas:**
- ✅ **Tiempo real**: Actualizaciones automáticas cuando cambian los datos
- ✅ **Eficiente**: Solo emite cuando hay cambios reales
- ✅ **Cancelable**: Se cancela automáticamente cuando el ViewModel se destruye

---

### 4. LaunchedEffect para Efectos Secundarios

```kotlin
// Ejecuta una vez cuando se compone la pantalla
LaunchedEffect(Unit) {
    viewModel.loadAllPOIs()
}

// Ejecuta cuando cambia 'poi'
LaunchedEffect(poi) {
    viewModel.loadWeatherForPOI(poi.ubicacion)
}

// Ejecuta cuando cambia 'uiState.generatedDescription'
LaunchedEffect(uiState.generatedDescription) {
    uiState.generatedDescription?.let { desc ->
        descripcion = desc
        viewModel.clearGeneratedDescription()
    }
}
```

**Características:**
- ✅ **Keys**: Solo se ejecuta cuando cambian las keys
- ✅ **Cancelable**: Se cancela si la key cambia antes de terminar
- ✅ **Lifecycle-aware**: Se cancela cuando el Composable sale de la composición

---

### 5. DisposableEffect para Limpieza

```kotlin
DisposableEffect(Unit) {
    // Código que se ejecuta cuando se compone
    onDispose {
        // Código de limpieza cuando se descompone
        viewModel.clearWeatherState()
    }
}
```

**Uso típico:**
- Limpiar suscripciones
- Cancelar operaciones pendientes
- Liberar recursos

---

### 6. viewModelScope para Coroutines

```kotlin
fun loadAllPOIs() {
    viewModelScope.launch {
        // Esta coroutine se cancela automáticamente
        // cuando el ViewModel se destruye
        val pois = poiRepository.getAllApprovedPOIs()
        // ...
    }
}
```

**Ventajas:**
- ✅ **Lifecycle-aware**: Se cancela cuando el ViewModel se destruye
- ✅ **Sin memory leaks**: Evita fugas de memoria
- ✅ **Automático**: No necesitas cancelar manualmente

---

### 7. collectAsState() para Observar Estado

```kotlin
@Composable
fun POIScreen(viewModel: PointOfInterestViewModel) {
    // Observa el StateFlow y se recompone cuando cambia
    val uiState by viewModel.uiState.collectAsState()
    
    // La UI se actualiza automáticamente cuando uiState cambia
    Text("POIs: ${uiState.pois.size}")
}
```

**Características:**
- ✅ **Reactivo**: Se recompone automáticamente cuando cambia el estado
- ✅ **Eficiente**: Solo se recompone cuando hay cambios reales
- ✅ **Type-safe**: El compilador verifica que el tipo sea correcto

---

## 🔍 Resumen del Flujo Completo

### 1. Inicialización de la App

```
MainActivity.onCreate()
    ↓
PointsApplication.onCreate()
    ↓
EnvironmentConfig.initialize()
    ↓
DefaultAppContainer(context)
    ↓
Configura Retrofit, Firebase, Room
    ↓
Crea repositorios
```

### 2. Navegación

```
AppNavigation
    ↓
NavHost con rutas
    ↓
composable(AppRoutes.POI_SUBMISSION) {
    POISubmissionScreen(
        viewModel = viewModel(factory = PointOfInterestViewModel.Factory)
    )
}
```

### 3. Creación del ViewModel

```
viewModel(factory = PointOfInterestViewModel.Factory)
    ↓
ViewModelProvider usa el Factory
    ↓
Factory.initializer {
    val application = PointsApplication
    val poiRepository = PointOfInterestRepository()
    val weatherRepository = application.container.weatherRepository
    val geminiRepository = application.container.geminiRepository
    PointOfInterestViewModel(poiRepository, weatherRepository, geminiRepository)
}
```

### 4. Interacción Usuario → ViewModel

```
Usuario presiona botón
    ↓
onClick { viewModel.submitPOI(poi) }
    ↓
ViewModel actualiza estado: isSubmitting = true
    ↓
ViewModel llama repository.createPOI(poi)
    ↓
Repository guarda en Firebase
    ↓
Repository retorna Result
    ↓
ViewModel actualiza estado según resultado
    ↓
View se recompone automáticamente (collectAsState)
```

### 5. Datos Reactivos (Firebase → View)

```
Firebase Firestore cambia
    ↓
Repository.addSnapshotListener detecta cambio
    ↓
Repository emite nuevo valor en Flow
    ↓
ViewModel.collect recibe nuevo valor
    ↓
ViewModel actualiza _uiState
    ↓
StateFlow emite nuevo valor
    ↓
View.collectAsState() recibe nuevo valor
    ↓
View se recompone automáticamente
```

---

## ✅ Mejores Prácticas

1. **Separación de Responsabilidades**
   - ✅ View: Solo UI, no lógica de negocio
   - ✅ ViewModel: Lógica de presentación, no acceso directo a datos
   - ✅ Repository: Acceso a datos, no lógica de negocio

2. **Estado Inmutable**
   - ✅ Usa `data class` con `copy()` para actualizaciones
   - ✅ Expone solo `StateFlow` (inmutable), no `MutableStateFlow`

3. **Manejo de Errores**
   - ✅ Usa `Result<T>` para operaciones que pueden fallar
   - ✅ Muestra errores en el UIState
   - ✅ No silencies errores, siempre informa al usuario

4. **Coroutines**
   - ✅ Usa `viewModelScope` para coroutines en ViewModels
   - ✅ Usa `suspend fun` para operaciones asíncronas
   - ✅ Usa `Dispatchers.IO` para operaciones de red/archivos

5. **Flows**
   - ✅ Usa `Flow<T>` para datos reactivos en tiempo real
   - ✅ Usa `callbackFlow` para convertir callbacks a Flows
   - ✅ Cancela listeners en `awaitClose`

6. **Testing**
   - ✅ ViewModels son fáciles de probar (sin dependencias de UI)
   - ✅ Repositories pueden ser mockeados
   - ✅ Usa `TestDispatcher` para pruebas de coroutines

---

## 📚 Recursos Adicionales

- **Jetpack Compose**: https://developer.android.com/jetpack/compose
- **ViewModel**: https://developer.android.com/topic/libraries/architecture/viewmodel
- **StateFlow**: https://kotlin.github.io/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/
- **Coroutines**: https://kotlinlang.org/docs/coroutines-overview.html
- **Firebase Firestore**: https://firebase.google.com/docs/firestore

---

**Documento creado para el proyecto Points App**  
**Última actualización: 2025**

