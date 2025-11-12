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
import com.example.points.models.EstadoIncidente
import com.example.points.models.EstadoEvento
import com.example.points.models.EstadoPOI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val datosDashboard: List<IncidentesPorTipo> = listOf(),
    val datosPorMes: List<DatosPorMes> = listOf(),
    val datosPorEstado: List<DatosPorEstado> = listOf(),
    val flag_error_dashboard: Boolean = false,
)

class DashboardViewModel(private val dashboardRepository: DashboardRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun cargarDashboard() {
        viewModelScope.launch {
            val result = dashboardRepository.getAllIncidents()
            result.onSuccess { incidents ->
                // Agrupar incidentes por tipo
                val resumen = incidents
                    .filter { !it.tipo.isNullOrBlank() } // excluir nulos o vacíos
                    .groupBy { it.tipo }
                    .map { (tipo, lista) ->
                        IncidentesPorTipo(tipo, lista.size)
                    }
                // Actualizar el estado de IU
                _uiState.value = _uiState.value.copy(
                    datosDashboard = resumen,
                    flag_error_dashboard = false
                )
            }.onFailure {
                _uiState.value = _uiState.value.copy(
                    flag_error_dashboard = true
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
                val incidents = incidentsResult.getOrNull() ?: emptyList()
                val events = eventsResult.getOrNull() ?: emptyList()
                val pois = poisResult.getOrNull() ?: emptyList()
                
                // Agrupar por mes
                val mesesMap = mutableMapOf<String, Triple<Int, Int, Int>>()
                
                // Procesar incidentes
                incidents.forEach { incident ->
                    val mes = obtenerMes(incident.fechaHora.toDate())
                    val (inc, evt, poi) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                    mesesMap[mes] = Triple(inc + 1, evt, poi)
                }
                
                // Procesar eventos
                events.forEach { event ->
                    val mes = obtenerMes(event.fechaCreacion.toDate())
                    val (inc, evt, poi) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                    mesesMap[mes] = Triple(inc, evt + 1, poi)
                }
                
                // Procesar POIs
                pois.forEach { poi ->
                    val mes = obtenerMes(poi.fechaCreacion.toDate())
                    val (inc, evt, p) = mesesMap.getOrDefault(mes, Triple(0, 0, 0))
                    mesesMap[mes] = Triple(inc, evt, p + 1)
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
                val incidents = incidentsResult.getOrNull() ?: emptyList()
                val events = eventsResult.getOrNull() ?: emptyList()
                val pois = poisResult.getOrNull() ?: emptyList()
                
                // Procesar incidentes
                val incidentesAtendidos = incidents.count { 
                    it.estado == EstadoIncidente.CONFIRMADO || it.estado == EstadoIncidente.RESUELTO 
                }
                val incidentesDenegados = incidents.count { 
                    it.estado == EstadoIncidente.RECHAZADO 
                }
                val incidentesEnRevision = incidents.count { 
                    it.estado == EstadoIncidente.EN_REVISION 
                }
                
                // Procesar eventos
                val eventosAtendidos = events.count { 
                    it.estado == EstadoEvento.APROBADO || it.estado == EstadoEvento.FINALIZADO 
                }
                val eventosDenegados = events.count { 
                    it.estado == EstadoEvento.RECHAZADO || it.estado == EstadoEvento.CANCELADO 
                }
                val eventosEnRevision = events.count { 
                    it.estado == EstadoEvento.EN_REVISION 
                }
                
                // Procesar POIs
                val poisAtendidos = pois.count { 
                    it.estado == EstadoPOI.APROBADO 
                }
                val poisDenegados = pois.count { 
                    it.estado == EstadoPOI.RECHAZADO || it.estado == EstadoPOI.SUSPENDIDO 
                }
                val poisEnRevision = pois.count { 
                    it.estado == EstadoPOI.EN_REVISION 
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
    
    private fun obtenerMes(date: java.util.Date): String {
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
                val application = (
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                        as PointsApplication
                )
                val dashboardRepository = application.container.dashboardRepository
                DashboardViewModel(dashboardRepository = dashboardRepository)
            }
        }
    }
}

