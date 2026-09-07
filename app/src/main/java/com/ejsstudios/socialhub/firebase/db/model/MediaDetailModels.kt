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

/** Información detallada de un archivo media para mostrar en modal */
@Keep
data class MediaDetail(
    val id: String,
    val fileName: String,
    val fileSize: String,
    val format: String,
    val type: ContextMediaType,
    val localUri: Uri?,
    val remoteUrl: String?,
    val description: String,
    val createdAt: Long,
    val dimensions: String? = null, // Para imágenes/videos: "1920x1080"
    val duration: String? = null // Para videos: "00:02:30"
)

/** Convierte ContextMedia a MediaDetail */
fun ContextMedia.toMediaDetail(): MediaDetail {
    return MediaDetail(
        id = this.id,
        fileName = this.fileName,
        fileSize = this.size,
        format = this.fileName.substringAfterLast('.', "").uppercase(),
        type = this.type,
        localUri = this.localUri,
        remoteUrl = this.remoteUrl,
        description = this.description,
        createdAt = this.createdAt
    )
}
