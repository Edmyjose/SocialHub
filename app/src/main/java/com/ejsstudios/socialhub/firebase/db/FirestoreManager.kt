/*
 * Copyright (c) 2025-2026. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 */
package com.ejsstudios.socialhub.firebase.db

import android.annotation.SuppressLint
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ExceptionHandler
import com.ejsstudios.socialhub.error.UserNotFoundException
import com.ejsstudios.socialhub.firebase.db.states.FirestoreState
import com.ejsstudios.socialhub.utils.Constants.COLLECTION_USERS
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.Transaction
import kotlin.reflect.KClass
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Gestor para operaciones con Firebase Firestore.
 *
 * Proporciona funciones para realizar operaciones CRUD (Crear, Leer, Actualizar, Eliminar) en
 * Firestore de manera simplificada y con manejo de errores.
 *
 * @author EJS Studios
 */
object FirestoreManager {

    private const val TAG = "FirestoreManager"

    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()

    /**
     * Agrega un nuevo documento a una colección.
     *
     * @param collection Nombre de la colección
     * @param data Mapa con los datos del documento
     * @return Result con el ID del documento creado o el error
     *
     * @example
     * ```kotlin
     * val data = mapOf(
     *     "name" to "Juan",
     *     "age" to 25,
     *     "email" to "juan@example.com"
     * )
     * val result = FirestoreManager.addDocument("users", data)
     * result.onSuccess { documentId ->
     *     println("Documento creado con ID: $documentId")
     * }
     * ```
     */
    suspend fun addDocument(
        collection: String,
        data: Map<String, Any?>,
        id: String? = null
    ): Result<String> {
        // Si el ID es nulo, se genera uno automáticamente
        val docRef =
            if (id != null) {
                firestore.collection(collection).document(id)
            } else {
                firestore.collection(collection).document()
            }

        val docId = docRef.id

        return try {
            docRef.set(data).await()
            Loggers.log(
                "i",
                TAG,
                "Documento agregado con éxito en la colección: $collection con el ID: $docId"
            )
            Result.success(docId)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    suspend fun addDocumentToSubcollection(
        userId: String,
        subcollection: String,
        data: Map<String, Any?>,
        docId: String? = null
    ): Result<String> {

        val parentRef = firestore
            .collection(COLLECTION_USERS)
            .document(userId)
            .collection(subcollection)

        return try {

            val finalId = if (!docId.isNullOrBlank()) {
                docId
            } else {
                // Referencia al contador
                val counterRef = firestore
                    .collection("counters")
                    .document("${COLLECTION_USERS}_${userId}_${subcollection}")

                // Transacción para generar ID incremental
                firestore.runTransaction { transaction ->
                    val snapshot = transaction.get(counterRef)
                    val next = (snapshot.getLong("next") ?: 0L) + 1L

                    transaction.set(
                        counterRef,
                        mapOf("next" to next),
                        SetOptions.merge()
                    )

                    next.toString()
                }.await()
            }

            // Guardar el documento usando el ID final
            parentRef.document(finalId).set(data).await()

            Result.success(finalId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un documento existente o lo crea si no existe (Upsert).
     * Usa set con SetOptions.merge() para evitar NOT_FOUND exceptions.
     *
     * @param collection Nombre de la colección
     * @param documentId ID del documento a actualizar
     * @param updatedData Mapa con los campos a actualizar
     * @return Result indicando éxito o error
     */
    suspend fun updateDocument(
        collection: String,
        documentId: String,
        updatedData: Map<String, Any?>
    ): Result<Unit> {
        return try {
            val docRef = firestore.collection(collection).document(documentId)

            if (updatedData.isNotEmpty()) {
                // Cambiado de update() a set(merge) para manejar documentos inexistentes
                docRef.set(updatedData, SetOptions.merge()).await()
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Updated data is empty"))
            }
        } catch (e: FirebaseFirestoreException) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza un documento dentro de una subcolección o lo crea si no existe (Upsert).
     *
     * @param parentDocumentId ID del documento padre (ej: "user123")
     * @param subcollection Subcolección (ej: "prefs", "ia", etc.)
     * @param documentId ID del documento dentro de la subcolección
     * @param updatedData Campos a actualizar
     */
    suspend fun updateSubcollectionDocument(
        parentDocumentId: String,
        subcollection: String,
        documentId: String,
        updatedData: Map<String, Any?>
    ): Result<Unit> {
        return try {
            if (updatedData.isEmpty()) {
                return Result.failure(IllegalArgumentException("Updated data is empty"))
            }

            val docRef =
                firestore
                    .collection(COLLECTION_USERS)
                    .document(parentDocumentId)
                    .collection(subcollection)
                    .document(documentId)

            // Cambiado de update() a set(merge) para manejar documentos inexistentes
            docRef.set(updatedData, SetOptions.merge()).await()

            Result.success(Unit)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un documento simple (sin transacción).
     *
     * @param collection Nombre de la colección
     * @param documentId ID del documento a eliminar
     * @return Result indicando éxito o error
     *
     * @example
     * ```kotlin
     * FirestoreManager.deleteDocument("users", "user123")
     * ```
     */
    suspend fun deleteDocument(collection: String, documentId: String): Result<Unit> {
        return try {
            firestore.collection(collection).document(documentId).delete().await()
            Loggers.log("i", TAG, "Documento eliminado: $collection/$documentId")
            Result.success(Unit)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    /** Elimina un documento dentro de una subcolección. */
    suspend fun deleteSubcollectionDocument(
        parentDocumentId: String,
        subcollection: String,
        documentId: String
    ): Result<Unit> {
        return try {
            val docRef = firestore
                .collection(COLLECTION_USERS)
                .document(parentDocumentId)
                .collection(subcollection)
                .document(documentId)

            docRef.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    /**
     * Genera una referencia de documento y su ID.
     *
     * @param collection Nombre de la colección
     * @return Par con la referencia del documento y su ID
     *
     * @example
     * ```kotlin
     * val (docRef, docId) = FirestoreManager.generateDocumentId("users")
     * println("Nuevo ID generado: $docId")
     * ```
     */
    fun generateDocumentId(collection: String): Pair<DocumentReference, String> {
        val docRef = firestore.collection(collection).document()
        return Pair(docRef, docRef.id)
    }

    /**
     * Obtiene un documento por su ID.
     *
     * @param collection Nombre de la colección
     * @param id ID del documento
     * @param convertDocument Función para convertir el DocumentSnapshot al tipo deseado
     * @return El documento convertido o null si no existe
     *
     * @example
     * ```kotlin
     * data class User(val name: String, val age: Int)
     *
     * val user = FirestoreManager.getDocumentById("users", "user123") { doc ->
     *     User(
     *         name = doc.getString("name") ?: "",
     *         age = doc.getLong("age")?.toInt() ?: 0
     *     )
     * }
     * ```
     */
    suspend fun <T> getDocumentById(
        collection: String,
        id: String,
        convertDocument: (DocumentSnapshot) -> T
    ): T? {
        return try {
            val documentSnapshot = firestore.collection(collection).document(id).get().await()

            if (documentSnapshot.exists()) {
                convertDocument(documentSnapshot)
            } else {
                null
            }
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            null
        }
    }

    /**
     * Obtiene todos los documentos de una subcolección de un usuario y los convierte a un modelo.
     *
     * @param parentCollection Nombre de la colección principal (ej. "users")
     * @param parentDocumentId ID del usuario
     * @param subcollection Nombre de la subcolección (ej. "prefs", "ia", etc.)
     * @param convertDocument Función para mapear DocumentSnapshot a modelo T
     * @return Result<List<T>> con los documentos convertidos o error
     */
    suspend fun <T> getSubcollectionDocuments(
        parentCollection: String,
        parentDocumentId: String,
        subcollection: String,
        convertDocument: (DocumentSnapshot) -> T
    ): Result<List<T>> {
        return try {
            val querySnapshot =
                firestore
                    .collection(parentCollection)
                    .document(parentDocumentId)
                    .collection(subcollection)
                    .get()
                    .await()

            val list = querySnapshot.documents.mapNotNull { doc -> convertDocument(doc) }
            Result.success(list)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene un documento específico de una subcolección de un usuario y lo convierte a un modelo.
     *
     * @param parentCollection Nombre de la colección principal
     * @param parentDocumentId ID del usuario
     * @param subcollection Nombre de la subcolección
     * @param documentId ID del documento dentro de la subcolección
     * @param convertDocument Función para mapear DocumentSnapshot a modelo T
     * @return Result<T> con el documento convertido o error
     */
    suspend fun <T> getSubcollectionDocumentById(
        parentCollection: String,
        parentDocumentId: String,
        subcollection: String,
        documentId: String,
        convertDocument: (DocumentSnapshot) -> T
    ): Result<T> {
        return try {
            val snapshot =
                firestore
                    .collection(parentCollection)
                    .document(parentDocumentId)
                    .collection(subcollection)
                    .document(documentId)
                    .get()
                    .await()

            if (!snapshot.exists())
                throw Exception("Document $documentId not found in $subcollection")
            Result.success(convertDocument(snapshot))
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    /**
     * Obtiene un documento por ID dentro de una subcolección.
     *
     * @return Result<DocumentSnapshot>
     */
    suspend fun getSubcollectionDocumentById(
        parentCollection: String,
        parentDocumentId: String,
        subcollection: String,
        documentId: String
    ): Result<DocumentSnapshot> {
        return try {
            val docRef =
                firestore
                    .collection(parentCollection)
                    .document(parentDocumentId)
                    .collection(subcollection)
                    .document(documentId)

            val snapshot = docRef.get().await()

            Result.success(snapshot)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene un documento de una subcolección de un usuario y lo convierte a un modelo T.
     *
     * @param T Tipo de dato de destino (UserProfile, Pref, Model, etc.)
     * @param parentDocumentId ID del documento principal (ej. usuario)
     * @param subcollection Nombre de la subcolección
     * @param documentId ID del documento dentro de la subcolección
     * @param clazz KClass del modelo T para la conversión
     * @return Result<T> con el documento convertido o error si no existe
     *
     * @example
     * ```kotlin
     * // Obtener un Pref de la subcolección "prefs"
     * val prefResult: Result<Pref> = repository.getSubcollectionDocumentById(
     *     parentDocumentId = "uid123",
     *     subcollection = "prefs",
     *     documentId = "pref1",
     *     clazz = Pref::class
     * )
     * ```
     */
    suspend fun <T : Any> getSubcollectionDocumentById(
        parentDocumentId: String,
        subcollection: String,
        documentId: String,
        clazz: KClass<T>
    ): Result<T> = runCatching {
        require(documentId.isNotEmpty()) { "Document ID required" }

        val snapshot =
            FirestoreManager.getSubcollectionDocumentById(
                parentCollection = COLLECTION_USERS,
                parentDocumentId = parentDocumentId,
                subcollection = subcollection,
                documentId = documentId
            )
                .getOrElse { throw it }

        val document =
            snapshot.toObject(clazz.java)
                ?: throw UserNotFoundException("Document not found in subcollection")

        // Opcional: guardar en cache local si quieres
        document
    }

    fun <T : Any> getCollectionDocumentByIdFlow(
        documentId: String,
        collection: String,
        clazz: KClass<T>
    ): Flow<FirestoreState<T?>> = callbackFlow {
        require(documentId.isNotEmpty()) { "Parent document ID required" }

        val documentRef = firestore.collection(collection).document(documentId)

        val listener =
            documentRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // NO cerrar. Solo logear y emitir null.
                    trySend(FirestoreState.Error(error))
                    return@addSnapshotListener
                }

                val doc = snapshot?.toObject(clazz.java)
                trySend(FirestoreState.Success(doc))
            }

        awaitClose { listener.remove() }
    }

    fun <T : Any> getSubcollectionDocumentByIdFlow(
        parentDocumentId: String,
        subcollection: String,
        documentId: String,
        clazz: KClass<T>
    ): Flow<FirestoreState<T?>> = callbackFlow {
        require(parentDocumentId.isNotEmpty()) { "Parent document ID required" }

        val documentRef = firestore
            .collection(COLLECTION_USERS)
            .document(parentDocumentId)
            .collection(subcollection)
            .document(documentId)

        val listener = documentRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // NO cerrar. Solo logear y emitir null.
                trySend(FirestoreState.Error(error))
                return@addSnapshotListener
            }

            val doc = snapshot?.toObject(clazz.java)
            trySend(FirestoreState.Success(doc))
        }

        awaitClose { listener.remove() }
    }


    /**
     * Obtiene documentos de una colección que coincidan con un campo específico en tiempo real.
     *
     * @param T Tipo de dato de destino
     * @param collection Nombre de la colección
     * @param fieldName Nombre del campo a filtrar
     * @param fieldValue Valor del campo a buscar
     * @param clazz KClass del modelo T para la conversión
     * @return Flow<FirestoreState<List<T>>> con actualizaciones en tiempo real
     *
     * @example
     * ```kotlin
     * // Obtener todos los contextos de una página específica
     * val contextsFlow = FirestoreManager.getCollectionDocumentsByFieldFlow(
     *     collection = "context",
     *     fieldName = "pageId",
     *     fieldValue = "page123",
     *     clazz = PageAIContext::class
     * )
     * ```
     */
    fun <T : Any> getCollectionDocumentsByFieldFlow(
        collection: String,
        fieldName: String,
        fieldValue: Any,
        clazz: KClass<T>
    ): Flow<FirestoreState<List<T>>> = callbackFlow {
        require(collection.isNotEmpty()) { "Collection name required" }
        require(fieldName.isNotEmpty()) { "Field name required" }

        val query = firestore.collection(collection).whereEqualTo(fieldName, fieldValue)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Loggers.log(
                    "e",
                    TAG,
                    "Error listening to collection query: ${error.message}"
                )
                trySend(FirestoreState.Error(error))
                return@addSnapshotListener
            }

            val documents = snapshot?.documents?.mapNotNull { it.toObject(clazz.java) }
                ?: emptyList()

            trySend(FirestoreState.Success(documents))
        }

        awaitClose {
            Loggers.log("e", TAG, "Closing listener for collection: $collection, field: $fieldName")
            listener.remove()
        }
    }

    fun <T : Any> getSubcollectionDocumentsByFieldFlow(
        parentDocumentId: String,
        subcollection: String,
        fieldName: String,
        fieldValue: Any,
        clazz: KClass<T>
    ): Flow<FirestoreState<List<T>>> = callbackFlow {
        require(subcollection.isNotEmpty()) { "Collection name required" }
        require(fieldName.isNotEmpty()) { "Field name required" }

        val query = firestore
            .collection(COLLECTION_USERS)
            .document(parentDocumentId)
            .collection(subcollection)
            .whereEqualTo(fieldName, fieldValue)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Loggers.log(
                    "e",
                    TAG,
                    "Error listening to collection query: ${error.message}"
                )
                trySend(FirestoreState.Error(error))
                return@addSnapshotListener
            }

            val documents = snapshot?.documents?.mapNotNull { it.toObject(clazz.java) }
                ?: emptyList()

            trySend(FirestoreState.Success(documents))
        }

        awaitClose {
            Loggers.log(
                "e",
                TAG,
                "Closing listener for collection: $subcollection, field: $fieldName"
            )
            listener.remove()
        }
    }

    fun <T : Any> getSubcollectionDocumentsFlow(
        parentDocumentId: String,
        subcollection: String,
        clazz: KClass<T>
    ): Flow<FirestoreState<List<T>>> = callbackFlow {
        require(parentDocumentId.isNotEmpty()) { "Parent document ID required" }

        val collectionRef =
            firestore
                .collection(COLLECTION_USERS)
                .document(parentDocumentId)
                .collection(subcollection)

        val listener =
            collectionRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(FirestoreState.Error(error))
                    return@addSnapshotListener
                }

                val list =
                    snapshot?.documents?.mapNotNull { it.toObject(clazz.java) }
                        ?: emptyList()

                trySend(FirestoreState.Success(list))
            }

        awaitClose { listener.remove() }
    }

    /**
     * Obtiene flujos de documentos de un grupo de colecciones (Collection Group).
     * Permite escuchar cambios en todas las subcolecciones con el mismo nombre en toda la DB.
     */
    fun <T : Any> getCollectionGroupDocumentsFlow(
        collectionId: String,
        fieldName: String,
        fieldValue: Any,
        clazz: KClass<T>
    ): Flow<FirestoreState<List<T>>> = callbackFlow {
        require(collectionId.isNotEmpty()) { "Collection ID required" }

        val query = firestore.collectionGroup(collectionId).whereEqualTo(fieldName, fieldValue)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(FirestoreState.Error(error))
                return@addSnapshotListener
            }

            val documents =
                snapshot?.documents?.mapNotNull { it.toObject(clazz.java) } ?: emptyList()
            trySend(FirestoreState.Success(documents))
        }

        awaitClose { listener.remove() }
    }

    /**
     * Cuenta documentos en un grupo de colecciones con filtros.
     */
    suspend fun countCollectionGroupDocuments(
        collectionId: String,
        fieldName: String,
        fieldValue: Any,
        statusField: String? = null,
        statusValue: Any? = null
    ): Int {
        return try {
            var query = firestore.collectionGroup(collectionId).whereEqualTo(fieldName, fieldValue)
            if (statusField != null && statusValue != null) {
                query = query.whereEqualTo(statusField, statusValue)
            }
            val snapshot = query.get().await()
            snapshot.size()
        } catch (e: Exception) {
            0
        }
    }

    /**
     * Elimina documentos de un grupo de colecciones que coincidan con un criterio.
     */
    suspend fun deleteCollectionGroupDocuments(
        collectionId: String,
        fieldName: String,
        fieldValue: Any
    ): Result<Unit> {
        return try {
            val snapshot = firestore.collectionGroup(collectionId)
                .whereEqualTo(fieldName, fieldValue)
                .get()
                .await()

            if (!snapshot.isEmpty) {
                val batch = firestore.batch()
                snapshot.documents.forEach { batch.delete(it.reference) }
                batch.commit().await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Obtiene todos los documentos de una subcolección de un usuario y los convierte a un modelo T.
     *
     * @param T Tipo de dato de destino (UserProfile, Pref, Model, etc.)
     * @param parentDocumentId ID del documento principal (ej. usuario)
     * @param subcollection Nombre de la subcolección
     * @param clazz KClass del modelo T para la conversión
     * @return Result<List<T>> con los documentos convertidos; devuelve lista vacía si no hay
     * documentos
     *
     * @example
     * ```kotlin
     * // Obtener todos los Pref de la subcolección "prefs"
     * val prefs: Result<List<Pref>> = repository.getSubcollectionDocuments(
     *     parentDocumentId = "uid123",
     *     subcollection = "prefs",
     *     clazz = Pref::class
     * )
     * ```
     */
    suspend fun <T : Any> getSubcollectionDocuments(
        parentDocumentId: String,
        subcollection: String,
        clazz: KClass<T>
    ): Result<List<T>> = runCatching {
        require(parentDocumentId.isNotEmpty()) { "Parent document ID required" }

        val list =
            FirestoreManager.getSubcollectionDocuments(
                parentCollection = COLLECTION_USERS,
                parentDocumentId = parentDocumentId,
                subcollection = subcollection
            ) { doc ->
                doc.toObject(clazz.java) // convertir a tipo genérico
            }
                .getOrElse { throw it }
                .filterNotNull() // eliminamos posibles nulls

        // opcional: guardar en cache si quieres mantener los datos sincronizados
        list
    }

    /**
     * Escucha cambios en tiempo real en una colección.
     *
     * @param collection Nombre de la colección
     * @param onDocumentChange Callback que se ejecuta cuando hay cambios
     * @return ListenerRegistration para poder detener la escucha
     *
     * @example
     * ```kotlin
     * val listener = FirestoreManager.listenToCollection("messages") { snapshot ->
     *     snapshot.documents.forEach { doc ->
     *         println("Mensaje: ${doc.data}")
     *     }
     * }
     *
     * // Para detener la escucha:
     * listener.remove()
     * ```
     */
    fun listenToCollection(
        collection: String,
        onDocumentChange: (QuerySnapshot?, Exception?) -> Unit
    ): ListenerRegistration {

        return firestore.collection(collection).addSnapshotListener { snapshots, e ->
            onDocumentChange(snapshots, e)
            /*if (e != null) {
                ExceptionHandler.handleCaughtException(e)
                return@addSnapshotListener
            }

            if (snapshots != null && !snapshots.isEmpty) {
                onDocumentChange(snapshots, e)
            }*/
        }
    }

    /**
     * Obtiene documentos que cumplan con ciertas condiciones.
     *
     * @param collection Nombre de la colección
     * @param conditions Mapa con las condiciones de búsqueda (campo -> valor)
     * @return Result con el QuerySnapshot o el error
     *
     * @example
     * ```kotlin
     * val conditions = mapOf(
     *     "age" to 25,
     *     "city" to "Madrid"
     * )
     * val result = FirestoreManager.getQuerySnapshot("users", conditions)
     * result.onSuccess { snapshot ->
     *     snapshot.documents.forEach { doc ->
     *         println("Usuario: ${doc.data}")
     *     }
     * }
     * ```
     */
    suspend fun getQuerySnapshot(
        collection: String,
        conditions: Map<String, Any>
    ): Result<QuerySnapshot> {
        return try {
            var query: Query = firestore.collection(collection)
            conditions.forEach { (key, value) -> query = query.whereEqualTo(key, value) }
            val querySnapshot = query.get().await()

            Result.success(querySnapshot)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    /**
     * Verifica si existe un documento que cumpla con las condiciones.
     *
     * @param collection Nombre de la colección
     * @param conditions Mapa con las condiciones de búsqueda
     * @return true si existe al menos un documento, false en caso contrario
     *
     * @example
     * ```kotlin
     * val exists = FirestoreManager.isFieldExistsInCollection(
     *     collection = "users",
     *     conditions = mapOf("email" to "juan@example.com")
     * )
     * if (exists) {
     *     println("El email ya está registrado")
     * }
     * ```
     */
    suspend fun isFieldExistsInCollection(
        collection: String,
        conditions: Map<String, Any>
    ): Boolean {
        return try {
            var query: Query = firestore.collection(collection)

            conditions.forEach { (key, value) -> query = query.whereEqualTo(key, value) }

            val querySnapshot = query.limit(1).get().await()
            !querySnapshot.isEmpty
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            false
        }
    }

    /**
     * Agrega un documento con transacción y lógica adicional.
     *
     * @param collection Nombre de la colección
     * @param collectionIdName Nombre del campo donde se guardará el ID
     * @param data Mapa con los datos del documento
     * @param onTransaction Función lambda para ejecutar lógica adicional en la transacción
     * @return Result con el ID del documento creado o el error
     *
     * @example
     * ```kotlin
     * val result = FirestoreManager.addWithTransaction(
     *     collection = "orders",
     *     collectionIdName = "orderId",
     *     data = mapOf("product" to "Laptop", "price" to 1000)
     * ) { transaction, orderId ->
     *     // Lógica adicional dentro de la transacción
     *     val inventoryRef = firestore.collection("inventory").document("laptop")
     *     transaction.update(inventoryRef, "stock", FieldValue.increment(-1))
     * }
     * ```
     */
    suspend fun addWithTransaction(
        collection: String,
        collectionIdName: String,
        data: Map<String, Any?>,
        onTransaction: (Transaction, String) -> Unit = { _, _ -> }
    ): Result<String> {
        val collectionRef = firestore.collection(collection).document()
        val collectionId = collectionRef.id

        val mutableData = data.toMutableMap().apply { this[collectionIdName] = collectionId }

        return try {
            firestore
                .runTransaction { transaction ->
                    transaction.set(collectionRef, mutableData)
                    onTransaction(transaction, collectionId)
                    return@runTransaction collectionId
                }
                .await()

            Result.success(collectionId)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un documento con transacción.
     *
     * @param documentCollection Nombre de la colección
     * @param documentId ID del documento a eliminar
     * @param onTransaction Función lambda para ejecutar lógica adicional en la transacción
     * @return Result indicando éxito o error
     *
     * @example
     * ```kotlin
     * FirestoreManager.deleteWithTransaction(
     *     documentCollection = "users",
     *     documentId = "user123"
     * ) { transaction ->
     *     // Eliminar documentos relacionados
     *     val ordersRef = firestore.collection("orders").document("order456")
     *     transaction.delete(ordersRef)
     * }
     * ```
     */
    suspend fun deleteWithTransaction(
        documentCollection: String,
        documentId: String,
        onTransaction: (Transaction) -> Unit = { _ -> }
    ): Result<Boolean> {
        return try {
            val documentRef = firestore.collection(documentCollection).document(documentId)

            firestore
                .runTransaction { transaction ->
                    onTransaction(transaction)
                    transaction.delete(documentRef)
                }
                .await()

            Result.success(true)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
            Result.failure(e)
        }
    }
}
