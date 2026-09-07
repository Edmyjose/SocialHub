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
package com.ejsstudios.socialhub.ui.views.hubsocial

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ejsstudios.socialhub.firebase.db.repository.SocialRepository
import com.ejsstudios.socialhub.firebase.login.LoginRepository
import com.ejsstudios.socialhub.platforms.facebook.FacebookViewModel
import com.ejsstudios.socialhub.platforms.reddit.RedditViewModel
import com.ejsstudios.socialhub.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HubSocialUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class HubSocialViewModel(
    application: Application,
    private val loginRepository: LoginRepository,
    private val socialRepository: SocialRepository,
    val facebookViewModel: FacebookViewModel,
    val redditViewModel: RedditViewModel
) : BaseViewModel<HubSocialUiState>(application, HubSocialUiState()) {

    val user = loginRepository.user
    val pages = facebookViewModel.allPages

    fun togglePage(pageId: String, isActive: Boolean) {
        viewModelScope.launch {
            socialRepository.togglePageActivation(pageId, isActive)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
