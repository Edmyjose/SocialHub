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

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcher
import androidx.activity.addCallback
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ejsstudios.socialhub.utils.Constants.ROUTE_DASHBOARD
import com.ejsstudios.socialhub.utils.Constants.ROUTE_HOME
import com.ejsstudios.socialhub.viewmodels.MainViewModel


/*fun goRoute(navController: NavHostController, route: String) {
    navController.navigate(route) {
        launchSingleTop = true
        restoreState = true
    }
}*/

fun goRoute(
    navController: NavHostController,
    route: String,
    popUpToRoute: String? = null,
    inclusive: Boolean = false,
    saveState: Boolean = false
) {
    navController.navigate(route) {
        launchSingleTop = true
        restoreState = true
        // Manejo opcional de popUpTo
        popUpToRoute?.let {
            popUpTo(it) {
                this.inclusive = inclusive
                this.saveState = saveState
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun NavigationOld(
    navController: NavHostController,
    routeConfigurations: List<RouteConfiguration>,
    startDestination: String = routeConfigurations.first().route,
) {
    SharedTransitionLayout {
        if (routeConfigurations.isEmpty()) return@SharedTransitionLayout
        NavHost(
            navController = navController,
            startDestination = startDestination,
        ) {
            routeConfigurations.forEach { routeConfig ->
                composable(routeConfig.route) { backStackEntry->
                    routeConfig.screen(backStackEntry, this@SharedTransitionLayout, this@composable )
                }
            }
        }
    }
    // Handle back navigation
    /*val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    BackHandler(navController, backDispatcher)*/

}
@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Navigation(
    navController: NavHostController,
    routeConfigurations: List<RouteConfiguration>,
    startDestination: String = routeConfigurations.first().route,
    modifier: Modifier
) {
    SharedTransitionLayout {
        if (routeConfigurations.isEmpty()) return@SharedTransitionLayout
        NavHost(
            navController = navController,
            startDestination =  startDestination,
            modifier = modifier
        ) {
            routeConfigurations.forEach { routeConfig ->
                composable(routeConfig.route) { backStackEntry ->
                    routeConfig.screen(backStackEntry, this@SharedTransitionLayout, this@composable)
                }
            }
        }
    }
    // Handle back navigation
    /*val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    BackHandler(navController, backDispatcher)*/

}

@Composable
fun <T : RouteItem> findMenuItemIndexByRoute(
    route: String,
    menuItems: List<T>
): Int {
    /*val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination?.route*/

    // Verifica el tipo de los elementos en la lista
    menuItems.forEach { item ->
        when (item) {
            is BottomNavItem -> {
                if (item.route == route) {
                    return item.id
                }
            }

            is DrawerMenuItem -> {
                if (item.route == route) {
                    return item.id
                }
                // Busca en los SubItem si existe
                item.subItems.forEach { subItem ->
                    if (subItem.route == route) {
                        return subItem.id
                    }
                }
            }
        }
    }
    return 0
}

@Composable
fun currentRoute( navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Composable
fun BackHandler(navController: NavHostController, backDispatcher: OnBackPressedDispatcher?) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    // Solo maneja el back si no estamos en la pantalla de inicio
    if (currentDestination?.route != ROUTE_HOME) {
        DisposableEffect(Unit) {
            // Crea el callback personalizado para manejar el botón de retroceso
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navController.navigate(ROUTE_HOME) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            }
            // Asigna el callback al dispatcher
            backDispatcher?.addCallback(callback)

            // Elimina el callback al finalizar el efecto
            onDispose {
                callback.remove()
            }
        }
    }
}

@Composable
fun BackHandlers(navController: NavHostController, backDispatcher: OnBackPressedDispatcher?) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = currentBackStackEntry?.destination

    if (currentDestination?.route != "ROUTE_HOME") {
        DisposableEffect(Unit) {
            val callback = backDispatcher?.addCallback {
                navController.navigate("ROUTE_HOME") {
                    popUpTo(navController.graph.startDestinationId) {
                        saveState = true
                    }
                    restoreState = true
                    launchSingleTop = true
                }
            }
            onDispose {
                callback?.remove()
            }
        }
    }
}

@Composable
fun ObserveNavigationIcon(navController: NavHostController, mainViewModel: MainViewModel) {
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    LaunchedEffect(currentBackStackEntry?.destination?.route) {
        when (currentBackStackEntry?.destination?.route) {
            ROUTE_HOME, ROUTE_DASHBOARD -> {
                /*mainViewModel.navigationViewModel.setNavigationIcon {
                    var backPressedTime by remember { mutableLongStateOf(0L) }
                    val context = LocalContext.current
                    val toast = remember {
                        Toast.makeText(
                            context,
                            context.getString(R.string.lbl_press_again_to_exit),
                            Toast.LENGTH_SHORT
                        )
                    }

                    IconButton(
                        onClick = {
                        if (backPressedTime + 2000 > System.currentTimeMillis()) {
                            toast.cancel()
                            (context as? Activity)?.finish() // Cierra la app
                        } else {
                            toast.show()
                        }
                        backPressedTime = System.currentTimeMillis()
                    }) {
                        Icon(Icons.Filled.Close, contentDescription = "ExitToApp")
                    }
                }*/
            }

            else -> {
                // En tu HubSocialScreen, por ejemplo:
                mainViewModel.navigationViewModel.setNavigationConfig(
                    icon = NavigationIconState.Back,
                    onClick = { navController.popBackStack() } // Lógica dinámica
                )

                /*mainViewModel.navigationViewModel.setNavigationIcon {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            modifier = Modifier
                                .size(32.dp)
                                .padding(start = 4.dp),
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "ArrowBack",
                            tint = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }*/
            }
        }
    }
}


