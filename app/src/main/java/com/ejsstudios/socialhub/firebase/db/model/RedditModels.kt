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
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelo para recibir la respuesta directa de Reddit API para el usuario.
 */
@Keep
@JsonClass(generateAdapter = true)
data class RedditUser(
    val id: String = "",
    val name: String = "",
    @Json(name = "snoovatar_img") val picture: String? = null,
    @Json(name = "total_karma") val karma: Int = 0
)

/**
 * Modelo que representa un Subreddit (Canal de Reddit).
 */
@Keep
@JsonClass(generateAdapter = true)
data class RedditSubreddit(
    @Json(name = "display_name") val id: String = "",
    @Json(name = "title") val name: String = "",
    @Json(name = "public_description") val description: String? = null,
    @Json(name = "community_icon") val iconUrl: String? = null,
    @Json(name = "subscribers") val subscribers: Int = 0
) {
    /**
     * Convierte el modelo de Reddit a nuestro modelo genérico de SocialMediaFirestore.
     */
    fun toFirestore(userId: String): SocialMediaFirestore {
        return SocialMediaFirestore(
            id = "reddit_$id",
            userId = userId,
            name = "/r/$id",
            platform = SocialPlatform.REDDIT.id,
            category = "Subreddit",
            picture = iconUrl?.substringBefore("?"),
            isActive = true
        )
    }
}

/**
 * Respuesta envoltorio de Reddit para listas de subreddits.
 */
@Keep
@JsonClass(generateAdapter = true)
data class RedditSubredditsResponse(
    val data: RedditDataContainer = RedditDataContainer()
)

@Keep
@JsonClass(generateAdapter = true)
data class RedditDataContainer(
    val children: List<RedditSubredditChild> = emptyList()
)

@Keep
@JsonClass(generateAdapter = true)
data class RedditSubredditChild(
    val data: RedditSubreddit
)
