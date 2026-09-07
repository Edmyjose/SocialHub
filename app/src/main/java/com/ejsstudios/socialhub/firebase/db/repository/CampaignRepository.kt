/*
 * Copyright (c) 2025-2026. EJS Studios. Todos los derechos reservados.
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
import com.ejsstudios.socialhub.firebase.db.local.dao.CampaignDao
import com.ejsstudios.socialhub.firebase.db.model.CampaignFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio de Campañas. 
 * Lectura desde Room, Escritura a Firestore.
 * La sincronización automática es gestionada por RealtimeSyncManager.
 */
class CampaignRepository(
    private val campaignDao: CampaignDao
) {

    private fun getPath(userId: String, pageId: String) =
        "users/$userId/socialhub/$pageId/campaigns"

    /**
     * Guarda una campaña: Solo escribimos en Firestore.
     * El Listener de RealtimeSyncManager se encargará de actualizar Room.
     */
    suspend fun saveCampaign(
        userId: String,
        pageId: String,
        campaign: CampaignFirestore
    ): Result<String> {
        return FirestoreManager.addDocument(
            collection = getPath(userId, pageId),
            data = campaign.toMap(),
            id = campaign.id
        )
    }

    suspend fun updateCampaign(
        userId: String,
        pageId: String,
        campaign: CampaignFirestore
    ): Result<Unit> {
        return FirestoreManager.updateDocument(
            collection = getPath(userId, pageId),
            documentId = campaign.id,
            updatedData = campaign.toMap()
        )
    }

    suspend fun deleteCampaign(userId: String, pageId: String, campaignId: String): Result<Unit> {
        return FirestoreManager.deleteDocument(
            collection = getPath(userId, pageId),
            documentId = campaignId
        )
    }

    /**
     * Observa las campañas de una página directamente desde Room.
     */
    fun getCampaignsByPageFlow(pageId: String): Flow<List<CampaignFirestore>> {
        return campaignDao.getCampaignsByPageIdFlow(pageId).map { list ->
            list.map { it.toFirestore() }
        }
    }
}
