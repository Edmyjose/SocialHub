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

package com.ejsstudios.socialhub.apiservices

import com.ejsstudios.socialhub.firebase.db.model.ChunkedEmbeddingRequest
import com.ejsstudios.socialhub.firebase.db.model.ChunkedEmbeddingResponse
import com.ejsstudios.socialhub.firebase.db.model.SimilaritySearchRequest
import com.ejsstudios.socialhub.firebase.db.model.SimilaritySearchResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API de Embeddings con Chunking Inteligente
 *
 * Interface Retrofit para comunicación con el backend PHP que implementa:
 * - Chunking inteligente de contextos (concatenación + división semántica)
 * - Generación de embeddings por chunk
 * - Búsqueda de similitud calculada en PHP
 *
 * **Arquitectura Técnica:**
 * - Comunicación: Retrofit + Moshi
 * - Serialización: Array multidimensional desde PHP (NO Base64)
 * - Moshi deserializa automáticamente List<Float>
 * - Búsqueda de similitud en PHP (NO en Android)
 *
 * **Flujo de Datos:**
 * 1. Android envía texto manual + archivo al backend PHP
 * 2. PHP concatena y aplica chunking inteligente
 * 3. PHP genera embeddings (1 por chunk)
 * 4. PHP retorna array de chunks con embeddings como List<Float>
 * 5. Moshi deserializa automáticamente
 * 6. Android serializa a Base64 solo para guardar en Firestore
 *
 * @see ChunkedEmbeddingRequest Request para generar embeddings con chunking
 * @see ChunkedEmbeddingResponse Response con chunks y embeddings
 * @see SimilaritySearchRequest Request para búsqueda de similitud
 * @see SimilaritySearchResponse Response con chunks similares ordenados
 */
interface EmbeddingApiService {

    /**
     * Generar embeddings con chunking inteligente
     *
     * Envía texto manual y contenido de archivo al backend PHP. El servidor:
     * 1. Concatena texto manual + archivo
     * 2. Aplica chunking inteligente (detecta tipo de contenido)
     * 3. Genera 1 embedding por chunk
     * 4. Retorna chunks con embeddings como array (Moshi deserializa a List<Float>)
     *
     * **Endpoint PHP:** `/api/android/generate-embeddings.php` (modo: chunked)
     *
     * **Tiempo estimado:** 5-60 segundos (depende del número de chunks)
     *
     * @param request Request con texto manual, archivo opcional, userId, pageId
     * @return Response con lista de chunks (cada chunk tiene text, embedding, metadata)
     *
     * @throws retrofit2.HttpException Si hay error HTTP (400, 500, etc.)
     * @throws java.io.IOException Si hay error de red
     */
    @POST("api/android/generate-embeddings-standalone.php")
    suspend fun generateChunkedEmbeddings(
        @Body request: ChunkedEmbeddingRequest
    ): ChunkedEmbeddingResponse

    /**
     * Buscar chunks similares (búsqueda calculada en PHP)
     *
     * Envía un query y la lista de chunks al backend PHP. El servidor:
     * 1. Genera embedding del query
     * 2. Calcula similitud coseno para cada chunk
     * 3. Ordena por similitud descendente
     * 4. Retorna top-K chunks más relevantes
     *
     * **Endpoint PHP:** `/api/android/search-similar-chunks.php`
     *
     * **Ventajas de búsqueda en PHP:**
     * - Cálculo más rápido en servidor
     * - Menor consumo de batería en Android
     * - Código reutilizable de otherCodes/services/EmbeddingService.php
     * - Fácil actualización de algoritmos sin cambiar app
     *
     * **Tiempo estimado:** < 2 segundos (para 10-50 chunks)
     *
     * @param request Request con query, contextId, userId, chunks, topK
     * @return Response con chunks similares ordenados por score
     *
     * @throws retrofit2.HttpException Si hay error HTTP
     * @throws java.io.IOException Si hay error de red
     */
    @POST("api/android/search-similar-chunks.php")
    suspend fun searchSimilarChunks(
        @Body request: SimilaritySearchRequest
    ): SimilaritySearchResponse

    companion object {
        /**
         * URL base del servidor PHP
         *
         * Debe configurarse en Constants.WEB_SERVER_URL Ejemplo: "https://example.com/" o
         * "http://192.168.1.100/"
         */
        const val BASE_URL_KEY = "WEB_SERVER_URL"
    }
}
