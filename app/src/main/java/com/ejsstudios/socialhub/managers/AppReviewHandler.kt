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

package com.ejsstudios.socialhub.managers

import android.app.Activity
import com.ejsstudios.socialhub.debug.Loggers
import com.google.android.play.core.review.ReviewManagerFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class AppReviewHandler(
    private val activity: Activity,
    private val preferencesManager: PreferencesManager,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private val reviewManager = ReviewManagerFactory.create(activity)

    fun trackAppLaunch() {
        scope.launch {
            preferencesManager.incrementAppLaunchCount()
            val currentLaunches = preferencesManager.appLaunchCount.first()
            if (currentLaunches == 1) {
                preferencesManager.setFirstLaunchDate(System.currentTimeMillis())
            }
            Loggers.log("e", "AppReviewHandler", "App launch tracked. Total launches: $currentLaunches")
        }
    }

    fun showReviewFlowIfNeeded() {
        scope.launch {
            if (shouldShowReviewPrompt()) {
                Loggers.log("e", "AppReviewHandler", "Condiciones cumplidas. Solicitando flujo de reseña.")
                val request = reviewManager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val reviewInfo = task.result
                        val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                        flow.addOnCompleteListener { _ ->
                            Loggers.log("e", "AppReviewHandler", "Flujo de reseña completado.")
                            scope.launch {
                                preferencesManager.setReviewFlowCompleted(true)
                            }
                        }
                    } else {
                        Loggers.log(
                            "e",
                            "AppReviewHandler",
                            "Error al solicitar el flujo de reseña: ${task.exception?.message}"
                        )
                    }
                }
            } else {
                Loggers.log(
                    "e",
                    "AppReviewHandler",
                    "No se cumplen las condiciones para mostrar la reseña."
                )
            }
        }
    }

    private suspend fun shouldShowReviewPrompt(): Boolean {
        val launchCount = preferencesManager.appLaunchCount.first()
        val firstLaunchDate = preferencesManager.firstLaunchDate.first()
        val isReviewCompleted = preferencesManager.reviewFlowCompleted.first()

        if (isReviewCompleted) return false

        val daysSinceFirstLaunch =
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - firstLaunchDate)

        return launchCount >= 5 && daysSinceFirstLaunch >= 3
    }
}