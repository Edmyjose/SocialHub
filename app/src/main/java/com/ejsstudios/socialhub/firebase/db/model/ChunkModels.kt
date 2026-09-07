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

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Modelos de Datos para Sistema de Chunking Inteligente
 *
 * Estos modelos soportan la comunicación Retrofit + Moshi con el backend PHP para chunking
 * inteligente de contextos y búsqueda de similitud.
 *
 * **Arquitectura:**
 * - PHP retorna embeddings como array multidimensional (NO Base64)
 * - Moshi deserializa automáticamente a List<Float>
 * - Android serializa a Base64 solo para Firestore (usando EmbeddingSerializer)
 *
 * @see com.ejsstudios.socialhub.data.remote.EmbeddingApiService
 * @see com.ejsstudios.socialhub.data.embedding.EmbeddingSerializer
 */

// ============================================================================
// REQUEST/RESPONSE: Generación de Embeddings con Chunking
// ============================================================================

/**
 * Request para generar embeddings con chunking inteligente
 *
 * Envía texto manual y contenido de archivo al backend PHP. El servidor concatena, aplica chunking
 * y genera embeddings.
 *
 * **Endpoint:** POST /api/android/generate-embeddings.php
 *
 * @property textContext Texto manual del contexto (obligatorio)
 * @property fileContent Contenido del archivo de contexto (opcional)
 * @property userId ID del usuario (para logs y tracking)
 * @property channelId ID de la cuenta asociada
 * @property enableChunking Flag para habilitar chunking (default: true)
 */
@JsonClass(generateAdapter = true)
data class ChunkedEmbeddingRequest(
    @Json(name = "text_context") val textContext: String,
    @Json(name = "file_content") val fileContent: String = "",
    @Json(name = "user_id") val userId: String,
    @Json(name = "channel_id") val channelId: String,
    @Json(name = "enable_chunking") val enableChunking: Boolean = true,
    @Json(name = "mode") val mode: String = "chunked"
)
/*data class ChunkedEmbeddingRequest(
        @Json(name = "text_context") val textContext: String,
        @Json(name = "file_content") val fileContent: String = "",
        @Json(name = "user_id") val userId: String,
        @Json(name = "page_id") val pageId: String,
        @Json(name = "enable_chunking") val enableChunking: Boolean = true,
        @Json(name = "mode") val mode: String = "chunked" // Añadimos el modo que el PHP requiere
)*/

/**
 * Response con chunks y embeddings
 *
 * PHP retorna array de chunks, cada uno con:
 * - text: Texto del chunk
 * - embedding: Array de floats (Moshi deserializa a List<Float>)
 * - metadata: Información del chunk (título, nivel, tipo, etc.)
 *
 * **IMPORTANTE:** Los embeddings vienen como array desde PHP, NO como Base64. Moshi los deserializa
 * automáticamente a List<Float>.
 *
 * @property success Indica si la operación fue exitosa
 * @property chunks Lista de chunks con embeddings
 * @property metadata Metadata general del procesamiento
 * @property errors Lista de errores (si los hay)
 */
@JsonClass(generateAdapter = true)
data class ChunkedEmbeddingResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "chunks") val chunks: List<ChunkData>? = null, // Nullable para manejar errores
    @Json(name = "metadata") val metadata: ProcessingMetadata? = null,
    @Json(name = "errors") val errors: List<String>? = null
)

/**
 * Datos de un chunk con embedding
 *
 * Representa un chunk de texto con su embedding y metadata.
 *
 * **IMPORTANTE:** El campo `embedding` es List<Float> porque Moshi deserializa automáticamente el
 * array de PHP. NO viene como Base64.
 *
 * Para guardar en Firestore, usar EmbeddingSerializer.serialize(embedding) para convertir a Base64.
 *
 * @property chunkId ID único del chunk (ej: "chunk_0", "chunk_1")
 * @property text Texto completo del chunk
 * @property embedding Vector de embedding (List<Float>) - Moshi deserializa automáticamente
 * @property metadata Metadata del chunk (título, nivel, tipo, etc.)
 */
@JsonClass(generateAdapter = true)
data class ChunkData(
    @Json(name = "chunkId") val chunkId: String,
    @Json(name = "text") val text: String,
    @Json(name = "embedding") val embedding: List<Float>, // Moshi deserializa array de PHP
    @Json(name = "metadata") val metadata: ChunkMetadata
) {
    val now = System.currentTimeMillis()
    fun toMap(): Map<String, Any?> =
        mapOf(
            "chunkId" to this.chunkId,
            "text" to this.text,
            "embedding" to this.embedding,
            "metadata" to this.metadata.toMap(),
            "createdAt" to now,
        )
}

/**
 * Metadata de un chunk
 *
 * Información adicional sobre el chunk para análisis y búsqueda.
 *
 * @property title Título de la sección (si existe, ej: "# Introducción")
 * @property level Nivel de jerarquía (1-6 para markdown, null si no aplica)
 * @property type Tipo de chunk: "section", "paragraph", "list"
 * @property wordCount Número de palabras en el chunk
 * @property position Posición del chunk en el documento (0-indexed)
 * @property dimensions Número de dimensiones del embedding (ej: 384)
 * @property source Origen del chunk: "manual" (texto manual) o "file" (archivo)
 */
@JsonClass(generateAdapter = true)
data class ChunkMetadata(
    @Json(name = "title") val title: String? = null,
    @Json(name = "level") val level: Int? = null,
    @Json(name = "type") val type: String,
    @Json(name = "wordCount") val wordCount: Int,
    @Json(name = "position") val position: Int,
    @Json(name = "dimensions") val dimensions: Int,
    @Json(name = "source") val source: String? = null
) {
    fun toMap(): Map<String, Any?> =
        mapOf(
            "title" to this.title,
            "level" to this.level,
            "type" to this.type,
            "wordCount" to this.wordCount,
            "position" to this.position,
            "dimensions" to this.dimensions,
            "source" to this.source
        )
}

/**
 * Metadata del procesamiento
 *
 * Información sobre el procesamiento de embeddings en el servidor.
 *
 * @property provider Proveedor de embeddings (huggingface, openrouter)
 * @property model Modelo usado (ej: "sentence-transformers/all-MiniLM-L6-v2")
 * @property dimensions Dimensiones del embedding (ej: 384)
 * @property processingTimeMs Tiempo de procesamiento en milisegundos
 * @property chunkingEnabled Indica si el chunking está habilitado
 * @property totalChunks Número total de chunks generados
 * @property combinedTextLength Longitud del texto combinado (manual + archivo)
 * @property hasFile Indica si se procesó un archivo
 */
@JsonClass(generateAdapter = true)
data class ProcessingMetadata(
    @Json(name = "provider") val provider: String,
    @Json(name = "model") val model: String,
    @Json(name = "dimensions") val dimensions: Int,
    @Json(name = "processing_time_ms") val processingTimeMs: Double,
    @Json(name = "chunking_enabled") val chunkingEnabled: Boolean? = true,
    @Json(name = "total_chunks") val totalChunks: Int? = 0,
    @Json(name = "combined_text_length") val combinedTextLength: Int? = 0,
    @Json(name = "has_file") val hasFile: Boolean? = false
)

// ============================================================================
// REQUEST/RESPONSE: Búsqueda de Similitud
// ============================================================================

/**
 * Request para búsqueda de similitud
 *
 * Envía un query y la lista de chunks al backend PHP. El servidor calcula similitud coseno y
 * retorna top-K resultados.
 *
 * **Endpoint:** POST /api/android/search-similar-chunks.php
 *
 * **IMPORTANTE:** Los embeddings en `chunks` deben ser List<Float>. Si están en Base64 (desde
 * Firestore), deserializar primero con EmbeddingSerializer.deserialize() antes de enviar.
 *
 * @property query Pregunta o término de búsqueda (ej: "enlaces", "productos")
 * @property contextId ID del contexto en Firestore
 * @property userId ID del usuario
 * @property chunks Lista de chunks con embeddings (List<Float>, NO Base64)
 * @property topK Número de resultados a retornar (default: 3)
 */
@JsonClass(generateAdapter = true)
data class SimilaritySearchRequest(
    @Json(name = "query") val query: String,
    @Json(name = "context_id") val contextId: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "chunks") val chunks: List<ChunkData>,
    @Json(name = "top_k") val topK: Int = 3
)

/**
 * Response con chunks similares
 *
 * PHP retorna los top-K chunks más similares al query, ordenados por score.
 *
 * @property success Indica si la búsqueda fue exitosa
 * @property results Lista de chunks con score de similitud (ordenados descendente)
 * @property totalCompared Número total de chunks comparados
 * @property query Query original de búsqueda
 */
@JsonClass(generateAdapter = true)
data class SimilaritySearchResponse(
    @Json(name = "success") val success: Boolean,
    @Json(name = "results") val results: List<ChunkWithSimilarity>,
    @Json(name = "total_compared") val totalCompared: Int,
    @Json(name = "query") val query: String
)

/**
 * Chunk con score de similitud
 *
 * Representa un chunk con su score de similitud al query.
 *
 * @property chunkId ID del chunk
 * @property text Texto del chunk
 * @property title Título de la sección (si existe)
 * @property similarity Score de similitud (0.0 - 1.0, mayor es más similar)
 * @property metadata Metadata del chunk
 */
@JsonClass(generateAdapter = true)
data class ChunkWithSimilarity(
    @Json(name = "chunkId") val chunkId: String,
    @Json(name = "text") val text: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "similarity") val similarity: Float,
    @Json(name = "metadata") val metadata: ChunkMetadata
)

// ============================================================================
// MODELOS PARA FIRESTORE
// ============================================================================

/**
 * Chunk para guardar en Firestore
 *
 * Versión del chunk con embedding serializado en Base64 para Firestore.
 *
 * **Conversión:**
 * ```kotlin
 * val chunkData: ChunkData = ... // Desde API (embedding es List<Float>)
 * val embeddingBase64 = EmbeddingSerializer.serialize(chunkData.embedding)
 * val firestoreChunk = FirestoreChunk(
 *     chunkId = chunkData.chunkId,
 *     text = chunkData.text,
 *     embedding = embeddingBase64, // Base64 para Firestore
 *     metadata = chunkData.metadata
 * )
 * ```
 *
 * @property chunkId ID único del chunk
 * @property text Texto del chunk
 * @property embedding Embedding serializado en Base64 (para Firestore)
 * @property metadata Metadata del chunk
 * @property createdAt Timestamp de creación (Firestore FieldValue.serverTimestamp())
 */
data class FirestoreChunk(
    val chunkId: String = "",
    val text: String = "",
    val embedding: String = "", // Base64 para Firestore
    val metadata: ChunkMetadata? = null,
    val createdAt: Any? = null // FieldValue.serverTimestamp()
)

/**
 * Metadata del contexto principal en Firestore
 *
 * Se guarda en el documento principal: users/{userId}/contexts/{contextId}
 *
 * @property totalChunks Número total de chunks en la subcolección
 * @property hasFile Indica si el contexto tiene archivo
 * @property fileName Nombre del archivo (si existe)
 * @property fileSize Tamaño del archivo en bytes (si existe)
 * @property createdAt Timestamp de creación
 * @property updatedAt Timestamp de última actualización
 */
data class ContextMetadata(
    val totalChunks: Int = 0,
    val hasFile: Boolean = false,
    val fileName: String? = null,
    val fileSize: Long? = null,
    val createdAt: Any? = null, // FieldValue.serverTimestamp()
    val updatedAt: Any? = null // FieldValue.serverTimestamp()
)
