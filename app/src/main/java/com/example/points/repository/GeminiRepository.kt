package com.example.points.repository

import com.example.points.models.CategoriaPOI

/**
 * Repositorio para operaciones con Google Gemini API
 */
interface GeminiRepository {
    /**
     * Genera una descripción para un POI basándose en su nombre, categoría y dirección opcional
     * @param nombre Nombre del POI
     * @param categoria Categoría del POI
     * @param direccion Dirección del POI (opcional)
     * @return Descripción generada o null si hay error
     */
    suspend fun generatePOIDescription(
        nombre: String,
        categoria: CategoriaPOI,
        direccion: String? = null
    ): Result<String>
}

