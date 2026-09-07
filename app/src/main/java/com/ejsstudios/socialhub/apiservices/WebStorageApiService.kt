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

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Interface Retrofit para la gestión de archivos en el servidor web (manage_context.php).
 * Maneja subidas y borrados de perfiles, imágenes, videos y archivos de contexto.
 */
interface WebStorageApiService {

    @Multipart
    @POST("manage_context.php")
    suspend fun uploadFile(
        @Part("action") action: RequestBody,
        @Part("userid") userId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("file_id") fileId: RequestBody,
        @Part("channel_id") channelid: RequestBody?,
        @Part file: MultipartBody.Part
    ): Response<ResponseBody>

    @Multipart
    @POST("manage_context.php")
    suspend fun deleteFile(
        @Part("action") action: RequestBody,
        @Part("userid") userId: RequestBody,
        @Part("type") type: RequestBody,
        @Part("file_id") fileId: RequestBody,
        @Part("channel_id") channelid: RequestBody?
    ): Response<ResponseBody>
}
