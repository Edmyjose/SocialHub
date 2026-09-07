/*
 * Copyright (c) 2026. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 */

package com.ejsstudios.socialhub.firebase.db.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.ejsstudios.socialhub.firebase.db.model.CampaignFirestore
import com.ejsstudios.socialhub.firebase.db.model.SocialPlatform

@Entity(tableName = "campaigns")
data class CampaignEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val platform: SocialPlatform?,
    val userId: String,
    val name: String,
    val description: String,
    val objective: String,
    val themeFocus: String,
    val campaignGuidance: String,
    val targetAudience: String,
    val toneOfVoice: String,
    val status: String,
    val schemaType: String,
    val maxPosts: Int,
    val autoApproveSchema: Boolean,
    val autoApproveContent: Boolean,
    val reviewHours: Int,
    val qualityThreshold: Float,
    val maxContextTokens: Int,
    val noveltyStrictness: String,
    val allowedPostTypes: List<String>,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toFirestore() = CampaignFirestore(
        id = id,
        channelId = channelId,
        platform = platform,
        userId = userId,
        name = name,
        description = description,
        objective = objective,
        themeFocus = themeFocus,
        campaignGuidance = campaignGuidance,
        targetAudience = targetAudience,
        toneOfVoice = toneOfVoice,
        status = status,
        schemaType = schemaType,
        maxPosts = maxPosts,
        autoApproveSchema = autoApproveSchema,
        autoApproveContent = autoApproveContent,
        reviewHours = reviewHours,
        qualityThreshold = qualityThreshold,
        maxContextTokens = maxContextTokens,
        noveltyStrictness = noveltyStrictness,
        allowedPostTypes = allowedPostTypes,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun CampaignFirestore.toEntity() = CampaignEntity(
    id = id,
    channelId = channelId,
    platform = platform,
    userId = userId,
    name = name,
    description = description,
    objective = objective,
    themeFocus = themeFocus,
    campaignGuidance = campaignGuidance,
    targetAudience = targetAudience,
    toneOfVoice = toneOfVoice,
    status = status,
    schemaType = schemaType,
    maxPosts = maxPosts,
    autoApproveSchema = autoApproveSchema,
    autoApproveContent = autoApproveContent,
    reviewHours = reviewHours,
    qualityThreshold = qualityThreshold,
    maxContextTokens = maxContextTokens,
    noveltyStrictness = noveltyStrictness,
    allowedPostTypes = allowedPostTypes,
    createdAt = createdAt,
    updatedAt = updatedAt
)


/**
 * Clase POJO para Room que combina la Campaña con sus listas relacionadas.
 */
data class CampaignWithDetails(
    @Embedded val campaign: CampaignEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "campaignId"
    )
    val schemes: List<SchemeEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "campaignId"
    )
    val posts: List<ContentEntity>
) {
    fun toFirestore(): CampaignFirestore {
        val domain = campaign.toFirestore()
        return domain.copy(
            schemes = schemes.map { it.toFirestore() },
            posts = posts.map { it.toFirestore() }
        )
    }
}

