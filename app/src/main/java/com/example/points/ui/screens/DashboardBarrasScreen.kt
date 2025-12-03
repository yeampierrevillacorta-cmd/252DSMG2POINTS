package com.example.points.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.IncidentesPorTipo
import com.example.points.ui.components.ModernBarChart
import com.example.points.ui.components.BarChartData

@Composable
fun BarrasScreen(data: List<IncidentesPorTipo>) {
    Barras(data)
}

@Composable
fun Barras(data: List<IncidentesPorTipo>) {
    if (data.isEmpty()) {
        Text(
            text = "No hay datos para mostrar",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
        return
    }
    
    // Lista de colores profesionales y vibrantes
    val colores = listOf(
        Color(0xFFFF6B6B), // Rojo vibrante
        Color(0xFF4ECDC4), // Turquesa
        Color(0xFFFFBE0B), // Amarillo dorado
        Color(0xFF9B59B6), // Morado
        Color(0xFF4CAF50), // Verde
        Color(0xFFFF8E53), // Naranja coral
        Color(0xFFE91E63), // Rosa
        Color(0xFF00BCD4), // Cian
        Color(0xFF3F51B5), // Azul índigo
        Color(0xFFFF9800), // Naranja
        Color(0xFF009688), // Verde azulado
    )
    
    // Convertir los datos reales a BarChartData
    val chartData = data.mapIndexed { index, incidente ->
        BarChartData(
            label = incidente.descripcion.take(15), // Limitar longitud para mejor visualización
            value = incidente.cantidad.toFloat(),
            color = colores[index % colores.size]
        )
    }
    
    // Usar el componente ModernBarChart mejorado con datos reales
    ModernBarChart(
        title = "📊 Incidentes por Tipo",
        data = chartData,
        showValues = true,
        showGrid = true,
        modifier = Modifier.fillMaxWidth()
    )
}

