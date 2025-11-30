package com.example.points.data

import android.content.Context
import com.example.points.data.repository.DashboardRepository
import com.example.points.database.PointsDatabase
import com.example.points.network.GeminiApiService
import com.example.points.network.SyncApiService
import com.example.points.network.WeatherApiService
import com.example.points.repository.DefaultGeminiRepository
import com.example.points.repository.DefaultSyncRepository
import com.example.points.repository.DefaultWeatherRepository
import com.example.points.repository.GeminiRepository
import com.example.points.repository.LocalPOIRepository
import com.example.points.repository.LocalSearchRepository
import com.example.points.repository.SyncRepository
import com.example.points.repository.WeatherRepository
import com.example.points.storage.LocalFileStorage
import com.example.points.utils.EnvironmentConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import android.util.Log
import com.example.points.BuildConfig
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import com.google.firebase.auth.FirebaseAuth

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    private val WEATHER_BASE_URL = "https://api.openweathermap.org/"
    private val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    private val BACKEND_BASE_URL: String
        get() = com.example.points.utils.EnvironmentConfig.BACKEND_BASE_URL
    
    // Configuración de Json
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = false
        prettyPrint = BuildConfig.DEBUG // Formato legible solo en debug
    }
    
    // Interceptor de logging para debugging
    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        Log.d("Retrofit", message)
    }.apply {
        // Solo mostrar logs en modo debug para evitar información sensible en producción
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY // Muestra request/response completo
        } else {
            HttpLoggingInterceptor.Level.NONE // No muestra nada en release
        }
    }
    
    // Interceptor para agregar headers comunes a las peticiones del backend
    private val backendHeadersInterceptor = okhttp3.Interceptor { chain ->
        val originalRequest = chain.request()
        
        // Solo agregar headers si es una petición al backend
        val isBackendRequest = originalRequest.url.toString().contains(BACKEND_BASE_URL)
        
        if (isBackendRequest) {
            val requestBuilder = originalRequest.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                // Agregar User-Agent para identificar la app
                .header("User-Agent", "MySyncApp-Android/1.0")
            
            // Obtener token JWT de Firebase Auth para autenticación
            // El backend requiere autenticación JWT según SecurityConfig
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser != null) {
                    // Obtener el ID token de forma bloqueante (el token generalmente está en caché)
                    // Si no está en caché, se obtiene del servidor (puede tomar unos milisegundos)
                    val tokenResult = runBlocking {
                        // Primero intentar obtener del caché (forceRefresh = false)
                        // Si falla, obtener del servidor (forceRefresh = true)
                        try {
                            currentUser.getIdToken(false).await()
                        } catch (e: Exception) {
                            Log.w("DefaultAppContainer", "⚠️ Token no en caché, obteniendo del servidor...")
                            currentUser.getIdToken(true).await()
                        }
                    }
                    
                    val token = tokenResult.token
                    if (token != null) {
                        requestBuilder.header("Authorization", "Bearer $token")
                        Log.d("DefaultAppContainer", "✅ Token JWT agregado al header Authorization")
                    } else {
                        Log.w("DefaultAppContainer", "⚠️ Token JWT es null")
                    }
                } else {
                    Log.w("DefaultAppContainer", "⚠️ Usuario no autenticado - la petición puede fallar con 401/403")
                }
            } catch (e: Exception) {
                Log.e("DefaultAppContainer", "❌ Error al obtener token de Firebase: ${e.message}", e)
                // Continuar sin token - el backend rechazará la petición con 401/403
            }
            
            val newRequest = requestBuilder.build()
            Log.d("DefaultAppContainer", "📤 [HEADERS] Request a: ${newRequest.url}")
            Log.d("DefaultAppContainer", "   Headers: ${newRequest.headers.names()}")
            if (BuildConfig.DEBUG) {
                // Solo mostrar Authorization header en debug (sin el token completo por seguridad)
                val authHeader = newRequest.header("Authorization")
                if (authHeader != null) {
                    // El header ya contiene "Bearer", solo mostrar los primeros caracteres
                    Log.d("DefaultAppContainer", "   Authorization: ${authHeader.take(30)}...")
                }
            }
            
            chain.proceed(newRequest)
        } else {
            chain.proceed(originalRequest)
        }
    }
    
    // Cliente OkHttp con configuración
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(backendHeadersInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Timeout de conexión
        .readTimeout(30, TimeUnit.SECONDS)    // Timeout de lectura
        .writeTimeout(30, TimeUnit.SECONDS)   // Timeout de escritura
        .build()
    
    // Cliente OkHttp específico para el backend (con headers adicionales)
    private val backendOkHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .addInterceptor(backendHeadersInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    // Instancia única de Firebase
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    
    // Base de datos Room
    private val database: PointsDatabase by lazy {
        PointsDatabase.getDatabase(context)
    }
    
    // Retrofit para Weather API
    private val weatherRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .client(okHttpClient) // Agregar cliente OkHttp con logging
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    // Retrofit para Gemini API
    private val geminiRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(GEMINI_BASE_URL)
            .client(okHttpClient) // Agregar cliente OkHttp con logging
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    // Retrofit para Backend Spring Boot
    private val backendRetrofit: Retrofit by lazy {
        val baseUrl = BACKEND_BASE_URL
        Log.d("DefaultAppContainer", "🔗 [RETROFIT] Configurando Retrofit para backend:")
        Log.d("DefaultAppContainer", "   📍 URL Base: $baseUrl")
        
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(backendOkHttpClient) // Usar cliente específico para backend
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    private val weatherApiService: WeatherApiService by lazy {
        weatherRetrofit.create(WeatherApiService::class.java)
    }
    
    private val geminiApiService: GeminiApiService by lazy {
        geminiRetrofit.create(GeminiApiService::class.java)
    }
    
    private val syncApiService: SyncApiService by lazy {
        Log.d("DefaultAppContainer", "✅ [RETROFIT] SyncApiService creado")
        Log.d("DefaultAppContainer", "   📍 Endpoint: ${BACKEND_BASE_URL}api/v1/sync/")
        backendRetrofit.create(SyncApiService::class.java)
    }
    
    override val weatherRepository: WeatherRepository by lazy {
        DefaultWeatherRepository(weatherApiService)
    }
    
    override val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(firestore)
    }
    
    override val preferencesManager: PreferencesManager by lazy {
        PreferencesManager(context)
    }
    
    override val localPOIRepository: LocalPOIRepository by lazy {
        LocalPOIRepository(database)
    }
    
    override val localSearchRepository: LocalSearchRepository by lazy {
        LocalSearchRepository(database)
    }
    
    override val localFileStorage: LocalFileStorage by lazy {
        LocalFileStorage(context)
    }
    
    override val geminiRepository: GeminiRepository? by lazy {
        // Forzar que EnvironmentConfig esté inicializado antes de acceder a la API key
        // (aunque ya debería estar inicializado en PointsApplication.onCreate())
        val apiKey = EnvironmentConfig.GEMINI_API_KEY
        Log.d("DefaultAppContainer", "Inicializando GeminiRepository...")
        Log.d("DefaultAppContainer", "GEMINI_API_KEY longitud: ${apiKey.length} caracteres")
        if (apiKey.isEmpty()) {
            Log.w("DefaultAppContainer", "❌ Gemini API key no configurada - GeminiRepository será null")
            Log.w("DefaultAppContainer", "   Verifica que el .env tenga GEMINI_API_KEY configurada")
            Log.w("DefaultAppContainer", "   Verifica que el .env esté en app/src/main/assets/.env")
            Log.w("DefaultAppContainer", "   Verifica que la aplicación se haya reinstalado después de actualizar el .env")
            null
        } else {
            Log.d("DefaultAppContainer", "✅ GeminiRepository inicializado correctamente")
            Log.d("DefaultAppContainer", "   API Key: ${apiKey.take(10)}... (longitud: ${apiKey.length})")
            DefaultGeminiRepository(geminiApiService)
        }
    }
    
    override val syncRepository: SyncRepository? by lazy {
        val backendUrl = EnvironmentConfig.BACKEND_BASE_URL
        Log.d("DefaultAppContainer", "Inicializando SyncRepository...")
        Log.d("DefaultAppContainer", "BACKEND_BASE_URL: $backendUrl")
        if (backendUrl.isEmpty()) {
            Log.w("DefaultAppContainer", "❌ Backend URL no configurada - SyncRepository será null")
            null
        } else {
            Log.d("DefaultAppContainer", "✅ SyncRepository inicializado correctamente")
            DefaultSyncRepository(
                syncApiService = syncApiService,
                localPOIRepository = localPOIRepository,
                context = context
            )
        }
    }
}

