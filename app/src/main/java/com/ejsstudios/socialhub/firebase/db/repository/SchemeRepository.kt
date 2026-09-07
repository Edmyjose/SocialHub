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
import com.ejsstudios.socialhub.firebase.db.local.dao.SchemeDao
import com.ejsstudios.socialhub.firebase.db.model.SchemeFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de Esquemas. 
 * Lectura desde Room, Escritura a Firestore.
 */
class SchemeRepository(
    private val schemeDao: SchemeDao
) {

    private fun getPath(userId: String, pageId: String, campaignId: String) =
        "users/$userId/socialhub/$pageId/campaigns/$campaignId/schemes"

    /**
     * Guarda un esquema: Solo escribimos en Firestore.
     */
    suspend fun saveScheme(
        userId: String,
        pageId: String,
        campaignId: String,
        scheme: SchemeFirestore
    ): Result<String> {
        return FirestoreManager.addDocument(
            collection = getPath(userId, pageId, campaignId),
            data = scheme.toMap(),
            id = scheme.id
        )
    }

    /**
     * Actualiza un esquema existente en Firestore.
     */
    suspend fun updateScheme(
        userId: String,
        pageId: String,
        campaignId: String,
        scheme: SchemeFirestore
    ): Result<Unit> {
        return FirestoreManager.updateDocument(
            collection = getPath(userId, pageId, campaignId),
            documentId = scheme.id,
            updatedData = scheme.toMap()
        )
    }

    suspend fun deleteScheme(
        userId: String,
        pageId: String,
        campaignId: String,
        schemeId: String
    ): Result<Unit> {
        return FirestoreManager.deleteDocument(
            collection = getPath(userId, pageId, campaignId),
            documentId = schemeId
        )
    }

    fun getSchemesByCampaignFlow(campaignId: String): Flow<List<SchemeFirestore>> {
        return schemeDao.getSchemesByCampaignIdFlow(campaignId).map { list ->
            list.map { it.toFirestore() }
        }
    }

    suspend fun getSchemeByDate(campaignId: String, date: String): SchemeFirestore? {
        return schemeDao.getSchemeByDate(campaignId, date)?.toFirestore()
    }
}
