/*
 * Copyright (c) 2025-2026. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 */
package com.ejsstudios.socialhub.firebase.db.model

import androidx.annotation.Keep
import com.ejsstudios.socialhub.R
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.util.UUID

/**
 * Estado de una campaña de marketing.
 */
@Keep
enum class CampaignStatus(val id: String, val stringRes: Int) {
    DRAFT("draft", R.string.status_draft),
    ACTIVE("active", R.string.status_active),
    PAUSED("paused", R.string.status_paused),
    COMPLETED("completed", R.string.status_completed),
    INACTIVE("inactive", R.string.status_inactive);

    companion object {
        fun fromId(id: String): CampaignStatus = entries.find { it.id == id } ?: DRAFT

        fun fromDisplayName(name: String): CampaignStatus {
            return entries.find { it.id.equals(name, ignoreCase = true) }
                ?: when (name.lowercase()) {
                    "draft", "borrador" -> DRAFT
                    "active", "activa" -> ACTIVE
                    "paused", "pausada" -> PAUSED
                    "completed", "finalizada" -> COMPLETED
                    "inactive", "eliminada" -> INACTIVE
                    else -> DRAFT
                }
        }
    }
}

/**
 * Modelo de dominio y Firestore para una Campaña de Marketing.
 */
@Keep
@IgnoreExtraProperties
data class CampaignFirestore(
    var id: String = UUID.randomUUID().toString(),

    @get:PropertyName("channel_id")
    @set:PropertyName("channel_id")
    var channelId: String = "",

    @get:PropertyName("platform")
    @set:PropertyName("platform")
    var platform: SocialPlatform? = null,

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("objective")
    @set:PropertyName("objective")
    var objective: String = "",

    @get:PropertyName("theme_focus")
    @set:PropertyName("theme_focus")
    var themeFocus: String = "",

    @get:PropertyName("campaign_guidance")
    @set:PropertyName("campaign_guidance")
    var campaignGuidance: String = "",

    @get:PropertyName("target_audience")
    @set:PropertyName("target_audience")
    var targetAudience: String = "",

    @get:PropertyName("tone")
    @set:PropertyName("tone")
    var toneOfVoice: String = "professional",

    var status: String = CampaignStatus.ACTIVE.id,

    @get:PropertyName("schema_type")
    @set:PropertyName("schema_type")
    var schemaType: String = "daily",

    /** Límite de posts a generar por cada esquema/día */
    @get:PropertyName("max_posts")
    @set:PropertyName("max_posts")
    var maxPosts: Int = 3,

    @get:PropertyName("auto_approve_schema")
    @set:PropertyName("auto_approve_schema")
    var autoApproveSchema: Boolean = false,

    @get:PropertyName("auto_approve_content")
    @set:PropertyName("auto_approve_content")
    var autoApproveContent: Boolean = false,

    @get:PropertyName("review_timeout_hours")
    @set:PropertyName("review_timeout_hours")
    var reviewHours: Int = 2,

    @get:PropertyName("content_quality_threshold")
    @set:PropertyName("content_quality_threshold")
    var qualityThreshold: Float = 0.7f,

    @get:PropertyName("max_context_tokens")
    @set:PropertyName("max_context_tokens")
    var maxContextTokens: Int = 2000,

    @get:PropertyName("theme_novelty_strictness")
    @set:PropertyName("theme_novelty_strictness")
    var noveltyStrictness: String = "medium",

    @get:PropertyName("allowed_post_types")
    @set:PropertyName("allowed_post_types")
    var allowedPostTypes: List<String> = emptyList(),

    // Relaciones (Pobladas localmente)
    var schemes: List<SchemeFirestore> = emptyList(),
    var posts: List<ContentPost> = emptyList(),

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "channel_id" to channelId,
        "platform" to platform?.id,
        "user_id" to userId,
        "name" to name,
        "description" to description,
        "objective" to objective,
        "theme_focus" to themeFocus,
        "campaign_guidance" to campaignGuidance,
        "target_audience" to targetAudience,
        "tone" to toneOfVoice,
        "status" to status,
        "schema_type" to schemaType,
        "max_posts" to maxPosts,
        "auto_approve_schema" to autoApproveSchema,
        "auto_approve_content" to autoApproveContent,
        "review_timeout_hours" to reviewHours,
        "content_quality_threshold" to qualityThreshold,
        "max_context_tokens" to maxContextTokens,
        "theme_novelty_strictness" to noveltyStrictness,
        "allowed_post_types" to allowedPostTypes,
        "created_at" to createdAt,
        "updated_at" to updatedAt
    )
}
