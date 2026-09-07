/*******************************************************************************
 * Copyright (c) 2025. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 ******************************************************************************/

package com.ejsstudios.socialhub.firebase

import android.content.Context
import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import com.ejsstudios.socialhub.debug.Loggers
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Gestor centralizado para Firebase Analytics.
 *
 * Proporciona funciones para registrar eventos, vistas de pantalla y errores en Firebase Analytics
 * de manera simplificada.
 *
 * @author EJS Studios
 */
object AnalyticsManager {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    /**
     * Inicializa Firebase Analytics.
     *
     * Debe llamarse antes de usar cualquier otra función del manager, típicamente en
     * Application.onCreate() o MainActivity.onCreate().
     *
     * @param context Contexto de la aplicación
     *
     * @example
     * ```kotlin
     * class MyApplication : Application() {
     *     override fun onCreate() {
     *         super.onCreate()
     *         AnalyticsManager.initialize(this)
     *     }
     * }
     * ```
     */
    fun initialize(context: Context) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context)
    }

    /**
     * Registra un evento personalizado con parámetros.
     *
     * @param eventName Nombre del evento a registrar
     * @param params Bundle con los parámetros del evento
     */
    private fun logEvent(eventName: String, params: Bundle) {
        if (AnalyticsManager::firebaseAnalytics.isInitialized) {
            firebaseAnalytics.logEvent(eventName, params)
        } else {
            Loggers.log("e", "AnalyticsManager", "FirebaseAnalytics is not initialized")
        }
        Loggers.log("e", "AnalyticsManager", "logEvent: $params")
    }

    /**
     * Registra un evento con un parámetro de texto.
     *
     * @param eventName Nombre del evento
     * @param eventParams Parámetro del evento como String
     *
     * @example
     * ```kotlin
     * AnalyticsManager.logEvent("user_action", "button_clicked")
     * ```
     */
    fun logEvent(eventName: String, eventParams: String) {
        val params = Bundle().apply { putString("event_param", eventParams) }
        logEvent(eventName, params)
        Loggers.log("e", "AnalyticsManager", "logEvent: ${params.getString("event_param")}")
    }

    /**
     * Registra un clic en un botón.
     *
     * @param buttonName Nombre del botón que fue clickeado
     *
     * @example
     * ```kotlin
     * Button(onClick = {
     *     AnalyticsManager.logButtonClicked("login_button")
     * }) {
     *     Text("Iniciar Sesión")
     * }
     * ```
     */
    fun logButtonClicked(buttonName: String) {
        val params = Bundle().apply { putString("button_name", buttonName) }
        logEvent("button_click", params)
        Loggers.log("e", "AnalyticsManager", "logButtonClicked: ${params.getString("button_name")}")
    }

    /**
     * Registra la visualización de una pantalla (para Activities/Fragments).
     *
     * @param screenName Nombre de la pantalla visualizada
     *
     * @example
     * ```kotlin
     * override fun onResume() {
     *     super.onResume()
     *     AnalyticsManager.logScreen("HomeScreen")
     * }
     * ```
     */
    fun logScreen(screenName: String) {
        val params =
            Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
            }
        logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
        Loggers.log(
            "e",
            "AnalyticsManager",
            "logScreenView: screenName ${FirebaseAnalytics.Event.SCREEN_VIEW} params $params"
        )
    }

    /**
     * Registra la visualización de una pantalla en Jetpack Compose.
     *
     * Utiliza DisposableEffect para registrar automáticamente cuando el composable entra en la
     * composición.
     *
     * @param screenName Nombre de la pantalla visualizada
     *
     * @example
     * ```kotlin
     * @Composable
     * fun HomeScreen() {
     *     AnalyticsManager.logScreenView("HomeScreen")
     *     // Resto del contenido
     * }
     * ```
     */
    @Composable
    fun logScreenView(screenName: String) {
        DisposableEffect(Unit) {
            val params =
                Bundle().apply {
                    putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                    putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
                }
            logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
            Loggers.log(
                "e",
                "AnalyticsManager",
                "logScreenView: screenName ${FirebaseAnalytics.Event.SCREEN_VIEW} params $params"
            )

            onDispose {
                // Código de limpieza si es necesario
            }
        }
    }

    /**
     * Registra un error en la aplicación.
     *
     * @param error Mensaje de error a registrar
     *
     * @example
     * ```kotlin
     * try {
     *     // Código que puede fallar
     * } catch (e: Exception) {
     *     AnalyticsManager.logError("Error en operación: ${e.message}")
     * }
     * ```
     */
    fun logError(error: String) {
        val params = Bundle().apply { putString("error_message", error) }
        logEvent("app_error", params)
        Loggers.log("e", "AnalyticsManager", "logError: ${params.getString("error_message")}")
    }
}
