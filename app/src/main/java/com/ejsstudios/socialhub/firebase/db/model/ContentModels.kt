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
 * Estado de una publicación.
 */
@Keep
enum class ContentStatus(val id: String, val stringRes: Int) {
    DRAFT("draft", R.string.status_content_draft),
    READY("ready", R.string.status_content_ready),
    SCHEDULED("scheduled", R.string.status_content_scheduled),
    PUBLISHED("published", R.string.status_content_published),
    FAILED("failed", R.string.status_content_failed);

    companion object {
        fun fromId(id: String): ContentStatus = entries.find { it.id == id } ?: DRAFT

        fun fromDisplayName(name: String): ContentStatus {
            return entries.find { it.id.equals(name, ignoreCase = true) }
                ?: when (name.lowercase()) {
                    "draft", "borrador" -> DRAFT
                    "ready", "listo" -> READY
                    "scheduled", "programado" -> SCHEDULED
                    "published", "publicado" -> PUBLISHED
                    "failed", "fallido" -> FAILED
                    else -> DRAFT
                }
        }
    }
}

/**
 * Modelo de dominio y Firestore para un Post / Publicación generada.
 *
 * Firestore path: users/{userId}/contents/{contentId}
 *
 * Este modelo vincula el post a su página de origen, campaña y esquema,
 * manteniendo trazabilidad y guardando el enlace directo a Facebook (postUrl).
 */
@Keep
@IgnoreExtraProperties
data class ContentPost(
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

    @get:PropertyName("scheme_id")
    @set:PropertyName("scheme_id")
    var schemeId: String = "",

    @get:PropertyName("scheme_name")
    @set:PropertyName("scheme_name")
    var schemeName: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("title")
    @set:PropertyName("title")
    var title: String = "",

    @get:PropertyName("copy_text")
    @set:PropertyName("copy_text")
    var copyText: String = "",

    // Campo de compatibilidad para Firestore antiguo
    @get:PropertyName("copyText")
    @set:PropertyName("copyText")
    var oldCopyText: String? = null,

    @get:PropertyName("image_url")
    @set:PropertyName("image_url")
    var imageUrl: String? = null,

    @get:PropertyName("image_urls")
    @set:PropertyName("image_urls")
    var imageUrls: List<String> = emptyList(),

    @get:PropertyName("image_ids")
    @set:PropertyName("image_ids")
    var imageIds: List<String> = emptyList(),

    val status: ContentStatus = ContentStatus.DRAFT,

    @get:PropertyName("estimated_time")
    @set:PropertyName("estimated_time")
    var estimatedTime: String? = null, // Formato HH:mm:ss (24h)

    @get:PropertyName("scheduled_timestamp")
    @set:PropertyName("scheduled_timestamp")
    var scheduledTimestamp: Long? = null,

    @get:PropertyName("facebook_post_id")
    @set:PropertyName("facebook_post_id")
    var facebookPostId: String? = null,

    @get:PropertyName("post_url")
    @set:PropertyName("post_url")
    var postUrl: String? = null,

    @get:PropertyName("error_message")
    @set:PropertyName("error_message")
    var errorMessage: String? = null,

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
        "scheme_id" to schemeId,
        "scheme_name" to schemeName,
        "user_id" to userId,
        "title" to title,
        "copy_text" to copyText,
        "image_url" to imageUrl,
        "image_urls" to imageUrls,
        "image_ids" to imageIds,
        "status" to status.id,
        "estimated_time" to estimatedTime,
        "scheduled_timestamp" to scheduledTimestamp,
        "facebook_post_id" to facebookPostId,
        "post_url" to postUrl,
        "error_message" to errorMessage,
        "created_at" to createdAt,
        "updated_at" to updatedAt
    )
}
