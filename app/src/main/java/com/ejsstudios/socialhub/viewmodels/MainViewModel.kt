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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.ejsstudios.socialhub.App
import com.ejsstudios.socialhub.ai.embedding.EmbeddingService
import com.ejsstudios.socialhub.apiservices.AIGenerationApiService
import com.ejsstudios.socialhub.apiservices.EmbeddingApiService
import com.ejsstudios.socialhub.firebase.db.FirestoreManager
import com.ejsstudios.socialhub.firebase.db.local.database.AppDatabase
import com.ejsstudios.socialhub.firebase.login.LoginViewModel
import com.ejsstudios.socialhub.navigation.NavigationViewModel
import com.ejsstudios.socialhub.platforms.facebook.FacebookApiService
import com.ejsstudios.socialhub.platforms.facebook.FacebookRepository
import com.ejsstudios.socialhub.platforms.facebook.FacebookViewModel
import com.ejsstudios.socialhub.platforms.reddit.RedditApiService
import com.ejsstudios.socialhub.platforms.reddit.RedditRepository
import com.ejsstudios.socialhub.platforms.reddit.RedditViewModel
import com.ejsstudios.socialhub.ui.theme.AppTheme
import com.ejsstudios.socialhub.ui.views.hubsocial.HubSocialViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

class MainViewModel(private val application: Application, mock: Boolean = false) :
    BaseViewModel<Unit>(application, Unit) {

    private val app = application as App

    // Infraestructura base
    val database: AppDatabase = app.db

    val apiService: FacebookApiService by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl(FacebookApiService.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(FacebookApiService::class.java)
    }

    val redditApiService: RedditApiService by lazy {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val client = OkHttpClient.Builder().addInterceptor(logging).build()
        Retrofit.Builder()
            .baseUrl(RedditApiService.BASE_URL)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(RedditApiService::class.java)
    }

    val aiGenerationApiService: AIGenerationApiService get() = app.aiGenerationApiService
    val embeddingApiService: EmbeddingApiService get() = app.embeddingApiService

    val preferencesManager = app.preferencesManager
    val authManager = app.authManager
    private val firestoreManager = FirestoreManager
    private val facebookRepository = FacebookRepository(apiService, authManager, firestoreManager)
    private val redditRepository = RedditRepository(redditApiService, authManager, firestoreManager)

    // Repositorios centralizados (Reutilizados de la clase App)
    val loginRepository = app.loginRepository
    val socialRepository = app.socialRepository
    val contextRepository = app.contextRepository
    val syncManager = app.syncManager

    val embeddingService = EmbeddingService()

    // Repositorios Globales (Compartidos con Workers)
    val campaignRepository get() = app.campaignRepository
    val schemeRepository get() = app.schemeRepository
    val contentRepository get() = app.contentRepository

    val navigationViewModel = NavigationViewModel(application)
    val loginViewModel = LoginViewModel(application, loginRepository, syncManager)
    val facebookViewModel = FacebookViewModel(application, facebookRepository, socialRepository)
    val redditViewModel = RedditViewModel(application, redditRepository, socialRepository)
    val dashboardViewModel = DashboardViewModel(
        application,
        loginRepository,
        socialRepository,
        contextRepository,
        campaignRepository,
        schemeRepository,
        preferencesManager
    )

    // ViewModels hijos
    val hubSocialViewModel = HubSocialViewModel(application, loginRepository, socialRepository, facebookViewModel, redditViewModel)

    /*val aiGenerationRepository = AIGenerationRepository(aiGenerationApiService)

    val contextSettingViewModel =
            ContextSettingViewModel(application, contextRepository, embeddingService)
    
    val schemesViewModel = SchemesViewModel(
        application, schemeRepository, aiGenerationRepository, loginRepository, socialRepository, contextRepository, embeddingApiService
    )
    val campaignsViewModel = CampaignsViewModel(
        application, campaignRepository, aiGenerationRepository, contextRepository, loginRepository, socialRepository, schemesViewModel
    )
    val contentsViewModel = ContentsViewModel(
        application, contentRepository, facebookRepository, aiGenerationRepository, loginRepository, socialRepository, contextRepository, embeddingApiService
    )*/

    // Estado global de la UI
    private val _currentTheme = MutableStateFlow(AppTheme.ORIGINAL)
    val currentTheme: StateFlow<AppTheme> = _currentTheme.asStateFlow()
    fun updateTheme(theme: AppTheme) {
        _currentTheme.value = theme
    }

    private val _paddingTop = MutableStateFlow<Dp>(0.dp)
    val paddingTop: StateFlow<Dp> = _paddingTop.asStateFlow()
    fun setPaddingTop(padding: Dp) {
        _paddingTop.value = padding
    }

    private val _paddingBottom = MutableStateFlow(0.dp)
    val paddingBottom: StateFlow<Dp> = _paddingBottom.asStateFlow()
    fun setPaddingBottom(padding: Dp) {
        _paddingBottom.value = padding
    }

    /*init {
        loginViewModel.checkActiveSession()
    }*/

    init {
        observeAuthentication()
    }

    private fun observeAuthentication() {
        viewModelScope.launch {
            loginRepository.user.collect { user ->
                if (user != null) {
                    syncManager.startSync(user.id)
                } else {
                    syncManager.stopSync()
                }
            }
        }
    }
}

class MainViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return MainViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
