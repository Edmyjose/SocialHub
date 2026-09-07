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

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.compose.material3.AlertDialog as ComposeAlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.net.toUri
import com.ejsstudios.socialhub.R
import com.ejsstudios.socialhub.utils.Constants.PLAYSTOREAPPURL
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * Objeto singleton que proporciona funciones para crear diálogos de alerta tanto para Jetpack
 * Compose como para Activities tradicionales (AppCompat y Material Design).
 *
 * Esta clase ofrece dos tipos de diálogos:
 * - **Diálogos Composables**: Para usar en interfaces de Jetpack Compose
 * - **Diálogos de Activity**: Para usar en Activities tradicionales con vistas XML
 *
 * Ejemplo de uso en Compose:
 * ```kotlin
 * var showDialog by remember { mutableStateOf(false) }
 * AlertDialogs.UpdateDialog(
 *     showDialog = showDialog,
 *     onDismiss = { showDialog = false },
 *     isMandatory = false
 * )
 * ```
 *
 * Ejemplo de uso en Activity:
 * ```kotlin
 * AlertDialogs.alertUpdate(
 *     context = this,
 *     title = "Actualización disponible",
 *     msg = "Hay una nueva versión",
 *     Mandatory = false
 * ).show()
 * ```
 */
object AlertDialogs {

    // ==================== DIÁLOGOS COMPOSABLES ====================

    /**
     * Un Composable que muestra un diálogo de actualización de la app, siguiendo las mejores
     * prácticas de Compose.
     *
     * @param showDialog Controla si el diálogo es visible. El estado debe ser gestionado por el
     * ViewModel.
     * @param onDismiss Lambda que se ejecuta cuando el diálogo se descarta.
     * @param isMandatory Si es `true`, el diálogo no se puede descartar y solo muestra el botón de
     * actualizar.
     *
     * Ejemplo:
     * ```kotlin
     * var showUpdateDialog by remember { mutableStateOf(true) }
     * UpdateDialog(
     *     showDialog = showUpdateDialog,
     *     onDismiss = { showUpdateDialog = false },
     *     isMandatory = false
     * )
     * ```
     */
    @Composable
    fun UpdateDialog(
        showDialog: Boolean,
        onDismiss: () -> Unit,
        isMandatory: Boolean,
    ) {
        val context = LocalContext.current
        if (showDialog) {
            ComposeAlertDialog(
                onDismissRequest = { if (!isMandatory) onDismiss() },
                title = { Text(text = stringResource(id = R.string.lbl_update_title)) },
                text = { Text(text = stringResource(id = R.string.lbl_update_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val browserIntent =
                                Intent(
                                    Intent.ACTION_VIEW,
                                    (PLAYSTOREAPPURL + context.packageName).toUri()
                                )
                            context.startActivity(browserIntent)
                            onDismiss()
                        }
                    ) { Text(text = stringResource(id = R.string.lbl_update)) }
                },
                dismissButton = {
                    if (!isMandatory) {
                        TextButton(onClick = onDismiss) {
                            Text(text = stringResource(id = R.string.lbl_later))
                        }
                    }
                }
            )
        }
    }

    /**
     * Un diálogo de mensaje genérico, reutilizable y idiomático para Compose.
     *
     * @param showDialog Controla si el diálogo es visible.
     * @param onDismissRequest Se ejecuta cuando el usuario intenta descartar el diálogo (ej.
     * tocando fuera).
     * @param title El título del diálogo.
     * @param message El cuerpo del mensaje del diálogo.
     * @param confirmButtonText El texto para el botón de confirmación.
     * @param onConfirm La acción a ejecutar cuando se presiona el botón de confirmación.
     * @param dismissButtonText El texto para el botón de descarte (opcional). Si es nulo, no se
     * muestra.
     * @param onDismiss La acción a ejecutar cuando se presiona el botón de descarte.
     *
     * Ejemplo:
     * ```kotlin
     * var showMessageDialog by remember { mutableStateOf(true) }
     * AppMessageDialog(
     *     showDialog = showMessageDialog,
     *     onDismissRequest = { showMessageDialog = false },
     *     title = "Confirmación",
     *     message = "¿Deseas continuar?",
     *     confirmButtonText = "Sí",
     *     onConfirm = { /* acción */ },
     *     dismissButtonText = "No",
     *     onDismiss = { /* acción */ }
     * )
     * ```
     */
    @Composable
    fun AppMessageDialog(
        showDialog: Boolean,
        onDismissRequest: () -> Unit,
        title: String,
        message: String,
        confirmButtonText: String,
        onConfirm: () -> Unit,
        dismissButtonText: String? = null,
        onDismiss: (() -> Unit)? = null
    ) {
        if (showDialog) {
            ComposeAlertDialog(
                onDismissRequest = onDismissRequest,
                title = { Text(text = title) },
                text = { Text(text = message) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            onConfirm()
                            onDismissRequest() // Cierra el diálogo
                        }
                    ) { Text(text = confirmButtonText) }
                },
                dismissButton = {
                    if (dismissButtonText != null) {
                        TextButton(
                            onClick = {
                                onDismiss?.invoke()
                                onDismissRequest() // Cierra el diálogo
                            }
                        ) { Text(text = dismissButtonText) }
                    }
                }
            )
        }
    }

    // ==================== DIÁLOGOS DE ACTIVITY ====================

    /**
     * Crea un diálogo de actualización usando MaterialAlertDialogBuilder.
     *
     * Este método es la versión recomendada para Activities que usan Material Design Components.
     * Muestra un diálogo que redirige al usuario a la Play Store para actualizar la aplicación.
     *
     * @param context El contexto de la aplicación (Activity o Application context).
     * @param title El título del diálogo.
     * @param msg El mensaje que se mostrará en el diálogo.
     * @param Mandatory Si es `true`, el diálogo no se puede cancelar y el usuario debe actualizar.
     * ```
     *                  Si es `false`, se muestra un botón "Más tarde" para cerrar el diálogo.
     * @return
     * ```
     * Un objeto [MaterialAlertDialogBuilder] configurado. Llama a `.show()` para mostrarlo.
     *
     * Ejemplo:
     * ```kotlin
     * AlertDialogs.alertUpdate(
     *     context = this,
     *     title = "Nueva versión disponible",
     *     msg = "Actualiza para obtener las últimas funciones",
     *     Mandatory = false
     * ).show()
     * ```
     */
    fun alertUpdate(
        context: Context,
        title: String?,
        msg: String?,
        Mandatory: Boolean
    ): MaterialAlertDialogBuilder {
        val builder = MaterialAlertDialogBuilder(context).setTitle(title).setMessage(msg)
        if (Mandatory) {
            builder.setCancelable(false)
            builder.setPositiveButton(R.string.lbl_update) { dialog, which ->
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, (PLAYSTOREAPPURL + context.packageName).toUri())
                context.startActivity(browserIntent)
            }
        } else {
            builder.setCancelable(true)
            builder.setPositiveButton(R.string.lbl_update) { dialog, which ->
                val browserIntent =
                    Intent(Intent.ACTION_VIEW, (PLAYSTOREAPPURL + context.packageName).toUri())
                context.startActivity(browserIntent)
                dialog.dismiss()
            }
            builder.setNeutralButton(R.string.lbl_later) { dialog, which -> dialog.dismiss() }
        }
        return builder
    }

    /**
     * Crea un diálogo de mensaje genérico con botones personalizables.
     *
     * Este método permite crear diálogos con acciones personalizadas para los botones positivo y
     * negativo. Útil para confirmaciones, alertas o mensajes que requieren una respuesta del
     * usuario.
     *
     * @param context El contexto de la aplicación (Activity o Application context).
     * @param title El título del diálogo.
     * @param msg El mensaje que se mostrará en el diálogo.
     * @param retryAlert Si es `true`, el botón positivo mostrará "Reintentar" en lugar de "OK".
     * @param setNegativeButton El recurso de string para el texto del botón negativo (por defecto:
     * R.string.lbl_cancel).
     * @param denyAction Lambda que se ejecuta cuando se presiona el botón negativo.
     * @param setPositiveButton El recurso de string para el texto del botón positivo (por defecto:
     * R.string.lbl_Ok).
     * @param okAction Lambda que se ejecuta cuando se presiona el botón positivo.
     * @return Un objeto [MaterialAlertDialogBuilder] configurado. Llama a `.show()` para mostrarlo.
     *
     * Ejemplo:
     * ```kotlin
     * AlertDialogs.alertMessage(
     *     context = this,
     *     title = "Error de conexión",
     *     msg = "No se pudo conectar al servidor",
     *     retryAlert = true,
     *     setNegativeButton = R.string.cancel,
     *     denyAction = { finish() },
     *     setPositiveButton = R.string.retry,
     *     okAction = { retryConnection() }
     * ).show()
     * ```
     */
    fun alertMessage(
        context: Context,
        title: String?,
        msg: String?,
        retryAlert: Boolean = false,
        setNegativeButton: Int = R.string.lbl_cancel,
        denyAction: () -> Unit = {},
        setPositiveButton: Int = R.string.lbl_Ok,
        okAction: () -> Unit = {}
    ): MaterialAlertDialogBuilder {
        val builder = MaterialAlertDialogBuilder(context).setTitle(title).setMessage(msg)
        builder.setCancelable(true)
        val buttonName = if (retryAlert) R.string.lbl_retry else setPositiveButton
        builder.setPositiveButton(buttonName) { dialog, which ->
            okAction()
            dialog.dismiss()
        }
        builder.setNeutralButton(setNegativeButton) { dialog, which ->
            denyAction()
            dialog.dismiss()
        }
        return builder
    }

    /**
     * Crea un diálogo de mensaje simple con opciones de botón OK o Reintentar.
     *
     * Este método es una versión simplificada para mostrar mensajes informativos o de error con la
     * opción de reintentar una acción o simplemente cerrar el diálogo.
     *
     * @param context El contexto de la aplicación (Activity o Application context).
     * @param title El título del diálogo.
     * @param msg El mensaje que se mostrará en el diálogo.
     * @param okButton Si es `true`, fuerza el botón positivo a mostrar "OK".
     * @param retryAlert Si es `true`, el botón positivo mostrará "Reintentar" y se añade un botón
     * "Cancelar".
     * @param onDismiss Lambda que se ejecuta cuando se cierra el diálogo (al presionar Cancelar o
     * fuera del diálogo).
     * @param okAction Lambda que se ejecuta cuando se presiona el botón positivo (OK o Reintentar).
     * @return Un objeto [MaterialAlertDialogBuilder] configurado. Llama a `.show()` para mostrarlo.
     *
     * Ejemplo básico:
     * ```kotlin
     * AlertDialogs.alertMessage(
     *     context = this,
     *     title = "Éxito",
     *     msg = "Operación completada correctamente",
     *     okButton = true,
     *     okAction = { /* continuar */ }
     * ).show()
     * ```
     *
     * Ejemplo con reintentar:
     * ```kotlin
     * AlertDialogs.alertMessage(
     *     context = this,
     *     title = "Error",
     *     msg = "No se pudo cargar los datos",
     *     retryAlert = true,
     *     onDismiss = { showPreviousScreen() },
     *     okAction = { loadData() }
     * ).show()
     * ```
     */
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
        var buttonName = if (retryAlert) R.string.lbl_retry else R.string.lbl_Ok
        buttonName = if (okButton) R.string.lbl_Ok else buttonName
        val onClickPositive: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                okAction()
                dialog.dismiss()
            }
        val onClickNeutral: DialogInterface.OnClickListener =
            DialogInterface.OnClickListener { dialog, which ->
                onDismiss()
                dialog.dismiss()
            }
        builder.setPositiveButton(buttonName, onClickPositive)
        if (retryAlert || okButton) {
            builder.setNeutralButton(R.string.lbl_cancel, onClickNeutral)
        }
        return builder
    }
}
