package com.example.points.repository

import com.example.points.models.weather.WeatherResponse

interface WeatherRepository {
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse
}

