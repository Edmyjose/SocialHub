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
package com.ejsstudios.socialhub.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.ejsstudios.socialhub.error.ExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseViewModel<STATE>(
    application: Application,
    private val initialState: STATE
) : AndroidViewModel(application) {
    val TAG = this::class.java.simpleName

    protected open val _uiState = MutableStateFlow(initialState)
    open val uiState: StateFlow<STATE> = _uiState.asStateFlow()

    open fun setState(state: STATE) {
        _uiState.value = state
    }

    open fun clearState() {
        _uiState.value = initialState
    }

    open fun handleException(e: Throwable) {
        ExceptionHandler.handleCaughtException(e)
    }
}