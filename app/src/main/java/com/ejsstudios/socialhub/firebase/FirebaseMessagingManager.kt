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

import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ExceptionHandler
import com.ejsstudios.socialhub.firebase.AnalyticsManager
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging

/**
 * Gestor para Firebase Cloud Messaging (FCM).
 *
 * Maneja la inicialización de FCM, obtención de tokens y suscripción a tópicos de notificaciones
 * push.
 *
 * @author EJS Studios
 */
object FirebaseMessagingManager {

    init {
        initializeFirebaseMessaging()
    }

    /**
     * Inicializa Firebase Cloud Messaging.
     *
     * Obtiene el token de FCM y se suscribe automáticamente a los tópicos "info" y "update".
     */
    private fun initializeFirebaseMessaging() {
        // Obtener el token de FCM
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                task.exception?.let { ExceptionHandler.handleCaughtException(it) }
                return@addOnCompleteListener
            }
            // Registrar el token obtenido
            Loggers.log("e", "FirebaseMessagingManager", "FCM Token: ${task.result}")
        }

        // Suscripción a tópicos predeterminados
        subscribeToTopic("info")
        subscribeToTopic("update")
    }

    /**
     * Suscribe el dispositivo a un tópico de FCM.
     *
     * Los tópicos permiten enviar notificaciones a grupos de dispositivos sin necesidad de conocer
     * sus tokens individuales.
     *
     * @param topic Nombre del tópico al que suscribirse
     *
     * @example
     * ```kotlin
     * FirebaseMessagingManager.subscribeToTopic("news")
     * FirebaseMessagingManager.subscribeToTopic("promotions")
     * ```
     */
    private fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic)
            .addOnCompleteListener { task: Task<Void?> ->
                val msg =
                    if (task.isSuccessful) {
                        "Subscribed to topic $topic"
                    } else {
                        AnalyticsManager.logError("failed to Subscribe $topic topic")
                        "failed to Subscribe $topic topic"
                    }
                Loggers.log("e", "FirebaseMessagingManager", msg)
            }
    }

    /**
     * Desuscribe el dispositivo de un tópico de FCM.
     *
     * @param topic Nombre del tópico del que desuscribirse
     *
     * @example
     * ```kotlin
     * FirebaseMessagingManager.unsubscribeFromTopic("promotions")
     * ```
     */
    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
            .addOnCompleteListener { task: Task<Void?> ->
                val msg =
                    if (task.isSuccessful) {
                        "Unsubscribed from topic $topic"
                    } else {
                        "Failed to unsubscribe from $topic topic"
                    }
                Loggers.log("e", "FirebaseMessagingManager", msg)
            }
    }

    /**
     * Obtiene el token actual de FCM.
     *
     * @param onSuccess Callback que recibe el token si se obtiene correctamente
     * @param onFailure Callback que se ejecuta si falla la obtención del token
     *
     * @example
     * ```kotlin
     * FirebaseMessagingManager.getToken(
     *     onSuccess = { token ->
     *         println("Token: $token")
     *     },
     *     onFailure = { exception ->
     *         println("Error: ${exception.message}")
     *     }
     * )
     * ```
     */
    fun getToken(onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful && task.result != null) {
                onSuccess(task.result)
            } else {
                task.exception?.let {
                    ExceptionHandler.handleCaughtException(it)
                    onFailure(it)
                }
            }
        }
    }
}
