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

import android.net.Uri
import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import java.util.UUID

/**
 * Tipos de media soportados para el contexto de una página.
 *
 * IMPORTANTE: Firestore serializa enums como strings usando el nombre del valor.
 * Se añade @Keep para evitar que R8 cambie los nombres de los valores durante la ofuscación.
 */
@Keep
enum class ContextMediaType {
    TEXT_FILE,
    IMAGE,
    VIDEO
}

@Keep
@IgnoreExtraProperties
data class ContextMedia(
    val id: String = UUID.randomUUID().toString(),
    @get:PropertyName("channel_id")
    @set:PropertyName("channel_id")
    var channelId: String = "",
    val platform: SocialPlatform? = null,
    val userId: String = "",
    @get:Exclude val localUri: Uri? = null,
    val fileName: String = "",
    val remoteUrl: String? = null,
    val description: String = "",
    val type: ContextMediaType = ContextMediaType.IMAGE,
    val size: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val embedding: List<Float> = emptyList(),
    @get:PropertyName("usage_count")
    @set:PropertyName("usage_count")
    var usageCount: Int = 0,
    @get:PropertyName("last_used_date")
    @set:PropertyName("last_used_date")
    var lastUsedDate: String? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to this.id,
        "channel_id" to this.channelId,
        "platform" to this.platform?.id,
        "userId" to this.userId,
        "fileName" to this.fileName,
        "remoteUrl" to this.remoteUrl,
        "description" to this.description,
        "type" to this.type.name,
        "size" to this.size,
        "embedding" to this.embedding,
        "usage_count" to this.usageCount,
        "last_used_date" to this.lastUsedDate,
        "createdAt" to this.createdAt
    )
}

@Keep
@IgnoreExtraProperties
data class ChannelAIContext(
    val id: String = "",
    val channelId: String = "",
    val platform: SocialPlatform = SocialPlatform.FACEBOOK_PAGE,
    val userId: String = "",
    val rawContext: String = "",
    val smartSummary: String = "",
    val contextFileUrl: String = "",
    @get:Exclude val localUri: Uri? = null,
    val fileName: String = "",
    val size: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val embedding: List<Float> = emptyList()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to this.id,
        "channel_id" to this.channelId,
        "platform" to this.platform.id,
        "userId" to this.userId,
        "rawContext" to this.rawContext,
        "smartSummary" to this.smartSummary,
        "contextFileUrl" to this.contextFileUrl,
        "fileName" to this.fileName,
        "size" to this.size,
        "embedding" to this.embedding,
        "createdAt" to this.createdAt,
        "updatedAt" to this.updatedAt
    )
}

/**
 * Genera el nombre válido para el archivo basado en el nombre original.
 * Respeta el nombre original del usuario y su extensión, asegurando que sea válido para Storage
 * (sin espacios ni caracteres especiales excepto - y _).
 *
 * @param type Tipo de media
 * @param index Índice en el array (usado como fallback si el nombre queda vacío)
 * @param originalFileName Nombre original del archivo proporcionado por el dispositivo (con extensión)
 * @return Nombre sanitizado con su extensión original
 */
fun generateServerFileName(type: ContextMediaType, index: Int, originalFileName: String): String {
    // 1. Obtener el nombre base y la extensión por separado
    val baseName = if (originalFileName.contains(".")) {
        originalFileName.substringBeforeLast(".")
    } else {
        originalFileName
    }

    val extension = if (originalFileName.contains(".")) {
        originalFileName.substringAfterLast(".")
    } else {
        when (type) {
            ContextMediaType.TEXT_FILE -> "txt"
            ContextMediaType.IMAGE -> "jpg"
            ContextMediaType.VIDEO -> "mp4"
        }
    }

    // 2. Sanitizar el nombre base:
    // - Reemplazar espacios por guiones bajos
    // - Eliminar todo lo que no sea A-Z, a-z, 0-9, - o _
    val sanitizedBase = baseName.replace(" ", "_")
        .replace(Regex("[^a-zA-Z0-9-_]"), "")

    // 3. Si el nombre queda vacío tras sanitizar, usar un fallback
    val finalBase = sanitizedBase.ifBlank {
        when (type) {
            ContextMediaType.TEXT_FILE -> "context_file"
            ContextMediaType.IMAGE -> "image_${index + 1}"
            ContextMediaType.VIDEO -> "video_${index + 1}"
        }
    }

    // 4. Retornar nombre final con extensión original
    return "$finalBase.$extension"
}
