package com.example.points.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.points.PointsApplication
import com.example.points.data.model.IncidentesPorTipo
import com.example.points.data.model.DatosPorMes
import com.example.points.data.model.DatosPorEstado
import com.example.points.data.repository.DashboardRepository
import com.example.points.models.Incident
import com.example.points.models.Event
import com.example.points.models.PointOfInterest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

data class DashboardUiState(
    val datosDashboard: List<IncidentesPorTipo> = listOf(),
    val datosPorMes: List<DatosPorMes> = listOf(),
    val datosPorEstado: List<DatosPorEstado> = listOf(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val flag_error_dashboard: Boolean = false,
)

class DashboardViewModel(private val dashboardRepository: DashboardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun cargarDashboard() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
                val result = dashboardRepository.getAllIncidents()
                result.onSuccess { incidents: List<Incident> ->
                    // Agrupar incidentes por tipo
                    val resumen = incidents
                        .filter { incident -> incident.tipo.isNotBlank() } // excluir vacíos
                        .groupBy { incident -> incident.tipo }
                        .map { (tipo, lista) ->
                            IncidentesPorTipo(tipo, lista.size)
                        }
                    // Actualizar el estado de IU
                    _uiState.value = _uiState.value.copy(
                        datosDashboard = resumen,
                        isLoading = false,
                        flag_error_dashboard = false,
                        errorMessage = null
                    )
                }.onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        flag_error_dashboard = true,
                        errorMessage = exception.message ?: "Error al cargar los datos del dashboard"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    flag_error_dashboard = true,
                    errorMessage = "Error inesperado: ${e.message}"
                )
            }
        }
    }
    
    fun cargarDatosMensuales() {
        viewModelScope.launch {
            val incidentsResult = dashboardRepository.getAllIncidents()
            val eventsResult = dashboardRepository.getAllEvents()
            val poisResult = dashboardRepository.getAllPOIs()
            
            if (incidentsResult.isSuccess && eventsResult.isSuccess && poisResult.isSuccess) {
                val incidents: List<Incident> = incidentsResult.getOrNull() ?: emptyList()
                val events: List<Event> = eventsResult.getOrNull() ?: emptyList()
                val pois: List<PointOfInterest> = poisResult.getOrNull() ?: emptyList()
                
                // Agrupar por mes
                val mesesMap = mutableMapOf<String, Triple<Int, Int, Int>>()
                
                // Procesar incidentes
                incidents.forEach { incident: Incident ->
                    try {
                        val fecha: Date = incident.fechaHora.toDate()
                        val mes = obtenerMes(fecha)
                        val (inc, evt, poi) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                        mesesMap[mes] = Triple(inc + 1, evt, poi)
                    } catch (e: Exception) {
                        // Si hay error al convertir la fecha, usar fecha actual
                        val mes = obtenerMes(Date())
                        val (inc, evt, poi) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                        mesesMap[mes] = Triple(inc + 1, evt, poi)
                    }
                }
                
                // Procesar eventos
                events.forEach { event: Event ->
                    try {
                        val fecha: Date = event.fechaCreacion.toDate()
                        val mes = obtenerMes(fecha)
                        val (inc, evt, poi) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                        mesesMap[mes] = Triple(inc, evt + 1, poi)
                    } catch (e: Exception) {
                        // Si hay error al convertir la fecha, usar fecha actual
                        val mes = obtenerMes(Date())
                        val (inc, evt, poi) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                        mesesMap[mes] = Triple(inc, evt + 1, poi)
                    }
                }
                
                // Procesar POIs
                pois.forEach { poi: PointOfInterest ->
                    try {
                        val fecha: Date = poi.fechaCreacion.toDate()
                        val mes = obtenerMes(fecha)
                        val (inc, evt, p) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                        mesesMap[mes] = Triple(inc, evt, p + 1)
                    } catch (e: Exception) {
                        // Si hay error al convertir la fecha, usar fecha actual
                        val mes = obtenerMes(Date())
                        val (inc, evt, p) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                        mesesMap[mes] = Triple(inc, evt, p + 1)
                    }
                }
                
                // Convertir a lista y ordenar por mes (cronológicamente usando la clave original)
                val datosPorMes = mesesMap.map { (mesKey, datos) ->
                    Pair(mesKey, DatosPorMes(formatearMesParaMostrar(mesKey), datos.first, datos.second, datos.third))
                }.sortedBy { it.first } // Ordenar por clave YYYY-MM
                    .map { it.second } // Extraer solo los datos
                
                _uiState.value = _uiState.value.copy(
                    datosPorMes = datosPorMes
                )
            }
        }
    }
    
    fun cargarDatosPorEstado() {
        viewModelScope.launch {
            val incidentsResult = dashboardRepository.getAllIncidents()
            val eventsResult = dashboardRepository.getAllEvents()
            val poisResult = dashboardRepository.getAllPOIs()
            
            if (incidentsResult.isSuccess && eventsResult.isSuccess && poisResult.isSuccess) {
                val incidents: List<Incident> = incidentsResult.getOrNull() ?: emptyList()
                val events: List<Event> = eventsResult.getOrNull() ?: emptyList()
                val pois: List<PointOfInterest> = poisResult.getOrNull() ?: emptyList()
                
                // Procesar incidentes
                val incidentesAtendidos = incidents.count { incident: Incident -> 
                    incident.estado == com.example.points.models.EstadoIncidente.CONFIRMADO || 
                    incident.estado == com.example.points.models.EstadoIncidente.RESUELTO 
                }
                val incidentesDenegados = incidents.count { incident: Incident -> 
                    incident.estado == com.example.points.models.EstadoIncidente.RECHAZADO 
                }
                val incidentesEnRevision = incidents.count { incident: Incident -> 
                    incident.estado == com.example.points.models.EstadoIncidente.EN_REVISION 
                }
                
                // Procesar eventos
                val eventosAtendidos = events.count { event: Event -> 
                    event.estado == com.example.points.models.EstadoEvento.APROBADO || 
                    event.estado == com.example.points.models.EstadoEvento.FINALIZADO 
                }
                val eventosDenegados = events.count { event: Event -> 
                    event.estado == com.example.points.models.EstadoEvento.RECHAZADO || 
                    event.estado == com.example.points.models.EstadoEvento.CANCELADO 
                }
                val eventosEnRevision = events.count { event: Event -> 
                    event.estado == com.example.points.models.EstadoEvento.EN_REVISION 
                }
                
                // Procesar POIs
                val poisAtendidos = pois.count { poi: PointOfInterest -> 
                    poi.estado == com.example.points.models.EstadoPOI.APROBADO 
                }
                val poisDenegados = pois.count { poi: PointOfInterest -> 
                    poi.estado == com.example.points.models.EstadoPOI.RECHAZADO || 
                    poi.estado == com.example.points.models.EstadoPOI.SUSPENDIDO 
                }
                val poisEnRevision = pois.count { poi: PointOfInterest -> 
                    poi.estado == com.example.points.models.EstadoPOI.EN_REVISION 
                }
                
                val datosPorEstado = listOf(
                    DatosPorEstado("Incidentes", incidentesAtendidos, incidentesDenegados, incidentesEnRevision),
                    DatosPorEstado("Eventos", eventosAtendidos, eventosDenegados, eventosEnRevision),
                    DatosPorEstado("POIs", poisAtendidos, poisDenegados, poisEnRevision)
                )
                
                _uiState.value = _uiState.value.copy(
                    datosPorEstado = datosPorEstado
                )
            }
        }
    }
    
    private fun obtenerMes(date: Date): String {
        val calendar = Calendar.getInstance()
        calendar.time = date
        val año = calendar.get(Calendar.YEAR)
        val mes = calendar.get(Calendar.MONTH) + 1 // Mes 1-12
        // Formato: "YYYY-MM" para ordenamiento correcto, pero mostrar como "Mes YYYY"
        return String.format("%04d-%02d", año, mes)
    }
    
    private fun formatearMesParaMostrar(mesKey: String): String {
        val partes = mesKey.split("-")
        if (partes.size == 2) {
            val año = partes[0].toIntOrNull() ?: 0
            val mes = partes[1].toIntOrNull() ?: 0
            val meses = arrayOf(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
            )
            if (mes in 1..12) {
                return "${meses[mes - 1]} $año"
            }
        }
        return mesKey
    }

    fun resetFlags() {
        _uiState.value = _uiState.value.copy(
            flag_error_dashboard = false
        )
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                try {
                    val application = try {
                        this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? PointsApplication
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardViewModel", "Error al obtener Application", e)
                        null
                    }
                    
                    if (application == null) {
                        android.util.Log.e("DashboardViewModel", "Application es null o no es PointsApplication")
                        throw IllegalStateException("Application no es una instancia de PointsApplication")
                    }
                    
                    android.util.Log.d("DashboardViewModel", "Inicializando DashboardViewModel...")
                    
                    // Intentar acceder al container (si no está inicializado, se lanzará una excepción)
                    val dashboardRepository = try {
                        application.container.dashboardRepository
                    } catch (e: UninitializedPropertyAccessException) {
                        android.util.Log.e("DashboardViewModel", "Container no está inicializado", e)
                        throw IllegalStateException("AppContainer no está inicializado. Verifica que PointsApplication.onCreate() se haya ejecutado correctamente.", e)
                    } catch (e: Exception) {
                        android.util.Log.e("DashboardViewModel", "Error al obtener dashboardRepository", e)
                        throw IllegalStateException("Error al obtener dashboardRepository: ${e.message}", e)
                    }
                    
                    android.util.Log.d("DashboardViewModel", "DashboardViewModel inicializado correctamente")
                    DashboardViewModel(dashboardRepository = dashboardRepository)
                } catch (e: Exception) {
                    android.util.Log.e("DashboardViewModel", "Error al inicializar ViewModel", e)
                    android.util.Log.e("DashboardViewModel", "Stack trace:", e)
                    throw e
                }
            }
        }
    }
}

