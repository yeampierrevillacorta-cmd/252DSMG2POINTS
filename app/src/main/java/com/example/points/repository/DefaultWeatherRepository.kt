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
            throw IllegalStateException("OpenWeatherMap API key no configurada")
        }
        
        return try {
            weatherApiService.getWeather(
                lat = lat,
                lon = lon,
                apiKey = apiKey
            )
        } catch (e: IOException) {
            throw e
        } catch (e: HttpException) {
            throw e
        }
    }
}

