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
import com.google.firebase.firestore.PropertyName
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelo para recibir la respuesta directa de Facebook Graph API
 */
@Keep
@JsonClass(generateAdapter = true)
data class FacebookUser(
    val id: String = "",
    val name: String = "",
    val email: String? = null,
    val picture: FacebookPicture? = null
)

// --- MODELOS DE API PARA PÁGINAS ---

@Keep
@JsonClass(generateAdapter = true)
data class FacebookPicture(
    val data: FacebookPictureData = FacebookPictureData()
)

@Keep
@JsonClass(generateAdapter = true)
data class FacebookPictureData(
    val url: String = ""
)

@Keep
@JsonClass(generateAdapter = true)
data class FacebookPagesResponse(
    val data: List<FacebookPage> = emptyList()
)

/**
 * Modelo que mapea la respuesta de Facebook API
 */
@Keep
@JsonClass(generateAdapter = true)
data class FacebookPage(
    val id: String = "",
    val name: String = "",
    @Json(name = "access_token")
    @get:PropertyName("access_token")
    val accessToken: String? = null,
    val category: String? = null,
    val picture: FacebookPicture? = null,
    val tasks: List<String> = emptyList()
) {
    fun toFirestore(userId: String): SocialMediaFirestore {
        return SocialMediaFirestore(
            id = this.id,
            userId = userId,
            name = this.name,
            platform = SocialPlatform.FACEBOOK_PAGE.id,
            accessToken = this.accessToken,
            category = this.category,
            picture = this.picture?.data?.url,
            tasks = this.tasks
        )
    }
}

@Keep
@JsonClass(generateAdapter = true)
data class FacebookPublishResponse(
    val id: String = "",
    @Json(name = "post_id") val postId: String? = null
)

// --- MODELOS PARA ESTADÍSTICAS (Insights) ---

@Keep
@JsonClass(generateAdapter = true)
data class FacebookPostInsightsResponse(
    val id: String = "",
    val reactions: FacebookSummary? = null,
    val comments: FacebookSummary? = null,
    val shares: FacebookShares? = null
)

@Keep
@JsonClass(generateAdapter = true)
data class FacebookSummary(
    val summary: FacebookSummaryData = FacebookSummaryData()
)

@Keep
@JsonClass(generateAdapter = true)
data class FacebookSummaryData(
    @Json(name = "total_count") val totalCount: Int = 0
)

@Keep
@JsonClass(generateAdapter = true)
data class FacebookShares(
    val count: Int = 0
)

/**
 * Modelo de UI para mostrar métricas simplificadas.
 */
data class PostMetrics(
    val likes: Int = 0,
    val comments: Int = 0,
    val shares: Int = 0
)
