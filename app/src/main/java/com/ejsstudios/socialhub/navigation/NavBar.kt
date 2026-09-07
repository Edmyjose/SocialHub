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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Green
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.ejsstudios.socialhub.utils.Constants.LocalDashboardNavController
import com.ejsstudios.socialhub.viewmodels.MainViewModel
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.firstOrNull
import kotlin.collections.forEach
import kotlin.collections.isNotEmpty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OverflowTopAppBars(mainViewModel: NavigationViewModel, modifier: Modifier = Modifier) {
    val maxBarIcon = mainViewModel.maxIconActionBar
    /*val navigationIcon by mainViewModel.navigationIcon.collectAsState()*/
    val iconState by mainViewModel.navigationIconState.collectAsStateWithLifecycle()
    val onIconClick by mainViewModel.onNavigationClick.collectAsStateWithLifecycle()

    val title by mainViewModel.titleState.collectAsState()
    val topMenuItems by mainViewModel.menuItems.collectAsState()
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier =
            modifier.fillMaxWidth()

    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = MaterialTheme.colorScheme.onPrimary,
                actionIconContentColor = MaterialTheme.colorScheme.onPrimary,
                navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
            ),
            navigationIcon = {
                /*Box(
                    modifier =
                        Modifier
                            .padding(8.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                MaterialTheme.colorScheme.onPrimary.copy(
                                    alpha = 0.2f
                                )
                            ),
                    contentAlignment = Alignment.Center
                ) { navigationIcon() }*/

                // Decidimos qué icono mostrar basándonos en el estado del ViewModel
                when (iconState) {
                    is NavigationIconState.Menu -> {
                        IconButton(onClick = { onIconClick?.invoke() }) {
                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(start = 4.dp),
                                imageVector = Icons.Default.Menu,
                                contentDescription = "manu",
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    is NavigationIconState.Back -> {
                        IconButton(onClick = { onIconClick?.invoke() }) {
                            Icon(
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(start = 4.dp),
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ArrowBack",
                                tint = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                    is NavigationIconState.Custom -> {
                        IconButton(onClick = { onIconClick?.invoke() }) {
                            Icon(
                                painter = painterResource(id = (iconState as NavigationIconState.Custom).iconRes),
                                contentDescription = (iconState as NavigationIconState.Custom).contentDescription
                            )
                        }
                    }
                    NavigationIconState.None -> { /* No dibujamos nada */ }
                }
            },
            title = {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp)
                        .basicMarquee(),
                    text = title,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            },
            actions = {
                val visibleActions = mutableListOf<TopMenuItems>()
                val overflowActions = mutableListOf<TopMenuItems>()

                // Prioritize ALWAYS items
                topMenuItems.filter { it.showAsAction == ShowAsAction.ALWAYS }.forEach { menuItem ->
                    if (visibleActions.size < maxBarIcon) {
                        visibleActions.add(menuItem)
                    } else {
                        overflowActions.add(menuItem)
                    }
                }

                // Add IF_ROOM items if space allows
                topMenuItems.filter { it.showAsAction == ShowAsAction.IF_ROOM }.forEach { menuItem ->
                    if (visibleActions.size < maxBarIcon && menuItem !in visibleActions
                    ) { // Ensure not already added
                        visibleActions.add(menuItem)
                    } else if (menuItem !in visibleActions
                    ) { // Add to overflow if not already there
                        overflowActions.add(menuItem)
                    }
                }

                // Add any remaining items (e.g., those with ShowAsAction.NEVER or
                // if maxBarIcon
                // was
                // small)
                // to overflow if they aren't already visible.
                topMenuItems
                    .filter { it !in visibleActions && it !in overflowActions }
                    .forEach { overflowActions.add(it) }

                // Display visible actions
                visibleActions.forEach { menuItem ->
                    ActionIconButton(menuItem = menuItem) // Helper composable
                }

                // Display overflow menu if there are items for it
                if (overflowActions.isNotEmpty()) {
                    IconButton(onClick = { showMenu = !showMenu }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "TopApp Menu")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        overflowActions.forEach { menuItem -> // Iterate over overflowActions
                            DropdownMenuItem(
                                onClick = {
                                    menuItem.onClick()
                                    showMenu = false
                                },
                                text = { Text(menuItem.name) },
                                leadingIcon = {
                                    Icon(menuItem.icon, contentDescription = menuItem.name)
                                }
                            )
                        }
                    }
                }
            },
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        )
    }
}

// Helper Composable for action items
@Composable
private fun ActionIconButton(menuItem: TopMenuItems) {
    IconButton(onClick = menuItem.onClick) {
        Icon(menuItem.icon, contentDescription = menuItem.name)
    }
}


@Composable
fun gradient() = listOf(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primaryContainer)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(mainViewModel: MainViewModel, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val navigationViewModel = mainViewModel.navigationViewModel
    val maxBarIcon = navigationViewModel.maxIconActionBar

    /*val navigationIcon by navigationViewModel.navigationIcon.collectAsState()*/
    val iconState by navigationViewModel.navigationIconState.collectAsStateWithLifecycle()
    val onIconClick by navigationViewModel.onNavigationClick.collectAsStateWithLifecycle()
    val title by navigationViewModel.titleState.collectAsState()
    val topMenuItems by navigationViewModel.menuItems.collectAsState()

    var showMenu by remember { mutableStateOf(false) }

    val visibleActions = mutableListOf<TopMenuItems>()
    val overflowActions = mutableListOf<TopMenuItems>()

    topMenuItems.filter { it.showAsAction == ShowAsAction.ALWAYS }.forEach { menuItem ->
        if (visibleActions.size < maxBarIcon) {
            visibleActions.add(menuItem)
        } else overflowActions.add(menuItem)
    }

    topMenuItems.filter { it.showAsAction == ShowAsAction.IF_ROOM }.forEach { menuItem ->
        if (visibleActions.size < maxBarIcon && menuItem !in visibleActions) {
            visibleActions.add(menuItem)
        } else if (menuItem !in visibleActions) {
            overflowActions.add(menuItem)
        }
    }

    topMenuItems.filter { it !in visibleActions && it !in overflowActions }.forEach {
        overflowActions.add(it)
    }

    // --------- BARRA TRADICIONAL ----------
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .onGloballyPositioned { layoutCoordinates ->
                val pxHeight = layoutCoordinates.size.height
                mainViewModel.setPaddingTop(with(density) { pxHeight.toDp() })
            },
        color = MaterialTheme.colorScheme.primaryContainer,
        shadowElevation = 4.dp
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            AnimatedHorizontalGradient(
                colors = gradient(),
                modifier = Modifier.matchParentSize()
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 4.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            )
            {
                // Decidimos qué icono mostrar basándonos en el estado del ViewModel
                when (iconState) {
                    is NavigationIconState.Menu -> {
                        IconButton(onClick = { onIconClick?.invoke() }) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is NavigationIconState.Back -> {
                        IconButton(onClick = { onIconClick?.invoke() }) {
                            Icon(
                                modifier = Modifier.size(28.dp),
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ArrowBack",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    is NavigationIconState.Custom -> {
                        IconButton(onClick = { onIconClick?.invoke() }) {
                            Icon(
                                painter = painterResource(id = (iconState as NavigationIconState.Custom).iconRes),
                                contentDescription = (iconState as NavigationIconState.Custom).contentDescription,
                                modifier = Modifier.size(28.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    NavigationIconState.None -> { /* No dibujamos nada */ }
                }

                Text(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .basicMarquee(),
                    text = title,
                    textAlign = TextAlign.Start,
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 18.sp
                )

                // ICONOS VISIBLES
                visibleActions.forEach { menuItem ->
                    IconButton(onClick = menuItem.onClick) {
                        Icon(
                            imageVector = menuItem.icon,
                            contentDescription = menuItem.name,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // OVERFLOW
                if (overflowActions.isNotEmpty()) {
                    Box {
                        IconButton(onClick = { showMenu = !showMenu }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "TopApp Menu",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false },
                            offset = DpOffset(0.dp, 8.dp)
                        ) {
                            overflowActions.forEach { menuItem ->
                                DropdownMenuItem(
                                    onClick = {
                                        menuItem.onClick()
                                        showMenu = false
                                    },
                                    text = { Text(menuItem.name) },
                                    leadingIcon = {
                                        Icon(menuItem.icon, contentDescription = menuItem.name)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBar(
    mainViewModel: MainViewModel,
    itemsBottomBar: List<BottomNavItem>,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val navController = LocalDashboardNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    var selectedItem by remember { mutableStateOf(itemsBottomBar.firstOrNull()) }
    LaunchedEffect(currentRoute) {
        selectedItem =
            itemsBottomBar.find { item ->
                item is BottomNavItem.RouteBased && item.route == currentRoute
            }
                ?: itemsBottomBar.firstOrNull()
    }

    if (itemsBottomBar.isEmpty()) return
    Box(
        modifier = modifier
            .padding(horizontal = 10.dp, vertical = 4.dp)
            .fillMaxWidth()
            .shadow(8.dp, RoundedCornerShape(40.dp), clip = false)
            .clip(RoundedCornerShape(40.dp))
            .onGloballyPositioned { layoutCoordinates ->
                val pxHeight = layoutCoordinates.size.height + 10
                mainViewModel.setPaddingBottom(with(density) { pxHeight.toDp() })
            },
    ) {
        AnimatedHorizontalGradient(
            colors = gradient(),
            modifier = Modifier.matchParentSize()
        )
        Row(modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = 12.dp,
                end = 12.dp,
                top = 8.dp,
                bottom = 4.dp
            ), // padding interno de iconos
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            itemsBottomBar.forEach { item ->
                BottomNavIconItem(
                    item = item,
                    isSelected = item == selectedItem,
                    onClick = {
                        selectedItem = item
                        when (item) {
                            is BottomNavItem.RouteBased -> goRoute(navController, item.route)
                            is BottomNavItem.CallbackBased -> item.callback()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun BottomNavIconItem(item: BottomNavItem, isSelected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
        /*.padding(horizontal = 8.dp, vertical = 4.dp)*/
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.secondary.copy(
                            alpha = 0.3f
                        )
                    } else {
                        Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector =
                    if (isSelected) {
                        (item as? BottomNavItem.RouteBased)?.selectedIcon
                            ?: item.unSelectedIcon
                    } else {
                        item.unSelectedIcon
                    },
                contentDescription = stringResource(item.name),
                tint =
                    if (isSelected) MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        }

        /*if (isSelected) {*/
        /*Text(
            text = stringResource(item.name),
            fontSize = 8.sp,
            *//*style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,*//*
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )*/
        /*}*/
    }
}

@Composable
fun BottomNavigationBars(itemsBottomBar: List<BottomNavItem>, modifier: Modifier = Modifier) {
    val navController = LocalDashboardNavController.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    var selectedItem by remember {
        mutableStateOf(itemsBottomBar.firstOrNull())
    } // Estado inicial: primer ítem
    LaunchedEffect(currentRoute) {
        selectedItem =
            itemsBottomBar.find { item ->
                item is BottomNavItem.RouteBased && item.route == currentRoute
            }
                ?: itemsBottomBar.firstOrNull()
    }

    if (itemsBottomBar.isEmpty()) return
    BottomAppBar(
        modifier = modifier,
        contentColor = Color.Transparent,
        /*tonalElevation = BottomAppBarDefaults.ContainerElevation,
        contentPadding = BottomAppBarDefaults.ContentPadding,
        windowInsets = WindowInsets.navigationBars,*/
        containerColor = Color.Transparent
    ) {
        NavigationBar(
            modifier =
                Modifier
                    .padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp
                    ) // separación del borde y del navbar
                    .shadow(8.dp, RoundedCornerShape(24.dp), clip = false)
                    .clip(RoundedCornerShape(48.dp)) // forma redondeada
            ,
            contentColor = Color.Transparent,
            tonalElevation = BottomAppBarDefaults.ContainerElevation,
            windowInsets = WindowInsets.navigationBars,
            containerColor = Color.Transparent
        ) {
            itemsBottomBar.forEach { item ->
                NavigationBarItem(
                    icon = {
                        Icon(
                            imageVector =
                                if (item == selectedItem) {
                                    (item as? BottomNavItem.RouteBased)?.selectedIcon
                                        ?: item.unSelectedIcon
                                } else {
                                    item.unSelectedIcon
                                },
                            contentDescription = stringResource(item.name),
                        )
                    },
                    /*colors = NavigationBarItemDefaults.colors().copy(
                        selectedIndicatorColor = Golden,
                        selectedIconColor = Green,
                        unselectedIconColor = Golden
                    ),*/
                    /*label = {
                        Text(
                            text = stringResource(item.name),
                            textAlign = TextAlign.Center
                        )
                    },*/
                    selected = item == selectedItem,
                    alwaysShowLabel = false,
                    onClick = {
                        selectedItem = item
                        when (item) {
                            is BottomNavItem.RouteBased -> {
                                goRoute(navController, item.route)
                            }

                            is BottomNavItem.CallbackBased -> {
                                item.callback()
                            }
                        }
                        /*selectedItemIndex = item.id*/
                    }
                )
            }
        }
    }
}

@Composable
fun RowScope.AnimatedFloatingActionButton(text: String, imageVector: ImageVector) {
    var expanded by remember { mutableStateOf(true) }
    FloatingActionButton(
        onClick = { expanded = !expanded },
        modifier = Modifier.align(Alignment.CenterVertically)
    ) {
        Row(Modifier.padding(start = 12.dp, end = 12.dp)) {
            Icon(
                imageVector = imageVector,
                contentDescription = "FAB",
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            AnimatedVisibility(expanded, modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(modifier = Modifier.padding(start = 12.dp), text = text)
            }
        }
    }
    Spacer(Modifier.requiredHeight(20.dp))
}

@Composable
fun Fab(
    imageVector: ImageVector = Icons.Outlined.Add,
    shape: Shape = FloatingActionButtonDefaults.shape,
    containerColor: Color = White,
    contentColor: Color = Green,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
    ) { Icon(imageVector, contentDescription = "FAB") }
}

@Composable
fun NeonPulseGradient(
    colors: List<Color>,
    modifier: Modifier = Modifier,
    pulseIntensity: Float = 0.25f
) {
    val infinite = rememberInfiniteTransition()

    val pulse by
    infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(1600, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            )
    )

    val animatedColors = colors.map { it.copy(alpha = (it.alpha * pulse).coerceIn(0f, 1f)) }

    val brush = Brush.horizontalGradient(animatedColors)

    Box(
        modifier = modifier
            .background(brush)
            .blur(12.dp) // Glow suave
    )
}

@Composable
fun AnimatedHorizontalGradient(modifier: Modifier = Modifier, colors: List<Color>) {
    val infiniteTransition = rememberInfiniteTransition()

    // Valor que cicla de 0f a 1f
    val offset by
    infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 3000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
    )

    val brush =
        Brush.horizontalGradient(
            colors = colors,
            startX = 1000f * offset,
            endX = 1000f * offset + 1000f // puedes ajustarlo según tu tamaño
        )

    Box(modifier = modifier.background(brush))
}
