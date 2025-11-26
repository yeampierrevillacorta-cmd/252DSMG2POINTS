# 🔔 Implementación de Notificaciones Automáticas de Incidentes Cercanos

## 📋 Requisitos

**Como ciudadano quiero recibir notificaciones automáticas sobre incidentes cercanos, para mantenerme informado de riesgos en mi entorno.**

### Funcionalidades Requeridas:

1. **Identificación de incidentes cercanos** (radio configurable: 1km, 3km, 5km)
2. **Notificación con información**: tipo, descripción, ubicación
3. **Configuración de categorías**: todos los incidentes o solo ciertos tipos
4. **Tecnologías**: FCM, Fused Location Provider, Firestore listeners, WorkManager, NotificationCompat

---

## 🎯 Arquitectura Propuesta

### Flujo Completo

```
┌─────────────────────────────────────────────────────────┐
│  1. Usuario inicia sesión                                 │
│     → Se registra FCM token en Firestore                 │
│     → Se guarda ubicación actual del usuario             │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  2. WorkManager ejecuta Worker periódicamente            │
│     → Obtiene ubicación actual (Fused Location Provider) │
│     → Consulta incidentes cercanos en Firestore          │
│     → Filtra por radio y categorías configuradas         │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  3. Si hay incidentes nuevos cercanos                    │
│     → Firebase Cloud Function envía notificación FCM     │
│     → O Worker envía notificación local                  │
└────────────────────┬────────────────────────────────────┘
                     ↓
┌─────────────────────────────────────────────────────────┐
│  4. Usuario recibe notificación                          │
│     → NotificationCompat muestra notificación            │
│     → Al hacer clic, abre detalles del incidente         │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 Lo que DEBES HACER fuera del Proyecto (Firebase Console)

### Paso 1: Habilitar Firebase Cloud Messaging (FCM)

1. **Abrir Firebase Console**
   - Ve a: https://console.firebase.google.com/
   - Selecciona tu proyecto

2. **Habilitar Cloud Messaging**
   - Ve a: `Project Settings` → `Cloud Messaging`
   - Asegúrate de que Cloud Messaging esté habilitado
   - Si no está habilitado, haz clic en `Enable`

3. **Obtener Server Key** (para Firebase Cloud Functions)
   - Ve a: `Project Settings` → `Cloud Messaging`
   - Copia el **Server Key** (lo necesitarás para Cloud Functions)
   - También copia el **Sender ID**

### Paso 2: Configurar Firebase Cloud Functions (Opcional pero Recomendado)

**Opción A: Usar Firebase Cloud Functions (Recomendado)**

1. **Instalar Firebase CLI**
   ```bash
   npm install -g firebase-tools
   ```

2. **Inicializar Firebase Functions**
   ```bash
   firebase login
   firebase init functions
   ```

3. **Configurar función para enviar notificaciones**
   - Se creará una función que escucha nuevos incidentes
   - Envía notificaciones FCM a usuarios cercanos
   - Ver código de ejemplo más adelante

**Opción B: Notificaciones Locales (Más Simple)**

- No requiere Cloud Functions
- WorkManager verifica incidentes y muestra notificaciones locales
- Menos escalable pero más simple de implementar

### Paso 3: Configurar Reglas de Seguridad de Firestore

**Asegúrate de que las reglas permitan:**

1. **Lectura de incidentes confirmados** (públicos)
2. **Escritura de tokens FCM** (solo el usuario autenticado)
3. **Lectura de ubicación del usuario** (solo el usuario autenticado)

**Reglas de Firestore sugeridas:**

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Incidentes: lectura pública de confirmados, escritura autenticada
    match /incidentes/{incidentId} {
      allow read: if resource.data.estado == "Confirmado";
      allow write: if request.auth != null;
    }
    
    // Tokens FCM: solo el usuario puede leer/escribir su propio token
    match /users/{userId}/fcmToken {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Ubicación del usuario: solo el usuario puede leer/escribir su ubicación
    match /users/{userId}/location {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Configuración de notificaciones: solo el usuario puede leer/escribir
    match /users/{userId}/notificationSettings {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### Paso 4: Configurar Índices de Firestore (Opcional)

**Para consultas geográficas eficientes, puedes crear índices:**

1. Ve a: Firebase Console → Firestore → Indexes
2. Crea un índice compuesto:
   - Colección: `incidentes`
   - Campos: `estado` (Ascending), `ubicacion.lat` (Ascending), `ubicacion.lon` (Ascending)

**Nota:** Para consultas geográficas más complejas, considera usar **Geohash** o **Firebase Geofire**.

---

## 🛠️ Lo que YO implementaré en el Proyecto

### Paso 1: Agregar Dependencias

**Archivo:** `app/build.gradle.kts`

```kotlin
dependencies {
    // ... dependencias existentes ...
    
    // Firebase Cloud Messaging
    implementation("com.google.firebase:firebase-messaging")
    
    // WorkManager para tareas en segundo plano
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Fused Location Provider (ya está incluido en play-services-location)
    // Pero necesitamos agregar dependencia para foreground service
    implementation("androidx.core:core-ktx:1.17.0")
}
```

### Paso 2: Agregar Permisos en AndroidManifest

**Archivo:** `app/src/main/AndroidManifest.xml`

```xml
<manifest ...>
    <!-- Permisos existentes -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.INTERNET" />
    
    <!-- Nuevos permisos para notificaciones y ubicación en segundo plano -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />
    
    <application ...>
        <!-- Servicio de mensajería FCM -->
        <service
            android:name=".services.IncidentNotificationService"
            android:exported="false">
            <intent-filter>
                <action android:name="com.google.firebase.MESSAGING_EVENT" />
            </intent-filter>
        </service>
        
        <!-- Servicio de ubicación en segundo plano -->
        <service
            android:name=".services.LocationForegroundService"
            android:foregroundServiceType="location"
            android:exported="false" />
    </application>
</manifest>
```

### Paso 3: Crear Modelo de Configuración de Notificaciones

**Archivo:** `app/src/main/java/com/example/points/models/NotificationSettings.kt`

```kotlin
package com.example.points.models

data class NotificationSettings(
    val enabled: Boolean = true,
    val radiusKm: Float = 3.0f, // Radio por defecto: 3km
    val enabledCategories: List<TipoIncidente> = TipoIncidente.values().toList(), // Todos por defecto
    val lastCheckedTimestamp: Long = 0L // Para evitar notificaciones duplicadas
)

enum class NotificationRadius(val displayName: String, val valueKm: Float) {
    RADIUS_1KM("1 km", 1.0f),
    RADIUS_3KM("3 km", 3.0f),
    RADIUS_5KM("5 km", 5.0f)
}
```

### Paso 4: Crear Servicio de Notificaciones FCM

**Archivo:** `app/src/main/java/com/example/points/services/IncidentNotificationService.kt`

```kotlin
package com.example.points.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.points.MainActivity
import com.example.points.R
import com.example.points.models.Incident
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class IncidentNotificationService : FirebaseMessagingService() {
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Guardar token en Firestore
        saveTokenToFirestore(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Verificar si el mensaje tiene datos
        if (remoteMessage.data.isNotEmpty()) {
            val incidentId = remoteMessage.data["incidentId"]
            val tipo = remoteMessage.data["tipo"]
            val descripcion = remoteMessage.data["descripcion"]
            val ubicacion = remoteMessage.data["ubicacion"]
            
            // Mostrar notificación
            showNotification(
                title = "⚠️ Incidente Cercano: $tipo",
                message = descripcion ?: "Hay un incidente cerca de tu ubicación",
                incidentId = incidentId ?: ""
            )
        }
    }
    
    private fun showNotification(title: String, message: String, incidentId: String) {
        val channelId = "incident_notifications"
        createNotificationChannel(channelId)
        
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("incidentId", incidentId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification) // Necesitarás crear este icono
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(incidentId.hashCode(), notification)
    }
    
    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Incidentes Cercanos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre incidentes cercanos a tu ubicación"
            }
            
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun saveTokenToFirestore(token: String) {
        // Implementar guardado de token en Firestore
        // Ver código completo más adelante
    }
}
```

### Paso 5: Crear Worker para Monitoreo de Incidentes

**Archivo:** `app/src/main/java/com/example/points/workers/IncidentMonitoringWorker.kt`

```kotlin
package com.example.points.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.points.models.Incident
import com.example.points.models.NotificationSettings
import com.example.points.models.TipoIncidente
import com.example.points.models.Ubicacion
import com.example.points.repository.IncidentRepository
import com.example.points.services.LocationService
import com.example.points.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import android.util.Log

class IncidentMonitoringWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    private val incidentRepository = IncidentRepository()
    private val locationService = LocationService(context)
    private val notificationHelper = NotificationHelper(context)
    
    override suspend fun doWork(): Result {
        return try {
            // 1. Obtener ubicación actual del usuario
            val userLocation = getCurrentUserLocation()
            if (userLocation == null) {
                Log.w("IncidentMonitoringWorker", "No se pudo obtener ubicación del usuario")
                return Result.retry() // Reintentar más tarde
            }
            
            // 2. Obtener configuración de notificaciones
            val settings = getNotificationSettings()
            if (!settings.enabled) {
                Log.d("IncidentMonitoringWorker", "Notificaciones deshabilitadas")
                return Result.success() // No hacer nada si están deshabilitadas
            }
            
            // 3. Obtener incidentes confirmados
            val allIncidents = incidentRepository.getConfirmedIncidents().first()
            
            // 4. Filtrar incidentes cercanos
            val nearbyIncidents = filterNearbyIncidents(
                incidents = allIncidents,
                userLocation = userLocation,
                radiusKm = settings.radiusKm,
                enabledCategories = settings.enabledCategories
            )
            
            // 5. Filtrar incidentes nuevos (no notificados antes)
            val newIncidents = filterNewIncidents(
                incidents = nearbyIncidents,
                lastCheckedTimestamp = settings.lastCheckedTimestamp
            )
            
            // 6. Mostrar notificaciones para incidentes nuevos
            newIncidents.forEach { incident ->
                notificationHelper.showIncidentNotification(incident)
            }
            
            // 7. Actualizar timestamp de última verificación
            updateLastCheckedTimestamp(System.currentTimeMillis())
            
            Log.d("IncidentMonitoringWorker", "Verificación completada. ${newIncidents.size} incidentes nuevos encontrados")
            Result.success()
            
        } catch (e: Exception) {
            Log.e("IncidentMonitoringWorker", "Error en worker", e)
            Result.retry() // Reintentar en caso de error
        }
    }
    
    private suspend fun getCurrentUserLocation(): Ubicacion? {
        return try {
            val locationState = locationService.getCurrentLocation()
            if (locationState.latitude != null && locationState.longitude != null) {
                Ubicacion(
                    lat = locationState.latitude!!,
                    lon = locationState.longitude!!,
                    direccion = ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("IncidentMonitoringWorker", "Error obteniendo ubicación", e)
            null
        }
    }
    
    private fun getNotificationSettings(): NotificationSettings {
        // Obtener desde SharedPreferences o Firestore
        // Implementación más adelante
        return NotificationSettings() // Por ahora, valores por defecto
    }
    
    private fun filterNearbyIncidents(
        incidents: List<Incident>,
        userLocation: Ubicacion,
        radiusKm: Float,
        enabledCategories: List<TipoIncidente>
    ): List<Incident> {
        return incidents.filter { incident ->
            // 1. Verificar que el tipo esté en las categorías habilitadas
            val incidentType = TipoIncidente.values().find { 
                it.displayName == incident.tipo 
            }
            val isCategoryEnabled = incidentType in enabledCategories
            
            // 2. Verificar que esté dentro del radio
            val distance = calculateDistance(
                userLocation.lat,
                userLocation.lon,
                incident.ubicacion.lat,
                incident.ubicacion.lon
            )
            val isWithinRadius = distance <= radiusKm
            
            isCategoryEnabled && isWithinRadius
        }
    }
    
    private fun filterNewIncidents(
        incidents: List<Incident>,
        lastCheckedTimestamp: Long
    ): List<Incident> {
        return incidents.filter { incident ->
            // Solo incluir incidentes creados después de la última verificación
            incident.fechaHora.toDate().time > lastCheckedTimestamp
        }
    }
    
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
        // Fórmula de Haversine para calcular distancia entre dos puntos
        val earthRadius = 6371.0 // Radio de la Tierra en km
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return (earthRadius * c).toFloat()
    }
    
    private fun updateLastCheckedTimestamp(timestamp: Long) {
        // Guardar en SharedPreferences o Firestore
        // Implementación más adelante
    }
}
```

### Paso 6: Crear Helper para Notificaciones

**Archivo:** `app/src/main/java/com/example/points/utils/NotificationHelper.kt`

```kotlin
package com.example.points.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.points.MainActivity
import com.example.points.R
import com.example.points.models.Incident

class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "incident_notifications"
    
    init {
        createNotificationChannel()
    }
    
    fun showIncidentNotification(incident: Incident) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("incidentId", incident.id)
            putExtra("navigateTo", "incident_detail")
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            incident.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("⚠️ Incidente Cercano: ${incident.tipo}")
            .setContentText(incident.descripcion)
            .setStyle(NotificationCompat.BigTextStyle().bigText(incident.descripcion))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(incident.id.hashCode(), notification)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Incidentes Cercanos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones sobre incidentes cercanos a tu ubicación"
                enableVibration(true)
                enableLights(true)
            }
            
            notificationManager.createNotificationChannel(channel)
        }
    }
}
```

### Paso 7: Crear Repositorio para Configuración de Notificaciones

**Archivo:** `app/src/main/java/com/example/points/repository/NotificationSettingsRepository.kt`

```kotlin
package com.example.points.repository

import com.example.points.models.NotificationSettings
import com.example.points.models.TipoIncidente
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationSettingsRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    private fun getSettingsPath(): String {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")
        return "users/$userId/notificationSettings"
    }
    
    suspend fun getSettings(): NotificationSettings {
        return try {
            val doc = firestore.document(getSettingsPath()).get().await()
            if (doc.exists()) {
                val data = doc.data!!
                NotificationSettings(
                    enabled = data["enabled"] as? Boolean ?: true,
                    radiusKm = (data["radiusKm"] as? Number)?.toFloat() ?: 3.0f,
                    enabledCategories = (data["enabledCategories"] as? List<*>)
                        ?.mapNotNull { TipoIncidente.values().find { it.displayName == it } }
                        ?: TipoIncidente.values().toList(),
                    lastCheckedTimestamp = (data["lastCheckedTimestamp"] as? Number)?.toLong() ?: 0L
                )
            } else {
                NotificationSettings() // Valores por defecto
            }
        } catch (e: Exception) {
            NotificationSettings() // Valores por defecto en caso de error
        }
    }
    
    suspend fun saveSettings(settings: NotificationSettings) {
        val data = mapOf(
            "enabled" to settings.enabled,
            "radiusKm" to settings.radiusKm,
            "enabledCategories" to settings.enabledCategories.map { it.displayName },
            "lastCheckedTimestamp" to settings.lastCheckedTimestamp
        )
        firestore.document(getSettingsPath()).set(data).await()
    }
    
    suspend fun saveFCMToken(token: String) {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")
        firestore.document("users/$userId/fcmToken").set(mapOf("token" to token)).await()
    }
    
    suspend fun saveUserLocation(lat: Double, lon: Double) {
        val userId = auth.currentUser?.uid
            ?: throw IllegalStateException("Usuario no autenticado")
        firestore.document("users/$userId/location").set(
            mapOf(
                "lat" to lat,
                "lon" to lon,
                "timestamp" to System.currentTimeMillis()
            )
        ).await()
    }
}
```

### Paso 8: Programar Worker con WorkManager

**Archivo:** `app/src/main/java/com/example/points/utils/WorkManagerHelper.kt`

```kotlin
package com.example.points.utils

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.points.workers.IncidentMonitoringWorker
import java.util.concurrent.TimeUnit

object WorkManagerHelper {
    private const val WORK_NAME = "incident_monitoring_work"
    
    fun startIncidentMonitoring(context: Context) {
        val workRequest = PeriodicWorkRequestBuilder<IncidentMonitoringWorker>(
            15, TimeUnit.MINUTES // Ejecutar cada 15 minutos
        )
            .setConstraints(
                androidx.work.Constraints.Builder()
                    .setRequiresCharging(false)
                    .setRequiredNetworkType(androidx.work.NetworkType.CONNECTED)
                    .build()
            )
            .build()
        
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Mantener trabajo existente
            workRequest
        )
    }
    
    fun stopIncidentMonitoring(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}
```

### Paso 9: Actualizar PreferencesManager

**Archivo:** `app/src/main/java/com/example/points/data/PreferencesManager.kt`

```kotlin
// Agregar nuevas propiedades para configuración de notificaciones
companion object {
    // ... propiedades existentes ...
    private const val KEY_NOTIFICATION_RADIUS = "notification_radius"
    private const val KEY_NOTIFICATION_CATEGORIES = "notification_categories"
    private const val KEY_LAST_INCIDENT_CHECK = "last_incident_check"
}

var notificationRadius: Float
    get() = prefs.getFloat(KEY_NOTIFICATION_RADIUS, 3.0f)
    set(value) = prefs.edit().putFloat(KEY_NOTIFICATION_RADIUS, value).apply()

var notificationCategories: Set<String>
    get() = prefs.getStringSet(KEY_NOTIFICATION_CATEGORIES, null) 
        ?: TipoIncidente.values().map { it.displayName }.toSet()
    set(value) = prefs.edit().putStringSet(KEY_NOTIFICATION_CATEGORIES, value).apply()

var lastIncidentCheck: Long
    get() = prefs.getLong(KEY_LAST_INCIDENT_CHECK, 0L)
    set(value) = prefs.edit().putLong(KEY_LAST_INCIDENT_CHECK, value).apply()
```

### Paso 10: Crear Pantalla de Configuración de Notificaciones

**Archivo:** `app/src/main/java/com/example/points/screens/NotificationSettingsScreen.kt`

```kotlin
package com.example.points.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.points.models.NotificationRadius
import com.example.points.models.TipoIncidente

@Composable
fun NotificationSettingsScreen(
    viewModel: NotificationSettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Configuración de Notificaciones",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Toggle para habilitar/deshabilitar notificaciones
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Recibir notificaciones de incidentes cercanos")
            Switch(
                checked = uiState.enabled,
                onCheckedChange = { viewModel.setNotificationsEnabled(it) }
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selección de radio
        Text("Radio de notificación:")
        NotificationRadius.values().forEach { radius ->
            RadioButton(
                selected = uiState.radiusKm == radius.valueKm,
                onClick = { viewModel.setNotificationRadius(radius.valueKm) }
            )
            Text(radius.displayName)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Selección de categorías
        Text("Tipos de incidentes:")
        TipoIncidente.values().forEach { tipo ->
            Checkbox(
                checked = uiState.enabledCategories.contains(tipo),
                onCheckedChange = { checked ->
                    viewModel.toggleCategory(tipo, checked)
                }
            )
            Text(tipo.displayName)
        }
    }
}
```

---

## 📊 Estructura de Datos en Firestore

### Colección: `users/{userId}/notificationSettings`

```json
{
  "enabled": true,
  "radiusKm": 3.0,
  "enabledCategories": ["Inseguridad", "Accidente de Tránsito"],
  "lastCheckedTimestamp": 1234567890
}
```

### Colección: `users/{userId}/fcmToken`

```json
{
  "token": "fcm_token_aqui",
  "updatedAt": "2024-01-01T00:00:00Z"
}
```

### Colección: `users/{userId}/location`

```json
{
  "lat": 40.7128,
  "lon": -74.0060,
  "timestamp": 1234567890
}
```

---

## 🚀 Pasos de Implementación Resumidos

### Fase 1: Configuración de Firebase (TÚ)

1. ✅ Habilitar Firebase Cloud Messaging en Firebase Console
2. ✅ Configurar reglas de seguridad de Firestore
3. ✅ (Opcional) Configurar Firebase Cloud Functions

### Fase 2: Implementación en el Proyecto (YO)

1. ✅ Agregar dependencias (FCM, WorkManager)
2. ✅ Agregar permisos en AndroidManifest
3. ✅ Crear servicio de notificaciones FCM
4. ✅ Crear Worker para monitoreo
5. ✅ Crear helper para notificaciones
6. ✅ Crear repositorio para configuración
7. ✅ Crear pantalla de configuración
8. ✅ Integrar con PointsApplication

---

## 📝 Código de Firebase Cloud Function (Opcional)

**Si decides usar Cloud Functions, aquí está el código:**

```javascript
// functions/index.js
const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp();

exports.sendIncidentNotifications = functions.firestore
  .document('incidentes/{incidentId}')
  .onCreate(async (snap, context) => {
    const incident = snap.data();
    
    // Solo enviar notificaciones para incidentes confirmados
    if (incident.estado !== 'Confirmado') {
      return null;
    }
    
    // Obtener todos los usuarios con notificaciones habilitadas
    const usersSnapshot = await admin.firestore()
      .collectionGroup('notificationSettings')
      .where('enabled', '==', true)
      .get();
    
    // Filtrar usuarios cercanos al incidente
    const nearbyUsers = [];
    for (const userDoc of usersSnapshot.docs) {
      const userId = userDoc.ref.parent.parent.id;
      const userLocation = await admin.firestore()
        .doc(`users/${userId}/location`)
        .get();
      
      if (userLocation.exists) {
        const location = userLocation.data();
        const distance = calculateDistance(
          location.lat,
          location.lon,
          incident.ubicacion.lat,
          incident.ubicacion.lon
        );
        
        const settings = userDoc.data();
        if (distance <= settings.radiusKm) {
          // Verificar que el tipo de incidente esté habilitado
          if (settings.enabledCategories.includes(incident.tipo)) {
            nearbyUsers.push({ userId, fcmToken: null });
          }
        }
      }
    }
    
    // Obtener tokens FCM de usuarios cercanos
    for (const user of nearbyUsers) {
      const tokenDoc = await admin.firestore()
        .doc(`users/${user.userId}/fcmToken`)
        .get();
      if (tokenDoc.exists) {
        user.fcmToken = tokenDoc.data().token;
      }
    }
    
    // Enviar notificaciones
    const messages = nearbyUsers
      .filter(user => user.fcmToken)
      .map(user => ({
        notification: {
          title: `⚠️ Incidente Cercano: ${incident.tipo}`,
          body: incident.descripcion
        },
        data: {
          incidentId: context.params.incidentId,
          tipo: incident.tipo,
          descripcion: incident.descripcion,
          ubicacion: `${incident.ubicacion.lat},${incident.ubicacion.lon}`
        },
        token: user.fcmToken
      }));
    
    if (messages.length > 0) {
      await admin.messaging().sendAll(messages);
    }
    
    return null;
  });

function calculateDistance(lat1, lon1, lat2, lon2) {
  // Fórmula de Haversine
  const R = 6371; // Radio de la Tierra en km
  const dLat = (lat2 - lat1) * Math.PI / 180;
  const dLon = (lon2 - lon1) * Math.PI / 180;
  const a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
    Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
    Math.sin(dLon / 2) * Math.sin(dLon / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
  return R * c;
}
```

---

## 🎯 Resumen: Qué Hacer TÚ vs Qué Hacer YO

### ✅ Lo que TÚ debes hacer (Fuera del Proyecto):

1. **Firebase Console:**
   - Habilitar Firebase Cloud Messaging
   - Configurar reglas de seguridad de Firestore
   - (Opcional) Configurar Firebase Cloud Functions

2. **Firebase Cloud Functions (Opcional):**
   - Instalar Firebase CLI
   - Crear función para enviar notificaciones
   - Desplegar función en Firebase

### ✅ Lo que YO implementaré (En el Proyecto):

1. **Código Android:**
   - Agregar dependencias
   - Crear servicios y workers
   - Crear pantallas de configuración
   - Integrar con la app existente

---

*Documento creado para explicar la implementación completa de notificaciones de incidentes cercanos.*

