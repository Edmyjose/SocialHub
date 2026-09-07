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
import com.ejsstudios.socialhub.firebase.db.model.ChannelAIContext
import com.ejsstudios.socialhub.firebase.db.model.ContextMedia
import com.ejsstudios.socialhub.firebase.db.model.ContextMediaType
import com.ejsstudios.socialhub.firebase.db.model.SocialPlatform

@Entity(tableName = "context_config")
data class ContextConfigEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val platform: SocialPlatform,
    val userId: String,
    val rawContext: String,
    val smartSummary: String,
    val contextFileUrl: String,
    val fileName: String,
    val size: String,
    val createdAt: Long,
    val updatedAt: Long,
    val embedding: List<Float>
)

@Entity(tableName = "media")
data class MediaEntity(
    @PrimaryKey val id: String,
    val channelId: String,
    val platform: SocialPlatform?,
    val userId: String,
    val fileName: String,
    val remoteUrl: String?,
    val description: String,
    val type: ContextMediaType,
    val size: String,
    val createdAt: Long,
    val embedding: List<Float>,
    val usageCount: Int,
    val lastUsedDate: String?
)

fun ChannelAIContext.toEntity() = ContextConfigEntity(
    id = id,
    channelId = channelId,
    platform = platform,
    userId = userId,
    rawContext = rawContext,
    smartSummary = smartSummary,
    contextFileUrl = contextFileUrl,
    fileName = fileName,
    size = size,
    createdAt = createdAt,
    updatedAt = updatedAt,
    embedding = embedding
)

fun ContextConfigEntity.toDomain() = ChannelAIContext(
    id = id,
    channelId = channelId,
    platform = platform,
    userId = userId,
    rawContext = rawContext,
    smartSummary = smartSummary,
    contextFileUrl = contextFileUrl,
    fileName = fileName,
    size = size,
    createdAt = createdAt,
    updatedAt = updatedAt,
    embedding = embedding
)

fun ContextMedia.toEntity() = MediaEntity(
    id = id,
    channelId = channelId,
    platform = platform,
    userId = userId,
    fileName = fileName,
    remoteUrl = remoteUrl,
    description = description,
    type = type,
    size = size,
    createdAt = createdAt,
    embedding = embedding,
    usageCount = usageCount,
    lastUsedDate = lastUsedDate
)

fun MediaEntity.toDomain() = ContextMedia(
    id = id,
    channelId = channelId,
    platform = platform,
    userId = userId,
    fileName = fileName,
    remoteUrl = remoteUrl,
    description = description,
    type = type,
    size = size,
    createdAt = createdAt,
    embedding = embedding,
    usageCount = usageCount,
    lastUsedDate = lastUsedDate
)
