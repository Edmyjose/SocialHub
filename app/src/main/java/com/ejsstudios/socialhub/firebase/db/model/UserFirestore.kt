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

package com.ejsstudios.socialhub.firebase.db.model

import androidx.annotation.Keep
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.PropertyName
import com.squareup.moshi.JsonClass


/**
 * Modelo de DOMINIO y FIRESTORE.
 * Representa la identidad básica del usuario en la aplicación (Perfil de SocialHub).
 *
 * Este modelo es independiente de las cuentas de redes sociales vinculadas para gestión.
 * Aquí se guarda quién es el usuario y cómo se autenticó en SocialHub AI.
 */
@Keep
@IgnoreExtraProperties
@JsonClass(generateAdapter = true)
data class UserFirestore(
    /**
     * UID único generado por Firebase Authentication.
     * Es la Primary Key que vincula todo el ecosistema del usuario.
     */
    @get:PropertyName("id")
    @set:PropertyName("id")
    var id: String = "",

    /**
     * Identificador del método de inicio de sesión (ej: "google.com", "facebook.com", "password").
     * Ayuda a saber qué flujo de re-autenticación seguir.
     */
    @get:PropertyName("auth_provider")
    @set:PropertyName("auth_provider")
    var authProvider: String = "email",

    /**
     * ID único proporcionado por el proveedor externo (ej: el UID de Google o el ID de Facebook).
     * Útil para evitar duplicidad de cuentas con el mismo correo en distintos proveedores.
     */
    @get:PropertyName("auth_external_id")
    @set:PropertyName("auth_external_id")
    var authExternalId: String? = null,

    /** Nombre para mostrar del usuario. */
    @get:PropertyName("name")
    @set:PropertyName("name")
    var name: String = "",

    /** Correo electrónico principal de la cuenta. */
    @get:PropertyName("email")
    @set:PropertyName("email")
    var email: String? = null,

    /** URL de la imagen de perfil del usuario. */
    @get:PropertyName("picture")
    @set:PropertyName("picture")
    var picture: String? = null,

    /** Saldo actual de créditos para uso de funciones de IA. */
    @get:PropertyName("credits")
    @set:PropertyName("credits")
    var credits: Int = 0,

    /** Nivel de suscripción actual (ej: "free", "pro", "agency"). */
    @get:PropertyName("subscription_tier")
    @set:PropertyName("subscription_tier")
    var subscriptionTier: String = "free",

    /** Timestamp de creación del perfil en milisegundos. */
    @get:PropertyName("created_at")
    @set:PropertyName("created_at")
    var createdAt: Long = System.currentTimeMillis(),

    /** Timestamp de la última actualización del perfil en milisegundos. */
    @get:PropertyName("updated_at")
    @set:PropertyName("updated_at")
    var updatedAt: Long = System.currentTimeMillis()
) {
    /**
     * Convierte el modelo a un mapa para persistencia en Firestore.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "auth_provider" to authProvider,
        "auth_external_id" to authExternalId,
        "name" to name,
        "email" to email,
        "picture" to picture,
        "credits" to credits,
        "subscription_tier" to subscriptionTier,
        "created_at" to createdAt,
        "updated_at" to updatedAt
    )
}


/**
 * Extensión para convertir el objeto [FirebaseUser] nativo de Firebase Auth
 * a nuestro modelo de dominio [UserFirestore] para persistencia y lógica interna.
 */
fun FirebaseUser.toFirestore(): UserFirestore {
    // Identificamos el proveedor principal ignorando el genérico de Firebase
    val providerInfo =
        this.providerData.find { it.providerId != "firebase" && it.providerId != "password" }
    val now = System.currentTimeMillis()

    return UserFirestore(
        id = this.uid,
        authProvider = providerInfo?.providerId ?: this.providerId,
        authExternalId = providerInfo?.uid,
        name = this.displayName ?: "",
        email = this.email,
        picture = this.photoUrl?.toString(),
        createdAt = now,
        updatedAt = now
    )
}
