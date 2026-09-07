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

import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.model.FacebookPage
import com.ejsstudios.socialhub.firebase.db.model.FacebookPicture
import com.ejsstudios.socialhub.firebase.db.model.FacebookPictureData
import com.ejsstudios.socialhub.firebase.db.model.FacebookPublishResponse
import com.ejsstudios.socialhub.firebase.db.model.FacebookUser
import com.ejsstudios.socialhub.firebase.db.model.PostMetrics
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.firebase.db.model.toFirestore
import com.ejsstudios.socialhub.firebase.login.AuthManager
import com.ejsstudios.socialhub.utils.Constants.COLLECTION_USERS

open class FacebookRepository(
    private val apiService: FacebookApiService,
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager
) {

    open suspend fun syncAndLinkFacebook(accessToken: String): Result<Boolean> {
        val currentUser =
            authManager.currentUser ?: return Result.failure(Exception("No hay sesión activa"))
        return try {
            authManager.linkFacebook(accessToken)

            getUserInfo(accessToken).fold(
                onSuccess = { fbUser ->
                    val userDoc = firestoreManager.getDocumentById(
                        COLLECTION_USERS,
                        currentUser.uid
                    ) { it.toObject(UserFirestore::class.java) }
                    val updatedUser =
                        (userDoc ?: currentUser.toFirestore()).copy(authExternalId = fbUser.id)
                    firestoreManager.updateDocument(
                        COLLECTION_USERS,
                        updatedUser.id,
                        mapOf("auth_external_id" to fbUser.id)
                    )
                    Result.success(true)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun getUserInfo(accessToken: String): Result<FacebookUser> {
        return try {
            val user = apiService.getUserInfo(accessToken = accessToken)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    open suspend fun getUserPages(
        accessToken: String,
        userId: String
    ): Result<List<SocialMediaFirestore>> {
        return try {
            val response = apiService.getUserPages(accessToken = accessToken)
            val pages = response.data.map { it.toFirestore(userId) }
            Result.success(pages)
        } catch (e: Exception) {
            Loggers.log("e", "FacebookRepository", "Error obteniendo páginas: ${e.message}")
            Result.failure(e)
        }
    }

    open suspend fun publishPost(
        channelid: String,
        message: String,
        pageAccessToken: String,
        published: Boolean? = null,
        scheduledPublishTime: Long? = null
    ): Result<FacebookPublishResponse> {
        return try {
            val response = apiService.publishPost(
                channelid = channelid,
                message = message,
                pageAccessToken = pageAccessToken,
                published = published,
                scheduledPublishTime = scheduledPublishTime
            )
            Result.success(response)
        } catch (e: Exception) {
            Loggers.log("e", "FacebookRepository", "Error publicando post: ${e.message}")
            Result.failure(e)
        }
    }

    open suspend fun publishPhoto(
        channelid: String,
        url: String,
        caption: String,
        pageAccessToken: String,
        published: Boolean? = null,
        scheduledPublishTime: Long? = null
    ): Result<FacebookPublishResponse> {
        return try {
            val response = apiService.publishPhoto(
                channelid = channelid,
                url = url,
                caption = caption,
                pageAccessToken = pageAccessToken,
                published = published,
                scheduledPublishTime = scheduledPublishTime
            )
            Result.success(response)
        } catch (e: Exception) {
            Loggers.log("e", "FacebookRepository", "Error publicando foto: ${e.message}")
            Result.failure(e)
        }
    }

    open suspend fun getPostInsights(postId: String, pageAccessToken: String): Result<PostMetrics> {
        return try {
            val response =
                apiService.getPostInsights(postId = postId, pageAccessToken = pageAccessToken)
            val metrics = PostMetrics(
                likes = response.reactions?.summary?.totalCount ?: 0,
                comments = response.comments?.summary?.totalCount ?: 0,
                shares = response.shares?.count ?: 0
            )
            Result.success(metrics)
        } catch (e: Exception) {
            Loggers.log("e", "FacebookRepository", "Error obteniendo insights: ${e.message}")
            Result.failure(e)
        }
    }
}

class MockFacebookRepository(
    apiService: FacebookApiService,
    authManager: AuthManager,
    firestoreManager: FirestoreManager
) : FacebookRepository(apiService, authManager, firestoreManager) {

    override suspend fun getUserPages(
        accessToken: String,
        userId: String
    ): Result<List<SocialMediaFirestore>> {
        return Result.success(
            listOf(
                FacebookPage(
                    id = "page1",
                    name = "Mock Page 1",
                    category = "Entertainment",
                    picture = FacebookPicture(
                        data = FacebookPictureData(
                            url = "https://example.com/mock_page1.jpg"
                        )
                    ),
                    tasks = listOf("ANALYZE", "ADVERTISE")
                ).toFirestore(userId),
                FacebookPage(
                    id = "page2",
                    name = "Mock Page 2",
                    category = "Business",
                    picture = FacebookPicture(
                        data = FacebookPictureData(
                            url = "https://example.com/mock_page2.jpg"
                        )
                    ),
                    tasks = listOf("MANAGE")
                ).toFirestore(userId)
            )
        )
    }
}
