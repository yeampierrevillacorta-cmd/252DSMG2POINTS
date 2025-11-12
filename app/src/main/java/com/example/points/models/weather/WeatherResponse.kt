package com.example.points.models.weather

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class WeatherResponse(
    @SerialName("current")
    val current: CurrentWeather
)

@Serializable
data class CurrentWeather(
    @SerialName("temp")
    val temperature: Double,
    @SerialName("feels_like")
    val feelsLike: Double,
    val weather: List<WeatherDescription>
)

@Serializable
data class WeatherDescription(
    val main: String,
    val description: String,
    val icon: String
)

