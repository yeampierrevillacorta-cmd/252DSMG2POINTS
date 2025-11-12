package com.example.points.repository

import com.example.points.models.CategoriaPOI
import com.example.points.models.gemini.Content
import com.example.points.models.gemini.GeminiRequest
import com.example.points.models.gemini.Part
import com.example.points.network.GeminiApiService
import com.example.points.utils.EnvironmentConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import android.util.Log
import retrofit2.HttpException
import java.io.IOException

/**
 * Implementación del repositorio de Gemini
 */
class DefaultGeminiRepository(
    private val geminiApiService: GeminiApiService
) : GeminiRepository {
    
    companion object {
        private const val TAG = "GeminiRepository"
    }
    
    override suspend fun generatePOIDescription(
        nombre: String,
        categoria: CategoriaPOI,
        direccion: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = EnvironmentConfig.GEMINI_API_KEY
            if (apiKey.isEmpty()) {
                Log.w(TAG, "Gemini API key no configurada")
                return@withContext Result.failure(
                    IllegalStateException("Gemini API key no configurada. Por favor, configura GEMINI_API_KEY en el archivo .env")
                )
            }
            
            // Construir el prompt para Gemini
            val prompt = buildPrompt(nombre, categoria, direccion)
            
            // Crear la solicitud
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                )
            )
            
            // Llamar a la API
            val response = geminiApiService.generateContent(apiKey, request)
            
            // Extraer el texto generado
            val generatedText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return@withContext Result.failure(
                    Exception("No se pudo generar la descripción. Respuesta vacía de Gemini.")
                )
            
            Log.d(TAG, "Descripción generada exitosamente para: $nombre")
            Result.success(generatedText.trim())
            
        } catch (e: IllegalStateException) {
            Log.e(TAG, "Error: API key no configurada", e)
            Result.failure(e)
        } catch (e: HttpException) {
            Log.e(TAG, "Error HTTP al generar descripción: ${e.code()}", e)
            val errorMessage = when (e.code()) {
                400 -> "Solicitud inválida a Gemini API"
                401 -> "API key inválida o no autorizada"
                403 -> "Acceso denegado a Gemini API"
                429 -> "Límite de solicitudes excedido. Intenta más tarde"
                500 -> "Error del servidor de Gemini API"
                else -> "Error del servidor: ${e.message()}"
            }
            Result.failure(Exception(errorMessage))
        } catch (e: IOException) {
            Log.e(TAG, "Error de red al generar descripción", e)
            Result.failure(Exception("Error de conexión. Verifica tu conexión a internet."))
        } catch (e: Exception) {
            Log.e(TAG, "Error inesperado al generar descripción", e)
            Result.failure(Exception("Error al generar descripción: ${e.message}"))
        }
    }
    
    /**
     * Construye el prompt para Gemini basándose en la información del POI
     */
    private fun buildPrompt(nombre: String, categoria: CategoriaPOI, direccion: String?): String {
        return """
            Genera una descripción breve, atractiva y profesional en español para un punto de interés con las siguientes características:
            
            - Nombre: $nombre
            - Categoría: ${categoria.displayName}
            ${if (direccion != null && direccion.isNotEmpty()) "- Dirección: $direccion" else ""}
            
            La descripción debe:
            - Tener entre 2 y 4 líneas (máximo 150 palabras)
            - Ser atractiva y profesional
            - Estar escrita en español
            - Resaltar las características principales del lugar
            - Ser apropiada para una aplicación de puntos de interés
            - No incluir información de contacto, precios ni horarios
            - Ser descriptiva pero concisa
            
            Solo genera la descripción, sin títulos ni encabezados.
        """.trimIndent()
    }
}

