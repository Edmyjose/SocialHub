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
package com.ejsstudios.socialhub.firebase.login

import android.content.Context
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.CloudStorageManager
import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.local.dao.UserDao
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.firebase.db.model.toFirestore
import com.ejsstudios.socialhub.managers.PreferencesManager
import com.ejsstudios.socialhub.utils.Constants.COLLECTION_USERS
import com.ejsstudios.socialhub.utils.ImageUtils
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.tasks.await

/**
 * Repositorio de Autenticación (Google / Guest).
 * La sincronización de datos ahora es gestionada por RealtimeSyncManager.
 */
class LoginRepository(
    private val context: Context,
    private val authManager: AuthManager,
    private val firestoreManager: FirestoreManager,
    private val userDao: UserDao
) {
    private val prefs = PreferencesManager(context)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val user: StateFlow<UserFirestore?> = prefs.userFlow
        .stateIn(scope, SharingStarted.WhileSubscribed(5000), null)

    suspend fun isUserAuthenticated(): FirebaseUser? {
        val localUser = prefs.getUser()
        val firebaseUser = authManager.currentUser
        return if (firebaseUser?.uid == localUser?.id && localUser != null) firebaseUser else null
    }

    /**
     * Sincroniza el Auth de Google. La descarga de datos vendrá vía Listener.
     */
    suspend fun syncGoogleUser(idToken: String): Result<UserFirestore> {
        val authResult = authManager.signInWithGoogle(idToken)
        return authResult.fold(
            onSuccess = { domainUser ->
                // Guardamos solo el perfil básico de Auth si es necesario, 
                // pero RealtimeSyncManager se encargará de traer la "verdad" de Firestore.
                Result.success(domainUser)
            },
            onFailure = { Result.failure(it) }
        )
    }

    /**
     * Crea un perfil de usuario inicial en Firestore si es nuevo.
     */
    suspend fun createInitialUserProfile(user: UserFirestore): Result<Unit> {
        return try {
            val remotePic = user.picture?.let { url ->
                if (url.startsWith("http")) {
                    ImageUtils.urlToByteArray(url)?.let { bytes ->
                        try {
                            CloudStorageManager.uploadUserProfileImage(user.id, bytes)
                        } catch (e: Exception) {
                            null
                        }
                    }
                } else url
            }
            val finalUser = user.copy(picture = remotePic)
            firestoreManager.addDocument(COLLECTION_USERS, finalUser.toMap(), finalUser.id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserCredits(userId: String, newCredits: Int) {
        try {
            // FLUJO UNIDIRECCIONAL: Solo escribimos en Firestore. 
            // El Listener actualizará Room/Prefs automáticamente.
            firestoreManager.updateDocument(
                COLLECTION_USERS,
                userId,
                mapOf("credits" to newCredits)
            )
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun signInWithGuestUser(email: String, password: String): Result<Boolean> {
        val authResult = authManager.signInWithEmail(email, password)
        return authResult.fold(
            onSuccess = { firebaseUser ->
                val userDomain = firebaseUser.toFirestore()
                val userProfile =
                    userDomain.copy(name = "App Store Reviewer", authProvider = "password")
                // Para invitados, también usamos el flujo de creación inicial si no existe
                createInitialUserProfile(userProfile)
                Result.success(true)
            },
            onFailure = { Result.failure(it) }
        )
    }

    suspend fun signOut() {
        authManager.signOut()
        prefs.clearAll()
        userDao.deleteAll()
    }

    suspend fun deleteAccount(): Result<Unit> {
        val firebaseUser =
            authManager.currentUser ?: return Result.failure(Exception("No hay sesión activa"))
        val userId = firebaseUser.uid

        return try {
            Loggers.log(
                "w",
                "LoginRepository",
                ">>> INICIANDO ELIMINACIÓN TOTAL DE CUENTA: $userId <<<"
            )

            // 1. LIMPIEZA DE STORAGE (Archivos físicos)
            Loggers.log(
                "d",
                "LoginRepository",
                "Paso 1: Limpiando carpeta de usuario en Cloud Storage..."
            )
            CloudStorageManager.deleteAllFilesInPath("users/$userId")

            // 2. LIMPIEZA DE FIRESTORE (Metadatos en cascada)
            Loggers.log("d", "LoginRepository", "Paso 2: Borrando subcolecciones globales...")
            // Grupos con 'user_id'
            firestoreManager.deleteCollectionGroupDocuments("campaigns", "user_id", userId)
            firestoreManager.deleteCollectionGroupDocuments("schemes", "user_id", userId)
            firestoreManager.deleteCollectionGroupDocuments("contents", "user_id", userId)

            // Grupos con 'userId'
            firestoreManager.deleteCollectionGroupDocuments("socialmedia", "userId", userId)
            firestoreManager.deleteCollectionGroupDocuments("context", "userId", userId)
            firestoreManager.deleteCollectionGroupDocuments("media", "userId", userId)

            // Borrar documento raíz del usuario
            firestoreManager.deleteDocument(COLLECTION_USERS, userId)

            // 3. LIMPIEZA DE CONTADORES
            // Los contadores están en 'counters' con IDs 'users_{userId}_{subcol}'
            // Aunque no podemos hacer group query por ID parcial, intentaremos borrar los conocidos
            listOf("socialmedia", "campaigns", "schemes", "contents").forEach { sub ->
                firestoreManager.deleteDocument("counters", "users_${userId}_$sub")
            }

            Loggers.log("d", "LoginRepository", "✓ Firestore y Storage limpiados con éxito.")

            // 4. ELIMINACIÓN DE AUTH (El punto de no retorno)
            try {
                firebaseUser.delete().await()
                Loggers.log("d", "LoginRepository", "Usuario eliminado de Firebase Auth")
            } catch (e: Exception) {
                Loggers.log("e", "LoginRepository", "Error eliminando de Auth: ${e.message}")

                // Detectar error de re-autenticación requerida
                if (e is FirebaseAuthRecentLoginRequiredException ||
                    e.message?.contains("sensitive") == true ||
                    e.message?.contains("recent login") == true
                ) {
                    return Result.failure(Exception("Por seguridad, esta acción requiere que hayas iniciado sesión recientemente.\n\nPor favor, realiza el flujo de 'Login Fresco':\n1. Cierra tu sesión actual.\n2. Inicia sesión nuevamente.\n3. Intenta borrar tu cuenta de inmediato."))
                }
                throw e
            }

            // 3. Limpiar datos locales
            prefs.clearAll()
            userDao.deleteAll()
            Loggers.log("d", "LoginRepository", "Datos locales limpiados")

            Result.success(Unit)
        } catch (e: Exception) {
            Loggers.log("e", "LoginRepository", "Fallo total en eliminación: ${e.message}")
            Result.failure(e)
        }
    }

    fun getCurrentUser(): FirebaseUser? = authManager.currentUser
}
