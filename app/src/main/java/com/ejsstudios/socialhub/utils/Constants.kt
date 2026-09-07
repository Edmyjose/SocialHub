/*******************************************************************************
 * Copyright (c) 2025. EJS Studios. Todos los derechos reservados.
 ******************************************************************************/
package com.ejsstudios.socialhub.utils

import android.app.Application
import android.content.Context
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.ejsstudios.socialhub.viewmodels.MainViewModel

object Constants {

    // --- CONFIGURACIÓN DE SERVIDOR WEB PROPIO ---
    // IP actualizada según tu requerimiento: 192.168.1.10
    const val WEB_SERVER_URL = "http://192.168.1.7/"
    // Endpoint unificado para toda la gestión de archivos (perfiles, páginas y contexto)
    const val MANAGE_FILE_ENDPOINT = "${WEB_SERVER_URL}manage_context.php"

    val PADDING_TOP = 0.dp
    val PADDING_BOTTOM = 0.dp
    
    val LocalRootNavController = staticCompositionLocalOf<NavHostController> { error("No Root NavController provided") }
        
    val LocalDashboardNavController = staticCompositionLocalOf<NavHostController> { error("No Dashboard NavController provided") }

    val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }


    fun getMockMainViewModel(application: Application): MainViewModel {
        return MainViewModel(application, true)
    }

    @Volatile
    var splashScreenOn = true

    // Premium Status
    var IS_PREMIUM_USER = false

    // Firebase Collections
    const val COLLECTION_USERS = "users"
    const val COLLECTION_SOCIAL_MEDIA = "socialmedia"
    const val COLLECTION_CONTEXT = "context"
    const val COLLECTION_CONTEXT_MEDIA = "contextMedia"
    const val SUBCOLLECTION_CHUNKS = "chunks"
    const val COLLECTION_CAMPAIGNS = "campaigns"
    const val COLLECTION_SCHEMES = "schemes"
    const val COLLECTION_CONTENTS = "contents"
    const val COLLECTION_PREFERENCES = "preferences"
    const val COLLECTION_SUBSCRIPTIONS = "subscriptions"
    const val COLLECTION_WHATS_NEW = "whats_new"

    // SharedPreferences Keys
    const val PREFS_NAME = "social_media_prefs"
    const val PREF_ONBOARDING_COMPLETED = "onboarding_completed"
    const val PREF_SELECTED_THEME = "selected_theme"

    // Navigation Routes
    const val ROUTE_SPLASH = "splash"
    const val ROUTE_LOGIN = "login"
    const val ROUTE_DASHBOARD = "dashboard"
    const val ROUTE_HOME = "home"
    const val ROUTE_PROFILE = "profile"
    const val ROUTE_THEME_SELECTOR = "theme_selector"
    const val ROUTE_WARDROBE = "wardrobe"
    const val ROUTE_PRIVACY = "privacy"
    const val ROUTE_HELP = "help"
    const val ROUTE_SETTINGS = "settings"
    
    // RUTAS DE NEGOCIO
    const val ROUTE_SOCIAL_HUB = "social_hub"
    const val ROUTE_CONTEXT_CONFIG = "context_config"
    const val ROUTE_CAMPAIGNS = "campaigns"
    const val ROUTE_CAMPAIGN_FORM = "campaign_form"
    const val ROUTE_CAMPAIGN_DETAIL = "campaign_detail"
    const val ROUTE_SCHEMES = "schemes"
    const val ROUTE_SCHEME_DETAIL = "scheme_detail"
    const val ROUTE_CONTENTS = "contents"
    const val ROUTE_CONTENT_DETAIL = "content_detail"

    const val PLAYSTOREAPPURL = "https://play.google.com/store/apps/details?id="
    const val FCM_CHANNEL_ID = "FCM_CHANNEL_ID"
}
