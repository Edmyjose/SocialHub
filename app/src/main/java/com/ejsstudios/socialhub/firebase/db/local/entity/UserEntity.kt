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
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val authProvider: String,
    val authExternalId: String?,
    val name: String,
    val email: String?,
    val picture: String?,
    val credits: Int,
    val subscriptionTier: String,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain() = UserFirestore(
        id = id,
        authProvider = authProvider,
        authExternalId = authExternalId,
        name = name,
        email = email,
        picture = picture,
        credits = credits,
        subscriptionTier = subscriptionTier,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun UserFirestore.toEntity() = UserEntity(
    id = id,
    authProvider = authProvider,
    authExternalId = authExternalId,
    name = name,
    email = email,
    picture = picture,
    credits = credits,
    subscriptionTier = subscriptionTier,
    createdAt = createdAt,
    updatedAt = updatedAt
)

