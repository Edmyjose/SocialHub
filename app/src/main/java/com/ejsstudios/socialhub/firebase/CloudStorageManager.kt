/*******************************************************************************
 * Copyright (c) 2025. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 ******************************************************************************/

package com.ejsstudios.socialhub.firebase

import android.net.Uri
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ExceptionHandler
import com.google.firebase.Firebase
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.storage
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

/**
 * Gestor para operaciones con Firebase Cloud Storage.
 *
 * Proporciona funciones para subir, descargar y eliminar archivos en Firebase Storage de manera
 * simplificada y con manejo de errores.
 *
 * @author EJS Studios
 */
object CloudStorageManager {

    private val storage = Firebase.storage
    private val storageRefer = storage.reference

    /**
     * Obtiene la ruta de almacenamiento para una tienda específica.
     *
     * @param storeId ID de la tienda
     * @return Ruta en formato "stores/{storeId}"
     */
    fun getStorePath(storeId: String): String {
        return "stores/$storeId"
    }

    /**
     * Sube una imagen de perfil de tienda a Firebase Storage.
     *
     * @param storeId ID de la tienda
     * @param imageByteArray Array de bytes de la imagen
     * @return URL de descarga de la imagen subida
     * @throws Exception Si falla la subida o la obtención de la URL
     *
     * @example
     * ```kotlin
     * try {
     *     val imageUrl = CloudStorageManager.uploadStoreImage(
     *         storeId = "store123",
     *         imageByteArray = imageBytes
     *     )
     *     println("Imagen subida: $imageUrl")
     * } catch (e: Exception) {
     *     println("Error: ${e.message}")
     * }
     * ```
     */
    suspend fun uploadStoreImage(storeId: String, imageByteArray: ByteArray): String {
        return suspendCancellableCoroutine { continuation ->
            val fileName = "store-profile-$storeId.jpg"
            val path = getStorePath(storeId) + "/store-profile/$fileName"
            val storageRef = storageRefer.child(path)

            val uploadTask = storageRef.putBytes(imageByteArray)
            uploadTask
                .addOnSuccessListener {
                    storageRef.downloadUrl
                        .addOnSuccessListener { uri -> continuation.resume(uri.toString()) }
                        .addOnFailureListener { e ->
                            ExceptionHandler.handleCaughtException(e)
                            continuation.resumeWithException(e)
                        }
                }
                .addOnFailureListener { e ->
                    ExceptionHandler.handleCaughtException(e)
                    continuation.resumeWithException(e)
                }

            continuation.invokeOnCancellation { uploadTask.cancel() }
        }
    }

    /**
     * Sube un archivo a una ruta específica en Storage.
     *
     * @param path Ruta donde se guardará el archivo
     * @param fileName Nombre del archivo
     * @param filePath URI del archivo local
     *
     * @example
     * ```kotlin
     * val fileUri = Uri.parse("content://...")
     * CloudStorageManager.uploadFile("users/user123", "profile.jpg", fileUri)
     * ```
     */
    suspend fun uploadFile(path: String, fileName: String, filePath: Uri) {
        val fileRef = getStorageReference(path).child(fileName)
        val uploadTask = fileRef.putFile(filePath)
        uploadTask.await()
    }

    /**
     * Obtiene una referencia de Storage para una ruta específica.
     *
     * @param path Ruta en Storage
     * @return StorageReference para la ruta especificada
     */
    fun getStorageReference(path: String): StorageReference {
        return storageRefer.child(path)
    }

    /**
     * Obtiene las URLs de todas las imágenes en una ruta específica.
     *
     * @param path Ruta donde buscar las imágenes
     * @return Lista de URLs de descarga de las imágenes
     *
     * @example
     * ```kotlin
     * val imageUrls = CloudStorageManager.getImagesFromPath("stores/store123")
     * imageUrls.forEach { url ->
     *     println("Imagen: $url")
     * }
     * ```
     */
    suspend fun getImagesFromPath(path: String): List<String> {
        val imageUrls = mutableListOf<String>()
        val listResult: ListResult = getStorageReference(path).listAll().await()
        for (item in listResult.items) {
            val url = item.downloadUrl.await().toString()
            imageUrls.add(url)
        }
        return imageUrls
    }

    /**
     * Elimina todas las imágenes de una tienda.
     *
     * @param storeId ID de la tienda
     * @return true si se eliminaron correctamente, false si la carpeta no existe
     *
     * @example
     * ```kotlin
     * val deleted = CloudStorageManager.deleteStoreImages("store123")
     * if (deleted) {
     *     println("Imágenes eliminadas")
     * }
     * ```
     */
    suspend fun deleteStoreImages(storeId: String): Boolean {
        val storeImagesPath = getStorePath(storeId)
        return if (folderExists(storeImagesPath)) {
            deleteAllFilesInPath(storeImagesPath)
        } else {
            Loggers.log(
                "e",
                "Storage",
                "La carpeta de imágenes de la tienda no existe: $storeImagesPath"
            )
            false
        }
    }

    /**
     * Elimina todos los archivos en una ruta específica de manera recursiva.
     *
     * @param path Ruta donde eliminar los archivos
     * @return true si se eliminaron correctamente, false si hubo un error
     */
    suspend fun deleteAllFilesInPath(path: String): Boolean {
        return try {
            val storageRef = getStorageReference(path)
            val listResult: ListResult = storageRef.listAll().await()

            // Eliminar todos los archivos en la carpeta
            for (item in listResult.items) {
                item.delete().await()
                Loggers.log("e", "Storage", "Item a eliminar ${item.name}")
            }

            // Recorrer subcarpetas recursivamente
            for (prefix in listResult.prefixes) {
                deleteAllFilesInPath(prefix.path)
                Loggers.log("e", "Storage", "Llamada recursiva para subcarpetas ${prefix.path}")
            }
            Loggers.log("e", "Storage", "Todos los archivos en la ruta $path han sido eliminados.")
            true
        } catch (e: Exception) {
            Loggers.log("e", "Storage", "Error al eliminar archivos en la ruta $path $e")
            false
        }
    }

    /**
     * Sube un array de bytes a una ruta específica en Storage.
     * @return URL de descarga
     */
    suspend fun uploadBytes(path: String, fileName: String, bytes: ByteArray): String {
        val fileRef = getStorageReference(path).child(fileName)
        val uploadTask = fileRef.putBytes(bytes)
        uploadTask.await()
        return fileRef.downloadUrl.await().toString()
    }

    /**
     * Sube una imagen de perfil de usuario a Firebase Storage.
     */
    suspend fun uploadUserProfileImage(userId: String, imageByteArray: ByteArray): String {
        val path = "users/$userId"
        val fileName = "profile.jpg"
        return uploadBytes(path, fileName, imageByteArray)
    }

    /**
     * Sube una imagen de perfil de página a Firebase Storage.
     */
    suspend fun uploadPageProfileImage(userId: String, pageId: String, imageByteArray: ByteArray): String {
        val path = "users/$userId/socialhub/$pageId"
        val fileName = "profile.jpg"
        return uploadBytes(path, fileName, imageByteArray)
    }

    /**
     * Sube una imagen de perfil de tienda.
     *
     * @param storeId ID de la tienda
     * @param fileName Nombre del archivo
     * @param filePath URI del archivo local
     */
    suspend fun uploadStoreProfileImage(storeId: String, fileName: String, filePath: Uri) {
        val storeProfilePath = "stores/$storeId/store-profile"
        uploadFile(storeProfilePath, fileName, filePath)
    }

    /**
     * Sube el archivo de texto de contexto a Firebase Storage.
     */
    suspend fun uploadContextFile(userId: String, pageId: String, bytes: ByteArray): String {
        val path = "users/$userId/socialhub/$pageId/context"
        val fileName = "context_file.txt"
        return uploadBytes(path, fileName, bytes)
    }

    /**
     * Sube una imagen o video de contexto a Firebase Storage usando su nombre sanitizado completo (con extensión).
     */
    suspend fun uploadContextMedia(userId: String, pageId: String, fileNameWithExt: String, bytes: ByteArray): String {
        val path = "users/$userId/socialhub/$pageId/context/media"
        return uploadBytes(path, fileNameWithExt, bytes)
    }

    /**
     * Elimina el archivo de contexto de una página.
     */
    suspend fun deleteContextFile(userId: String, pageId: String): Boolean {
        return try {
            val path = "users/$userId/socialhub/$pageId/context/context_file.txt"
            storageRefer.child(path).delete().await()
            true
        } catch (e: Exception) { false }
    }

    /**
     * Elimina un archivo de media específico de una página.
     */
    suspend fun deleteContextMedia(userId: String, pageId: String, fileNameWithExt: String): Boolean {
        return try {
            val path = "users/$userId/socialhub/$pageId/context/media/$fileNameWithExt"
            storageRefer.child(path).delete().await()
            true
        } catch (e: Exception) { false }
    }

    /**
     * Verifica si existe una carpeta en Storage.
     *
     * @param path Ruta de la carpeta a verificar
     * @return true si la carpeta existe y contiene archivos o subcarpetas
     */
    suspend fun folderExists(path: String): Boolean {
        return try {
            val listResult: ListResult = getStorageReference(path).listAll().await()
            listResult.items.isNotEmpty() || listResult.prefixes.isNotEmpty()
        } catch (e: Exception) {
            Loggers.log(
                "e",
                "folderExists",
                "Error verificando existencia de la carpeta: ${e.message}"
            )
            false
        }
    }

    /**
     * Obtiene todas las URLs de imágenes de una tienda.
     *
     * @param storeId ID de la tienda
     * @return Lista de URLs de las imágenes
     */
    suspend fun getStoreImages(storeId: String): List<String> {
        val storeImagesPath = "stores/$storeId"
        return getImagesFromPath(storeImagesPath)
    }
}
