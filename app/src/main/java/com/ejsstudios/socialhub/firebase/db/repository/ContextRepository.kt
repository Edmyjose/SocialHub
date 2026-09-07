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

import com.ejsstudios.socialhub.apiservices.EmbeddingApiService
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ContextSettingException
import com.ejsstudios.socialhub.firebase.CloudStorageManager
import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.local.dao.ContextDao
import com.ejsstudios.socialhub.firebase.db.local.entity.toDomain
import com.ejsstudios.socialhub.firebase.db.model.ChannelAIContext
import com.ejsstudios.socialhub.firebase.db.model.ChunkData
import com.ejsstudios.socialhub.firebase.db.model.ChunkWithSimilarity
import com.ejsstudios.socialhub.firebase.db.model.ContextMedia
import com.ejsstudios.socialhub.firebase.db.model.ContextMediaType
import com.ejsstudios.socialhub.firebase.db.model.SimilaritySearchRequest
import com.ejsstudios.socialhub.managers.WebStorageResponse
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

/**
 * Modelo de datos para el contexto completo de una página (SSOT local).
 */
data class CompletePageContext(
    val context: ChannelAIContext,
    val images: List<ContextMedia> = emptyList(),
    val videos: List<ContextMedia> = emptyList()
) {
    val hasContextFile: Boolean get() = context.contextFileUrl.isNotBlank()
    val hasImages: Boolean get() = images.isNotEmpty()
    val hasVideos: Boolean get() = videos.isNotEmpty()
}

/**
 * Repositorio de Contexto IA.
 * 
 * Gestiona la configuración de entrenamiento y material multimedia.
 * Sigue el principio Offline-First: lectura desde Room, escritura a Firestore/Storage.
 */
class ContextRepository(
    private val contextDao: ContextDao
) {

    companion object {
        private const val TAG = "ContextRepository"
    }

    /**
     * Sube un archivo a Firebase Cloud Storage.
     * Soporta archivos de texto (.txt), imágenes y videos con su extensión original.
     */
    suspend fun uploadFile(
        userId: String,
        pageId: String,
        fileId: String,
        bytes: ByteArray,
        type: String,
        onProgress: (Float) -> Unit = {}
    ): WebStorageResponse {
        return try {
            onProgress(0.3f)
            val url = when (type) {
                "context_file" -> CloudStorageManager.uploadContextFile(userId, pageId, bytes)
                "image", "video" -> CloudStorageManager.uploadContextMedia(
                    userId,
                    pageId,
                    fileId,
                    bytes
                )

                else -> throw Exception("Tipo no soportado: $type")
            }
            onProgress(1.0f)
            WebStorageResponse.Success(url)
        } catch (e: Exception) {
            onProgress(0f)
            WebStorageResponse.Error(ContextSettingException.UploadFailed(fileId, e))
        }
    }

    /**
     * Obtiene el contexto completo de una página observando únicamente Room.
     * La sincronización desde Firestore es gestionada por RealtimeSyncManager.
     */
    fun getCompletePageContextFlow(userId: String, pageId: String): Flow<CompletePageContext?> {
        Loggers.log("d", TAG, "Observando Room para contexto de página: $pageId")

        return combine(
            contextDao.getConfigBychannelIdFlow(pageId),
            contextDao.getMediaBychannelIdFlow(pageId)
        ) { configEntity, mediaEntities ->
            val context = configEntity?.toDomain() ?: return@combine null

            CompletePageContext(
                context = context,
                images = mediaEntities.map { it.toDomain() }
                    .filter { it.type == ContextMediaType.IMAGE },
                videos = mediaEntities.map { it.toDomain() }
                    .filter { it.type == ContextMediaType.VIDEO }
            )
        }
    }

    /**
     * Guarda la configuración de contexto (config) únicamente en Firestore.
     */
    suspend fun savePageContext(pageAIContext: ChannelAIContext): Result<String> {
        return FirestoreManager.addDocumentToSubcollection(
            userId = pageAIContext.userId,
            subcollection = "socialhub/${pageAIContext.channelId}/context",
            data = pageAIContext.toMap(),
            docId = "config"
        )
    }

    /**
     * Guarda un archivo de media únicamente en Firestore.
     */
    suspend fun saveContextMedia(contextMedia: ContextMedia): Result<String> {
        return FirestoreManager.addDocumentToSubcollection(
            userId = contextMedia.userId,
            subcollection = "socialhub/${contextMedia.channelId}/media",
            data = contextMedia.toMap(),
            docId = contextMedia.id
        )
    }

    /**
     * Actualiza la descripción y embedding de un media en Firestore.
     */
    suspend fun updateContextMediaDescription(
        media: ContextMedia,
        description: String,
        embedding: List<Float>
    ): Result<Unit> {
        val updatedData = media.copy(description = description, embedding = embedding).toMap()
        return FirestoreManager.updateSubcollectionDocument(
            parentDocumentId = media.userId,
            subcollection = "socialhub/${media.channelId}/media",
            documentId = media.id,
            updatedData = updatedData
        )
    }

    /**
     * Elimina el archivo de contexto de Firestore y Storage.
     */
    suspend fun deleteContextFile(userId: String, pageId: String, fileName: String): Result<Unit> {
        FirestoreManager.deleteSubcollectionDocument(
            userId,
            "socialhub/$pageId/context",
            "config"
        )
        val success = CloudStorageManager.deleteContextFile(userId, pageId)
        return if (success) Result.success(Unit) else Result.failure(Exception("Fallo al borrar en Storage"))
    }

    /**
     * Elimina un media completamente de Firestore y Cloud Storage.
     */
    suspend fun deleteContextMediaComplete(
        userId: String,
        pageId: String,
        mediaId: String,
        fileName: String,
        type: String
    ): Result<Unit> {
        FirestoreManager.deleteSubcollectionDocument(userId, "socialhub/$pageId/media", mediaId)
        val success = CloudStorageManager.deleteContextMedia(userId, pageId, fileName)
        return if (success) Result.success(Unit) else Result.failure(Exception("Fallo al borrar media en Storage"))
    }

    /**
     * Guarda los fragmentos (chunks) en Firestore.
     * Estos datos son pesados y no se sincronizan a Room automáticamente.
     */
    suspend fun saveContextChunks(
        userId: String,
        contextId: String,
        chunks: List<ChunkData>
    ): Result<Unit> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            val batch = firestore.batch()
            chunks.forEach { chunk ->
                val ref = firestore.collection("users").document(userId)
                    .collection("socialmedia").document(contextId)
                    .collection("context").document("config")
                    .collection("chunks").document(chunk.chunkId)
                batch.set(ref, chunk.toMap())
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Recupera los fragmentos de contexto bajo demanda desde Firestore.
     */
    suspend fun getContextChunks(
        userId: String,
        contextId: String,
        limit: Int? = 100
    ): Result<List<ChunkData>> {
        return try {
            val firestore = FirebaseFirestore.getInstance()
            var query = firestore.collection("users").document(userId)
                .collection("socialmedia").document(contextId)
                .collection("context").document("config")
                .collection("chunks").orderBy("metadata.position")
            if (limit != null) query = query.limit(limit.toLong())
            val snapshot = query.get().await()
            val chunks = snapshot.documents.mapNotNull { it.toObject(ChunkData::class.java) }
            Result.success(chunks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Realiza una búsqueda semántica de fragmentos relevantes.
     */
    suspend fun searchSimilarChunks(
        userId: String,
        contextId: String,
        query: String,
        topK: Int = 3,
        embeddingApi: EmbeddingApiService
    ): Result<List<ChunkWithSimilarity>> {
        return try {
            val chunksResult = getContextChunks(userId, contextId, limit = null)
            val chunks = chunksResult.getOrNull() ?: return Result.success(emptyList())
            val request = SimilaritySearchRequest(query, contextId, userId, chunks, topK)
            val response =
                withTimeout(30000.milliseconds) { embeddingApi.searchSimilarChunks(request) }
            if (response.success) Result.success(response.results) else Result.failure(Exception("Search failed"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
