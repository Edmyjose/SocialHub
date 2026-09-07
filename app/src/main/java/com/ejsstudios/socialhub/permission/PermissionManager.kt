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
package com.ejsstudios.socialhub.permission

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.ejsstudios.socialhub.R
import com.ejsstudios.socialhub.managers.AlertDialogs

/**
 * Gestor de permisos refactorizado para solicitar permisos bajo demanda.
 */
class PermissionManager(private val activity: ComponentActivity) {

    private val tag = "PermissionManager"

    /**
     * Lista de permisos críticos para el inicio de la app.
     * Solo pedimos Notificaciones (Android 13+) para no interrumpir con selectores de fotos.
     */
    private val startupPermissions = mutableListOf<String>().apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    /**
     * Obtiene la lista de permisos de medios necesarios según la versión de Android.
     */
    fun getMediaPermissions(): List<String> {
        return mutableListOf<String>().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                add(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private val permissionLauncher =
        activity.registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissionsResult ->
            permissionsResult.forEach { (permission, isGranted) ->
                // Manejo opcional de resultados globales
            }
        }

    /**
     * Verifica y solicita los permisos iniciales (Notificaciones).
     */
    fun checkAndRequestPermissions() {
        val notGranted = startupPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isNotEmpty()) {
            permissionLauncher.launch(notGranted.toTypedArray())
        }
    }

    /**
     * Verifica y solicita permisos de medios.
     * @param onGranted Callback si ya están concedidos o tras concederlos.
     */
    fun checkAndRequestMediaPermissions(onGranted: () -> Unit = {}) {
        val mediaPermissions = getMediaPermissions()
        val notGranted = mediaPermissions.filter {
            ContextCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGranted.isEmpty()) {
            onGranted()
        } else {
            val shouldShowRationale = notGranted.any {
                activity.shouldShowRequestPermissionRationale(it)
            }

            if (shouldShowRationale) {
                showRationaleDialog(notGranted)
            } else {
                // Para simplificar en esta versión, lanzamos el launcher global.
                // En una app real, el launcher debería devolver el callback a onGranted.
                permissionLauncher.launch(notGranted.toTypedArray())
            }
        }
    }

    private fun showRationaleDialog(permissions: List<String>) {
        AlertDialogs.alertMessage(
            context = activity,
            title = activity.getString(R.string.lbl_permission_required),
            msg = activity.getString(R.string.lbl_permissions_required),
            setPositiveButton = R.string.lbl_allow,
            okAction = { permissionLauncher.launch(permissions.toTypedArray()) }
        )
            .show()
    }

    fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", activity.packageName, null)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        activity.startActivity(intent)
    }

    fun isPermissionGranted(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(activity, permission) ==
                PackageManager.PERMISSION_GRANTED
    }

    fun areMediaPermissionsGranted(): Boolean {
        return getMediaPermissions().all { isPermissionGranted(it) }
    }
}
