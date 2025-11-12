package com.example.points.data

import com.example.points.data.repository.DashboardRepository
import com.example.points.network.WeatherApiService
import com.example.points.repository.DefaultWeatherRepository
import com.example.points.repository.WeatherRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit

class DefaultAppContainer : AppContainer {
    
    private val BASE_URL = "https://api.openweathermap.org/"
    
    private val json = Json {
        ignoreUnknownKeys = true
    }
    
    // Instancia única de Firebase
    private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
    }
    
    private val weatherApiService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
    
    override val weatherRepository: WeatherRepository by lazy {
        DefaultWeatherRepository(weatherApiService)
    }
    
    override val dashboardRepository: DashboardRepository by lazy {
        DashboardRepository(firestore)
    }
}

