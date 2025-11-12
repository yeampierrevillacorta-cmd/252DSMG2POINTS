package com.example.points.data

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de preferencias de usuario usando SharedPreferences
 * Implementa almacenamiento local simple para la Unidad 5 de Android Basics
 */
class PreferencesManager(context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    
    companion object {
        private const val PREFS_NAME = "points_preferences"
        
        // Claves de preferencias
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LANGUAGE = "language"
        private const val KEY_FIRST_LAUNCH = "first_launch"
        private const val KEY_USE_GPS = "use_gps"
        private const val KEY_SEARCH_RADIUS = "search_radius"
        private const val KEY_SHOW_ONLY_APPROVED = "show_only_approved"
        private const val KEY_LAST_SEARCH_QUERY = "last_search_query"
        private const val KEY_LAST_CATEGORY_FILTER = "last_category_filter"
    }
    
    // Preferencias de notificaciones
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_NOTIFICATIONS_ENABLED, value).apply()
    
    // Modo de tema (light, dark, system)
    var themeMode: String
        get() = prefs.getString(KEY_THEME_MODE, "system") ?: "system"
        set(value) = prefs.edit().putString(KEY_THEME_MODE, value).apply()
    
    // Idioma seleccionado
    var language: String
        get() = prefs.getString(KEY_LANGUAGE, "es") ?: "es"
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()
    
    // Primera ejecución (onboarding)
    var isFirstLaunch: Boolean
        get() = prefs.getBoolean(KEY_FIRST_LAUNCH, true)
        set(value) = prefs.edit().putBoolean(KEY_FIRST_LAUNCH, value).apply()
    
    // Usar GPS para ubicación
    var useGps: Boolean
        get() = prefs.getBoolean(KEY_USE_GPS, true)
        set(value) = prefs.edit().putBoolean(KEY_USE_GPS, value).apply()
    
    // Radio de búsqueda (en kilómetros)
    var searchRadius: Float
        get() = prefs.getFloat(KEY_SEARCH_RADIUS, 10.0f)
        set(value) = prefs.edit().putFloat(KEY_SEARCH_RADIUS, value).apply()
    
    // Mostrar solo POIs aprobados
    var showOnlyApproved: Boolean
        get() = prefs.getBoolean(KEY_SHOW_ONLY_APPROVED, true)
        set(value) = prefs.edit().putBoolean(KEY_SHOW_ONLY_APPROVED, value).apply()
    
    // Última búsqueda realizada
    var lastSearchQuery: String
        get() = prefs.getString(KEY_LAST_SEARCH_QUERY, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_SEARCH_QUERY, value).apply()
    
    // Último filtro de categoría utilizado
    var lastCategoryFilter: String
        get() = prefs.getString(KEY_LAST_CATEGORY_FILTER, "") ?: ""
        set(value) = prefs.edit().putString(KEY_LAST_CATEGORY_FILTER, value).apply()
    
    // Limpiar todas las preferencias
    fun clear() {
        prefs.edit().clear().apply()
    }
    
    // Limpiar preferencias específicas
    fun clearSearchPreferences() {
        prefs.edit()
            .remove(KEY_LAST_SEARCH_QUERY)
            .remove(KEY_LAST_CATEGORY_FILTER)
            .apply()
    }
}

