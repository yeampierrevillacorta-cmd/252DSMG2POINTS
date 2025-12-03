package com.example.points.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.IncidentesPorTipo
import com.example.points.ui.components.ModernCard
import com.github.tehras.charts.piechart.PieChart
import com.github.tehras.charts.piechart.PieChartData
import com.github.tehras.charts.piechart.renderer.SimpleSliceDrawer

@Composable
fun PieScreen(data: List<IncidentesPorTipo>) {
    Pie(data)
}

@Composable
fun Pie(data: List<IncidentesPorTipo>) {
    if (data.isEmpty()) {
        Text(
            text = "No hay datos para mostrar",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
        return
    }
    
    val datos = data
    val slices = ArrayList<PieChartData.Slice>()
    val total = datos.sumOf { it.cantidad.toDouble() }.toFloat()
    
    if (total <= 0) {
        Text(
            text = "No hay datos válidos para mostrar",
            modifier = Modifier.padding(16.dp),
            textAlign = TextAlign.Center
        )
        return
    }
    
    // Lista de colores profesionales (mismos que el gráfico de barras)
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
    
    // Crear un mapa de colores por índice para mantener consistencia
    val coloresAsignados = datos.mapIndexed { index, _ ->
        colores[index % colores.size]
    }

    datos.forEachIndexed { index, datosItem ->
        slices.add(
            PieChartData.Slice(
                value = datosItem.cantidad.toFloat(),
                color = coloresAsignados[index]
            )
        )
    }
    
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp),
        elevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📈 Distribución de Incidentes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Total: ${total.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Gráfico de pie mejorado
            PieChart(
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 20.dp)
                    .height(280.dp)
                    .shadow(8.dp, CircleShape),
                sliceDrawer = SimpleSliceDrawer(
                    sliceThickness = 85f
                ),
                pieChartData = PieChartData(
                    slices = slices
                )
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Leyenda mejorada con cards individuales
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                datos.forEachIndexed { index, it ->
                    val porcentaje = if (total > 0) {
                        (it.cantidad / total * 100).toInt()
                    } else {
                        0
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                coloresAsignados[index].copy(alpha = 0.1f),
                                androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .shadow(4.dp, CircleShape)
                                    .background(
                                        coloresAsignados[index],
                                        shape = CircleShape
                                    )
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = it.descripcion,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "${it.cantidad}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = coloresAsignados[index]
                            )
                            Text(
                                text = "($porcentaje%)",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                            )
                        }
                    }
                }
            }
        }
    }
}

