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

package com.ejsstudios.socialhub.ai.embedding

import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ContextSettingException
import com.ejsstudios.socialhub.utils.Constants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.SocketTimeoutException
import java.net.URL

/**
 * Servicio de Embeddings para Android
 *
 * Genera embeddings llamando al endpoint PHP del servidor. El servidor se encarga de la generación
 * usando HuggingFace o OpenRouter.
 *
 * Ventajas de este enfoque:
 * - API keys seguras en el servidor (no expuestas en APK)
 * - Fácil actualización de modelos sin cambiar la app
 * - Menor tamaño del APK
 * - Menor consumo de batería
 *
 * @see EmbeddingSerializer para serialización local de embeddings
 */
class EmbeddingService {

    companion object {
        private const val TAG = "EmbeddingService"

        // TODO: Configurar URL del servidor
        private const val ENDPOINT_URL =
            Constants.WEB_SERVER_URL + "/api/android/generate-embeddings.php"

        // Timeouts
        private const val CONNECT_TIMEOUT_MS = 10000 // 10 segundos
        private const val READ_TIMEOUT_MS = 30000 // 30 segundos
    }

    /**
     * Genera embedding para un texto individual. Llama al endpoint PHP en modo "single".
     *
     * @param text Texto del contexto (50-8000 caracteres)
     * @return Result con EmbeddingResult o error
     */
    suspend fun generateTextEmbedding(text: String): Result<EmbeddingResult> {
        return generateEmbedding("text", text)
    }

    /**
     * Genera embedding para contenido de archivo. Llama al endpoint PHP en modo "single".
     *
     * @param content Contenido del archivo
     * @return Result con EmbeddingResult o error
     */
    suspend fun generateFileEmbedding(content: String): Result<EmbeddingResult> {
        return generateEmbedding("file", content)
    }

    /**
     * Genera embedding para descripción de media (imagen/video). Llama al endpoint PHP en modo
     * "single".
     *
     * @param description Descripción de la imagen o video (50-500 caracteres)
     * @return Result con EmbeddingResult o error
     */
    suspend fun generateDescriptionEmbedding(description: String): Result<EmbeddingResult> {
        return generateEmbedding("description", description)
    }

    /**
     * Genera embedding llamando al endpoint PHP.
     *
     * @param type Tipo de contenido: "text", "file", "description"
     * @param content Contenido a procesar
     * @return Result con EmbeddingResult o error
     */
    private suspend fun generateEmbedding(type: String, content: String): Result<EmbeddingResult> {
        return withContext(Dispatchers.IO) {
            var connection: HttpURLConnection? = null

            try {
                Loggers.log("e", TAG, "Generando embedding para tipo: $type")

                // 1. Configurar conexión HTTP
                val url = URL(ENDPOINT_URL)
                connection = url.openConnection() as HttpURLConnection

                connection.apply {
                    requestMethod = "POST"
                    doOutput = true
                    doInput = true
                    connectTimeout = CONNECT_TIMEOUT_MS
                    readTimeout = READ_TIMEOUT_MS
                    setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                    setRequestProperty("Accept", "application/json")
                }

                // 2. Construir request JSON
                val requestBody = JSONObject().apply {
                    put("mode", "single")
                    put("type", type)
                    put("content", content)
                }

                // 3. Enviar request
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }

                // 4. Verificar código de respuesta
                val responseCode = connection.responseCode

                if (responseCode != HttpURLConnection.HTTP_OK) {
                    val errorStream = connection.errorStream
                    val errorMessage = if (errorStream != null) {
                        BufferedReader(InputStreamReader(errorStream, Charsets.UTF_8)).use {
                            it.readText()
                        }
                    } else {
                        "HTTP error: $responseCode"
                    }

                    Loggers.log("e", TAG, "Error HTTP $responseCode: $errorMessage")
                    /*return@withContext Result.failure(ContextSettingException.ServerUnreachable(Exception("Error HTTP $responseCode: $errorMessage")))*/
                    throw ContextSettingException.ServerUnreachable(Exception("Error HTTP $responseCode: $errorMessage"))
                    /*return@withContext Result.failure(Exception("Error HTTP $responseCode: $errorMessage") )*/
                }

                // 5. Leer respuesta
                val response = BufferedReader(
                    InputStreamReader(
                        connection.inputStream,
                        Charsets.UTF_8
                    )
                ).use { it.readText() }

                // 6. Parsear respuesta JSON
                val jsonResponse = JSONObject(response)

                if (!jsonResponse.getBoolean("success")) {
                    val error = jsonResponse.optString("error", "Unknown error")
                    Loggers.log("e", TAG, "Error del servidor: $error")
                    throw ContextSettingException.ServerUnreachable(Exception(error))
                    /*return@withContext Result.failure()*/
                }

                // 7. Extraer embedding
                val embeddingArray = jsonResponse.getJSONArray("embedding")
                val embedding =
                    List(embeddingArray.length()) { i -> embeddingArray.getDouble(i).toFloat() }

                // 8. Extraer metadata (opcional)
                val metadata = if (jsonResponse.has("metadata")) {
                    val metadataObj = jsonResponse.getJSONObject("metadata")
                    EmbeddingMetadata(
                        provider = metadataObj.optString("provider", "unknown"),
                        model = metadataObj.optString("model", "unknown"),
                        dimensions = metadataObj.optInt("dimensions", embedding.size)
                    )
                } else {
                    // Metadata por defecto si no viene en la respuesta
                    EmbeddingMetadata(
                        provider = "unknown",
                        model = "unknown",
                        dimensions = embedding.size
                    )
                }

                Loggers.log(
                    "e",
                    TAG,
                    "Embedding generado exitosamente: ${embedding.size} dimensiones (${metadata.provider}/${metadata.model})"
                )

                Result.success(EmbeddingResult(embedding, metadata))
            } catch (e: ContextSettingException) {
                Result.failure(e)
            } catch (e: SocketTimeoutException) {
                Loggers.log("e", TAG, "Timeout generando embedding: ${e.message}")
                Result.failure(
                    ContextSettingException.ServerUnreachable(
                        Exception(
                            "Timeout: La generación de embedding tardó demasiado",
                            e
                        )
                    )
                )
            } catch (e: IOException) {
                Loggers.log("e", TAG, "Error de red generando embedding: ${e.message}")
                Result.failure(
                    ContextSettingException.ServerUnreachable(
                        Exception(
                            "Error de red: ${e.message}",
                            e
                        )
                    )
                )
            } catch (e: JSONException) {
                Loggers.log("e", TAG, "Error parseando respuesta JSON: ${e.message}")
                Result.failure(
                    ContextSettingException.ServerUnreachable(
                        Exception(
                            "Error parseando respuesta del servidor",
                            e
                        )
                    )
                )
            } catch (e: Exception) {
                Loggers.log("e", TAG, "Error inesperado generando embedding: ${e.message}")
                Result.failure(
                    ContextSettingException.ServerUnreachable(
                        Exception(
                            "Error inesperado",
                            e
                        )
                    )
                )
            } finally {
                connection?.disconnect()
            }
        }
    }
}

/**
 * Resultado de generación de embedding.
 *
 * @property embedding Vector de embedding (List<Float>)
 * @property metadata Metadata del embedding (provider, model, dimensions)
 */
data class EmbeddingResult(val embedding: List<Float>, val metadata: EmbeddingMetadata)

/**
 * Metadata del embedding.
 *
 * @property provider Proveedor del embedding (huggingface, openrouter)
 * @property model Modelo usado para generar el embedding
 * @property dimensions Número de dimensiones del embedding
 */
data class EmbeddingMetadata(val provider: String, val model: String, val dimensions: Int)
