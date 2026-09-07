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
package com.ejsstudios.socialhub.firebase.db.states

sealed class FirestoreState<T> {
    class Loading<T> : FirestoreState<T>()
    data class Success<T>(val data: T) : FirestoreState<T>()
    data class Error<T>(val exception: Exception) : FirestoreState<T>()
}
