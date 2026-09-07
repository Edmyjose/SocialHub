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

package com.ejsstudios.socialhub.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.annotation.DrawableRes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.net.HttpURLConnection
import java.net.URL

object ImageUtils {

    /**
     * Descarga una imagen desde una URL y la devuelve como ByteArray.
     */
    suspend fun urlToByteArray(url: String): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            input.readBytes()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convierte un recurso drawable a ByteArray.
     */
    fun drawableToByteArray(context: Context, @DrawableRes drawableRes: Int): ByteArray? {
        return try {
            val bitmap = BitmapFactory.decodeResource(context.resources, drawableRes)
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            outputStream.toByteArray()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Descarga una imagen desde una URL y la convierte a una cadena Base64.
     */
    suspend fun urlToBase64(url: String): String? = withContext(Dispatchers.IO) {
        urlToByteArray(url)?.let { bytes ->
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }

    /**
     * Convierte un recurso drawable a una cadena Base64.
     */
    fun drawableToBase64(context: Context, @DrawableRes drawableRes: Int): String? {
        return drawableToByteArray(context, drawableRes)?.let { bytes ->
            Base64.encodeToString(bytes, Base64.NO_WRAP)
        }
    }

    /**
     * Convierte un Bitmap a una cadena Base64.
     */
    fun bitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Convierte una cadena Base64 a un Bitmap.
     */
    fun base64ToBitmap(base64String: String): Bitmap? {
        return try {
            val cleanString = base64String
                .substringAfter("base64,")
                .replace("\\s".toRegex(), "")
                .replace("\n", "")
                .replace("\r", "")

            val decodedBytes = Base64.decode(cleanString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            null
        }
    }
}
