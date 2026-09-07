/*******************************************************************************
 * Copyright (c) 2026. EJS Studios. Todos los derechos reservados.
 * Este código fuente es propiedad de EJS Studios y está protegido por las leyes de derechos de autor.
 * No se permite la copia, distribución, modificación o uso de este código fuente, total o parcialmente, sin
 * el consentimiento previo y por escrito de EJS Studios.
 *
 * Cualquier uso no autorizado de este código fuente será perseguido legalmente según las leyes aplicables.
 *
 * Para obtener una licencia de uso, contacta a: info@ejsstudios.com
 ******************************************************************************/
package com.ejsstudios.socialhub.error

/**
 * Excepción personalizada que incluye un código de error además del mensaje.
 *
 * Esta clase permite crear excepciones con códigos de error específicos para facilitar el manejo y
 * categorización de errores en la aplicación.
 *
 * @param errorCode El código numérico que identifica el tipo de error.
 * @param message El mensaje descriptivo del error.
 *
 * Ejemplo de uso:
 * ```kotlin
 * throw CustomException(
 *     errorCode = ErrorCodes.VALIDATION_INVALID_TIMEZONE,
 *     message = "Formato de zona horaria inválido"
 * )
 *
 * // Capturar y manejar
 * try {
 *     // código que puede lanzar CustomException
 * } catch (e: CustomException) {
 *     when (e.errorCode) {
 *         ErrorCodes.VALIDATION_INVALID_TIMEZONE -> handleInvalidTimezone()
 *         ErrorCodes.NETWORK_ERROR -> handleNetworkError()
 *         else -> handleGenericError()
 *     }
 * }
 * ```
 */
open class CustomException(val errorCode: Int, message: String) : Exception(message)

/**
 * Excepción para usuario no encontrado
 */
class UserNotFoundException(message: String = "Usuario no encontrado") : Exception(message)

/**
 * Excepción para validación de datos
 */
class ValidationException(
    errorCode: Int = ErrorCodes.VALIDATION_GENERAL,
    message: String = "Error de validación"
) : CustomException(errorCode, message)

/**
 * Excepción para errores de red
 */
class NetworkException(
    errorCode: Int = ErrorCodes.NETWORK_ERROR,
    message: String = "Error de conexión"
) : CustomException(errorCode, message)

/**
 * Excepción para errores de servidor
 */
class ServerException(
    errorCode: Int = ErrorCodes.SERVER_ERROR,
    message: String = "Error del servidor"
) : CustomException(errorCode, message)

/**
 * Excepción para errores de base de datos
 */
class DatabaseException(
    errorCode: Int = ErrorCodes.DATABASE_ERROR,
    message: String = "Error de base de datos"
) : CustomException(errorCode, message)

/**
 * Excepción para errores de IA
 */
class AIException(
    errorCode: Int = ErrorCodes.AI_SERVICE_ERROR,
    message: String = "Error de servicio de IA"
) : CustomException(errorCode, message)

/**
 * Excepción para errores de suscripción
 */
class SubscriptionException(
    errorCode: Int = ErrorCodes.SUBSCRIPTION_REQUIRED,
    message: String = "Error de suscripción"
) : CustomException(errorCode, message)
