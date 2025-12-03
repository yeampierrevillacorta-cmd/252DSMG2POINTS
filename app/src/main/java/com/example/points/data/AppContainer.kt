package com.example.points.data

import android.content.Context
import com.example.points.data.repository.DashboardRepository
import com.example.points.repository.GeminiRepository
import com.example.points.repository.LocalPOIRepository
import com.example.points.repository.LocalSearchRepository
import com.example.points.repository.SyncRepository
import com.example.points.repository.WeatherRepository
import com.example.points.storage.LocalFileStorage
import com.example.points.sync.data.SyncPreferences
import com.example.points.sync.repository.RemoteSyncRepository
import com.example.points.sync.worker.SyncWorkManager

interface AppContainer {
    val weatherRepository: WeatherRepository
    val dashboardRepository: DashboardRepository
    val preferencesManager: PreferencesManager
    val localPOIRepository: LocalPOIRepository
    val localSearchRepository: LocalSearchRepository
    val localFileStorage: LocalFileStorage
    val geminiRepository: GeminiRepository?
<<<<<<< HEAD
    val syncPreferences: SyncPreferences
    val remoteSyncRepository: RemoteSyncRepository
    val syncWorkManager: SyncWorkManager
=======
    val syncRepository: SyncRepository?
>>>>>>> 3616147010ca71a00c51183b96f2dd12eda121ab
}

