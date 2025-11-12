package com.example.points.data

import android.content.Context
import com.example.points.data.repository.DashboardRepository
import com.example.points.database.PointsDatabase
import com.example.points.network.GeminiApiService
import com.example.points.network.WeatherApiService
import com.example.points.repository.DefaultGeminiRepository
import com.example.points.repository.DefaultWeatherRepository
import com.example.points.repository.GeminiRepository
import com.example.points.repository.LocalPOIRepository
import com.example.points.repository.LocalSearchRepository
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

class DefaultAppContainer(private val context: Context) : AppContainer {
    
    private val WEATHER_BASE_URL = "https://api.openweathermap.org/"
    private val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    
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
    
    // Cliente OkHttp con configuración
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Timeout de conexión
        .readTimeout(30, TimeUnit.SECONDS)    // Timeout de lectura
        .writeTimeout(30, TimeUnit.SECONDS)   // Timeout de escritura
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
    
    private val weatherApiService: WeatherApiService by lazy {
        weatherRetrofit.create(WeatherApiService::class.java)
    }
    
    private val geminiApiService: GeminiApiService by lazy {
        geminiRetrofit.create(GeminiApiService::class.java)
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
}

