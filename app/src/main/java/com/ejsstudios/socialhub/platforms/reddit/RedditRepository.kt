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

import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.model.RedditUser
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.firebase.db.model.toFirestore
import com.ejsstudios.socialhub.firebase.login.AuthManager
import com.ejsstudios.socialhub.utils.Constants.COLLECTION_USERS

open class RedditRepository(
    private val apiService: RedditApiService,
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager
) {

    private val TAG = "RedditRepository"

    /**
     * Sincroniza la información de Reddit con el perfil del usuario en Firestore.
     */
    open suspend fun syncAndLinkReddit(accessToken: String): Result<Boolean> {
        val currentUser =
            authManager.currentUser ?: return Result.failure(Exception("No hay sesión activa"))
        return try {
            val bearerToken = "Bearer $accessToken"
            getUserInfo(bearerToken).fold(
                onSuccess = { redditUser ->
                    val userDoc = firestoreManager.getDocumentById(
                        COLLECTION_USERS,
                        currentUser.uid
                    ) { it.toObject(UserFirestore::class.java) }

                    val updatedUser =
                        (userDoc ?: currentUser.toFirestore()).copy(authExternalId = redditUser.id)

                    firestoreManager.updateDocument(
                        COLLECTION_USERS,
                        updatedUser.id,
                        mapOf("auth_external_id" to redditUser.id)
                    )
                    Result.success(true)
                },
                onFailure = { Result.failure(it) }
            )
        } catch (e: Exception) {
            Loggers.log("e", TAG, "Error en syncAndLinkReddit: ${e.message}")
            Result.failure(e)
        }
    }

    open suspend fun getUserInfo(bearerToken: String): Result<RedditUser> {
        return try {
            val user = apiService.getUserInfo(bearerToken = bearerToken)
            Result.success(user)
        } catch (e: Exception) {
            Loggers.log("e", TAG, "Error obteniendo info de Reddit: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Obtiene los subreddits del usuario (canales de Reddit).
     */
    open suspend fun getUserSubreddits(
        accessToken: String,
        userId: String
    ): Result<List<SocialMediaFirestore>> {
        return try {
            val bearerToken = "Bearer $accessToken"
            val response = apiService.getMineSubreddits(bearerToken = bearerToken)
            val subreddits = response.data.children.map { it.data.toFirestore(userId) }
            Result.success(subreddits)
        } catch (e: Exception) {
            Loggers.log("e", TAG, "Error obteniendo subreddits: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Publica un contenido en un subreddit.
     */
    open suspend fun submitPost(
        accessToken: String,
        subreddit: String,
        kind: String,
        title: String,
        text: String? = null,
        url: String? = null
    ): Result<Unit> {
        return try {
            val bearerToken = "Bearer $accessToken"
            apiService.submitPost(
                bearerToken = bearerToken,
                subreddit = subreddit,
                kind = kind,
                title = title,
                text = text,
                url = url
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Loggers.log("e", TAG, "Error enviando post a Reddit: ${e.message}")
            Result.failure(e)
        }
    }
}
