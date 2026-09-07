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

package com.ejsstudios.socialhub.managers

import com.ejsstudios.socialhub.apiservices.WebStorageApiService
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ContextSettingException
import com.ejsstudios.socialhub.utils.Constants.WEB_SERVER_URL
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Resultado de las operaciones de WebStorage para un manejo limpio y tipado.
 * Evita el uso de nulos ambiguos y centraliza el manejo de errores.
 */
sealed class WebStorageResponse {
    data class Success(val url: String) : WebStorageResponse()
    data class Error(val exception: ContextSettingException) : WebStorageResponse()
}

/**
 * Gestor avanzado para la administración de archivos en el servidor web propio. Centraliza las
 * operaciones de subida y borrado para perfiles, páginas y contexto IA.
 * Refactorizado para usar Retrofit y devolver estados tipados [WebStorageResponse].
 */
object WebStorageManager {

    private val client =
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

    private val api: WebStorageApiService by lazy {
        Retrofit.Builder()
            .baseUrl(WEB_SERVER_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(WebStorageApiService::class.java)
    }

    private fun String.toPlainTextBody(): RequestBody =
        this.toRequestBody("text/plain".toMediaTypeOrNull())

    /**
     * Sube un archivo al servidor PHP unificado.
     * @param userId ID del usuario (propietario)
     * @param fileId ID único del archivo (UUID o AssetId)
     * @param fileBytes Contenido del archivo en bytes
     * @param type Tipo de recurso ('user_profile', 'page_profile', 'context_file', 'image', 'video')
     * @param channelid ID de la página (obligatorio para recursos de contexto)
     * @return [WebStorageResponse] con la URL o el error procesado.
     */
    suspend fun uploadFile(
        userId: String,
        fileId: String,
        fileBytes: ByteArray,
        type: String,
        channelid: String? = null
    ): WebStorageResponse {
        Loggers.log("d", "WebStorageManager", "Subiendo ($type) via Retrofit")

        val mediaType = when (type) {
            "video" -> "video/mp4"
            "context_file" -> "text/plain"
            else -> "image/jpeg"
        }.toMediaTypeOrNull()

        val extension = when (type) {
            "context_file" -> ".txt"
            "video" -> ".mp4"
            else -> ".jpg"
        }

        val filePart = MultipartBody.Part.createFormData(
            "file",
            "$fileId$extension",
            fileBytes.toRequestBody(mediaType)
        )

        return try {
            val response = api.uploadFile(
                action = "upload".toPlainTextBody(),
                userId = userId.toPlainTextBody(),
                type = type.toPlainTextBody(),
                fileId = fileId.toPlainTextBody(),
                channelid = channelid?.toPlainTextBody(),
                file = filePart
            )

            if (response.isSuccessful) {
                val responseData = response.body()?.string() ?: ""
                val json = JSONObject(responseData)
                val isSuccess =
                    json.optBoolean("success", false) || json.optString("status") == "success"
                val url = json.optString("url", json.optString("path", ""))

                if (isSuccess && url.isNotEmpty()) {
                    WebStorageResponse.Success(url)
                } else {
                    val message = json.optString("message", "Error lógico en el servidor")
                    Loggers.log("e", "WebStorageManager", "PHP Error: $message")
                    WebStorageResponse.Error(
                        ContextSettingException.UploadFailed(
                            fileId,
                            Exception(message)
                        )
                    )
                }
            } else {
                Loggers.log("e", "WebStorageManager", "Server Error: ${response.code()}")
                WebStorageResponse.Error(ContextSettingException.ServerError(response.code()))
            }
        } catch (e: Exception) {
            Loggers.log("e", "WebStorageManager", "Network/Process Error: ${e.message}")
            WebStorageResponse.Error(ContextSettingException.NetworkError(e))
        }
    }

    /**
     * Elimina un archivo físicamente del servidor.
     */
    suspend fun deleteFile(
        userId: String,
        fileId: String,
        type: String,
        channelid: String? = null
    ): Boolean {
        Loggers.log("d", "WebStorageManager", "Eliminando archivo: $fileId (tipo: $type)")

        return try {
            val response = api.deleteFile(
                action = "delete".toPlainTextBody(),
                userId = userId.toPlainTextBody(),
                type = type.toPlainTextBody(),
                fileId = fileId.toPlainTextBody(),
                channelid = channelid?.toPlainTextBody()
            )

            if (response.isSuccessful) {
                val responseData = response.body()?.string() ?: ""
                val json = JSONObject(responseData)
                json.optBoolean("success", false)
            } else {
                false
            }
        } catch (e: Exception) {
            Loggers.log("e", "WebStorageManager", "Error eliminando: ${e.message}")
            false
        }
    }

    /** Método de compatibilidad para la subida de imágenes de perfil. */
    suspend fun uploadImage(
        userId: String,
        assetId: String,
        imageBytes: ByteArray
    ): WebStorageResponse {
        return uploadFile(
            userId = userId,
            fileId = assetId,
            fileBytes = imageBytes,
            type = if (assetId == userId) "user_profile" else "page_profile",
            channelid = if (assetId != userId) assetId else null
        )
    }
}
