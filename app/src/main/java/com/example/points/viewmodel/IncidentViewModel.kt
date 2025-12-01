package com.example.points.viewmodel

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.points.PointsApplication
import com.example.points.models.Incident
import com.example.points.models.EstadoIncidente
import com.example.points.models.TipoIncidente
import com.example.points.models.TipoUsuario
import com.example.points.models.Ubicacion
import com.example.points.models.User
import com.example.points.repository.IncidentRepository
import com.example.points.repository.UserRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class IncidentUiState(
    val incidents: List<Incident> = emptyList(),
    val filteredIncidents: List<Incident> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedIncident: Incident? = null,
    val selectedType: TipoIncidente? = null,
    val selectedStatus: EstadoIncidente? = null,
    val currentUser: User? = null,
    val isUserAdmin: Boolean = false
)

data class CreateIncidentUiState(
    val tipo: TipoIncidente = TipoIncidente.INSEGURIDAD,
    val descripcion: String = "",
    val ubicacion: Ubicacion = Ubicacion(),
    val selectedImageUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val isAnalyzingImage: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null,
    val detectionResult: String? = null  // Mensaje del resultado de la detección
)

class IncidentViewModel(
    val repository: IncidentRepository,
    private val userRepository: UserRepository
) : ViewModel() {
    
    constructor() : this(
        repository = IncidentRepository(null),
        userRepository = UserRepository(
            com.google.firebase.firestore.FirebaseFirestore.getInstance(),
            com.google.firebase.auth.FirebaseAuth.getInstance()
        )
    )
    
    private val _uiState = MutableStateFlow(IncidentUiState())
    val uiState: StateFlow<IncidentUiState> = _uiState.asStateFlow()
    
    private val _createIncidentState = MutableStateFlow(CreateIncidentUiState())
    val createIncidentState: StateFlow<CreateIncidentUiState> = _createIncidentState.asStateFlow()
    
    init {
        // Inicializar el estado con una lista vacía para evitar crashes
        _uiState.value = _uiState.value.copy(
            incidents = emptyList(),
            filteredIncidents = emptyList(),
            isLoading = false
        )
        loadAllIncidents()
        loadCurrentUser()
    }
    
    fun loadAllIncidents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                repository.getAllIncidents().collect { incidents ->
                    _uiState.value = _uiState.value.copy(
                        incidents = incidents,
                        filteredIncidents = incidents,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("IncidentViewModel", "Error cargando incidentes: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }
    }
    
    fun filterByType(tipo: TipoIncidente?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val filteredIncidents = if (tipo == null) {
                currentState.incidents
            } else {
                currentState.incidents.filter { incident ->
                    incident.tipo == tipo.displayName
                }
            }
            
            _uiState.value = currentState.copy(
                selectedType = tipo,
                filteredIncidents = filteredIncidents,
                isLoading = false,
                errorMessage = null
            )
        }
    }
    
    fun filterByStatus(estado: EstadoIncidente?) {
        viewModelScope.launch {
            val currentState = _uiState.value
            val filteredIncidents = if (estado == null) {
                currentState.incidents
            } else {
                currentState.incidents.filter { incident ->
                    incident.estado == estado
                }
            }
            
            _uiState.value = currentState.copy(
                selectedStatus = estado,
                filteredIncidents = filteredIncidents,
                isLoading = false,
                errorMessage = null
            )
        }
    }
    
    fun selectIncident(incident: Incident?) {
        _uiState.value = _uiState.value.copy(selectedIncident = incident)
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
    
    // Funciones para crear incidente
    fun updateIncidentType(tipo: TipoIncidente) {
        _createIncidentState.value = _createIncidentState.value.copy(tipo = tipo)
    }
    
    fun updateDescription(descripcion: String) {
        _createIncidentState.value = _createIncidentState.value.copy(descripcion = descripcion)
    }
    
    fun updateLocation(ubicacion: Ubicacion) {
        _createIncidentState.value = _createIncidentState.value.copy(ubicacion = ubicacion)
    }
    
    fun updateSelectedImage(uri: Uri?) {
        _createIncidentState.value = _createIncidentState.value.copy(selectedImageUri = uri)
    }
    
    fun createIncident(context: Context) {
        viewModelScope.launch {
            val currentState = _createIncidentState.value
            
            if (currentState.descripcion.isBlank()) {
                _createIncidentState.value = currentState.copy(
                    errorMessage = "La descripción es requerida"
                )
                return@launch
            }
            
            if (currentState.ubicacion.lat == 0.0 && currentState.ubicacion.lon == 0.0) {
                _createIncidentState.value = currentState.copy(
                    errorMessage = "La ubicación es requerida"
                )
                return@launch
            }
            
            _createIncidentState.value = currentState.copy(
                isSubmitting = true,
                errorMessage = null,
                detectionResult = null
            )
            
            try {
                // Subir imagen si existe
                var imageUrl: String? = null
                var prioridad: String? = null
                var etiqueta_ia: String? = null
                var detectionMessage: String? = null
                
                if (currentState.selectedImageUri != null) {
                    // Subir imagen a Firebase Storage
                    val uploadResult = repository.uploadImage(currentState.selectedImageUri)
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull()
                        
                        // Analizar imagen con IA
                        _createIncidentState.value = currentState.copy(
                            isAnalyzingImage = true,
                            isSubmitting = true
                        )
                        
                        val analysisResult = repository.analyzeImageForThreats(
                            currentState.selectedImageUri,
                            context
                        )
                        
                        if (analysisResult.isSuccess) {
                            val detection = analysisResult.getOrNull()
                            if (detection != null) {
                                // Determinar prioridad basada en cantidad de amenazas
                                if (detection.cantidad_amenazas > 0) {
                                    prioridad = "ALTA"
                                    val primeraAmenaza = detection.detalles.firstOrNull()
                                    etiqueta_ia = primeraAmenaza?.objeto
                                    detectionMessage = "⚠️ Se detectó una amenaza (${primeraAmenaza?.objeto ?: "objeto desconocido"}) con ${String.format("%.0f", (primeraAmenaza?.confianza ?: 0.0) * 100)}% de confianza. Prioridad: ALTA"
                                } else {
                                    prioridad = "BAJA"
                                    detectionMessage = "✓ No se detectaron amenazas en la imagen. Prioridad: BAJA"
                                }
                            } else {
                                prioridad = "MEDIA"
                                detectionMessage = "⚠️ No se pudo procesar el resultado del análisis"
                            }
                        } else {
                            // Si falla el análisis, usar prioridad media por defecto
                            val errorMsg = analysisResult.exceptionOrNull()?.message ?: "Error desconocido"
                            prioridad = "MEDIA"
                            detectionMessage = "⚠️ No se pudo analizar la imagen: $errorMsg. El incidente se guardará con prioridad media."
                        }
                        
                        _createIncidentState.value = currentState.copy(
                            isAnalyzingImage = false,
                            detectionResult = detectionMessage
                        )
                    } else {
                        val errorMsg = uploadResult.exceptionOrNull()?.message ?: "Error desconocido"
                        _createIncidentState.value = currentState.copy(
                            isSubmitting = false,
                            isAnalyzingImage = false,
                            errorMessage = "Error al subir la imagen: $errorMsg"
                        )
                        return@launch
                    }
                } else {
                    // Si no hay imagen, usar prioridad media por defecto
                    prioridad = "MEDIA"
                }
                
                // Crear el incidente con la información de detección
                val incident = Incident(
                    tipo = currentState.tipo.displayName,
                    descripcion = currentState.descripcion,
                    fotoUrl = imageUrl,
                    ubicacion = currentState.ubicacion,
                    fechaHora = Timestamp.now(),
                    estado = EstadoIncidente.PENDIENTE,
                    prioridad = prioridad,
                    etiqueta_ia = etiqueta_ia
                )
                
                val result = repository.createIncident(incident)
                if (result.isSuccess) {
                    _createIncidentState.value = CreateIncidentUiState(
                        submitSuccess = true,
                        detectionResult = detectionMessage
                    )
                } else {
                    _createIncidentState.value = currentState.copy(
                        isSubmitting = false,
                        isAnalyzingImage = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al crear el incidente"
                    )
                }
            } catch (e: Exception) {
                Log.e("IncidentViewModel", "Error al crear incidente: ${e.message}", e)
                _createIncidentState.value = currentState.copy(
                    isSubmitting = false,
                    isAnalyzingImage = false,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }
    }
    
    fun resetCreateIncidentState() {
        _createIncidentState.value = CreateIncidentUiState()
    }
    
    fun clearCreateIncidentError() {
        _createIncidentState.value = _createIncidentState.value.copy(errorMessage = null)
    }
    
    fun loadCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser().getOrNull()
                val isAdmin = user?.tipo == TipoUsuario.ADMINISTRADOR
                
                _uiState.value = _uiState.value.copy(
                    currentUser = user,
                    isUserAdmin = isAdmin
                )
            } catch (e: Exception) {
                Log.e("IncidentViewModel", "Error al cargar usuario actual: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    currentUser = null,
                    isUserAdmin = false
                )
            }
        }
    }
    
    // Funciones para administradores
    fun updateIncidentStatus(incidentId: String, newStatus: EstadoIncidente) {
        viewModelScope.launch {
            try {
                val result = repository.updateIncidentStatus(incidentId, newStatus)
                if (result.isFailure) {
                    Log.e("IncidentViewModel", "Error al actualizar estado del incidente: ${result.exceptionOrNull()?.message}")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar el estado del incidente"
                    )
                }
            } catch (e: Exception) {
                Log.e("IncidentViewModel", "Error al actualizar estado del incidente: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = e.message ?: "Error desconocido al actualizar el incidente"
                )
            }
        }
    }
    
    fun getIncidentsByStatus(estado: EstadoIncidente) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedStatus = estado
            )
            
            try {
                repository.getIncidentsByStatus(estado).collect { incidents ->
                    _uiState.value = _uiState.value.copy(
                        incidents = incidents,
                        filteredIncidents = incidents,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al filtrar por estado"
                )
            }
        }
    }
    
    fun getPendingIncidents() {
        getIncidentsByStatus(EstadoIncidente.PENDIENTE)
    }
    
    fun getConfirmedIncidents() {
        getIncidentsByStatus(EstadoIncidente.CONFIRMADO)
    }
    
    fun getResolvedIncidents() {
        getIncidentsByStatus(EstadoIncidente.RESUELTO)
    }
    
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? PointsApplication
                val detectionApiService = application?.container?.detectionApiService
                val repository = IncidentRepository(detectionApiService)
                val userRepository = UserRepository(
                    com.google.firebase.firestore.FirebaseFirestore.getInstance(),
                    com.google.firebase.auth.FirebaseAuth.getInstance()
                )
                IncidentViewModel(repository, userRepository)
            }
        }
    }
}
