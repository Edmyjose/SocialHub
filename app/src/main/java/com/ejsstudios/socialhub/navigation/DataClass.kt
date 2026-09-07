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

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Help
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Share
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import com.ejsstudios.socialhub.R
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CAMPAIGNS
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CONTENTS
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CONTEXT_CONFIG
import com.ejsstudios.socialhub.utils.Constants.ROUTE_HELP
import com.ejsstudios.socialhub.utils.Constants.ROUTE_HOME
import com.ejsstudios.socialhub.utils.Constants.ROUTE_PRIVACY
import com.ejsstudios.socialhub.utils.Constants.ROUTE_PROFILE
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SCHEMES
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SOCIAL_HUB

object myRoute {

    val bottomNavItems = listOf(
        BottomNavItem.RouteBased(
            id = 1,
            name = R.string.nav_dashboard,
            selectedIcon = Icons.Filled.Home,
            unSelectedIcon = Icons.Outlined.Home,
            route = ROUTE_HOME,
        ),
        BottomNavItem.RouteBased(
            id = 2,
            name = R.string.nav_social_hub,
            selectedIcon = Icons.Filled.Share,
            unSelectedIcon = Icons.Outlined.Share,
            route = ROUTE_SOCIAL_HUB,
        ),
        BottomNavItem.RouteBased(
            id = 3,
            name = R.string.nav_profile,
            selectedIcon = Icons.Filled.AccountCircle,
            unSelectedIcon = Icons.Outlined.AccountCircle,
            route = ROUTE_PROFILE,
        )
    )

    val drawerMenuItems = listOf(
        DrawerMenuItem(
            id = 1,
            title = "Inicio",
            route = ROUTE_HOME,
            selectedIcon = Icons.Filled.Home,
            unSelectedIcon = Icons.Outlined.Home
        ),
        DrawerMenuItem(
            id = 2,
            title = "Social Hub",
            route = ROUTE_SOCIAL_HUB,
            selectedIcon = Icons.Filled.Share,
            unSelectedIcon = Icons.Outlined.Share
        ),
        DrawerMenuItem(
            id = 3,
            title = "Configurar Contexto",
            route = ROUTE_CONTEXT_CONFIG,
            selectedIcon = Icons.Filled.SettingsSuggest,
            unSelectedIcon = Icons.Outlined.SettingsSuggest
        ),
        DrawerMenuItem(
            id = 4,
            title = "Campañas",
            route = ROUTE_CAMPAIGNS,
            selectedIcon = Icons.Filled.Campaign,
            unSelectedIcon = Icons.Outlined.Campaign
        ),
        DrawerMenuItem(
            id = 5,
            title = "Esquemas",
            route = ROUTE_SCHEMES,
            selectedIcon = Icons.Filled.AccountTree,
            unSelectedIcon = Icons.Outlined.AccountTree
        ),
        DrawerMenuItem(
            id = 6,
            title = "Contenidos",
            route = ROUTE_CONTENTS,
            selectedIcon = Icons.Filled.AutoStories,
            unSelectedIcon = Icons.Outlined.AutoStories
        ),
        DrawerMenuItem(
            id = 7,
            title = "Privacidad",
            route = ROUTE_PRIVACY,
            selectedIcon = Icons.Filled.Lock,
            unSelectedIcon = Icons.Outlined.Lock
        ),
        DrawerMenuItem(
            id = 8,
            title = "Ayuda y Soporte",
            route = ROUTE_HELP,
            selectedIcon = Icons.Filled.Help,
            unSelectedIcon = Icons.Outlined.Help
        )
    )
}

data class RouteConfiguration @OptIn(ExperimentalSharedTransitionApi::class) constructor(
    val route: String,
    val screen: @Composable (
        backStackEntry: NavBackStackEntry,
        sharedTransitionScope: SharedTransitionScope,
        animatedContentScope: AnimatedContentScope
    ) -> Unit
)

data class DrawerMenuItem(
    val id: Int,
    val title: String,
    override val route: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector,
    val enabled: Boolean = true,
    val subItems: List<SubItem> = emptyList()
) : RouteItem

data class SubItem(
    val id: Int,
    val title: String,
    override val route: String,
    val selectedIcon: ImageVector,
    val unSelectedIcon: ImageVector
) : RouteItem

interface RouteItem {
    val route: String
}

sealed class BottomNavItem(
    open val id: Int,
    open val name: Int,
    open val selectedIcon: ImageVector,
    open val unSelectedIcon: ImageVector
) : RouteItem {
    data class RouteBased(
        override val id: Int,
        override val name: Int,
        override val selectedIcon: ImageVector,
        override val unSelectedIcon: ImageVector,
        override val route: String
    ) : BottomNavItem(id, name, selectedIcon, unSelectedIcon)

    data class CallbackBased(
        override val id: Int,
        override val name: Int,
        override val selectedIcon: ImageVector,
        override val unSelectedIcon: ImageVector,
        override val route: String = "",
        val callback: () -> Unit
    ) : BottomNavItem(id, name, selectedIcon, unSelectedIcon)
}

enum class ShowAsAction {
    ALWAYS,
    IF_ROOM,
    NEVER
}

data class TopMenuItems(
    val icon: ImageVector,
    val name: String,
    val onClick: () -> Unit,
    val showAsAction: ShowAsAction = ShowAsAction.IF_ROOM
)
