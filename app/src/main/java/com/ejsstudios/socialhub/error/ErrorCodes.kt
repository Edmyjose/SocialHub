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
package com.ejsstudios.socialhub.error

import android.content.Context
import com.ejsstudios.socialhub.R

/**
 * Códigos de error centralizados para toda la aplicación socialhub.
 * 
 * Organización por rangos:
 * - 1000-1999: Errores de validación
 * - 2000-2999: Errores de red
 * - 3000-3999: Errores de servidor
 * - 4000-4999: Errores de base de datos
 * - 5000-5999: Errores de archivos
 * - 6000-6999: Errores de IA
 * - 7000-7999: Errores de suscripción
 * - 8000-8999: Errores de autenticación
 * - 9000-9999: Errores de Social Media / Graph API
 * - 10000-10999: Errores generales
 */
object ErrorCodes {
    // Errores generales (10000-10999)
    const val UNKNOWN_ERROR = 10000
    const val GENERAL_ERROR = 10001
    const val NOT_IMPLEMENTED = 10002
    const val PERMISSION_DENIED = 10003
    const val TIMEOUT = 10004
    
    // Errores de validación (1000-1999)
    const val VALIDATION_GENERAL = 1000
    const val VALIDATION_REQUIRED_FIELD = 1001
    const val VALIDATION_MIN_LENGTH = 1002
    const val VALIDATION_MAX_LENGTH = 1003
    const val VALIDATION_INVALID_EMAIL = 1004
    const val VALIDATION_INVALID_PASSWORD = 1005
    const val VALIDATION_INVALID_DATE = 1006
    const val VALIDATION_INVALID_TIME = 1007
    const val VALIDATION_INVALID_TIMEZONE = 1008
    const val VALIDATION_INVALID_COORDINATES = 1009
    const val VALIDATION_INVALID_FORMAT = 1010
    
    // Errores de red (2000-2999)
    const val NETWORK_ERROR = 2000
    const val NETWORK_UNAVAILABLE = 2001
    const val NETWORK_TIMEOUT = 2002
    const val NETWORK_SERVER_UNREACHABLE = 2003
    
    // Errores de servidor (3000-3999)
    const val SERVER_ERROR = 3000
    const val SERVER_UNAVAILABLE = 3001
    const val SERVER_MAINTENANCE = 3002
    const val SERVER_RATE_LIMIT = 3003
    
    // Errores de base de datos (4000-4999)
    const val DATABASE_ERROR = 4000
    const val DATABASE_CONNECTION = 4001
    const val DATABASE_QUERY = 4002
    const val DATABASE_TRANSACTION = 4003
    const val DATABASE_NOT_FOUND = 4004
    const val DATABASE_CONSTRAINT = 4005
    
    // Errores de archivos (5000-5999)
    const val FILE_ERROR = 5000
    const val FILE_NOT_FOUND = 5001
    const val FILE_READ_ERROR = 5002
    const val FILE_WRITE_ERROR = 5003
    const val FILE_PERMISSION = 5004
    const val FILE_TOO_LARGE = 5005
    const val FILE_INVALID_FORMAT = 5006
    
    // Errores de IA (6000-6999)
    const val AI_SERVICE_ERROR = 6000
    const val AI_SERVICE_UNAVAILABLE = 6001
    const val AI_RATE_LIMIT = 6002
    const val AI_COST_LIMIT = 6003
    const val AI_INVALID_RESPONSE = 6004
    const val AI_PROMPT_TOO_LONG = 6005
    const val AI_CONTEXT_TOO_LARGE = 6006
    const val AI_EMPTY_RESPONSE = 6007
    
    // Errores de suscripción (7000-7999)
    const val SUBSCRIPTION_REQUIRED = 7000
    const val SUBSCRIPTION_EXPIRED = 7001
    const val SUBSCRIPTION_NOT_FOUND = 7002
    const val PURCHASE_FAILED = 7003
    const val RESTORE_FAILED = 7004
    const val BILLING_ERROR = 7005
    
    // Errores de autenticación (8000-8999)
    const val AUTH_ERROR = 8000
    const val AUTH_FAILED = 8001
    const val USER_NOT_FOUND = 8002
    const val INVALID_CREDENTIALS = 8003
    const val EMAIL_EXISTS = 8004
    const val ACCOUNT_DISABLED = 8005
    const val TOKEN_EXPIRED = 8006
    
    // Errores de Social Media (9000-9999)
    const val SOCIAL_MEDIA_ERROR = 9000
    const val CONTENT_GENERATION_ERROR = 9001
    const val PAGE_DATA_INCOMPLETE = 9002
    const val CONTEXT_DATA_INCOMPLETE = 9003
    const val GRAPH_API_ERROR = 9004
    const val PERMISSION_INSUFFICIENT = 9005
    const val FACEBOOK_SYNC_ERROR = 9006
    
    /**
     * Obtiene el ID del recurso string para el mensaje de error basado en el código de error.
     */
    fun getStringResourceId(errorCode: Int): Int? {
        return when (errorCode) {
            // Errores de validación
            VALIDATION_INVALID_TIMEZONE -> R.string.error_validation_invalid_timezone
            VALIDATION_INVALID_COORDINATES -> R.string.error_validation_invalid_coordinates
            VALIDATION_INVALID_DATE -> R.string.error_validation_invalid_date
            VALIDATION_INVALID_TIME -> R.string.error_validation_invalid_time
            VALIDATION_REQUIRED_FIELD -> R.string.error_validation_required_field
            
            // Errores de red
            NETWORK_ERROR -> R.string.error_network
            NETWORK_UNAVAILABLE -> R.string.error_network_unavailable
            NETWORK_TIMEOUT -> R.string.error_network_timeout
            NETWORK_SERVER_UNREACHABLE -> R.string.error_server_unreachable
            
            // Errores de IA
            AI_SERVICE_ERROR -> R.string.error_ai_service
            AI_SERVICE_UNAVAILABLE -> R.string.error_ai_service_unavailable
            AI_RATE_LIMIT -> R.string.error_ai_rate_limit
            AI_COST_LIMIT -> R.string.error_ai_cost_limit
            AI_INVALID_RESPONSE -> R.string.error_ai_invalid_response
            AI_EMPTY_RESPONSE -> R.string.error_ai_empty_response
            
            // Errores de suscripción
            SUBSCRIPTION_REQUIRED -> R.string.error_subscription_required
            SUBSCRIPTION_EXPIRED -> R.string.error_subscription_expired
            PURCHASE_FAILED -> R.string.error_purchase_failed
            RESTORE_FAILED -> R.string.error_restore_failed
            
            // Errores de autenticación
            AUTH_FAILED -> R.string.error_auth_failed
            USER_NOT_FOUND -> R.string.error_user_not_found
            INVALID_CREDENTIALS -> R.string.error_invalid_credentials
            EMAIL_EXISTS -> R.string.error_email_exists
            
            // Errores de Social Media
            CONTENT_GENERATION_ERROR -> R.string.error_content_generation
            PAGE_DATA_INCOMPLETE -> R.string.error_page_data_incomplete
            CONTEXT_DATA_INCOMPLETE -> R.string.error_context_data_incomplete
            
            // Errores de base de datos
            DATABASE_CONNECTION -> R.string.error_database_connection
            DATABASE_QUERY -> R.string.error_database_query
            DATABASE_NOT_FOUND -> R.string.error_database_not_found
            
            // Errores de archivos
            FILE_NOT_FOUND -> R.string.error_file_not_found
            FILE_READ_ERROR -> R.string.error_file_read
            FILE_WRITE_ERROR -> R.string.error_file_write
            FILE_TOO_LARGE -> R.string.error_file_too_large
            
            // Errores generales
            UNKNOWN_ERROR -> R.string.error_unknown
            GENERAL_ERROR -> R.string.error_general
            NOT_IMPLEMENTED -> R.string.error_not_implemented
            PERMISSION_DENIED -> R.string.error_permission_denied
            TIMEOUT -> R.string.error_timeout
            
            // Por defecto
            else -> null
        }
    }
    
    /**
     * Obtiene un mensaje de error amigable para el usuario basado en el código de error.
     */
    fun getFriendlyMessage(context: Context, errorCode: Int, defaultMessage: String = "Unknown error"): String {
        val stringResId = getStringResourceId(errorCode)
        return if (stringResId != null) {
            context.getString(stringResId)
        } else {
            defaultMessage
        }
    }
    
    /**
     * Verifica si un error es recuperable.
     */
    fun isRecoverable(errorCode: Int): Boolean {
        return when (errorCode) {
            NETWORK_ERROR,
            NETWORK_UNAVAILABLE,
            NETWORK_TIMEOUT,
            AI_RATE_LIMIT,
            PURCHASE_FAILED,
            AUTH_FAILED,
            INVALID_CREDENTIALS -> true
            
            SUBSCRIPTION_REQUIRED,
            SUBSCRIPTION_EXPIRED,
            USER_NOT_FOUND,
            EMAIL_EXISTS -> false
            
            else -> false
        }
    }
    
    /**
     * Obtiene el título apropiado para el diálogo de error.
     */
    fun getDialogTitle(context: Context, errorCode: Int): String {
        val titleResId = getDialogTitleResourceId(errorCode)
        return context.getString(titleResId)
    }
    
    /**
     * Obtiene el ID del recurso string para el título del diálogo.
     */
    fun getDialogTitleResourceId(errorCode: Int): Int {
        return when {
            errorCode in 1000..1999 -> R.string.error_title_warning
            errorCode in 2000..2999 -> R.string.error_title_attention
            errorCode in 3000..3999 -> R.string.error_title_attention
            errorCode in 6000..6999 -> R.string.error_title_warning
            errorCode in 7000..7999 -> R.string.error_title_info
            errorCode in 8000..8999 -> R.string.error_title_attention
            errorCode in 9000..9999 -> R.string.error_title_attention
            else -> R.string.error_title_attention
        }
    }
}
