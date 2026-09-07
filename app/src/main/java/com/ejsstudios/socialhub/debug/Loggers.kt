package com.ejsstudios.socialhub.debug // Cambiado el package

import android.content.Context
import android.util.Log

// se usa más adelante

object Loggers {

    var isDebugMode = true // Controla si los logs se muestran. Cambiar a 'false' para producción.

    private const val TAG_PREFIX = ""

    fun d(tag: String, message: String) {
        if (isDebugMode) {
            Log.d(TAG_PREFIX + tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (isDebugMode) {
            Log.e(TAG_PREFIX + tag, message, throwable)
        }
    }

    fun w(tag: String, message: String) {
        if (isDebugMode) {
            Log.w(TAG_PREFIX + tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (isDebugMode) {
            Log.i(TAG_PREFIX + tag, message)
        }
    }

    fun v(tag: String, message: String) {
        if (isDebugMode) {
            Log.v(TAG_PREFIX + tag, message)
        }
    }

    // Función genérica log que acepta tipo como primer parámetro
    fun log(type: String, tag: String, message: String) {
        when (type.lowercase()) {
            "d" -> d(tag, message)
            "e" -> e(tag, message)
            "w" -> w(tag, message)
            "i" -> i(tag, message)
            "v" -> v(tag, message)
            else -> i(tag, message) // Por defecto usa info
        }
    }

    // Manteniendo la función logE original
    fun logE(tag: String?, msg: Throwable?) {
        if (isDebugMode) {
            Log.e(tag, "Error: ", msg)
        }
    }

    // La función toast original, pero comentada ya que la gestión de UI (Toasts) la haremos con
    // NotificationManager
    /**
     *
     * @param context get app context
     * @param type Type Message
     * @param msg message String
     */
    fun toast(context: Context?, type: String, msg: String?) {
        /*
        if (msg != null && msg != "") {
            if (type == "e") {
                // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            if (type == "i") {
                // Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
            // ... otras implementaciones de toast si fueran necesarias
        }
        */
    }
}
