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

package com.ejsstudios.socialhub.platforms.reddit

import com.ejsstudios.socialhub.firebase.db.model.RedditSubredditsResponse
import com.ejsstudios.socialhub.firebase.db.model.RedditUser
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface RedditApiService {

    /**
     * Obtiene la información del usuario autenticado.
     */
    @GET("api/v1/me")
    suspend fun getUserInfo(
        @Header("Authorization") bearerToken: String,
        @Header("User-Agent") userAgent: String = "android:com.ejsstudios.socialhub:v1.0.0 (by /u/ejsstudios)"
    ): RedditUser

    /**
     * Obtiene los subreddits donde el usuario es moderador o suscriptor.
     */
    @GET("subreddits/mine/subscriber")
    suspend fun getMineSubreddits(
        @Header("Authorization") bearerToken: String,
        @Header("User-Agent") userAgent: String = "android:com.ejsstudios.socialhub:v1.0.0 (by /u/ejsstudios)"
    ): RedditSubredditsResponse

    /**
     * Publica un post en un subreddit.
     */
    @FormUrlEncoded
    @POST("api/submit")
    suspend fun submitPost(
        @Header("Authorization") bearerToken: String,
        @Field("sr") subreddit: String,
        @Field("kind") kind: String, // "self", "link", "image"
        @Field("title") title: String,
        @Field("text") text: String? = null,
        @Field("url") url: String? = null,
        @Header("User-Agent") userAgent: String = "android:com.ejsstudios.socialhub:v1.0.0 (by /u/ejsstudios)"
    )

    companion object {
        const val BASE_URL = "https://oauth.reddit.com/"
    }
}
