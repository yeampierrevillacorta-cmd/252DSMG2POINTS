package com.example.points.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedIncident: Incident? = null,
    val filterType: TipoIncidente? = null,
    val filterStatus: EstadoIncidente? = null,
    val currentUser: User? = null,
    val isUserAdmin: Boolean = false
)

data class CreateIncidentUiState(
    val tipo: TipoIncidente = TipoIncidente.INSEGURIDAD,
    val descripcion: String = "",
    val ubicacion: Ubicacion = Ubicacion(),
    val selectedImageUri: Uri? = null,
    val isSubmitting: Boolean = false,
    val submitSuccess: Boolean = false,
    val errorMessage: String? = null
)

class IncidentViewModel(
    val repository: IncidentRepository = IncidentRepository(),
    private val userRepository: UserRepository = UserRepository(
        com.google.firebase.firestore.FirebaseFirestore.getInstance(),
        com.google.firebase.auth.FirebaseAuth.getInstance()
    )
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(IncidentUiState())
    val uiState: StateFlow<IncidentUiState> = _uiState.asStateFlow()
    
    private val _createIncidentState = MutableStateFlow(CreateIncidentUiState())
    val createIncidentState: StateFlow<CreateIncidentUiState> = _createIncidentState.asStateFlow()
    
    init {
        loadAllIncidents()
        loadCurrentUser()
    }
    
    fun loadAllIncidents() {
        viewModelScope.launch {
            Log.d("IncidentViewModel", "Iniciando carga de incidentes...")
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                repository.getAllIncidents().collect { incidents ->
                    Log.d("IncidentViewModel", "Incidentes recibidos: ${incidents.size}")
                    incidents.forEach { incident ->
                        Log.d("IncidentViewModel", "Incidente: ${incident.id} - ${incident.tipo} - Lat: ${incident.ubicacion.lat}, Lon: ${incident.ubicacion.lon}")
                    }
                    _uiState.value = _uiState.value.copy(
                        incidents = incidents,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                Log.e("IncidentViewModel", "Error cargando incidentes", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error desconocido"
                )
            }
        }
    }
    
    fun filterByType(tipo: TipoIncidente?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                filterType = tipo
            )
            
            try {
                if (tipo == null) {
                    loadAllIncidents()
                } else {
                    repository.getIncidentsByType(tipo).collect { incidents ->
                        _uiState.value = _uiState.value.copy(
                            incidents = incidents,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al filtrar"
                )
            }
        }
    }
    
    fun filterByStatus(estado: EstadoIncidente?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                filterStatus = estado
            )
            
            try {
                if (estado == null) {
                    loadAllIncidents()
                } else {
                    repository.getIncidentsByStatus(estado).collect { incidents ->
                        _uiState.value = _uiState.value.copy(
                            incidents = incidents,
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error al filtrar"
                )
            }
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
    
    fun createIncident() {
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
                errorMessage = null
            )
            
            try {
                // Subir imagen si existe
                var imageUrl: String? = null
                if (currentState.selectedImageUri != null) {
                    val uploadResult = repository.uploadImage(currentState.selectedImageUri)
                    if (uploadResult.isSuccess) {
                        imageUrl = uploadResult.getOrNull()
                    } else {
                        _createIncidentState.value = currentState.copy(
                            isSubmitting = false,
                            errorMessage = "Error al subir la imagen"
                        )
                        return@launch
                    }
                }
                
                // Crear el incidente
                val incident = Incident(
                    tipo = currentState.tipo.displayName,
                    descripcion = currentState.descripcion,
                    fotoUrl = imageUrl,
                    ubicacion = currentState.ubicacion,
                    fechaHora = Timestamp.now(),
                    estado = EstadoIncidente.PENDIENTE
                )
                
                val result = repository.createIncident(incident)
                if (result.isSuccess) {
                    _createIncidentState.value = CreateIncidentUiState(
                        submitSuccess = true
                    )
                } else {
                    _createIncidentState.value = currentState.copy(
                        isSubmitting = false,
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al crear el incidente"
                    )
                }
            } catch (e: Exception) {
                _createIncidentState.value = currentState.copy(
                    isSubmitting = false,
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
                
                Log.d("IncidentViewModel", "Usuario cargado: ${user?.nombre}, Tipo: ${user?.tipo}, Es admin: $isAdmin")
            } catch (e: Exception) {
                Log.e("IncidentViewModel", "Error al cargar usuario actual", e)
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
                Log.d("IncidentViewModel", "Actualizando estado del incidente $incidentId a ${newStatus.displayName}")
                
                val result = repository.updateIncidentStatus(incidentId, newStatus)
                if (result.isSuccess) {
                    Log.d("IncidentViewModel", "Estado del incidente actualizado exitosamente")
                    // El estado se actualizará automáticamente a través del listener de Firebase
                } else {
                    Log.e("IncidentViewModel", "Error al actualizar estado del incidente", result.exceptionOrNull())
                    _uiState.value = _uiState.value.copy(
                        errorMessage = result.exceptionOrNull()?.message ?: "Error al actualizar el estado del incidente"
                    )
                }
            } catch (e: Exception) {
                Log.e("IncidentViewModel", "Error al actualizar estado del incidente", e)
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
                filterStatus = estado
            )
            
            try {
                repository.getIncidentsByStatus(estado).collect { incidents ->
                    _uiState.value = _uiState.value.copy(
                        incidents = incidents,
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
}
