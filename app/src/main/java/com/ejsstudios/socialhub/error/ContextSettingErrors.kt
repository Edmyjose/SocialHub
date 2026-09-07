/**
 * ***************************************************************************** Copyright (c) 2026.
 * EJS Studios. Todos los derechos reservados.
 */
package com.ejsstudios.socialhub.error

/** Códigos de error específicos para Context Setting */
object ContextErrorCodes {

        const val UNKNOW = 9999
        // Errores de validación (1000-1999)
        const val TEXT_CONTEXT_EMPTY = 1001
        const val TEXT_CONTEXT_TOO_SHORT = 1002
        const val TEXT_CONTEXT_TOO_LONG = 1003
        const val FILE_INVALID_FORMAT = 1004
        const val FILE_TOO_LARGE = 1005
        const val IMAGE_INVALID_FORMAT = 1006
        const val IMAGE_TOO_LARGE = 1007
        const val VIDEO_INVALID_FORMAT = 1008
        const val VIDEO_TOO_LARGE = 1009
        const val DESCRIPTION_TOO_SHORT = 1010
        const val DESCRIPTION_TOO_LONG = 1011
        const val MAX_IMAGES_EXCEEDED = 1012
        const val MAX_VIDEOS_EXCEEDED = 1013
        const val MEDIA_NOT_FOUND = 1014

        // Errores de embeddings (1020-1029)
        const val EMBEDDING_GENERATION_FAILED = 1020
        const val EMBEDDING_SERIALIZATION_FAILED = 1021
        const val EMBEDDING_DESERIALIZATION_FAILED = 1022

        // Errores de chunking (1030-1039)
        const val CHUNKING_FAILED = 1030
        const val CHUNKING_NO_CHUNKS_GENERATED = 1031
        const val CHUNKING_SERVER_ERROR = 1032
        const val CHUNKING_NETWORK_ERROR = 1033
        const val CHUNKING_INVALID_RESPONSE = 1034
        const val CHUNKING_TIMEOUT = 1035

        // Errores de red (2000-2999)
        const val NETWORK_ERROR = 2001
        const val SERVER_UNREACHABLE = 2002
        const val UPLOAD_FAILED = 2003
        const val DELETE_FAILED = 2004
        const val SERVER_ERROR = 2005

        // Errores de Firestore (3000-3999)
        const val FIRESTORE_SAVE_ERROR = 3001
        const val FIRESTORE_DOCUMENT_TOO_LARGE = 3002
        const val FIRESTORE_PERMISSION_DENIED = 3003

        // Errores de archivo local (4000-4999)
        const val FILE_NOT_FOUND = 4001
        const val FILE_READ_ERROR = 4002
        const val CACHE_WRITE_ERROR = 4003
}

/** Excepciones específicas para Context Setting */
sealed class ContextSettingException(errorCode: Int, message: String): CustomException(errorCode, message) {

        class Unknow(msg: String? = "Error Desconocido") : ContextSettingException(ContextErrorCodes.UNKNOW, msg ?: "Error Desconocido")

        // Validaciones
        class TextContextEmpty :
                ContextSettingException(
                        ContextErrorCodes.TEXT_CONTEXT_EMPTY,
                        "El contexto de texto es obligatorio"
                )

        class TextContextTooShort :
                ContextSettingException(
                        ContextErrorCodes.TEXT_CONTEXT_TOO_SHORT,
                        "El contexto debe tener al menos 50 caracteres"
                )

        class TextContextTooLong :
                ContextSettingException(
                        ContextErrorCodes.TEXT_CONTEXT_TOO_LONG,
                        "El contexto no puede exceder 5000 caracteres"
                )

        class FileInvalidFormat(format: String) :
                ContextSettingException(
                        ContextErrorCodes.FILE_INVALID_FORMAT,
                        "Formato de archivo inválido: $format. Solo se permiten archivos .txt"
                )

        class FileTooLarge(size: Long) :
                ContextSettingException(
                        ContextErrorCodes.FILE_TOO_LARGE,
                        "Archivo muy grande: ${size / 1024 / 1024}MB. Máximo permitido: 2MB"
                )

        class MediaNotFound(type: String) :
                ContextSettingException(
                        ContextErrorCodes.MEDIA_NOT_FOUND,
                        "$type no encontrada"
                )

        class ImageInvalidFormat(format: String) :
                ContextSettingException(
                        ContextErrorCodes.IMAGE_INVALID_FORMAT,
                        "Formato de imagen inválido: $format. Formatos permitidos: jpg, jpeg, png, webp"
                )

        class ImageTooLarge(size: Long) :
                ContextSettingException(
                        ContextErrorCodes.IMAGE_TOO_LARGE,
                        "Imagen muy grande: ${size / 1024 / 1024}MB. Máximo permitido: 5MB"
                )

        class VideoInvalidFormat(format: String) :
                ContextSettingException(
                        ContextErrorCodes.VIDEO_INVALID_FORMAT,
                        "Formato de video inválido: $format. Formatos permitidos: mp4, mov"
                )

        class VideoTooLarge(size: Long) :
                ContextSettingException(
                        ContextErrorCodes.VIDEO_TOO_LARGE,
                        "Video muy grande: ${size / 1024 / 1024}MB. Máximo permitido: 50MB"
                )

        class DescriptionTooShort :
                ContextSettingException(
                        ContextErrorCodes.DESCRIPTION_TOO_SHORT,
                        "La descripción debe tener al menos 50 caracteres"
                )

        class DescriptionTooLong :
                ContextSettingException(
                        ContextErrorCodes.DESCRIPTION_TOO_LONG,
                        "La descripción no puede exceder 500 caracteres"
                )

        class MaxImagesExceeded :
                ContextSettingException(
                        ContextErrorCodes.MAX_IMAGES_EXCEEDED,
                        "Máximo 20 imágenes permitidas"
                )

        class MaxVideosExceeded :
                ContextSettingException(
                        ContextErrorCodes.MAX_VIDEOS_EXCEEDED,
                        "Máximo 5 videos permitidos"
                )

        // Errores de red
        class NetworkError(cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.NETWORK_ERROR,
                        "Error de conexión: ${cause?.message ?: "Sin conexión a internet"}"
                )

        class ServerUnreachable(cause: Throwable?) : ContextSettingException(
                ContextErrorCodes.SERVER_UNREACHABLE,
                cause?.localizedMessage ?: "No se pudo conectar al servidor."
        )

        class ServerError(statusCode: Int) : ContextSettingException(
                ContextErrorCodes.SERVER_ERROR,
                "Error del servidor: código $statusCode"
        )

        class UploadFailed(fileName: String, cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.UPLOAD_FAILED,
                        "Error al subir $fileName: ${cause?.message ?: "Error desconocido"}"
                )

        // Errores de Firestore
        class FirestoreSaveError(cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.FIRESTORE_SAVE_ERROR,
                        "Error al guardar en Firestore: ${cause?.message ?: "Error desconocido"}"
                )

        class FirestoreSaveFailed(itemName: String? = "", cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.FIRESTORE_SAVE_ERROR,
                        "Error al guardar $itemName en Firestore: ${cause?.message ?: "Error desconocido"}"
                )

        class FirestoreDocumentTooLarge(size: Long) :
                ContextSettingException(
                        ContextErrorCodes.FIRESTORE_DOCUMENT_TOO_LARGE,
                        "Documento muy grande: ${size / 1024}KB. Máximo permitido: 1MB"
                )

        // Errores de archivo local
        class FileNotFound(fileName: String) :
                ContextSettingException(
                        ContextErrorCodes.FILE_NOT_FOUND,
                        "Archivo no encontrado: $fileName"
                )

        class FileReadError(fileName: String, cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.FILE_READ_ERROR,
                        "Error al leer archivo $fileName: ${cause?.message}"
                )

        // Errores de embeddings
        class EmbeddingGenerationFailed(itemType: String? = "", cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.EMBEDDING_GENERATION_FAILED,
                        "Error al generar embedding para $itemType: ${cause?.message ?: "Error desconocido"}"
                )

        class EmbeddingSerializationFailed(cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.EMBEDDING_SERIALIZATION_FAILED,
                        "Error al serializar embedding: ${cause?.message ?: "Error desconocido"}"
                )

        class EmbeddingDeserializationFailed(cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.EMBEDDING_DESERIALIZATION_FAILED,
                        "Error al deserializar embedding: ${cause?.message ?: "Error desconocido"}"
                )

        // Errores de chunking
        class ChunkingFailed(cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.CHUNKING_FAILED,
                        "Error al procesar chunking: ${cause?.message ?: "Error desconocido"}"
                )

        class ChunkingNoChunksGenerated :
                ContextSettingException(
                        ContextErrorCodes.CHUNKING_NO_CHUNKS_GENERATED,
                        "El servidor no generó chunks. Verifica que el texto tenga contenido suficiente."
                )

        class ChunkingServerError(statusCode: Int, serverMessage: String?) :
                ContextSettingException(
                        ContextErrorCodes.CHUNKING_SERVER_ERROR,
                        "Error del servidor durante chunking (código $statusCode): ${serverMessage ?: "Error desconocido"}"
                )

        class ChunkingNetworkError(cause: Throwable?) :
                ContextSettingException(
                        ContextErrorCodes.CHUNKING_NETWORK_ERROR,
                        "Error de red durante chunking: ${cause?.message ?: "Sin conexión a internet"}"
                )

        class ChunkingInvalidResponse(details: String) :
                ContextSettingException(
                        ContextErrorCodes.CHUNKING_INVALID_RESPONSE,
                        "Respuesta inválida del servidor de chunking: $details"
                )

        class ChunkingTimeout :
                ContextSettingException(
                        ContextErrorCodes.CHUNKING_TIMEOUT,
                        "Tiempo de espera agotado durante el procesamiento de chunking"
                )
}

/** Resultado de operación con manejo de errores */
sealed class ContextResult<out T> {
        data class Success<T>(val data: T) : ContextResult<T>()
        data class Error(val exception: ContextSettingException) : ContextResult<Nothing>()
        data object Loading : ContextResult<Nothing>()
}
