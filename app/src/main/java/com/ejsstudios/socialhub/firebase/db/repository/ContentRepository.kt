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
package com.ejsstudios.socialhub.firebase.db.repository

import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.local.dao.ContentDao
import com.ejsstudios.socialhub.firebase.db.model.ContentPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de Contenidos. 
 * Lectura desde Room, Escritura a Firestore.
 */
class ContentRepository(private val contentDao: ContentDao) {

    private fun getPath(userId: String, pageId: String, campaignId: String, schemeId: String) =
        "users/$userId/socialhub/$pageId/campaigns/$campaignId/schemes/$schemeId/contents"

    /**
     * Guarda una publicación: Solo escribimos en Firestore.
     */
    suspend fun savePost(
        userId: String,
        pageId: String,
        campaignId: String,
        schemeId: String,
        post: ContentPost
    ): Result<String> {
        return FirestoreManager.addDocument(
            collection = getPath(userId, pageId, campaignId, schemeId),
            data = post.toMap(),
            id = post.id
        )
    }

    suspend fun deletePost(
        userId: String,
        pageId: String,
        campaignId: String,
        schemeId: String,
        postId: String
    ): Result<Unit> {
        return FirestoreManager.deleteDocument(
            collection = getPath(userId, pageId, campaignId, schemeId),
            documentId = postId
        )
    }

    fun getPostsByPageFlow(pageId: String): Flow<List<ContentPost>> {
        return contentDao.getContentsByPageIdFlow(pageId).map { list ->
            list.map { it.toFirestore() }
        }
    }

    // --- SECCIÓN DE PUBLICACIONES (Subcolección de Contenido) ---

    private fun getPublicationPath(
        userId: String,
        pageId: String,
        campaignId: String,
        schemeId: String,
        contentId: String
    ) =
        "${getPath(userId, pageId, campaignId, schemeId)}/$contentId/publications"

    suspend fun savePublication(
        userId: String,
        pageId: String,
        campaignId: String,
        schemeId: String,
        contentId: String,
        publicationData: Map<String, Any?>,
        pubId: String? = null
    ): Result<String> {
        return FirestoreManager.addDocument(
            collection = getPublicationPath(userId, pageId, campaignId, schemeId, contentId),
            data = publicationData,
            id = pubId
        )
    }
}
