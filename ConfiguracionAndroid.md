# 📱 Configuración de Android para MySyncApp Backend

Este documento contiene la documentación completa de la implementación de sincronización con el backend Spring Boot desplegado en Google Cloud Run.

---

## 📋 Tabla de Contenidos

1. [Dependencias Gradle](#1-dependencias-gradle)
2. [Estructura del Proyecto](#2-estructura-del-proyecto)
3. [Configuración de Red](#3-configuración-de-red)
4. [Modelos de Datos (DTOs)](#4-modelos-de-datos-dtos)
5. [Configuración de Retrofit](#5-configuración-de-retrofit)
6. [Servicio de API](#6-servicio-de-api)
7. [Autenticación JWT](#7-autenticación-jwt)
8. [Repository de Sincronización](#8-repository-de-sincronización)
9. [Sincronización Automática con WorkManager](#9-sincronización-automática-con-workmanager)
10. [UI de Configuración](#10-ui-de-configuración)
11. [Ejemplos de Uso](#11-ejemplos-de-uso)
12. [Configuración de Variables](#12-configuración-de-variables)

---

## 1. Dependencias Gradle

### 1.1 Dependencias en `app/build.gradle.kts`

```kotlin
dependencies {
    // Retrofit para llamadas HTTP
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    
    // OkHttp para interceptores y logging
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Kotlinx Serialization (NO Gson)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    
    // WorkManager para sincronización automática
    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
    // Firebase Auth (para JWT)
    implementation("com.google.firebase:firebase-auth")
    
    // Coroutines (ya incluido en el proyecto)
    // implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

### 1.2 Plugin de Serialization

En `app/build.gradle.kts`:

```kotlin
plugins {
    // ... otros plugins
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
}
```

---

## 2. Estructura del Proyecto

### 2.1 Estructura Real Implementada

```
app/src/main/java/com/example/points/
├── data/
│   ├── DefaultAppContainer.kt      # Contenedor de dependencias con Retrofit configurado
│   └── AppContainer.kt              # Interfaz del contenedor
├── network/
│   └── SyncApiService.kt            # Interface Retrofit + DTOs
├── repository/
│   ├── SyncRepository.kt            # Interface del repositorio
│   └── DefaultSyncRepository.kt    # Implementación del repositorio
├── worker/
│   ├── SyncWorker.kt                # Worker para WorkManager
│   └── SyncWorkManager.kt           # Gestor de sincronización automática
├── viewmodel/
│   └── SyncSettingsViewModel.kt    # ViewModel para UI de configuración
├── screens/
│   └── SyncSettingsScreen.kt       # Pantalla de configuración de sincronización
├── utils/
│   └── EnvironmentConfig.kt         # Configuración de URLs y variables
└── PointsApplication.kt             # Application class con inicialización
```

---

## 3. Configuración de Red

### 3.1 Permisos en `AndroidManifest.xml`

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    
    <!-- Permiso de Internet -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    
    <application
        ...>
        <!-- ... resto de configuración -->
    </application>
</manifest>
```

**Nota**: No se requiere `usesCleartextTraffic="true"` porque usamos HTTPS en producción.

---

## 4. Modelos de Datos (DTOs)

### 4.1 `FavoritePOIDto` (Estructura Simplificada)

**Archivo:** `app/src/main/java/com/example/points/network/SyncApiService.kt`

```kotlin
package com.example.points.network

import kotlinx.serialization.Serializable

/**
 * DTO para POI favorito
 * Estructura basada en la entidad FavoritePOI del backend Spring Boot
 */
@Serializable
data class FavoritePOIDto(
    val userId: String,
    val poiId: String,
    val name: String,              // ← Campo "name" (no "nombre")
    val isFavorite: Boolean = true,
    val timestamp: String          // ISO-8601 format
)
```

**Diferencias con la estructura anterior:**
- ✅ Campo `name` en lugar de `nombre`
- ✅ Campo `isFavorite: Boolean` agregado
- ✅ Campo `timestamp: String` en formato ISO-8601
- ❌ Eliminados campos: `descripcion`, `categoria`, `direccion`, `lat`, `lon`, `calificacion`, `imagenUrl`, `createdAt`, `updatedAt`, `deleted`

### 4.2 `SyncRequest` (Simplificado)

```kotlin
@Serializable
data class SyncRequest(
    val favorites: List<FavoritePOIDto> = emptyList()
)
```

**Cambios:**
- ✅ Solo contiene lista de `favorites`
- ❌ Eliminados: `deviceId`, `userId`, `lastSyncAt`, `cached`, `searchHistory`

### 4.3 `SyncResponse`

```kotlin
@Serializable
data class SyncResponse(
    val serverTimestamp: String? = null,
    val favorites: List<FavoritePOIDto> = emptyList()
)
```

**Nota:** `serverTimestamp` es opcional porque el backend puede no devolverlo.

---

## 5. Configuración de Retrofit

### 5.1 Configuración en `DefaultAppContainer.kt`

**Archivo:** `app/src/main/java/com/example/points/data/DefaultAppContainer.kt`

```kotlin
package com.example.points.data

import com.example.points.utils.EnvironmentConfig
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import com.example.points.BuildConfig
import java.util.concurrent.TimeUnit

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    // URL del backend desde EnvironmentConfig
    private val BACKEND_BASE_URL: String
        get() = EnvironmentConfig.BACKEND_BASE_URL
    
    // Configuración de Json (Kotlinx Serialization)
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        prettyPrint = BuildConfig.DEBUG
    }
    
    // Interceptor de logging
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("Retrofit", message)
    }.apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }
    
    // Interceptor para headers y autenticación JWT
    private val backendHeadersInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        val isBackendRequest = originalRequest.url.toString().contains(BACKEND_BASE_URL)
        
        if (isBackendRequest) {
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "MySyncApp-Android/1.0")
            
            // Obtener token JWT de Firebase Auth
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    val tokenResult = runBlocking {
                        try {
                            currentUser.getIdToken(false).await() // Del caché
                        } catch (e: Exception) {
                            currentUser.getIdToken(true).await()  // Del servidor
                        }
                    }
                    val token = tokenResult.token
                    if (token != null) {
                        requestBuilder.header("Authorization", "Bearer $token")
                    }
                }
            } catch (e: Exception) {
                Log.e("DefaultAppContainer", "Error al obtener token: ${e.message}")
            }
            
            chain.proceed(requestBuilder.build())
        } else {
            chain.proceed(originalRequest)
        }
    }
    
    // Cliente OkHttp para backend
    private val backendOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(backendHeadersInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Retrofit para Backend Spring Boot
    private val backendRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BACKEND_BASE_URL)
            .client(backendOkHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    // Servicio de API
    private val syncApiService: SyncApiService by lazy {
        backendRetrofit.create(SyncApiService::class.java)
    }
    
    // Repository de sincronización
    override val syncRepository: SyncRepository? by lazy {
        DefaultSyncRepository(
            syncApiService = syncApiService,
            localPOIRepository = localPOIRepository,
            context = context
        )
    }
}
```

---

## 6. Servicio de API

### 6.1 `SyncApiService.kt`

**Archivo:** `app/src/main/java/com/example/points/network/SyncApiService.kt`

```kotlin
package com.example.points.network

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Servicio de API para sincronización con el backend Spring Boot
 */
interface SyncApiService {
    /**
     * Envía cambios del cliente al servidor
     * @param request Request con los datos a sincronizar
     * @return Response vacío si es exitoso
     */
    @POST("api/v1/sync/push")
    suspend fun pushChanges(@Body request: SyncRequest): Response<Unit>
    
    /**
     * Obtiene cambios del servidor desde la última sincronización
     * @param userId ID del usuario
     * @param lastSyncAt Fecha de la última sincronización en formato ISO 8601
     * @return Response con los cambios del servidor
     */
    @GET("api/v1/sync/pull")
    suspend fun pullChanges(
        @Query("userId") userId: String,
        @Query("lastSyncAt") lastSyncAt: String
    ): Response<SyncResponse>
}

// DTOs definidos en el mismo archivo
@Serializable
data class SyncRequest(
    val favorites: List<FavoritePOIDto> = emptyList()
)

@Serializable
data class SyncResponse(
    val serverTimestamp: String? = null,
    val favorites: List<FavoritePOIDto> = emptyList()
)

@Serializable
data class FavoritePOIDto(
    val userId: String,
    val poiId: String,
    val name: String,
    val isFavorite: Boolean = true,
    val timestamp: String // ISO-8601 format
)
```

---

## 7. Autenticación JWT

### 7.1 Implementación Automática

La autenticación JWT está implementada automáticamente en el interceptor `backendHeadersInterceptor` en `DefaultAppContainer.kt`.

**Cómo funciona:**
1. El interceptor detecta peticiones al backend
2. Obtiene el usuario actual de Firebase Auth
3. Obtiene el token JWT (del caché o del servidor)
4. Agrega el header `Authorization: Bearer <token>`
5. El backend valida el token con `JwtAuthenticationFilter`

**No se requiere TokenManager separado** porque el token se obtiene directamente de Firebase Auth en cada petición.

### 7.2 Configuración del Backend

El backend debe tener configurado `SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/sync/**").authenticated()
                .anyRequest().permitAll()
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 8. Repository de Sincronización

### 8.1 `DefaultSyncRepository.kt`

**Archivo:** `app/src/main/java/com/example/points/repository/DefaultSyncRepository.kt`

```kotlin
package com.example.points.repository

import com.example.points.network.SyncApiService
import com.example.points.network.FavoritePOIDto
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.format.DateTimeFormatter

class DefaultSyncRepository(
    private val syncApiService: SyncApiService,
    private val localPOIRepository: LocalPOIRepository,
    private val context: Context
) : SyncRepository {
    
    private val auth = FirebaseAuth.getInstance()
    
    /**
     * Envía cambios locales al servidor
     */
    override suspend fun pushChanges(userId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Obtener favoritos locales
            val favoritesList = localPOIRepository.getAllFavoritesList()
            
            // Convertir a DTOs
            val favoriteDtos = favoritesList.map { poi ->
                poi.toFavoritePOIDto()
            }
            
            // Crear request
            val request = SyncRequest(
                favorites = favoriteDtos
            )
            
            // Enviar al servidor
            val response = syncApiService.pushChanges(request)
            
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Obtiene cambios del servidor
     */
    override suspend fun pullChanges(userId: String): Result<SyncResult> = withContext(Dispatchers.IO) {
        try {
            val lastSyncAt = getLastSyncTimestamp()
            
            val response = syncApiService.pullChanges(
                userId = userId,
                lastSyncAt = lastSyncAt ?: ""
            )
            
            if (response.isSuccessful && response.body() != null) {
                val syncResponse = response.body()!!
                
                // Procesar favoritos recibidos
                val favorites = syncResponse.favorites
                var added = 0
                var updated = 0
                var removed = 0
                
                favorites.forEach { dto ->
                    if (dto.isFavorite) {
                        // Agregar o actualizar
                        val poi = dto.toPointOfInterest()
                        localPOIRepository.addFavorite(poi)
                        added++
                    } else {
                        // Eliminar
                        localPOIRepository.removeFavorite(dto.poiId)
                        removed++
                    }
                }
                
                // Guardar timestamp
                val serverTimestamp = syncResponse.serverTimestamp ?: Instant.now().toString()
                saveLastSyncTimestamp(serverTimestamp)
                
                Result.success(SyncResult(
                    serverTimestamp = serverTimestamp,
                    favoritesAdded = added,
                    favoritesUpdated = updated,
                    favoritesRemoved = removed,
                    message = "Sincronización completada"
                ))
            } else {
                Result.failure(Exception("Error ${response.code()}: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Sincronización completa (pull + push)
     */
    override suspend fun sync(userId: String): Result<SyncResult> = withContext(Dispatchers.IO) {
        // Primero pull, luego push
        val pullResult = pullChanges(userId)
        if (pullResult.isSuccess) {
            pushChanges(userId)
        }
        pullResult
    }
    
    // Funciones de conversión
    private fun PointOfInterest.toFavoritePOIDto(): FavoritePOIDto {
        val userId = getCurrentUserId() ?: "unknown"
        return FavoritePOIDto(
            userId = userId,
            poiId = id,
            name = nombre,
            isFavorite = true,
            timestamp = Instant.now().toString()
        )
    }
    
    private fun FavoritePOIDto.toPointOfInterest(): PointOfInterest {
        return PointOfInterest(
            id = poiId,
            nombre = name,
            // Nota: Algunos campos quedarán vacíos porque el backend no los envía
            descripcion = "",
            categoria = CategoriaPOI.OTRO,
            // ...
        )
    }
}
```

---

## 9. Sincronización Automática con WorkManager

### 9.1 `SyncWorker.kt`

**Archivo:** `app/src/main/java/com/example/points/worker/SyncWorker.kt`

```kotlin
package com.example.points.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.points.PointsApplication
import com.example.points.repository.SyncRepository
import com.google.firebase.auth.FirebaseAuth

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser == null) {
                return Result.success() // No hay usuario autenticado
            }
            
            val app = applicationContext as? PointsApplication
            val syncRepository = app?.container?.syncRepository
            
            if (syncRepository == null) {
                return Result.failure()
            }
            
            val result = syncRepository.sync(currentUser.uid)
            
            if (result.isSuccess) {
                Result.success()
            } else {
                Result.retry() // Reintentar si falla
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
```

### 9.2 `SyncWorkManager.kt`

**Archivo:** `app/src/main/java/com/example/points/worker/SyncWorkManager.kt`

```kotlin
package com.example.points.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.points.data.PreferencesManager
import java.util.concurrent.TimeUnit

class SyncWorkManager(
    private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    
    private val workManager = WorkManager.getInstance(context)
    
    /**
     * Inicia sincronización periódica
     */
    fun startPeriodicSync() {
        val intervalHours = preferencesManager.autoSyncIntervalHours
        val syncOnlyWifi = preferencesManager.syncOnlyWifi
        
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (syncOnlyWifi) NetworkType.UNMETERED else NetworkType.CONNECTED
            )
            .setRequiresBatteryNotLow(true)
            .build()
        
        val workRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            intervalHours.toLong(), 
            TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .build()
        
        workManager.enqueueUniquePeriodicWork(
            SyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }
    
    /**
     * Detiene sincronización periódica
     */
    fun stopPeriodicSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
    }
}
```

### 9.3 Inicialización en `PointsApplication.kt`

```kotlin
package com.example.points

import android.app.Application
import com.example.points.data.DefaultAppContainer
import com.example.points.worker.SyncWorkManager

class PointsApplication : Application() {
    
    val container = DefaultAppContainer(this)
    private lateinit var syncWorkManager: SyncWorkManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Inicializar sincronización automática
        syncWorkManager = SyncWorkManager(
            context = this,
            preferencesManager = container.preferencesManager
        )
        
        if (container.preferencesManager.autoSyncEnabled) {
            syncWorkManager.startPeriodicSync()
        }
    }
    
    fun restartAutoSync() {
        if (container.preferencesManager.autoSyncEnabled) {
            syncWorkManager.startPeriodicSync()
        } else {
            syncWorkManager.stopPeriodicSync()
        }
    }
}
```

---

## 10. UI de Configuración

### 10.1 `SyncSettingsScreen.kt`

**Archivo:** `app/src/main/java/com/example/points/screens/SyncSettingsScreen.kt`

Pantalla de Jetpack Compose para configurar:
- ✅ Activar/desactivar sincronización automática
- ✅ Intervalo de sincronización (slider)
- ✅ Sincronizar solo en WiFi
- ✅ Botón "Sincronizar Ahora"
- ✅ Mostrar última sincronización

### 10.2 `SyncSettingsViewModel.kt`

**Archivo:** `app/src/main/java/com/example/points/viewmodel/SyncSettingsViewModel.kt`

ViewModel que gestiona el estado de la UI y las operaciones de sincronización.

---

## 11. Ejemplos de Uso

### 11.1 Sincronización Manual desde ViewModel

```kotlin
class MyViewModel(
    private val syncRepository: SyncRepository
) : ViewModel() {
    
    fun syncNow() {
        viewModelScope.launch {
            val currentUser = FirebaseAuth.getInstance().currentUser
            if (currentUser != null) {
                val result = syncRepository.sync(currentUser.uid)
                result.onSuccess {
                    // Sincronización exitosa
                }.onFailure { error ->
                    // Manejar error
                }
            }
        }
    }
}
```

### 11.2 Sincronización desde UI

```kotlin
@Composable
fun SyncButton(
    viewModel: SyncSettingsViewModel
) {
    Button(
        onClick = { viewModel.syncNow() },
        enabled = !viewModel.isSyncing
    ) {
        Text("Sincronizar Ahora")
    }
}
```

---

## 12. Configuración de Variables

### 12.1 URL del Backend

**Archivo:** `app/src/main/java/com/example/points/utils/EnvironmentConfig.kt`

```kotlin
val BACKEND_BASE_URL: String
    get() = getEnvValue("BACKEND_BASE_URL").ifEmpty { 
        "https://mysyncapp-backend-860998153214.us-central1.run.app/" 
    }
```

**URL de Producción:**
```
https://mysyncapp-backend-860998153214.us-central1.run.app/
```

### 12.2 Configuración en `.env`

```env
BACKEND_BASE_URL=https://mysyncapp-backend-860998153214.us-central1.run.app/
```

---

## ✅ Checklist de Implementación

- [x] Dependencias agregadas en `build.gradle.kts`
- [x] Permisos de Internet en `AndroidManifest.xml`
- [x] Modelos de datos (DTOs) creados con Kotlinx Serialization
- [x] Retrofit configurado en `DefaultAppContainer.kt`
- [x] `SyncApiService` implementado
- [x] Autenticación JWT implementada en interceptor
- [x] `DefaultSyncRepository` creado
- [x] WorkManager configurado para sincronización automática
- [x] UI de configuración implementada
- [x] URL del backend configurada
- [x] Probar conexión con backend en producción

---

## 🔧 Solución de Problemas

### Error: HTTP 403 Forbidden
- **Causa:** El backend requiere autenticación JWT
- **Solución:** Verifica que el usuario esté autenticado en Firebase Auth
- **Verificar:** Revisa logs con filtro `DefaultAppContainer` para ver si el token se agrega

### Error: "Unable to resolve host"
- **Causa:** URL incorrecta o sin conexión a internet
- **Solución:** Verifica `BACKEND_BASE_URL` en `EnvironmentConfig.kt`

### Error: SerializationException
- **Causa:** DTOs no marcados con `@Serializable`
- **Solución:** Asegúrate de que todos los DTOs tengan `@Serializable`

### Error: Token JWT es null
- **Causa:** Usuario no autenticado o token expirado
- **Solución:** Verifica que el usuario haya iniciado sesión con Firebase Auth

---

## 📚 Referencias

- [Retrofit Documentation](https://square.github.io/retrofit/)
- [Kotlinx Serialization](https://kotlinlang.org/docs/serialization.html)
- [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- [Firebase Auth](https://firebase.google.com/docs/auth)

---

## 🚀 Estado Actual

✅ **Implementación Completa:**
- Sincronización push/pull funcional
- Autenticación JWT automática
- Sincronización automática con WorkManager
- UI de configuración
- Manejo de errores
- Logging detallado

**Última actualización:** Diciembre 2024
