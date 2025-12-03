package com.example.points.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.points.PointsApplication
import com.example.points.models.PointOfInterest
import com.example.points.models.CategoriaPOI
import com.example.points.models.EstadoPOI
import com.example.points.models.Ubicacion
import com.example.points.models.weather.WeatherResponse
import com.example.points.repository.PointOfInterestRepository
import com.example.points.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import android.util.Log
import retrofit2.HttpException
import java.io.IOException

data class POIUIState(
    val isLoading: Boolean = false,
    val pois: List<PointOfInterest> = emptyList(),
    val filteredPOIs: List<PointOfInterest> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: CategoriaPOI? = null,
    val showOnlyNearby: Boolean = false,
    val userLocation: Pair<Double, Double>? = null,
    val errorMessage: String? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val isLoadingWeather: Boolean = false,
    val weatherResponse: WeatherResponse? = null,
    val weatherError: String? = null,
    val selectedPOI: PointOfInterest? = null,
    // Estados para generación de descripción con Gemini
    val isGeneratingDescription: Boolean = false,
    val generatedDescription: String? = null,
    val descriptionGenerationError: String? = null
)

class PointOfInterestViewModel(
    private val poiRepository: PointOfInterestRepository,
    private val weatherRepository: WeatherRepository,
    private val geminiRepository: com.example.points.repository.GeminiRepository? = null
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(POIUIState())
    val uiState: StateFlow<POIUIState> = _uiState.asStateFlow()
    
    // Control de concurrencia para evitar múltiples cargas simultáneas
    private var currentLoadJob: Job? = null
    
    init {
        loadAllPOIs()
        // Cargar POIs de prueba temporalmente para debug
        loadTestPOIs()
    }
    
    // Método temporal para cargar POIs de prueba
    private fun loadTestPOIs() {
        viewModelScope.launch {
            try {
                val testPOIs = listOf(
                    PointOfInterest(
                        id = "test1",
                        nombre = "Restaurante Test",
                        descripcion = "Restaurante de prueba",
                        categoria = CategoriaPOI.COMIDA,
                        direccion = "Calle Test 123",
                        ubicacion = com.example.points.models.Ubicacion(lat = 40.4168, lon = -3.7038), // Madrid
                        telefono = "123456789",
                        email = "test@test.com",
                        sitioWeb = "https://test.com",
                        imagenes = emptyList(),
                        calificacion = 4.5,
                        totalCalificaciones = 10,
                        estado = EstadoPOI.APROBADO,
                        fechaCreacion = com.google.firebase.Timestamp.now(),
                        fechaActualizacion = com.google.firebase.Timestamp.now(),
                        usuarioId = "test_user",
                        horarios = emptyList(),
                        caracteristicas = emptyList()
                    ),
                    PointOfInterest(
                        id = "test2",
                        nombre = "Parque Test",
                        descripcion = "Parque de prueba",
                        categoria = CategoriaPOI.PARQUES,
                        direccion = "Parque Test 456",
                        ubicacion = com.example.points.models.Ubicacion(lat = 40.4200, lon = -3.7100), // Madrid
                        telefono = null,
                        email = null,
                        sitioWeb = null,
                        imagenes = emptyList(),
                        calificacion = 4.0,
                        totalCalificaciones = 5,
                        estado = EstadoPOI.APROBADO,
                        fechaCreacion = com.google.firebase.Timestamp.now(),
                        fechaActualizacion = com.google.firebase.Timestamp.now(),
                        usuarioId = "test_user",
                        horarios = emptyList(),
                        caracteristicas = emptyList()
                    )
                )
                
                Log.d("POIViewModel", "Cargando POIs de prueba: ${testPOIs.size}")
                _uiState.value = _uiState.value.copy(
                    pois = testPOIs,
                    filteredPOIs = applyFilters(testPOIs),
                    isLoading = false
                )
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error cargando POIs de prueba", e)
            }
        }
    }
    
    fun loadAllPOIs() {
        // Cancelar carga anterior si existe
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                Log.d("POIViewModel", "Iniciando carga de POIs...")
                poiRepository.getAllApprovedPOIs().collect { pois ->
                    Log.d("POIViewModel", "POIs recibidos: ${pois.size}")
                    pois.forEach { poi ->
                        Log.d("POIViewModel", "POI: ${poi.nombre} - Estado: ${poi.estado.displayName} - Coord: (${poi.ubicacion.lat}, ${poi.ubicacion.lon})")
                    }
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        filteredPOIs = applyFilters(pois),
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI loading was cancelled")
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar los puntos de interés: ${e.message}"
                )
            }
        }
    }
    
    fun loadPOIsByCategory(categoria: CategoriaPOI) {
        // Cancelar carga anterior si existe
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                poiRepository.getPOIsByCategory(categoria).collect { pois ->
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        filteredPOIs = applyFilters(pois),
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI loading by category was cancelled")
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading POIs by category", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar los puntos de interés: ${e.message}"
                )
            }
        }
    }
    
    fun searchPOIs(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        
        if (query.isBlank()) {
            loadAllPOIs()
        } else {
            // Cancelar carga anterior si existe
            currentLoadJob?.cancel()
            
            currentLoadJob = viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                
                try {
                    poiRepository.searchPOIs(query).collect { pois ->
                        _uiState.value = _uiState.value.copy(
                            pois = pois,
                            filteredPOIs = applyFilters(pois),
                            isLoading = false
                        )
                    }
                } catch (e: CancellationException) {
                    // No mostrar error para cancelaciones normales
                    Log.d("POIViewModel", "POI search was cancelled")
                } catch (e: Exception) {
                    Log.e("POIViewModel", "Error searching POIs", e)
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Error al buscar puntos de interés: ${e.message}"
                    )
                }
            }
        }
    }
    
    fun loadNearbyPOIs(lat: Double, lon: Double, radiusKm: Double = 5.0) {
        // Cancelar carga anterior si existe
        currentLoadJob?.cancel()
        
        currentLoadJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true, 
                errorMessage = null,
                userLocation = Pair(lat, lon)
            )
            
            try {
                poiRepository.getNearbyPOIs(lat, lon, radiusKm).collect { pois ->
                    _uiState.value = _uiState.value.copy(
                        pois = pois,
                        filteredPOIs = applyFilters(pois),
                        isLoading = false
                    )
                }
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI loading nearby was cancelled")
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error loading nearby POIs", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al cargar puntos cercanos: ${e.message}"
                )
            }
        }
    }
    
    fun setCategoryFilter(categoria: CategoriaPOI?) {
        _uiState.value = _uiState.value.copy(selectedCategory = categoria)
        _uiState.value = _uiState.value.copy(filteredPOIs = applyFilters(_uiState.value.pois))
    }
    
    fun toggleNearbyFilter() {
        val newShowOnlyNearby = !_uiState.value.showOnlyNearby
        _uiState.value = _uiState.value.copy(showOnlyNearby = newShowOnlyNearby)
        _uiState.value = _uiState.value.copy(filteredPOIs = applyFilters(_uiState.value.pois))
    }
    
    fun clearFilters() {
        _uiState.value = _uiState.value.copy(
            searchQuery = "",
            selectedCategory = null,
            showOnlyNearby = false
        )
        loadAllPOIs()
    }
    
    private fun applyFilters(pois: List<PointOfInterest>): List<PointOfInterest> {
        var filtered = pois
        
        // Filtrar por categoría
        _uiState.value.selectedCategory?.let { categoria ->
            filtered = filtered.filter { it.categoria == categoria }
        }
        
        // Filtrar por búsqueda
        if (_uiState.value.searchQuery.isNotBlank()) {
            val query = _uiState.value.searchQuery.lowercase()
            filtered = filtered.filter { poi ->
                poi.nombre.lowercase().contains(query) ||
                poi.descripcion.lowercase().contains(query) ||
                poi.direccion.lowercase().contains(query)
            }
        }
        
        // Filtrar por cercanía (si se tiene ubicación del usuario)
        if (_uiState.value.showOnlyNearby && _uiState.value.userLocation != null) {
            val userLocation = _uiState.value.userLocation!!
            val (userLat, userLon) = userLocation
            filtered = filtered.filter { poi ->
                val distance = calculateDistance(userLat, userLon, poi.ubicacion.lat, poi.ubicacion.lon)
                distance <= 5.0 // 5 km de radio
            }.sortedBy { poi ->
                calculateDistance(userLat, userLon, poi.ubicacion.lat, poi.ubicacion.lon)
            }
        }
        
        return filtered
    }
    
    fun submitPOI(poi: PointOfInterest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            
            try {
                val result = poiRepository.createPOI(poi)
                result.fold(
                    onSuccess = { poiId ->
                        Log.d("POIViewModel", "POI submitted successfully: $poiId")
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            submitSuccess = true
                        )
                        // Recargar la lista después de un breve delay
                        kotlinx.coroutines.delay(1000)
                        loadAllPOIs()
                    },
                    onFailure = { error ->
                        Log.e("POIViewModel", "Error submitting POI", error)
                        _uiState.value = _uiState.value.copy(
                            isSubmitting = false,
                            errorMessage = "Error al enviar el punto de interés: ${error.message}"
                        )
                    }
                )
            } catch (e: CancellationException) {
                // No mostrar error para cancelaciones normales
                Log.d("POIViewModel", "POI submission was cancelled")
                _uiState.value = _uiState.value.copy(isSubmitting = false)
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error submitting POI", e)
                _uiState.value = _uiState.value.copy(
                    isSubmitting = false,
                    errorMessage = "Error al enviar el punto de interés: ${e.message}"
                )
            }
        }
    }
    
    fun clearSubmitSuccess() {
        _uiState.value = _uiState.value.copy(submitSuccess = false)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // Obtener POI por ID
    suspend fun getPOIById(poiId: String): Result<PointOfInterest?> {
        return try {
            val result = poiRepository.getPOIById(poiId)
            result.fold(
                onSuccess = { poi ->
                    _uiState.value = _uiState.value.copy(selectedPOI = poi)
                },
                onFailure = {}
            )
            result
        } catch (e: Exception) {
            Log.e("POIViewModel", "Error getting POI by ID", e)
            Result.failure(e)
        }
    }
    
    // Cargar clima para un POI
    fun loadWeatherForPOI(ubicacion: Ubicacion) {
        viewModelScope.launch {
            // Verificar si la API key está configurada antes de intentar cargar
            val apiKey = com.example.points.utils.EnvironmentConfig.OPENWEATHER_API_KEY
            if (apiKey.isEmpty()) {
                Log.w("POIViewModel", "OpenWeatherMap API key no configurada - omitiendo carga de clima")
                _uiState.value = _uiState.value.copy(
                    isLoadingWeather = false,
                    weatherError = null, // No mostramos error, solo no cargamos el clima
                    weatherResponse = null
                )
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
            } catch (e: IllegalStateException) {
                // API key no configurada - no es un error crítico
                Log.w("POIViewModel", "No se puede cargar clima: ${e.message}")
                _uiState.value = _uiState.value.copy(
                    weatherError = null, // No mostramos error si la API key no está configurada
                    isLoadingWeather = false,
                    weatherResponse = null
                )
            } catch (e: IOException) {
                Log.e("POIViewModel", "Error de red al cargar clima", e)
                _uiState.value = _uiState.value.copy(
                    weatherError = "Error de red",
                    isLoadingWeather = false
                )
            } catch (e: HttpException) {
                Log.e("POIViewModel", "Error del servidor al cargar clima", e)
                _uiState.value = _uiState.value.copy(
                    weatherError = "Error del servidor",
                    isLoadingWeather = false
                )
            } catch (e: Exception) {
                Log.e("POIViewModel", "Error inesperado al cargar clima", e)
                _uiState.value = _uiState.value.copy(
                    weatherError = "Error al cargar el clima",
                    isLoadingWeather = false
                )
            }
        }
    }
    
    // Limpiar estado del clima
    fun clearWeatherState() {
        _uiState.value = _uiState.value.copy(
            isLoadingWeather = false,
            weatherResponse = null,
            weatherError = null
        )
    }
    
    // Actualizar ubicación del usuario
    fun updateUserLocation(lat: Double, lon: Double) {
        _uiState.value = _uiState.value.copy(userLocation = Pair(lat, lon))
    }
    
    // Generar descripción para un POI usando Gemini
    fun generateDescription(nombre: String, categoria: CategoriaPOI, direccion: String? = null) {
        viewModelScope.launch {
            // Validar que el nombre y la categoría no estén vacíos
            if (nombre.isBlank()) {
                _uiState.value = _uiState.value.copy(
                    isGeneratingDescription = false,
                    descriptionGenerationError = "El nombre del POI es requerido para generar la descripción",
                    generatedDescription = null
                )
                return@launch
            }
            
            _uiState.value = _uiState.value.copy(
                isGeneratingDescription = true,
                descriptionGenerationError = null,
                generatedDescription = null
            )
            
            // Intentar usar Gemini si está disponible
            val geminiRepo = geminiRepository
            if (geminiRepo != null) {
                try {
                    val result = geminiRepo.generatePOIDescription(nombre, categoria, direccion)
                    
                    result.fold(
                        onSuccess = { description ->
                            Log.d("POIViewModel", "Descripción generada exitosamente: $description")
                            _uiState.value = _uiState.value.copy(
                                isGeneratingDescription = false,
                                generatedDescription = description,
                                descriptionGenerationError = null
                            )
                        },
                        onFailure = { exception ->
                            // Si falla Gemini, generar descripción predeterminada
                            Log.w("POIViewModel", "Error al generar con Gemini, usando descripción predeterminada", exception)
                            val defaultDescription = generateDefaultDescription(nombre, categoria, direccion)
                            _uiState.value = _uiState.value.copy(
                                isGeneratingDescription = false,
                                generatedDescription = defaultDescription,
                                descriptionGenerationError = null
                            )
                        }
                    )
                } catch (e: Exception) {
                    // Si hay excepción, generar descripción predeterminada
                    Log.w("POIViewModel", "Excepción al generar con Gemini, usando descripción predeterminada", e)
                    val defaultDescription = generateDefaultDescription(nombre, categoria, direccion)
                    _uiState.value = _uiState.value.copy(
                        isGeneratingDescription = false,
                        generatedDescription = defaultDescription,
                        descriptionGenerationError = null
                    )
                }
            } else {
                // Si no hay Gemini disponible, generar descripción predeterminada directamente
                Log.d("POIViewModel", "Gemini no disponible, generando descripción predeterminada")
                val defaultDescription = generateDefaultDescription(nombre, categoria, direccion)
                _uiState.value = _uiState.value.copy(
                    isGeneratingDescription = false,
                    generatedDescription = defaultDescription,
                    descriptionGenerationError = null
                )
            }
        }
    }
    
    /**
     * Genera una descripción predeterminada basada en la información del POI
     */
    private fun generateDefaultDescription(
        nombre: String,
        categoria: CategoriaPOI,
        direccion: String?
    ): String {
        val categoriaDesc = when (categoria) {
            CategoriaPOI.COMIDA -> "un establecimiento gastronómico que ofrece una experiencia culinaria única"
            CategoriaPOI.ENTRETENIMIENTO -> "un lugar de entretenimiento ideal para disfrutar en tu tiempo libre"
            CategoriaPOI.CULTURA -> "un espacio cultural que enriquece la vida de la comunidad"
            CategoriaPOI.DEPORTE -> "un centro deportivo donde puedes mantenerte activo y saludable"
            CategoriaPOI.SALUD -> "un centro de salud comprometido con el bienestar de la comunidad"
            CategoriaPOI.EDUCACION -> "una institución educativa que contribuye al desarrollo y aprendizaje"
            CategoriaPOI.TRANSPORTE -> "un punto de transporte que facilita la movilidad urbana"
            CategoriaPOI.SERVICIOS -> "un centro de servicios que atiende las necesidades de la comunidad"
            CategoriaPOI.TURISMO -> "un destino turístico que vale la pena visitar"
            CategoriaPOI.RECARGA_ELECTRICA -> "una estación de recarga eléctrica para vehículos sostenibles"
            CategoriaPOI.PARQUES -> "un espacio verde perfecto para relajarse y disfrutar de la naturaleza"
            CategoriaPOI.SHOPPING -> "un centro comercial con diversas opciones de compra y servicios"
            CategoriaPOI.OTRO -> "un punto de interés destacado en la zona"
        }
        
        val ubicacionDesc = if (direccion != null && direccion.isNotEmpty()) {
            "Ubicado en $direccion, "
        } else {
            ""
        }
        
        return "$nombre es $categoriaDesc. $ubicacionDesc" +
                "Un lugar que merece ser conocido y visitado por su contribución a la comunidad."
    }
    
    // Limpiar el estado de la descripción generada
    fun clearGeneratedDescription() {
        _uiState.value = _uiState.value.copy(
            generatedDescription = null,
            descriptionGenerationError = null
        )
    }
    
    // Limpiar el error de generación de descripción
    fun clearDescriptionGenerationError() {
        _uiState.value = _uiState.value.copy(
            descriptionGenerationError = null
        )
    }
    
    override fun onCleared() {
        super.onCleared()
        // Cancelar cualquier trabajo pendiente cuando el ViewModel se destruye
        currentLoadJob?.cancel()
    }
    
    // Calcular distancia entre dos puntos (fórmula de Haversine)
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val earthRadius = 6371.0 // Radio de la Tierra en kilómetros
        
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        
        val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
                kotlin.math.cos(Math.toRadians(lat1)) * kotlin.math.cos(Math.toRadians(lat2)) *
                kotlin.math.sin(dLon / 2) * kotlin.math.sin(dLon / 2)
        
        val c = 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
        
        return earthRadius * c
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as PointsApplication)
                val poiRepository = PointOfInterestRepository()
                val weatherRepository = application.container.weatherRepository
                val geminiRepository = application.container.geminiRepository
                PointOfInterestViewModel(poiRepository, weatherRepository, geminiRepository)
            }
        }
    }
}
