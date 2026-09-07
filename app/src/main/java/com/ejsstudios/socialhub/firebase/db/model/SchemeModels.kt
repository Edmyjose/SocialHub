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
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.UUID

/**
 * Estado de aprobación de un esquema.
 */
@Keep
enum class SchemeStatus(val id: String, val stringRes: Int) {
    PENDING("pending", R.string.status_scheme_pending),
    APPROVED("approved", R.string.status_scheme_approved),
    REJECTED("rejected", R.string.status_scheme_rejected);

    companion object {
        fun fromId(id: String): SchemeStatus = entries.find { it.id == id } ?: PENDING

        fun fromDisplayName(name: String): SchemeStatus {
            return entries.find { it.id.equals(name, ignoreCase = true) }
                ?: when (name.lowercase()) {
                    "pending", "pendiente" -> PENDING
                    "approved", "aprobado" -> APPROVED
                    "rejected", "rechazado" -> REJECTED
                    else -> PENDING
                }
        }
    }
}

/**
 * Tipo de esquema narrativo para la generación de contenidos.
 */
@Keep
enum class SchemeType {
    EDUCATIONAL,    // Post Educativo de Valor
    DIRECT_SALE,    // Post de Venta Directa
    INTERACTION,    // Post de Interacción / Engagement
    SOCIAL_PROOF,   // Post de Prueba Social / Testimonio
    STORYTELLING,   // Post Narrativo / Historia
    LAUNCH,         // Post de Lanzamiento de Producto
    CUSTOM          // Esquema personalizado por el usuario
}

/**
 * Representa una idea de publicación específica dentro de un esquema.
 */
@Keep
@IgnoreExtraProperties
@JsonClass(generateAdapter = true)
data class PostIdea(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val instruction: String = "",

    @field:Json(name = "estimated_time")
    @get:PropertyName("estimated_time")
    @set:PropertyName("estimated_time")
    var estimatedTime: String = "12:00:00",

    val tone: String = "professional",

    @field:Json(name = "content_type")
    @get:PropertyName("content_type")
    @set:PropertyName("content_type")
    var contentType: String = "short_post"
)

/**
 * Modelo de dominio y Firestore para un Esquema Narrativo.
 */
@Keep
@IgnoreExtraProperties
data class SchemeFirestore(
    val id: String = UUID.randomUUID().toString(),

    @get:PropertyName("channel_id")
    @set:PropertyName("channel_id")
    var channelId: String = "",

    @get:PropertyName("platform")
    @set:PropertyName("platform")
    var platform: SocialPlatform? = null,

    @get:PropertyName("campaign_id")
    @set:PropertyName("campaign_id")
    var campaignId: String = "",

    @get:PropertyName("campaign_name")
    @set:PropertyName("campaign_name")
    var campaignName: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("description")
    @set:PropertyName("description")
    var description: String = "",

    @get:PropertyName("scheme_type")
    @set:PropertyName("scheme_type")
    var schemeType: SchemeType = SchemeType.EDUCATIONAL,

    @get:PropertyName("narrative_structure")
    @set:PropertyName("narrative_structure")
    var narrativeStructure: String = "",

    @get:PropertyName("call_to_action")
    @set:PropertyName("call_to_action")
    var callToAction: String = "",

    @get:PropertyName("image_guidelines")
    @set:PropertyName("image_guidelines")
    var imageGuidelines: String = "",

    @get:PropertyName("target_date")
    @set:PropertyName("target_date")
    var targetDate: String = "", // Formato YYYY-MM-DD

    var status: SchemeStatus = SchemeStatus.PENDING,

    @field:Json(name = "post_ideas")
    @get:PropertyName("post_ideas")
    @set:PropertyName("post_ideas")
    var postIdeas: List<PostIdea> = emptyList(),

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
        "campaign_id" to campaignId,
        "campaign_name" to campaignName,
        "user_id" to userId,
        "name" to name,
        "description" to description,
        "scheme_type" to schemeType.name,
        "narrative_structure" to narrativeStructure,
        "call_to_action" to callToAction,
        "image_guidelines" to imageGuidelines,
        "target_date" to targetDate,
        "status" to status.id,
        "post_ideas" to postIdeas.map {
            mapOf(
                "id" to it.id,
                "title" to it.title,
                "instruction" to it.instruction,
                "estimated_time" to it.estimatedTime,
                "tone" to it.tone,
                "content_type" to it.contentType
            )
        },
        "created_at" to createdAt,
        "updated_at" to updatedAt
    )

    fun getSchemeTypeDisplayName(): String {
        return when (schemeType) {
            SchemeType.EDUCATIONAL -> "📚 Educativo"
            SchemeType.DIRECT_SALE -> "💰 Venta Directa"
            SchemeType.INTERACTION -> "💬 Interacción"
            SchemeType.SOCIAL_PROOF -> "⭐ Prueba Social"
            SchemeType.STORYTELLING -> "📖 Storytelling"
            SchemeType.LAUNCH -> "🚀 Lanzamiento"
            SchemeType.CUSTOM -> "🎨 Personalizado"
        }
    }
}
