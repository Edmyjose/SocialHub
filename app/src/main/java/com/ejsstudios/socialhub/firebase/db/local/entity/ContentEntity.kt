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
import com.ejsstudios.socialhub.firebase.db.model.ContentPost
import com.ejsstudios.socialhub.firebase.db.model.ContentStatus
import com.ejsstudios.socialhub.firebase.db.model.SocialPlatform

@Entity(tableName = "contents")
data class ContentEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val platform: SocialPlatform?,
    val campaignId: String,
    val campaignName: String,
    val schemeId: String,
    val schemeName: String,
    val userId: String,
    val title: String,
    val copyText: String,
    val imageUrl: String?,
    val imageUrls: List<String>,
    val imageIds: List<String>,
    val status: ContentStatus,
    val estimatedTime: String?,
    val scheduledTimestamp: Long?,
    val facebookPostId: String?,
    val postUrl: String?,
    val errorMessage: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toFirestore() = ContentPost(
        id = id,
        channelId = channelId,
        platform = platform,
        campaignId = campaignId,
        campaignName = campaignName,
        schemeId = schemeId,
        schemeName = schemeName,
        userId = userId,
        title = title,
        copyText = copyText,
        imageUrl = imageUrl,
        imageUrls = imageUrls,
        imageIds = imageIds,
        status = status,
        estimatedTime = estimatedTime,
        scheduledTimestamp = scheduledTimestamp,
        facebookPostId = facebookPostId,
        postUrl = postUrl,
        errorMessage = errorMessage,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun ContentPost.toEntity() = ContentEntity(
    id = id,
    channelId = channelId,
    platform = platform,
    campaignId = campaignId,
    campaignName = campaignName,
    schemeId = schemeId,
    schemeName = schemeName,
    userId = userId,
    title = title,
    copyText = copyText,
    imageUrl = imageUrl,
    imageUrls = imageUrls,
    imageIds = imageIds,
    status = status,
    estimatedTime = estimatedTime,
    scheduledTimestamp = scheduledTimestamp,
    facebookPostId = facebookPostId,
    postUrl = postUrl,
    errorMessage = errorMessage,
    createdAt = createdAt,
    updatedAt = updatedAt
)


