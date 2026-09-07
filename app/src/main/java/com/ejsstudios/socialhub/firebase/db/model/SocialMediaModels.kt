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

package com.ejsstudios.socialhub.firebase.db.model

import androidx.annotation.Keep
import com.ejsstudios.socialhub.R
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName


/**
 * Plataformas soportadas por la aplicación.
 */
@Keep
enum class SocialPlatform(val id: String, val stringRes: Int) {
    FACEBOOK_PAGE("facebook_page", R.string.platform_facebook_page),
    FACEBOOK_GROUP("facebook_group", R.string.platform_facebook_group),
    INSTAGRAM("instagram", R.string.platform_instagram),
    X_TWITTER("x_twitter", R.string.platform_x_twitter),
    THREADS("threads", R.string.platform_threads),
    REDDIT("reddit", R.string.platform_reddit);

    companion object {
        fun fromId(id: String): SocialPlatform = entries.find { it.id == id } ?: FACEBOOK_PAGE

        fun fromDisplayName(name: String): SocialPlatform {
            return entries.find { it.id.equals(name, ignoreCase = true) }
                ?: when (name.lowercase()) {
                    "facebook", "facebook page" -> FACEBOOK_PAGE
                    "facebook group" -> FACEBOOK_GROUP
                    "instagram" -> INSTAGRAM
                    "x / twitter", "twitter", "x_twitter" -> X_TWITTER
                    "threads" -> THREADS
                    "reddit" -> REDDIT
                    else -> FACEBOOK_PAGE
                }
        }
    }
}

/**
 * Modelo de dominio y Firestore para una Cuenta de Red Social.
 */

@Keep
@IgnoreExtraProperties
data class SocialMediaFirestore(
    var id: String = "",

    @get:PropertyName("user_id")
    @set:PropertyName("user_id")
    var userId: String = "",

    @get:PropertyName("platform")
    @set:PropertyName("platform")
    var platform: String = SocialPlatform.FACEBOOK_PAGE.id,

    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    @get:PropertyName("access_token")
    @set:PropertyName("access_token")
    var accessToken: String? = null,

    @get:PropertyName("category")
    @set:PropertyName("category")
    var category: String? = null,

    @get:PropertyName("picture")
    @set:PropertyName("picture")
    var picture: String? = null,

    @get:PropertyName("tasks")
    @set:PropertyName("tasks")
    var tasks: List<String> = emptyList(),

    @get:PropertyName("isActive")
    @set:PropertyName("isActive")
    var isActive: Boolean = true,

    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis(),

    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "user_id" to userId,
        "platform" to platform,
        "name" to name,
        "access_token" to accessToken,
        "category" to category,
        "picture" to picture,
        "tasks" to tasks,
        "isActive" to isActive,
        "created_at" to createdAt,
        "updated_at" to updatedAt
    )
}