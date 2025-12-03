package com.example.points.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.points.data.model.DatosPorMes
import com.example.points.ui.components.ModernCard
import com.example.points.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun DashboardMensualScreen(data: List<DatosPorMes>) {
    ModernCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 0.dp, vertical = 8.dp),
        elevation = 4.dp
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(20.dp)
        ) {
            // Título mejorado
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📅 Distribución Mensual",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = "${data.size} meses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(0.6f)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            BarrasMensuales(data)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            LeyendaMensual()
        }
    }
}

@Composable
fun BarrasMensuales(data: List<DatosPorMes>) {
    if (data.isEmpty()) {
        Text(
            text = "No hay datos disponibles",
            modifier = Modifier.padding(16.dp),
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        return
    }
    
    // Colores para cada tipo de dato
    val colorIncidentes = Color(0xFFFF6B6B) // Rojo vibrante
    val colorEventos = Color(0xFF4ECDC4) // Turquesa
    val colorPOIs = Color(0xFF6BCF7F) // Verde
    
    val maxValue = data.maxOfOrNull { 
        maxOf(it.incidentes, it.eventos, it.pois).toFloat()
    } ?: 1f
    
    // Scroll horizontal para meses
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        data.forEach { datosMes ->
            MonthBarGroup(
                mes = datosMes.mes,
                incidentes = datosMes.incidentes,
                eventos = datosMes.eventos,
                pois = datosMes.pois,
                maxValue = maxValue,
                colorIncidentes = colorIncidentes,
                colorEventos = colorEventos,
                colorPOIs = colorPOIs
            )
        }
    }
}

@Composable
fun MonthBarGroup(
    mes: String,
    incidentes: Int,
    eventos: Int,
    pois: Int,
    maxValue: Float,
    colorIncidentes: Color,
    colorEventos: Color,
    colorPOIs: Color
) {
    var animationPlayed by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        animationPlayed = true
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.width(100.dp)
    ) {
        // Mes
        Text(
            text = mes.split(" ").firstOrNull() ?: mes,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        // Barras verticales agrupadas
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            // Barra de incidentes
            AnimatedBar(
                value = incidentes.toFloat(),
                maxValue = maxValue,
                color = colorIncidentes,
                animationPlayed = animationPlayed,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Barra de eventos
            AnimatedBar(
                value = eventos.toFloat(),
                maxValue = maxValue,
                color = colorEventos,
                animationPlayed = animationPlayed,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            // Barra de POIs
            AnimatedBar(
                value = pois.toFloat(),
                maxValue = maxValue,
                color = colorPOIs,
                animationPlayed = animationPlayed,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Valores
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = "$incidentes",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorIncidentes,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "$eventos",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorEventos,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                text = "$pois",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = colorPOIs,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AnimatedBar(
    value: Float,
    maxValue: Float,
    color: Color,
    animationPlayed: Boolean,
    modifier: Modifier = Modifier
) {
    val animatedHeight = remember { Animatable(0f) }
    
    LaunchedEffect(animationPlayed) {
        if (animationPlayed) {
            animatedHeight.animateTo(
                targetValue = value / maxValue,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
    }
    
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .fillMaxHeight(animatedHeight.value.coerceIn(0f, 1f))
                .shadow(4.dp, RoundedCornerShape(6.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color,
                            color.copy(alpha = 0.7f)
                        )
                    ),
                    shape = RoundedCornerShape(6.dp)
                )
        )
    }
}

@Composable
fun LeyendaMensual() {
    val colorIncidentes = Color(0xFFFF6B6B)
    val colorEventos = Color(0xFF4ECDC4)
    val colorPOIs = Color(0xFF6BCF7F)
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemLeyendaMensual("🚨 Incidentes", colorIncidentes)
        ItemLeyendaMensual("🎉 Eventos", colorEventos)
        ItemLeyendaMensual("📍 POIs", colorPOIs)
    }
}

@Composable
fun ItemLeyendaMensual(texto: String, color: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .shadow(4.dp, CircleShape)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = texto,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

