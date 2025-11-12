package com.example.points.data

import com.example.points.repository.WeatherRepository

interface AppContainer {
    val weatherRepository: WeatherRepository
}

