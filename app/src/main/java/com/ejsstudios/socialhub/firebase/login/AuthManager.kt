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
package com.ejsstudios.socialhub.firebase.login

import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.firebase.db.model.toFirestore
import com.facebook.login.LoginManager
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

/**
 * Manager responsible for Firebase Authentication operations.
 */
class AuthManager {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val TAG = "AuthManager"

    /**
     * Authenticates with Firebase using a Google ID Token.
     */
    suspend fun signInWithGoogle(idToken: String): Result<UserFirestore> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user.toFirestore())
            } else {
                Result.failure(Exception("Firebase user is null after Google sign-in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sign in with Email and Password.
     * Used for the Guest/Reviewer login to bypass Firestore permissions.
     */
    suspend fun signInWithEmail(email: String, password: String): Result<FirebaseUser> {
        return try {
            val authResult = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = authResult.user ?: throw Exception("Auth failed")

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Authenticates with Firebase using a Facebook Access Token.
     */
    suspend fun signInWithFacebook(accessToken: String): Result<UserFirestore> {
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val authResult = auth.signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                Result.success(user.toFirestore())
            } else {
                Result.failure(Exception("Firebase user is null after sign-in"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Links a Facebook account to the currently signed-in user.
     * This provides unified identity (Google + Facebook) under the same Firebase UID.
     */
    suspend fun linkFacebook(accessToken: String): Result<FirebaseUser> {
        val user =
            auth.currentUser ?: return Result.failure(Exception("No user is currently signed in"))
        return try {
            val credential = FacebookAuthProvider.getCredential(accessToken)
            val result = user.linkWithCredential(credential).await()
            val updatedUser = result.user ?: throw Exception("Linking failed")
            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out from Firebase and Facebook SDK.
     * Ensures all session tokens are invalidated and logs any broker failures.
     */
    fun signOut() {
        try {
            Loggers.log("e", TAG, "Iniciando proceso de cierre de sesión en AuthManager")

            // 1. Firebase SignOut
            auth.signOut()
            Loggers.log("e", TAG, "Firebase Auth: Sesión cerrada.")

            // 2. Facebook SDK SignOut
            // Crucial para evitar que el Broker de Google/Facebook mantenga la sesión
            LoginManager.getInstance().logOut()
            Loggers.log("e", TAG, "Facebook SDK: Sesión cerrada.")

        } catch (e: Exception) {
            Loggers.log("e", TAG, "Error crítico durante el signOut: ${e.message}")
            // Relanzamos la excepción para que el ViewModel pueda capturarla y mostrarla en la UI
            throw e
        }
    }

    /**
     * Gets the current authenticated user.
     */
    val currentUser: FirebaseUser?
        get() = auth.currentUser
}
