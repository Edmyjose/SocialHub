/*
 * Copyright (c) 2025-2026. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 */
package com.ejsstudios.socialhub.error

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Process
import android.util.Log
import com.ejsstudios.socialhub.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlin.system.exitProcess

/**
 * Manejador global de excepciones para la aplicación socialhub.
 */
object ExceptionHandler : Thread.UncaughtExceptionHandler {

    private var appContext: Context? = null
    private const val TAG = "ExceptionHandler"

    fun initialize(context: Context) {
        appContext = context.applicationContext
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        throwable.printStackTrace()
        val errorMessage =
                "Error: ${throwable::class.java.simpleName}\nMessage: ${throwable.message}"

        Log.e(TAG, "Uncaught exception in thread: ${thread.name}", throwable)

        appContext?.let { context ->
            val intent = Intent(context, ErrorDisplayActivity::class.java).apply {
                        putExtra("error_message", errorMessage)
                        putExtra("error_type", throwable::class.java.simpleName)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
            context.startActivity(intent)
        }

        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            Log.e(TAG, "Thread interrupted while waiting", e)
        }

        Process.killProcess(Process.myPid())
        exitProcess(2)
    }

    fun handleUncaughtException(thread: Thread, throwable: Throwable) {
        throwable.printStackTrace()
        logException("Uncaught exception in thread: ${thread.name}", throwable)
    }

    fun handleCaughtException(throwable: Exception, onDismiss: () -> Unit = {}) {
        FirebaseCrashlytics.getInstance().recordException(throwable)
        throwable.printStackTrace()
        logException("Caught exception", throwable, onDismiss)
    }

    fun handleCaughtException(throwable: Throwable, onDismiss: () -> Unit = {}) {
        throwable.printStackTrace()
        logException("Caught exception", throwable, onDismiss)
    }

    fun handleCaughtException(throwable: String, onDismiss: () -> Unit = {}) {
        logException("Caught exception", throwable, onDismiss)
    }

    fun handleCaughtException(throwable: CustomException, onDismiss: () -> Unit = {}) {
        throwable.printStackTrace()
        logException("Caught exception [Code: ${throwable.errorCode}]", throwable, onDismiss)
    }

    /*
    fun handleCaughtException(throwable: IronSourceError, onDismiss: () -> Unit = {}) {
        FirebaseCrashlytics.getInstance().recordException(Throwable("${throwable.errorCode} - ${throwable.errorMessage}"))
        logException("Caught exception", throwable.errorMessage) { onDismiss() }
    }
    
    fun handleCaughtException(throwable: LevelPlayAdError, onDismiss: () -> Unit = {}) {
        FirebaseCrashlytics.getInstance().recordException(Throwable("${throwable.getErrorCode()} - ${throwable.getErrorMessage()}"))
        logException("Caught exception", throwable.getErrorMessage()) { onDismiss() }
    }
    */

    private fun logException(message: String, throwable: String, onDismiss: () -> Unit = {}) {
        val exceptionMessage = "$message: $throwable"
        Log.e(TAG, exceptionMessage)
        showErrorDialog(exceptionMessage, onDismiss)
    }

    private fun logException(message: String, throwable: Throwable, onDismiss: () -> Unit = {}) {
        val exceptionMessage = "$message: ${throwable.message}"
        Log.e(TAG, message, throwable)
        showErrorDialog(exceptionMessage, onDismiss)
    }

    private fun showErrorDialog(message: String, onDismiss: () -> Unit = {}) {
        val currentActivity: Activity? = (appContext as? Activity)

        if (currentActivity != null) {
            currentActivity.runOnUiThread {
                Handler(Looper.getMainLooper()).post {
                    try {
                        alertMessage(
                                        context = currentActivity,
                                        title = currentActivity.getString(R.string.error_title_attention),
                                        msg = message,
                                        okButton = true,
                                        onDismiss = onDismiss
                                )
                                .show()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error showing dialog", e)
                    }
                }
            }
        } else {
            appContext?.let { context ->
                val intent =
                        Intent(context, ErrorDisplayActivity::class.java).apply {
                            putExtra("error_message", message)
                            putExtra("error_type", "Error")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                context.startActivity(intent)
            }
        }
    }

    fun alertMessage(
        context: Context,
        title: String?,
        msg: String?,
        okButton: Boolean = false,
        retryAlert: Boolean = false,
        onDismiss: () -> Unit = {},
        okAction: () -> Unit = {}
    ): MaterialAlertDialogBuilder {
        val builder = MaterialAlertDialogBuilder(context).setTitle(title).setMessage(msg)
        builder.setCancelable(true)
        var buttonName = if (retryAlert) R.string.button_retry else R.string.dialog_ok
        buttonName = if (okButton) R.string.dialog_ok else buttonName
        val onClickPositive: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialog, _ ->
                okAction()
                dialog.dismiss()
            }
        val onClickNeutral: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialog, _ ->
                onDismiss()
                dialog.dismiss()
            }
        builder.setPositiveButton(buttonName, onClickPositive)
        if (retryAlert || okButton) {
            builder.setNeutralButton(R.string.button_cancel, onClickNeutral)
        }
        return builder
    }
}
