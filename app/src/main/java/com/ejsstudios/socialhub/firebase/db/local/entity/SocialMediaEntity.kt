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

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore

@Keep
@Entity(tableName = "socialmedia")
data class SocialMediaEntity(
    @PrimaryKey val id: String,
    val name: String,
    val platform: String,
    val accessToken: String?,
    val category: String?,
    val picture: String?,
    val tasks: List<String>,
    val userId: String,
    val isActive: Boolean,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toFirestore() = SocialMediaFirestore(
        id = id,
        userId = userId,
        platform = platform,
        name = name,
        accessToken = accessToken,
        category = category,
        picture = picture,
        tasks = tasks,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun SocialMediaFirestore.toEntity() = SocialMediaEntity(
    id = id,
    name = name,
    platform = platform,
    accessToken = accessToken,
    category = category,
    picture = picture,
    tasks = tasks,
    userId = userId,
    isActive = isActive,
    createdAt = createdAt,
    updatedAt = updatedAt
)

