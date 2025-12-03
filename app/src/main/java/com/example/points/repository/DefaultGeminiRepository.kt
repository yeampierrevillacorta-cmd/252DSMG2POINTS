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
            Log.w(TAG, "API key no configurada, generando descripción automática", e)
            // Generar descripción automática cuando no hay API key
            Result.success(generateDefaultDescription(nombre, categoria, direccion))
        } catch (e: HttpException) {
            Log.w(TAG, "Error HTTP al generar descripción: ${e.code()}, generando descripción automática", e)
            // Generar descripción automática cuando hay error HTTP
            Result.success(generateDefaultDescription(nombre, categoria, direccion))
        } catch (e: IOException) {
            Log.w(TAG, "Error de red al generar descripción, generando descripción automática", e)
            // Generar descripción automática cuando hay error de conexión
            Result.success(generateDefaultDescription(nombre, categoria, direccion))
        } catch (e: Exception) {
            Log.w(TAG, "Error inesperado al generar descripción, generando descripción automática", e)
            // Generar descripción automática cuando hay cualquier otro error
            Result.success(generateDefaultDescription(nombre, categoria, direccion))
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
    
    /**
     * Genera una descripción automática basada en la información del POI
     */
    private fun generateDefaultDescription(
        nombre: String,
        categoria: CategoriaPOI,
        direccion: String?
    ): String {
        val categoriaDesc = when (categoria) {
            CategoriaPOI.COMIDA -> "un establecimiento gastronómico que ofrece una experiencia culinaria única"
            CategoriaPOI.ENTRETENIMIENTO -> "un lugar de entretenimiento ideal para disfrutar en tu tiempo libre"
            CategoriaPOI.CULTURA -> "un espacio cultural que enriquece la vida de la comunidad"
            CategoriaPOI.DEPORTE -> "un centro deportivo donde puedes mantenerte activo y saludable"
            CategoriaPOI.SALUD -> "un centro de salud comprometido con el bienestar de la comunidad"
            CategoriaPOI.EDUCACION -> "una institución educativa que contribuye al desarrollo y aprendizaje"
            CategoriaPOI.TRANSPORTE -> "un punto de transporte que facilita la movilidad urbana"
            CategoriaPOI.SERVICIOS -> "un centro de servicios que atiende las necesidades de la comunidad"
            CategoriaPOI.TURISMO -> "un destino turístico que vale la pena visitar"
            CategoriaPOI.RECARGA_ELECTRICA -> "una estación de recarga eléctrica para vehículos sostenibles"
            CategoriaPOI.PARQUES -> "un espacio verde perfecto para relajarse y disfrutar de la naturaleza"
            CategoriaPOI.SHOPPING -> "un centro comercial con diversas opciones de compra y servicios"
            CategoriaPOI.OTRO -> "un punto de interés destacado en la zona"
        }
        
        val ubicacionDesc = if (direccion != null && direccion.isNotEmpty()) {
            "Ubicado en $direccion, "
        } else {
            ""
        }
        
        return "$nombre es $categoriaDesc. $ubicacionDesc" +
                "Un lugar que merece ser conocido y visitado por su contribución a la comunidad."
    }
}

