/*******************************************************************************
 * Copyright (c) 2026. EJS Studios. Todos los derechos reservados.
 ******************************************************************************/
package com.ejsstudios.socialhub.firebase.db.model

import androidx.annotation.Keep
import com.squareup.moshi.JsonClass

/**
 * Request para validar la novedad de un tema.
 */
@Keep
@JsonClass(generateAdapter = true)
data class ValidateNoveltyRequest(
    val current_theme: String,
    val recent_themes: List<String>,
    val threshold: Float = 0.85f
)

/**
 * Respuesta de la validación de novedad.
 */
@Keep
@JsonClass(generateAdapter = true)
data class ValidateNoveltyResponse(
    val success: Boolean,
    val is_novel: Boolean = true,
    val similarity_score: Float = 0f,
    val conflicting_theme: String? = null,
    val threshold_used: Float = 0.85f,
    val message: String? = null,
    val error: String? = null
)

/**
 * Request para preparar el prompt y contar tokens.
 */
@Keep
@JsonClass(generateAdapter = true)
data class PreparePromptRequest(
    val pieces: Map<String, String>,
    val token_limit: Int = 4000
)

/**
 * Respuesta de la preparación del prompt.
 */
@Keep
@JsonClass(generateAdapter = true)
data class PreparePromptResponse(
    val success: Boolean,
    val final_prompt: String? = null,
    val token_count: Int = 0,
    val is_truncated: Boolean = false,
    val token_limit: Int = 4000,
    val message: String? = null,
    val error: String? = null
)

/**
 * Request para ejecutar la IA (Proxy).
 */
@Keep
@JsonClass(generateAdapter = true)
data class ExecuteAIRequest(
    val prompt: String,
    val options: Map<String, Any> = emptyMap()
)

/**
 * Respuesta de la ejecución de IA.
 */
@Keep
@JsonClass(generateAdapter = true)
data class ExecuteAIResponse(
    val success: Boolean,
    val content: String? = null,
    val usage: AIUsage? = null,
    val model: String? = null,
    val error: String? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class AIUsage(
    val prompt_tokens: Int = 0,
    val completion_tokens: Int = 0,
    val total_tokens: Int = 0
)

/**
 * Request para actualizar la memoria semántica.
 */
@Keep
@JsonClass(generateAdapter = true)
data class UpdateMemoryRequest(
    val content: String
)

/**
 * Respuesta de la actualización de memoria semántica.
 */
@Keep
@JsonClass(generateAdapter = true)
data class UpdateMemoryResponse(
    val success: Boolean,
    val summary: String? = null,
    val embedding: List<Float>? = null,
    val original_length: Int = 0,
    val error: String? = null
)
