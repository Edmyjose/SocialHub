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

import com.ejsstudios.socialhub.firebase.db.model.ExecuteAIRequest
import com.ejsstudios.socialhub.firebase.db.model.ExecuteAIResponse
import com.ejsstudios.socialhub.firebase.db.model.PreparePromptRequest
import com.ejsstudios.socialhub.firebase.db.model.PreparePromptResponse
import com.ejsstudios.socialhub.firebase.db.model.UpdateMemoryRequest
import com.ejsstudios.socialhub.firebase.db.model.UpdateMemoryResponse
import com.ejsstudios.socialhub.firebase.db.model.ValidateNoveltyRequest
import com.ejsstudios.socialhub.firebase.db.model.ValidateNoveltyResponse
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface Retrofit para los servicios atómicos de IA en PHP.
 */
interface AIGenerationApiService {

    /**
     * Valida si un tema es original comparándolo con el historial.
     */
    @POST("api/android/validate-novelty.php")
    suspend fun validateNovelty(
        @Body request: ValidateNoveltyRequest
    ): ValidateNoveltyResponse

    /**
     * Une las piezas del prompt y calcula los tokens exactos.
     */
    @POST("api/android/prepare-prompt.php")
    suspend fun preparePrompt(
        @Body request: PreparePromptRequest
    ): PreparePromptResponse

    /**
     * Ejecuta la llamada a la IA (DeepSeek/OpenRouter) vía Proxy.
     */
    @POST("api/android/execute-ai.php")
    suspend fun executeAI(
        @Body request: ExecuteAIRequest
    ): ExecuteAIResponse

    /**
     * Genera un resumen semántico y su vector de memoria.
     */
    @POST("api/android/update-memory.php")
    suspend fun updateMemory(
        @Body request: UpdateMemoryRequest
    ): UpdateMemoryResponse

    /**
     * Delegar selección inteligente de imágenes al motor PHP.
     */
    /*@POST("api/android/select-images-standalone.php")
    suspend fun selectImages(
        @Body request: ImageSelectionRequest
    ): ImageSelectionResponse*/
}
