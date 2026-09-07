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
import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.ejsstudios.socialhub.BuildConfig
import com.ejsstudios.socialhub.R
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ExceptionHandler
import com.ejsstudios.socialhub.managers.AlertDialogs
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.ConfigUpdate
import com.google.firebase.remoteconfig.ConfigUpdateListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.google.firebase.remoteconfig.remoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

// Constantes para las claves de Remote Config
const val ADMOB: String = "admob"
const val IRONSOURCE: String = "is"
const val ADSOURCE: String = "adsource"
const val ADSACTIVE: String = "adsactive"
const val NEWUPDATE: String = "newUpdate"
const val UPDATEMANDATORY: String = "mandatoryUpdate"
const val LAST_UPDATE_TIME: String = "lastUpdateTime"

/**
 * Gestor para Firebase Remote Config.
 *
 * Maneja la configuración remota de la aplicación, incluyendo:
 * - Control de anuncios (AdMob/IronSource)
 * - Verificación de actualizaciones
 * - Configuraciones dinámicas de la app
 *
 * @author EJS Studios
 */
object RemoteConfigManager {
    private const val PREF_SETTINGS: String = "setting"
    private lateinit var preferences: SharedPreferences
    private val mFirebaseRemoteConfig = Firebase.remoteConfig

    /**
     * Inicializa Remote Config.
     *
     * Debe llamarse en Application.onCreate() o MainActivity.onCreate().
     *
     * @param context Contexto de la aplicación
     *
     * @example
     * ```kotlin
     * class MyApplication : Application() {
     *     override fun onCreate() {
     *         super.onCreate()
     *         RemoteConfigManager.initialize(this)
     *     }
     * }
     * ```
     */
    fun initialize(context: Context) {
        preferences =
            context.applicationContext.getSharedPreferences(PREF_SETTINGS, Context.MODE_PRIVATE)
        initRemoteConfig()
    }

    fun initializeRemoteConfig(context: Context, lifecycleScope: LifecycleCoroutineScope) {
        try {
            lifecycleScope.launch {
                try {
                    fetchConfig()
                } catch (e: Exception) {
                    ExceptionHandler.handleCaughtException(e)
                }
            }
            checkUpdates(context, true)
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
        }
    }

    /** Configura Remote Config con valores por defecto y listeners. */
    private fun initRemoteConfig() {
        // Valores por defecto
        val remoteConfigDefaults: MutableMap<String, Any> = HashMap()
        remoteConfigDefaults[ADMOB] = false
        remoteConfigDefaults[NEWUPDATE] = 1
        remoteConfigDefaults[ADSACTIVE] = false
        remoteConfigDefaults[ADSOURCE] = ADMOB
        remoteConfigDefaults[UPDATEMANDATORY] = false

        // Configuración de Remote Config
        val configSettings: FirebaseRemoteConfigSettings =
            FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(3600) // 1 hora
                .build()

        mFirebaseRemoteConfig.setConfigSettingsAsync(configSettings)
        mFirebaseRemoteConfig.setDefaultsAsync(remoteConfigDefaults)

        // Listener para actualizaciones en tiempo real
        mFirebaseRemoteConfig.addOnConfigUpdateListener(
            object : ConfigUpdateListener {
                override fun onUpdate(configUpdate: ConfigUpdate) {
                    Loggers.log("e", "RemoteConfigManager", "Updated keys: ${configUpdate.updatedKeys}")

                    if (configUpdate.updatedKeys.contains(ADSACTIVE) ||
                        configUpdate.updatedKeys.contains(ADMOB) ||
                        configUpdate.updatedKeys.contains(NEWUPDATE) ||
                        configUpdate.updatedKeys.contains(ADSOURCE) ||
                        configUpdate.updatedKeys.contains(UPDATEMANDATORY)
                    ) {

                        mFirebaseRemoteConfig.activate().addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val editor = preferences.edit()

                                // Actualizar configuración de anuncios
                                if (configUpdate.updatedKeys.contains(ADMOB)) {
                                    editor.putBoolean(
                                        ADMOB,
                                        mFirebaseRemoteConfig.getBoolean(ADMOB)
                                    )
                                }
                                if (configUpdate.updatedKeys.contains(ADSACTIVE)) {
                                    editor.putBoolean(
                                        ADSACTIVE,
                                        mFirebaseRemoteConfig.getBoolean(ADSACTIVE)
                                    )
                                }
                                if (configUpdate.updatedKeys.contains(ADSOURCE)) {
                                    editor.putString(
                                        ADSOURCE,
                                        mFirebaseRemoteConfig.getString(ADSOURCE)
                                    )
                                }

                                // Actualizar configuración de versión
                                if (configUpdate.updatedKeys.contains(NEWUPDATE) ||
                                    configUpdate.updatedKeys.contains(
                                        UPDATEMANDATORY
                                    )
                                ) {
                                    val newVer =
                                        mFirebaseRemoteConfig.getLong(NEWUPDATE).toInt()
                                    val mandatoryUpdate =
                                        mFirebaseRemoteConfig.getBoolean(UPDATEMANDATORY)

                                    editor.putInt(NEWUPDATE, newVer)
                                    editor.putBoolean(UPDATEMANDATORY, mandatoryUpdate)
                                }

                                editor.apply()
                            } else {
                                Loggers.log(
                                    "e",
                                    "RemoteConfigManager",
                                    "Activation failed: ${task.exception?.message}"
                                )
                            }
                        }
                    }
                }

                override fun onError(e: FirebaseRemoteConfigException) {
                    ExceptionHandler.handleCaughtException(e)
                }
            }
        )
    }

    /**
     * Obtiene la configuración remota de Firebase.
     *
     * Debe llamarse al iniciar la app para obtener los valores más recientes.
     *
     * @example
     * ```kotlin
     * lifecycleScope.launch {
     *     RemoteConfigManager.fetchConfig()
     *     // Ahora puedes usar los valores actualizados
     * }
     * ```
     */
    suspend fun fetchConfig() =
        withContext(Dispatchers.IO) {
            try {
                mFirebaseRemoteConfig.fetchAndActivate().await()
                val editor = preferences.edit()

                val versionCode = BuildConfig.VERSION_CODE
                val newVer = mFirebaseRemoteConfig.getLong(NEWUPDATE).toInt()
                val mandatoryUpdate = mFirebaseRemoteConfig.getBoolean(UPDATEMANDATORY)
                val adsource = mFirebaseRemoteConfig.getString(ADSOURCE)

                Loggers.log(
                    "i",
                    "RemoteConfigManager",
                    "fetchConfig: adsource:$adsource newVer:$newVer currentVersion:$versionCode"
                )

                editor.putInt(NEWUPDATE, newVer)
                editor.putBoolean(UPDATEMANDATORY, mandatoryUpdate)
                editor.putBoolean(ADMOB, mFirebaseRemoteConfig.getBoolean(ADMOB))
                editor.putBoolean(ADSACTIVE, mFirebaseRemoteConfig.getBoolean(ADSACTIVE))
                editor.putString(ADSOURCE, adsource)
                editor.apply()
            } catch (e: Exception) {
                Log.e("RemoteConfigManager", "Error fetching config: ${e.message}")
                ExceptionHandler.handleCaughtException(e)
            }
        }

    /**
     * Verifica si hay una actualización disponible.
     *
     * @return true si la versión remota es mayor que la actual
     *
     * @example
     * ```kotlin
     * if (RemoteConfigManager.hasUpdate()) {
     *     // Mostrar diálogo de actualización
     * }
     * ```
     */
    fun hasUpdate(): Boolean {
        val versionCode = BuildConfig.VERSION_CODE
        return preferences.getInt(NEWUPDATE, 1) > versionCode
    }

    /**
     * Verifica actualizaciones y muestra alertas/notificaciones si es necesario.
     *
     * @param context Contexto de la aplicación
     * @param showAlert Si debe mostrar un diálogo de alerta
     *
     * @example
     * ```kotlin
     * override fun onResume() {
     *     super.onResume()
     *     RemoteConfigManager.checkUpdates(this, showAlert = true)
     * }
     * ```
     */
    fun checkUpdates(context: Context, showAlert: Boolean) {
        val realVersion = BuildConfig.VERSION_CODE
        val updateVersion = preferences.getInt(NEWUPDATE, 0)
        val updateMandatory = preferences.getBoolean(UPDATEMANDATORY, false)

        // Verificar si hay actualización disponible
        if (updateVersion > realVersion) {
            // Mostrar alerta si se solicita
            if (showAlert) {
                AlertDialogs.alertUpdate(
                    context = context,
                    title =
                        context.getString(
                            if (updateMandatory) R.string.lbl_mandatory_update
                            else R.string.lbl_normal_update
                        ),
                    msg =
                        context.getString(
                            if (updateMandatory)
                                R.string.lbl_update_mandatory_content
                            else R.string.lbl_update_normal_content
                        ),
                    Mandatory = updateMandatory
                ).show()
            }

            // Verificar si han pasado más de 4 horas desde la última notificación
            val currentTime = System.currentTimeMillis()
            val lastUpdateTime = preferences.getLong(LAST_UPDATE_TIME, 0L)
            val hoursPassed = (currentTime - lastUpdateTime) >= (4 * 60 * 60 * 1000)

            if (hoursPassed) {
                // Aquí puedes mostrar una notificación si tienes un NotificationManager
                // showUpdateNotification(context, updateMandatory)

                // Actualizar la hora de la última notificación
                preferences.edit().putLong(LAST_UPDATE_TIME, currentTime).apply()
            }
        }
    }

    /**
     * Obtiene la fuente de anuncios configurada.
     *
     * @return "admob" o "is" (IronSource)
     *
     * @example
     * ```kotlin
     * when (RemoteConfigManager.getAdsSource()) {
     *     "admob" -> initializeAdMob()
     *     "is" -> initializeIronSource()
     * }
     * ```
     */
    fun getAdsSource(): String {
        return preferences.getString(ADSOURCE, ADMOB) ?: ADMOB
    }

    /**
     * Verifica si AdMob está habilitado.
     *
     * @return true si AdMob está habilitado
     */
    fun getAdmob(): Boolean {
        return preferences.getBoolean(ADMOB, false)
    }

    /**
     * Verifica si los anuncios están activos.
     *
     * @return true si los anuncios están activos
     *
     * @example
     * ```kotlin
     * if (RemoteConfigManager.getAdsActive()) {
     *     // Cargar y mostrar anuncios
     * }
     * ```
     */
    fun getAdsActive(): Boolean {
        return preferences.getBoolean(ADSACTIVE, false)
    }

    /**
     * Verifica si la actualización es obligatoria.
     *
     * @return true si la actualización es obligatoria
     */
    fun isUpdateMandatory(): Boolean {
        return preferences.getBoolean(UPDATEMANDATORY, false)
    }

    /**
     * Obtiene el número de versión disponible en Remote Config.
     *
     * @return Número de versión disponible
     */
    fun getAvailableVersion(): Int {
        return preferences.getInt(NEWUPDATE, BuildConfig.VERSION_CODE)
    }
}
