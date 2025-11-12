package com.example.points.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.points.data.model.IncidentesPorTipo

@Composable
fun DashboardScreen() {
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory)
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    LaunchedEffect(Unit) {
        try {
            viewModel.cargarDashboard()
            viewModel.cargarDatosMensuales()
            viewModel.cargarDatosPorEstado()
        } catch (e: Exception) {
            android.util.Log.e("DashboardScreen", "Error al cargar datos en LaunchedEffect", e)
        }
    }
    
    when {
        // Mostrar indicador de carga inicial
        uiState.isLoading && uiState.datosDashboard.isEmpty() && 
        uiState.datosPorMes.isEmpty() && uiState.datosPorEstado.isEmpty() -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        
        // Mostrar mensaje de error si no hay datos y hay error
        uiState.errorMessage != null && uiState.datosDashboard.isEmpty() && 
        uiState.datosPorMes.isEmpty() && uiState.datosPorEstado.isEmpty() -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Error al cargar los datos",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = uiState.errorMessage ?: "Error desconocido",
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Red
                    )
                }
            }
        }
        
        // Mostrar gráficos
        else -> {
            val datos: List<IncidentesPorTipo> = uiState.datosDashboard
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            ) {
                // Gráficos originales
                if (datos.isNotEmpty()) {
                    BarrasScreen(datos)
                    Spacer(modifier = Modifier.height(24.dp))
                    PieScreen(datos)
                } else if (!uiState.isLoading) {
                    Text(
                        text = "No hay datos de incidentes disponibles",
                        modifier = Modifier.padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Nuevo gráfico mensual
                if (uiState.datosPorMes.isNotEmpty()) {
                    DashboardMensualScreen(uiState.datosPorMes)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                
                // Nuevo gráfico por estado
                if (uiState.datosPorEstado.isNotEmpty()) {
                    DashboardEstadoScreen(uiState.datosPorEstado)
                }
            }
        }
    }
}

