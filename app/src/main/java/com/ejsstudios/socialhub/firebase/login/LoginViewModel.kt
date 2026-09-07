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

import android.app.Application
import android.content.Context
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.lifecycle.viewModelScope
import com.ejsstudios.socialhub.R
import com.ejsstudios.socialhub.firebase.db.RealtimeSyncManager
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.viewmodels.BaseViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel de Login optimizado para el flujo de Sincronización Maestra.
 */
class LoginViewModel(
    application: Application,
    private val loginRepository: LoginRepository,
    private val syncManager: RealtimeSyncManager
) : BaseViewModel<LoginUiState>(application, LoginUiState()) {

    val user = loginRepository.user

    /**
     * Inicia sesión con las credenciales de Google obtenidas.
     */
    fun onGoogleCredential(result: GetCredentialResponse) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Autenticando...") }
            try {
                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
                    
                    loginRepository.syncGoogleUser(googleIdToken).fold(
                        onSuccess = { authUser ->
                            waitForInitialTruth(authUser)
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(isLoading = false, error = error.localizedMessage) }
                        }
                    )
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Tipo de credencial no soportado") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
            }
        }
    }

    /**
     * Lógica centralizada para esperar el primer snapshot de Firestore antes de decidir.
     */
    private suspend fun waitForInitialTruth(authUser: UserFirestore) {
        _uiState.update { it.copy(loadingMessage = "Sincronizando con la nube...") }
        
        // 1. Activar Listeners (esto hará que Firestore responda)
        syncManager.startSync(authUser.id)
        
        // 2. Esperar a que el Listener de Perfil emita su primer valor (Success incluso si es null)
        syncManager.isInitialSyncComplete.filter { it }.first()
        
        val cloudUser = loginRepository.user.value
        
        if (cloudUser == null) {
            // Caso B: Usuario nuevo real en Firestore. Creamos su perfil.
            _uiState.update { it.copy(loadingMessage = "Creando nuevo perfil...") }
            loginRepository.createInitialUserProfile(authUser).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true, loadingMessage = null) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = "Fallo al crear perfil: ${e.localizedMessage}") }
                }
            )
        } else {
            // Caso A: El usuario ya existía en Firestore y el Listener ya llenó Room.
            _uiState.update { it.copy(isLoading = false, isLoginSuccessful = true, loadingMessage = null) }
        }
    }

    /**
     * Construye la petición para el Credential Manager.
     */
    fun buildSignInRequest(context: Context): GetCredentialRequest {
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(context.getString(R.string.web_client_id))
            .setAutoSelectEnabled(false)
            .build()

        return GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
    }

    fun onGoogleSignInError(message: String) {
        _uiState.update { it.copy(isLoading = false, error = message) }
    }

    fun onLoginError(message: String) {
        _uiState.update { it.copy(isLoading = false, error = message) }
    }

    suspend fun checkActiveSession(isLoading:(Boolean, Boolean?, String?) -> Unit = { _, _, _ -> }) {
        val firebaseUser = loginRepository.getCurrentUser()
        if (firebaseUser != null) {
            _uiState.update { it.copy(isLoading = true, loadingMessage = "Verificando sesión...") }
            isLoading(true, null, "Verificando sesión...")
            
            // Re-arrancar sincronización si ya estamos autenticados en Firebase Auth
            val authUser = UserFirestore(
                id = firebaseUser.uid,
                name = firebaseUser.displayName ?: "Usuario Social",
                email = firebaseUser.email ?: "",
                picture = firebaseUser.photoUrl?.toString()
            )
            
            waitForInitialTruth(authUser)
            
            if (_uiState.value.isLoginSuccessful == true) {
                isLoading(false, true, null)
            } else {
                isLoading(false, false, null)
            }
        } else {
            _uiState.update { it.copy(isLoginSuccessful = false) }
            isLoading(false, false, null)
        }
    }

    fun loginAsGuest(email: String, password: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, loadingMessage = "Entrando como invitado...") }
            loginRepository.signInWithGuestUser(email, password).fold(
                onSuccess = {
                    val firebaseUser = loginRepository.getCurrentUser()
                    if (firebaseUser != null) {
                        val guestUser = UserFirestore(id = firebaseUser.uid)
                        waitForInitialTruth(guestUser)
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.localizedMessage) }
                }
            )
        }
    }

    fun onLoginStarted() {
        _uiState.update { it.copy(isLoading = true, error = null, loadingMessage = "Iniciando...") }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                syncManager.stopSync() // Detener antes de borrar datos
                loginRepository.signOut()
                _uiState.update { LoginUiState(isLoading = false, isLoginSuccessful = false) }
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                syncManager.stopSync()
                loginRepository.deleteAccount().fold(
                    onSuccess = {
                        _uiState.update { LoginUiState(isLoading = false, isLoginSuccessful = false) }
                    },
                    onFailure = { e ->
                        _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                    }
                )
            } catch (e: Exception) {
                handleException(e)
            }
        }
    }
}
