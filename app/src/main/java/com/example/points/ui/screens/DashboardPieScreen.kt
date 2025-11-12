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
import androidx.compose.ui.unit.dp
import com.example.points.data.model.IncidentesPorTipo
import com.example.points.utils.Utils
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer

@Composable
fun PieScreen(data: List<IncidentesPorTipo>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Gráfico Pie")
        Pie(data)
    }
}

@Composable
fun Pie(data: List<IncidentesPorTipo>) {
    val datos = data
    val slices = ArrayList<PieChartData.Slice>()
    val total = datos.sumOf { it.cantidad.toDouble() }.toFloat()
    
    // Lista de colores predefinidos para asegurar consistencia
    val colores = listOf(
        androidx.compose.ui.graphics.Color(0xFFF44336), // Rojo
        androidx.compose.ui.graphics.Color(0xFF4CAF50), // Verde
        androidx.compose.ui.graphics.Color(0xFFFFEB3B), // Amarillo
        androidx.compose.ui.graphics.Color(0xFF673AB7), // Morado
        androidx.compose.ui.graphics.Color(0xFF9C27B0), // Morado oscuro
        androidx.compose.ui.graphics.Color(0xFF03A9F4), // Azul
        androidx.compose.ui.graphics.Color(0xFFCDDC39), // Verde lima
        androidx.compose.ui.graphics.Color(0xFFE91E63), // Rosa
        androidx.compose.ui.graphics.Color(0xFF00BCD4), // Cian
        androidx.compose.ui.graphics.Color(0xFFFF9800), // Naranja
        androidx.compose.ui.graphics.Color(0xFF009688), // Verde azulado
    )
    
    // Crear un mapa de colores por índice para mantener consistencia
    val coloresAsignados = datos.mapIndexed { index, _ ->
        colores[index % colores.size]
    }

    datos.mapIndexed { index, datos ->
        slices.add(
            PieChartData.Slice(
                value = datos.cantidad.toFloat(),
                color = coloresAsignados[index]
            )
        )
    }
    Column(
        modifier = Modifier
            .padding(2.dp, 80.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PieChart(
            modifier = Modifier
                .padding(30.dp, 80.dp)
                .height(300.dp),
            sliceDrawer = SimpleSliceDrawer(
                sliceThickness = 100f
            ),
            pieChartData = PieChartData(
                slices = slices
            )
        )
        Spacer(modifier = Modifier.height(1.dp))
        // Leyenda del Pie - usar el mismo color que el slice correspondiente
        datos.forEachIndexed { index, it ->
            val porcentaje = (it.cantidad / total * 100).toInt()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .background(
                            coloresAsignados[index],
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${it.descripcion}: ${it.cantidad} (${porcentaje}%)")
            }
        }
    }
}

