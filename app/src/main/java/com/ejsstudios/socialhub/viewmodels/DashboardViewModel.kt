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
package com.ejsstudios.socialhub.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.ejsstudios.socialhub.firebase.db.model.CampaignFirestore
import com.ejsstudios.socialhub.firebase.db.model.ContentStatus
import com.ejsstudios.socialhub.firebase.db.model.SchemeStatus
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.repository.CampaignRepository
import com.ejsstudios.socialhub.firebase.db.repository.CompletePageContext
import com.ejsstudios.socialhub.firebase.db.repository.ContextRepository
import com.ejsstudios.socialhub.firebase.db.repository.SchemeRepository
import com.ejsstudios.socialhub.firebase.db.repository.SocialRepository
import com.ejsstudios.socialhub.firebase.login.LoginRepository
import com.ejsstudios.socialhub.managers.PreferencesManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class DashboardEvent {
    data class ShowToast(val message: String) : DashboardEvent()
}

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isSyncing: Boolean = false,
    val error: String? = null,
    val pageContext: CompletePageContext? = null,
    val campaigns: List<CampaignFirestore> = emptyList(),
    val hasCampaigns: Boolean = false,
    val hasSchemes: Boolean = false,
    val isContextConfigured: Boolean = false,
    val syncMessage: String? = null,

    // Métricas Precisas
    val pendingSchemesCount: Int = 0,
    val pendingPostsCount: Int = 0,
    val publishedTodayCount: Int = 0,
    val lastVigilanteRun: String = "Nunca"
)

class DashboardViewModel(
    application: Application,
    private val loginRepository: LoginRepository,
    private val socialRepository: SocialRepository,
    private val contextRepository: ContextRepository,
    private val campaignRepository: CampaignRepository,
    private val schemeRepository: SchemeRepository,
    private val preferencesManager: PreferencesManager
) : BaseViewModel<DashboardUiState>(application, DashboardUiState()) {

    val user = loginRepository.user
    val pages = socialRepository.activePages
    val selectedPage = socialRepository.selectedPage

    private val _events = Channel<DashboardEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeData()
        observeMeticulousData()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeMeticulousData() {
        // Observar última ejecución del vigilante
        preferencesManager.lastVigilanteRunFlow.onEach { time ->
            _uiState.update { it.copy(lastVigilanteRun = time) }
        }.launchIn(viewModelScope)

        // Observar métricas globales de la página seleccionada
        selectedPage.flatMapLatest { page ->
            if (page == null) flowOf(Triple(0, 0, 0))
            else {
                campaignRepository.getCampaignsByPageFlow(page.id).map { campaigns ->
                    var totalPendingSchemes = 0
                    var totalPendingPosts = 0
                    var publishedToday = 0

                    for (campaign in campaigns) {
                        totalPendingSchemes += campaign.schemes.count { it.status == SchemeStatus.PENDING }
                        totalPendingPosts += campaign.posts.count { it.status == ContentStatus.READY }
                        publishedToday += campaign.posts.count { it.status == ContentStatus.PUBLISHED }
                    }
                    Triple(totalPendingSchemes, totalPendingPosts, publishedToday)
                }
            }
        }.onEach { (sPending, pPending, pPublished) ->
            _uiState.update {
                it.copy(
                    pendingSchemesCount = sPending,
                    pendingPostsCount = pPending,
                    publishedTodayCount = pPublished
                )
            }
        }.launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeData() {
        viewModelScope.launch {
            combine(user, selectedPage) { u, p -> Pair(u, p) }
                .collectLatest { (u, p) ->
                    if (u != null && p != null) {
                        loadDashboardData(u.id, p.id)
                    } else {
                        // Resetear estado si no hay selección
                        _uiState.update {
                            it.copy(
                                pageContext = null,
                                isContextConfigured = false,
                                hasCampaigns = false,
                                hasSchemes = false
                            )
                        }
                    }
                }
        }

        selectedPage.flatMapLatest { page ->
            if (page != null) campaignRepository.getCampaignsByPageFlow(page.id)
            else flowOf(emptyList())
        }.onEach { list ->
            _uiState.update { it.copy(campaigns = list, hasCampaigns = list.isNotEmpty()) }
            if (list.isNotEmpty()) {
                checkIfAnyCampaignHasSchemes(list)
            } else {
                _uiState.update { it.copy(hasSchemes = false) }
            }
        }.launchIn(viewModelScope)
    }

    private suspend fun checkIfAnyCampaignHasSchemes(campaigns: List<CampaignFirestore>) {
        var foundAny = false
        for (campaign in campaigns) {
            val schemes = schemeRepository.getSchemesByCampaignFlow(campaign.id).first()
            if (schemes.isNotEmpty()) {
                foundAny = true
                break
            }
        }
        _uiState.update { it.copy(hasSchemes = foundAny) }
    }

    private suspend fun loadDashboardData(userId: String, pageId: String) {
        val isFirstLoad = _uiState.value.pageContext == null
        if (isFirstLoad) {
            _uiState.update { it.copy(isLoading = true, syncMessage = "Cargando contexto...") }
        } else {
            _uiState.update { it.copy(isSyncing = true, syncMessage = "Actualizando datos...") }
        }

        contextRepository.getCompletePageContextFlow(userId, pageId).collectLatest { context ->
            val isConfigured = context?.context?.rawContext?.let { it.length >= 50 } ?: false
            _uiState.update {
                it.copy(
                    pageContext = context,
                    isLoading = false,
                    isSyncing = false,
                    isContextConfigured = isConfigured,
                    syncMessage = null
                )
            }
        }
    }

    fun selectPage(page: SocialMediaFirestore) {
        socialRepository.selectPage(page)
    }

    fun showToast(message: String) {
        viewModelScope.launch {
            _events.send(DashboardEvent.ShowToast(message))
        }
    }

    fun isContextComplete(): Boolean {
        val ctx = _uiState.value.pageContext ?: return false
        return ctx.context.rawContext.length >= 50 && ctx.hasContextFile && ctx.hasImages
    }
}
