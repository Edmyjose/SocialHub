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

package com.ejsstudios.socialhub.platforms.reddit

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.repository.SocialRepository
import com.ejsstudios.socialhub.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RedditUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val pendingSubreddits: List<SocialMediaFirestore> = emptyList(),
    val currentSubredditToLink: SocialMediaFirestore? = null,
    val isLinkingComplete: Boolean = false
)

class RedditViewModel(
    application: Application,
    private val redditRepository: RedditRepository,
    private val socialRepository: SocialRepository
) : BaseViewModel<RedditUiState>(application, RedditUiState()) {

    /**
     * Inicia el proceso de vinculación de Reddit tras obtener el token de acceso (OAuth2).
     */
    fun startRedditLinking(accessToken: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            val userId = socialRepository.userFlow.value?.id
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false, error = "Sesión no encontrada") }
                return@launch
            }

            redditRepository.syncAndLinkReddit(accessToken).fold(
                onSuccess = {
                    Loggers.log("e", TAG, "Reddit Linked Success, fetching subreddits...")
                    fetchSubreddits(accessToken, userId)
                },
                onFailure = { error ->
                    Loggers.log("e", TAG, "Reddit Linking Failed: ${error.message}")
                    _uiState.update { it.copy(error = "Vínculo fallido: ${error.localizedMessage}") }
                    fetchSubreddits(accessToken, userId)
                }
            )
        }
    }

    private fun fetchSubreddits(token: String, userId: String) {
        viewModelScope.launch {
            redditRepository.getUserSubreddits(token, userId).fold(
                onSuccess = { subreddits ->
                    if (subreddits.isNotEmpty()) {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                pendingSubreddits = subreddits,
                                isLinkingComplete = false
                            )
                        }
                        showNextPendingSubreddit()
                    } else {
                        _uiState.update { it.copy(isLoading = false, error = "No se encontraron subreddits.") }
                    }
                },
                onFailure = { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.localizedMessage) }
                }
            )
        }
    }

    private fun showNextPendingSubreddit() {
        val next = _uiState.value.pendingSubreddits.firstOrNull()
        if (next != null) {
            _uiState.update {
                it.copy(
                    currentSubredditToLink = next,
                    pendingSubreddits = it.pendingSubreddits.drop(1)
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    currentSubredditToLink = null,
                    isLinkingComplete = true
                )
            }
        }
    }

    fun linkCurrentSubreddit() {
        val subreddit = _uiState.value.currentSubredditToLink ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            socialRepository.linkPage(subreddit).fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    showNextPendingSubreddit()
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.localizedMessage) }
                }
            )
        }
    }

    fun skipCurrentSubreddit() {
        showNextPendingSubreddit()
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
