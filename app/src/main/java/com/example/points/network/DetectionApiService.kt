package com.example.points.network

import com.example.points.models.detection.DetectionResponse
import okhttp3.MultipartBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Servicio de API para detección de amenazas
 * URL Base: Configurar DETECTION_API_URL en el archivo .env
 */
interface DetectionApiService {
    /**
     * Analiza una imagen para detectar amenazas
     * @param apiKey API key para autenticación (header X-API-Key)
     * @param imagePart Imagen en formato multipart/form-data
     * @return Respuesta con la detección de objetos y amenazas
     */
    @Multipart
    @POST("detectar")
    suspend fun detectarAmenazas(
        @Header("X-API-Key") apiKey: String,
        @Part imagePart: MultipartBody.Part
    ): DetectionResponse
}

