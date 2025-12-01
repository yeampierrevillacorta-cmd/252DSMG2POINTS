package com.example.points.data

import android.content.Context
import com.example.points.data.repository.DashboardRepository
import com.example.points.network.DetectionApiService
import com.example.points.repository.GeminiRepository
import com.example.points.repository.LocalPOIRepository
import com.example.points.repository.LocalSearchRepository
import com.example.points.repository.WeatherRepository
import com.example.points.storage.LocalFileStorage

interface AppContainer {
    val weatherRepository: WeatherRepository
    val dashboardRepository: DashboardRepository
    val preferencesManager: PreferencesManager
    val localPOIRepository: LocalPOIRepository
    val localSearchRepository: LocalSearchRepository
    val localFileStorage: LocalFileStorage
    val geminiRepository: GeminiRepository?
    val detectionApiService: DetectionApiService
}

