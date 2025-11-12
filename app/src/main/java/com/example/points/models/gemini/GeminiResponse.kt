package com.example.points.models.gemini

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

/**
 * Response de la API de Gemini
 */
@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>? = null,
    val promptFeedback: PromptFeedback? = null
)

@Serializable
data class Candidate(
    val content: ContentResponse,
    @SerialName("finishReason")
    val finishReason: String? = null,
    val index: Int? = null,
    @SerialName("safetyRatings")
    val safetyRatings: List<SafetyRating>? = null
)

@Serializable
data class ContentResponse(
    val parts: List<PartResponse>
)

@Serializable
data class PartResponse(
    val text: String
)

@Serializable
data class SafetyRating(
    val category: String? = null,
    val probability: String? = null
)

@Serializable
data class PromptFeedback(
    val blockReason: String? = null,
    val safetyRatings: List<SafetyRating>? = null
)

