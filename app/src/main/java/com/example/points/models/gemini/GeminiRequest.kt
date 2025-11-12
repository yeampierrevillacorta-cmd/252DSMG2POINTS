package com.example.points.models.gemini

import kotlinx.serialization.Serializable

/**
 * Request para la API de Gemini
 */
@Serializable
data class GeminiRequest(
    val contents: List<Content>
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String
)

