package com.example.points.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.DatosPorEstado
import com.example.points.ui.components.ModernCard
import com.example.points.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DashboardEstadoScreen(data: List<DatosPorEstado>) {
    if (data.isEmpty()) return
    
    val total = data.sumOf { it.atendido + it.denegado + it.enRevision }
    
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            modifier = Modifier.padding(20.dp)
        ) {
            // Título mejorado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "✅ Estado de Reportes",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "Total: $total",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            BarrasPorEstado(data)
        }
    }
}

@Composable
fun BarrasPorEstado(data: List<DatosPorEstado>) {
    if (data.isEmpty()) {
        Text(
            text = "No hay datos disponibles",
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        return
    }
    
    // Colores para cada estado
    val colorAtendido = PointsSuccess // Verde
    val colorDenegado = PointsError // Rojo
    val colorEnRevision = FeedbackWarning // Naranja
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        data.forEach { datosEstado ->
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Título de categoría
                Text(
                    text = datosEstado.tipo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Barras horizontales para cada estado
                StatusBar(
                    label = "✅ Atendido",
                    value = datosEstado.atendido,
                    total = datosEstado.atendido + datosEstado.denegado + datosEstado.enRevision,
                    color = colorAtendido
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                StatusBar(
                    label = "❌ Denegado",
                    value = datosEstado.denegado,
                    total = datosEstado.atendido + datosEstado.denegado + datosEstado.enRevision,
                    color = colorDenegado
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                StatusBar(
                    label = "👀 En Revisión",
                    value = datosEstado.enRevision,
                    total = datosEstado.atendido + datosEstado.denegado + datosEstado.enRevision,
                    color = colorEnRevision
                )
            }
        }
    }
}

@Composable
fun StatusBar(
    label: String,
    value: Int,
    total: Int,
    color: Color
) {
    var animationPlayed by remember { mutableStateOf(false) }
    val animatedProgress = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        delay(100)
        animationPlayed = true
        animatedProgress.animateTo(
            targetValue = if (total > 0) value.toFloat() / total.toFloat() else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }
    
    val porcentaje = if (total > 0) ((value.toFloat() / total.toFloat()) * 100).toInt() else 0
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "$value ($porcentaje%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(36.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color.LightGray.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress.value.coerceIn(0f, 1f))
                    .shadow(4.dp, RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(18.dp)
                    )
            )
        }
    }
}
