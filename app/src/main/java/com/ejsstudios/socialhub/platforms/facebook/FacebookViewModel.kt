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
package com.ejsstudios.socialhub.platforms.facebook

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.repository.SocialRepository
import com.ejsstudios.socialhub.viewmodels.BaseViewModel
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FacebookUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingPages: List<SocialMediaFirestore> = emptyList(),
    val currentPageToLink: SocialMediaFirestore? = null,
    val otherOwnerId: String? = null,
    val isLinkingComplete: Boolean = false
)

class FacebookViewModel(
    application: Application,
    private val facebookRepository: FacebookRepository,
    private val socialRepository: SocialRepository
) : BaseViewModel<FacebookUiState>(application, FacebookUiState()) {

    val allPages = socialRepository.allPages
    val activePages = socialRepository.activePages
    val selectedPage = socialRepository.selectedPage

    val facebookCallback = object : FacebookCallback<LoginResult> {
        override fun onSuccess(result: LoginResult) {
            Loggers.log("e", "FacebookViewModel", "Facebook Login Success")
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true, error = null) }

                // Obtenemos el userId actual desde el repositorio
                val userId = socialRepository.userFlow.value?.id
                if (userId == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "Sesión de usuario no encontrada"
                        )
                    }
                    return@launch
                }

                facebookRepository.syncAndLinkFacebook(result.accessToken.token).fold(
                    onSuccess = {
                        Loggers.log("e", "FacebookViewModel", "Linking Success, fetching pages...")
                        fetchPagesAfterLinking(result.accessToken.token, userId)
                    },
                    onFailure = { error ->
                        Loggers.log("e", "FacebookViewModel", "Linking Failed: ${error.message}")
                        _uiState.update { it.copy(error = "Vínculo Auth omitido: ${error.localizedMessage}") }
                        fetchPagesAfterLinking(result.accessToken.token, userId)
                    }
                )
            }
        }

        private fun fetchPagesAfterLinking(token: String, userId: String) {
            viewModelScope.launch {
                facebookRepository.getUserPages(token, userId).fold(
                    onSuccess = { fetchedPages ->
                        if (fetchedPages.isNotEmpty()) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    pendingPages = fetchedPages,
                                    isLinkingComplete = false
                                )
                            }
                            showNextPendingPage()
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    error = "No se encontraron páginas."
                                )
                            }
                        }
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = error.localizedMessage
                            )
                        }
                    }
                )
            }
        }

        override fun onCancel() {
            _uiState.update { it.copy(isLoading = false, error = "Vinculación cancelada") }
        }

        override fun onError(error: FacebookException) {
            _uiState.update { it.copy(isLoading = false, error = error.localizedMessage) }
        }
    }

    private fun showNextPendingPage() {
        val next = _uiState.value.pendingPages.firstOrNull()
        if (next != null) {
            viewModelScope.launch {
                val owner = socialRepository.checkPageOwnership(next.id)
                _uiState.update {
                    it.copy(
                        currentPageToLink = next,
                        otherOwnerId = owner,
                        pendingPages = it.pendingPages.drop(1)
                    )
                }
            }
        } else {
            _uiState.update {
                it.copy(
                    currentPageToLink = null,
                    otherOwnerId = null,
                    isLinkingComplete = true
                )
            }
        }
    }

    fun linkCurrentPage() {
        val page = _uiState.value.currentPageToLink ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            socialRepository.linkPage(page).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    showNextPendingPage()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
            )
        }
    }

    fun skipCurrentPage() {
        showNextPendingPage()
    }

    fun togglePage(pageId: String, isActive: Boolean) {
        viewModelScope.launch {
            socialRepository.togglePageActivation(pageId, isActive)
        }
    }

    fun selectPage(page: SocialMediaFirestore) {
        socialRepository.selectPage(page)
    }

    fun onLinkingStarted() {
        _uiState.update { it.copy(isLoading = true, error = null) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
