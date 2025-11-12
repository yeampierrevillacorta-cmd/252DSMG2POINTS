package com.example.points.network

import com.example.points.models.gemini.GeminiRequest
import com.example.points.models.gemini.GeminiResponse
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Servicio de API para Google Gemini
 * Documentación: https://ai.google.dev/api
 */
interface GeminiApiService {
    /**
     * Genera contenido usando el modelo Gemini Pro
     * @param apiKey API key de Google Gemini
     * @param request Request con el prompt y configuración
     * @return Respuesta con el contenido generado
     */
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

