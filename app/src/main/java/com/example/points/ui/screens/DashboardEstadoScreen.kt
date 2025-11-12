package com.example.points.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.DatosPorEstado
import com.github.tehras.charts.bar.BarChart
import com.github.tehras.charts.bar.BarChartData
import com.github.tehras.charts.bar.renderer.label.SimpleValueDrawer

@Composable
fun DashboardEstadoScreen(data: List<DatosPorEstado>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "Distribución por Estado",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        BarrasPorEstado(data)
        Spacer(modifier = Modifier.height(16.dp))
        LeyendaEstados()
    }
}

@Composable
fun BarrasPorEstado(data: List<DatosPorEstado>) {
    val barras = ArrayList<BarChartData.Bar>()
    
    // Colores para cada estado
    val colorAtendido = androidx.compose.ui.graphics.Color(0xFF4CAF50) // Verde
    val colorDenegado = androidx.compose.ui.graphics.Color(0xFFF44336) // Rojo
    val colorEnRevision = androidx.compose.ui.graphics.Color(0xFFFF9800) // Naranja
    
    // Crear barras agrupadas por tipo
    data.forEach { datosEstado ->
        // Barra de atendidos
        barras.add(
            BarChartData.Bar(
                label = "${datosEstado.tipo}\nAtendido",
                value = datosEstado.atendido.toFloat(),
                color = colorAtendido
            )
        )
        // Barra de denegados
        barras.add(
            BarChartData.Bar(
                label = "${datosEstado.tipo}\nDenegado",
                value = datosEstado.denegado.toFloat(),
                color = colorDenegado
            )
        )
        // Barra de en revisión
        barras.add(
            BarChartData.Bar(
                label = "${datosEstado.tipo}\nEn Revisión",
                value = datosEstado.enRevision.toFloat(),
                color = colorEnRevision
            )
        )
    }
    
    if (barras.isNotEmpty()) {
        BarChart(
            modifier = Modifier
                .padding(30.dp, 20.dp)
                .height(400.dp),
            labelDrawer = SimpleValueDrawer(
                drawLocation = SimpleValueDrawer.DrawLocation.XAxis
            ),
            barChartData = BarChartData(
                bars = barras
            )
        )
    } else {
        Text(
            text = "No hay datos disponibles",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun LeyendaEstados() {
    val colorAtendido = androidx.compose.ui.graphics.Color(0xFF4CAF50)
    val colorDenegado = androidx.compose.ui.graphics.Color(0xFFF44336)
    val colorEnRevision = androidx.compose.ui.graphics.Color(0xFFFF9800)
    
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            text = "Leyenda:",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        ItemLeyenda("Atendido", colorAtendido)
        ItemLeyenda("Denegado", colorDenegado)
        ItemLeyenda("En Revisión", colorEnRevision)
    }
}

@Composable
fun ItemLeyenda(texto: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(texto)
    }
}

