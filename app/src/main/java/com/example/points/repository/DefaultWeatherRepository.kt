package com.example.points.repository

import com.example.points.models.weather.WeatherResponse
import com.example.points.network.WeatherApiService
import com.example.points.utils.EnvironmentConfig
import retrofit2.HttpException
import java.io.IOException

class DefaultWeatherRepository(
    private val weatherApiService: WeatherApiService
) : WeatherRepository {
    
    override suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        val apiKey = EnvironmentConfig.OPENWEATHER_API_KEY
        if (apiKey.isEmpty()) {
            android.util.Log.w("DefaultWeatherRepository", "OpenWeatherMap API key no configurada")
            throw IllegalStateException("OpenWeatherMap API key no configurada. Por favor, configura OPENWEATHER_API_KEY en el archivo .env")
        }
        
        return try {
            weatherApiService.getWeather(
                lat = lat,
                lon = lon,
                apiKey = apiKey
            )
        } catch (e: IOException) {
            android.util.Log.e("DefaultWeatherRepository", "Error de red al obtener clima", e)
            throw e
        } catch (e: HttpException) {
            android.util.Log.e("DefaultWeatherRepository", "Error HTTP al obtener clima: ${e.code()}", e)
            throw e
        }
    }
}

