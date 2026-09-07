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

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ejsstudios.socialhub.firebase.db.model.PostIdea
import com.ejsstudios.socialhub.firebase.db.model.SchemeFirestore
import com.ejsstudios.socialhub.firebase.db.model.SchemeStatus
import com.ejsstudios.socialhub.firebase.db.model.SchemeType
import com.ejsstudios.socialhub.firebase.db.model.SocialPlatform

@Entity(tableName = "schemes")
data class SchemeEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val platform: SocialPlatform?,
    val campaignId: String,
    val campaignName: String,
    val userId: String,
    val name: String,
    val description: String,
    val schemeType: SchemeType,
    val narrativeStructure: String,
    val callToAction: String,
    val imageGuidelines: String,
    val targetDate: String,
    val status: SchemeStatus,
    val postIdeas: List<PostIdea>,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toFirestore() = SchemeFirestore(
        id = id,
        channelId = channelId,
        platform = platform,
        campaignId = campaignId,
        campaignName = campaignName,
        userId = userId,
        name = name,
        description = description,
        schemeType = schemeType,
        narrativeStructure = narrativeStructure,
        callToAction = callToAction,
        imageGuidelines = imageGuidelines,
        targetDate = targetDate,
        status = status,
        postIdeas = postIdeas,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SchemeFirestore.toEntity() = SchemeEntity(
    id = id,
    channelId = channelId,
    platform = platform,
    campaignId = campaignId,
    campaignName = campaignName,
    userId = userId,
    name = name,
    description = description,
    schemeType = schemeType,
    narrativeStructure = narrativeStructure,
    callToAction = callToAction,
    imageGuidelines = imageGuidelines,
    targetDate = targetDate,
    status = status,
    postIdeas = postIdeas,
    createdAt = createdAt,
    updatedAt = updatedAt
)

