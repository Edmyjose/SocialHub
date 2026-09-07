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

package com.ejsstudios.socialhub.navigation

import android.app.Application
import androidx.compose.material3.FabPosition
import com.ejsstudios.socialhub.viewmodels.BaseViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Representa el estado del icono de navegación en la TopAppBar.
 * Siguiendo las mejores prácticas, evitamos guardar @Composables en el ViewModel.
 */
sealed class NavigationIconState {
    object None : NavigationIconState()
    object Menu : NavigationIconState()
    object Back : NavigationIconState()
    data class Custom(val iconRes: Int, val contentDescription: String?) : NavigationIconState()
}

class NavigationViewModel(application: Application) : BaseViewModel<Unit>(application, Unit) {

    val maxIconActionBar = 2

    // Estado del icono de navegación (UDF)
    private val _navigationIconState = MutableStateFlow<NavigationIconState>(NavigationIconState.None)
    val navigationIconState: StateFlow<NavigationIconState> = _navigationIconState.asStateFlow()

    // Callback para el click del icono
    private val _onNavigationClick = MutableStateFlow<(() -> Unit)?>(null)
    val onNavigationClick: StateFlow<(() -> Unit)?> = _onNavigationClick.asStateFlow()

    /**
     * Configura el icono de navegación y su acción de forma dinámica.
     */
    fun setNavigationConfig(
        icon: NavigationIconState,
        onClick: (() -> Unit)? = null
    ) {
        _navigationIconState.value = icon
        _onNavigationClick.value = onClick
    }

    private val _fabAction = MutableStateFlow<(() -> Unit)?>(null)
    val fabAction: StateFlow<(() -> Unit)?> = _fabAction.asStateFlow()

    fun setFabAction(fab: (() -> Unit)?) {
        _fabAction.value = fab
    }

    private val _floatingActionButtonPosition = MutableStateFlow(FabPosition.Center)
    val floatingActionButtonPosition: StateFlow<FabPosition> = _floatingActionButtonPosition.asStateFlow()

    fun setFloatingActionButtonPosition(newFabPosition: FabPosition) {
        _floatingActionButtonPosition.value = newFabPosition
    }

    private val _titleState = MutableStateFlow("")
    val titleState: StateFlow<String> = _titleState.asStateFlow()

    fun setTitle(title: String) {
        _titleState.value = title
    }

    private val _menuItems = MutableStateFlow<List<TopMenuItems>>(emptyList())
    val menuItems: StateFlow<List<TopMenuItems>> = _menuItems.asStateFlow()

    private val _showTopBar = MutableStateFlow(false)
    val showTopBar: StateFlow<Boolean> = _showTopBar.asStateFlow()

    fun setShowTopBar(active: Boolean) {
        _showTopBar.value = active
    }

    fun setMenuItems(itemList: List<TopMenuItems>) {
        _menuItems.value = itemList
    }

    private val _bottomBarAction = MutableStateFlow<List<BottomNavItem>?>(null)
    val bottomBarAction: StateFlow<List<BottomNavItem>?> = _bottomBarAction.asStateFlow()

    fun setBottomBarAction(newAction: List<BottomNavItem>?) {
        _bottomBarAction.value = newAction
    }
}
