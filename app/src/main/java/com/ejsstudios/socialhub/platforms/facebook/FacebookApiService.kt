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

package com.ejsstudios.socialhub.platforms.facebook

import com.ejsstudios.socialhub.firebase.db.model.FacebookPagesResponse
import com.ejsstudios.socialhub.firebase.db.model.FacebookPostInsightsResponse
import com.ejsstudios.socialhub.firebase.db.model.FacebookPublishResponse
import com.ejsstudios.socialhub.firebase.db.model.FacebookUser
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface FacebookApiService {

    @GET("me")
    suspend fun getUserInfo(
        @Query("fields") fields: String = "id,name,email,picture.type(large)",
        @Query("access_token") accessToken: String
    ): FacebookUser

    @GET("me/accounts")
    suspend fun getUserPages(
        @Query("fields") fields: String = "id,name,access_token,category,picture.type(large),tasks",
        @Query("access_token") accessToken: String
    ): FacebookPagesResponse

    @POST("{page_id}/feed")
    suspend fun publishPost(
        @Path("channel_id") channelid: String,
        @Query("message") message: String,
        @Query("access_token") pageAccessToken: String,
        @Query("published") published: Boolean? = null,
        @Query("scheduled_publish_time") scheduledPublishTime: Long? = null
    ): FacebookPublishResponse

    @POST("{page_id}/photos")
    suspend fun publishPhoto(
        @Path("channel_id") channelid: String,
        @Query("url") url: String,
        @Query("caption") caption: String,
        @Query("access_token") pageAccessToken: String,
        @Query("published") published: Boolean? = null,
        @Query("scheduled_publish_time") scheduledPublishTime: Long? = null
    ): FacebookPublishResponse

    @GET("{post_id}")
    suspend fun getPostInsights(
        @Path("post_id") postId: String,
        @Query("fields") fields: String = "reactions.summary(true),comments.summary(true),shares",
        @Query("access_token") pageAccessToken: String
    ): FacebookPostInsightsResponse

    companion object {
        const val BASE_URL = "https://graph.facebook.com/v23.0/"
    }
}
