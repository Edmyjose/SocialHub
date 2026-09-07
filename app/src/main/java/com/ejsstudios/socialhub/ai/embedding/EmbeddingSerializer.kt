/**
 * ***************************************************************************** Copyright (c) 2026.
 * EJS Studios. Todos los derechos reservados.
 */
package com.ejsstudios.socialhub.ai.embedding

import android.util.Base64
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Utilidad para serializar y deserializar embeddings localmente en Android.
 *
 * Esta clase proporciona conversión entre List<Float> y String Base64 para almacenar embeddings en
 * Firestore de manera eficiente.
 *
 * Características:
 * - NO requiere IA
 * - NO requiere llamada a PHP
 * - Operación local instantánea (< 1ms)
 * - Funciona offline
 * - Soporta cualquier dimensión de embedding (384, 768, 1024, etc.)
 *
 * Formato de serialización:
 * - List<Float> → ByteArray (usando ByteBuffer con LITTLE_ENDIAN)
 * - ByteArray → Base64 String (usando Android Base64 con NO_WRAP)
 *
 * Ejemplo de uso:
 * ```kotlin
 * val embedding = listOf(0.1f, 0.2f, 0.3f, ...)
 * val serialized = EmbeddingSerializer.serialize(embedding)
 * val deserialized = EmbeddingSerializer.deserialize(serialized)
 * ```
 */
object EmbeddingSerializer {

    /**
     * Dimensiones comunes de embeddings según el modelo. Solo para referencia, el serializador
     * acepta cualquier dimensión.
     */
    private const val DIMENSIONS_MINILM_L6 = 384 // sentence-transformers/all-MiniLM-L6-v2
    private const val DIMENSIONS_BGE_BASE = 768 // BAAI/bge-base-en-v1.5
    private const val DIMENSIONS_E5_LARGE = 1024 // intfloat/multilingual-e5-large

    /**
     * Serializa un embedding a String Base64.
     *
     * Conversión: List<Float> → ByteArray → Base64 String
     *
     * NOTA: Acepta embeddings de cualquier dimensión (384, 768, 1024, etc.)
     *
     * @param embedding Lista de floats que representa el embedding
     * @return String Base64 que representa el embedding serializado
     * @throws IllegalArgumentException si el embedding está vacío
     */
    fun serialize(embedding: List<Float>): String {
        require(embedding.isNotEmpty()) { "El embedding no puede estar vacío" }

        // Crear buffer de bytes (4 bytes por float)
        val buffer = ByteBuffer.allocate(embedding.size * 4)
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        // Escribir cada float al buffer
        embedding.forEach { value -> buffer.putFloat(value) }

        // Convertir a Base64
        val bytes = buffer.array()
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    /**
     * Deserializa un String Base64 a embedding.
     *
     * Conversión: Base64 String → ByteArray → List<Float>
     *
     * NOTA: Retorna el embedding con las dimensiones originales (puede ser 384, 768, 1024, etc.)
     *
     * @param serialized String Base64 que representa el embedding
     * @return Lista de floats que representa el embedding
     * @throws IllegalArgumentException si el string no es Base64 válido
     */
    fun deserialize(serialized: String): List<Float> {
        try {
            // Decodificar Base64
            val bytes = Base64.decode(serialized, Base64.NO_WRAP)

            // Crear buffer para leer floats
            val buffer = ByteBuffer.wrap(bytes)
            buffer.order(ByteOrder.LITTLE_ENDIAN)

            // Leer floats del buffer
            val embedding = mutableListOf<Float>()
            while (buffer.hasRemaining()) {
                embedding.add(buffer.float)
            }

            require(embedding.isNotEmpty()) { "El embedding deserializado está vacío" }

            return embedding
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Error al deserializar embedding: ${e.message}", e)
        } catch (e: Exception) {
            throw IllegalArgumentException("String Base64 inválido: ${e.message}", e)
        }
    }

    /**
     * Valida que un embedding serializado sea válido.
     *
     * Verifica que:
     * - El string sea Base64 válido
     * - Al deserializar, no esté vacío
     *
     * @param serialized String Base64 que representa el embedding
     * @return true si el embedding es válido, false en caso contrario
     */
    fun isValid(serialized: String): Boolean {
        return try {
            val deserialized = deserialize(serialized)
            deserialized.isNotEmpty()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene las dimensiones de un embedding serializado sin deserializarlo completamente.
     *
     * @param serialized String Base64 que representa el embedding
     * @return Número de dimensiones del embedding, o null si es inválido
     */
    fun getDimensions(serialized: String): Int? {
        return try {
            val bytes = Base64.decode(serialized, Base64.NO_WRAP)
            bytes.size / 4 // Cada float ocupa 4 bytes
        } catch (e: Exception) {
            null
        }
    }
}
