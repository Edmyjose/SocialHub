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

package com.ejsstudios.socialhub.ui.views.dashboard

import android.app.Application
import android.widget.Toast
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.SettingsSuggest
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.model.UserFirestore
import com.ejsstudios.socialhub.navigation.CustomNavigationDrawer
import com.ejsstudios.socialhub.navigation.DrawerMenuItem
import com.ejsstudios.socialhub.navigation.Navigation
import com.ejsstudios.socialhub.navigation.NavigationIconState
import com.ejsstudios.socialhub.navigation.RouteConfiguration
import com.ejsstudios.socialhub.navigation.ShowAsAction
import com.ejsstudios.socialhub.navigation.TopBar
import com.ejsstudios.socialhub.navigation.TopMenuItems
import com.ejsstudios.socialhub.navigation.goRoute
import com.ejsstudios.socialhub.ui.theme.SocialHubTheme
import com.ejsstudios.socialhub.ui.views.hubsocial.HubSocialScreen
import com.ejsstudios.socialhub.utils.Constants
import com.ejsstudios.socialhub.utils.Constants.LocalDashboardNavController
import com.ejsstudios.socialhub.utils.Constants.LocalSnackbarHostState
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CAMPAIGNS
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CAMPAIGN_DETAIL
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CAMPAIGN_FORM
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CONTENTS
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CONTENT_DETAIL
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CONTEXT_CONFIG
import com.ejsstudios.socialhub.utils.Constants.ROUTE_HELP
import com.ejsstudios.socialhub.utils.Constants.ROUTE_HOME
import com.ejsstudios.socialhub.utils.Constants.ROUTE_PRIVACY
import com.ejsstudios.socialhub.utils.Constants.ROUTE_PROFILE
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SCHEMES
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SCHEME_DETAIL
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SETTINGS
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SOCIAL_HUB
import com.ejsstudios.socialhub.utils.Constants.getMockMainViewModel
import com.ejsstudios.socialhub.viewmodels.DashboardEvent
import com.ejsstudios.socialhub.viewmodels.DashboardUiState
import com.ejsstudios.socialhub.viewmodels.MainViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun DashboardScreen(mainViewModel: MainViewModel, onLogout: () -> Unit) {
    val dashboardViewModel = mainViewModel.dashboardViewModel
    val facebookViewModel = mainViewModel.facebookViewModel
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val dashboardNavController = rememberNavController()
    val context = LocalContext.current
    val page by facebookViewModel.selectedPage.collectAsStateWithLifecycle()

    val navBackStackEntry by dashboardNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: ROUTE_HOME

    // MANEJAR ATRÁS DENTRO DEL DASHBOARD
    /*val canGoBackInternal = navBackStackEntry?.destination?.route != ROUTE_HOME
    BackHandler(enabled = canGoBackInternal) {
        dashboardNavController.popBackStack()
    }*/

    // 1. ESCUCHAR EVENTOS DEL VIEWMODEL (Toast)
    LaunchedEffect(Unit) {
        dashboardViewModel.events.collect { event ->
            when (event) {
                is DashboardEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    // 2. LÓGICA DINÁMICA DE TÍTULO SEGÚN RUTA (Icono siempre Menú)
    LaunchedEffect(currentRoute) {
        // Configuramos el icono de Menú de forma permanente
        mainViewModel.navigationViewModel.setNavigationConfig(
            icon = NavigationIconState.Menu,
            onClick = { scope.launch { drawerState.open() } }
        )
    }

    val uiState by dashboardViewModel.uiState.collectAsState()
    val drawerMenuItems =
        remember(page?.id, uiState.isContextConfigured, uiState.hasCampaigns, uiState.hasSchemes) {
            listOf(
                DrawerMenuItem(
                    id = 1,
                    title = "Inicio",
                    route = ROUTE_HOME,
                    enabled = true,
                    selectedIcon = Icons.Filled.Home,
                    unSelectedIcon = Icons.Outlined.Home
                ),
                DrawerMenuItem(
                    id = 2,
                    title = "Social Hub",
                    route = ROUTE_SOCIAL_HUB,
                    enabled = true,
                    selectedIcon = Icons.Filled.Share,
                    unSelectedIcon = Icons.Outlined.Share
                ),
                DrawerMenuItem(
                    id = 3,
                    title = "Configurar Contexto",
                    route = ROUTE_CONTEXT_CONFIG,
                    enabled = page != null,
                    selectedIcon = Icons.Filled.SettingsSuggest,
                    unSelectedIcon = Icons.Outlined.SettingsSuggest
                ),
                DrawerMenuItem(
                    id = 4,
                    title = "Campañas",
                    route = ROUTE_CAMPAIGNS,
                    enabled = page != null && uiState.isContextConfigured,
                    selectedIcon = Icons.Filled.Campaign,
                    unSelectedIcon = Icons.Outlined.Campaign
                ),
                DrawerMenuItem(
                    id = 5,
                    title = "Esquemas",
                    route = ROUTE_SCHEMES,
                    enabled = page != null && uiState.isContextConfigured && uiState.hasCampaigns,
                    selectedIcon = Icons.Filled.AccountTree,
                    unSelectedIcon = Icons.Outlined.AccountTree
                ),
                DrawerMenuItem(
                    id = 6,
                    title = "Contenidos",
                    route = ROUTE_CONTENTS,
                    enabled = page != null && uiState.isContextConfigured && uiState.hasCampaigns && uiState.hasSchemes,
                    selectedIcon = Icons.Filled.AutoStories,
                    unSelectedIcon = Icons.Outlined.AutoStories
                ),
                DrawerMenuItem(
                    id = 7,
                    title = "Perfil",
                    route = ROUTE_PROFILE,
                    enabled = true,
                    selectedIcon = Icons.Filled.Person,
                    unSelectedIcon = Icons.Outlined.Person
                ),
                DrawerMenuItem(
                    id = 8,
                    title = "Ajustes",
                    route = ROUTE_SETTINGS,
                    enabled = true,
                    selectedIcon = Icons.Filled.Settings,
                    unSelectedIcon = Icons.Outlined.Settings
                )
            )
        }

    val topMenuItems = listOf(
        TopMenuItems(
            icon = Icons.Outlined.Person,
            name = "Perfil",
            onClick = { dashboardNavController.navigate(ROUTE_PROFILE) },
            showAsAction = ShowAsAction.NEVER
        ),
        TopMenuItems(
            icon = Icons.Outlined.Settings,
            name = "Configuración",
            onClick = { dashboardNavController.navigate(ROUTE_SETTINGS) },
            showAsAction = ShowAsAction.NEVER
        ),
        TopMenuItems(
            icon = Icons.Outlined.Lock,
            name = "Privacidad",
            onClick = { dashboardNavController.navigate(ROUTE_PRIVACY) },
            showAsAction = ShowAsAction.NEVER
        ),
        TopMenuItems(
            icon = Icons.AutoMirrored.Outlined.Help,
            name = "Ayuda y Soporte",
            onClick = { dashboardNavController.navigate(ROUTE_HELP) },
            showAsAction = ShowAsAction.NEVER
        ),
        TopMenuItems(
            icon = Icons.AutoMirrored.Outlined.Logout,
            name = "Logout",
            onClick = { onLogout() },
            showAsAction = ShowAsAction.NEVER
        )
    )

    val routeConfigurations = listOf(
        RouteConfiguration(ROUTE_HOME) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Social Media AI")
            DashboardHomeContent(mainViewModel = mainViewModel)
        },
        RouteConfiguration(ROUTE_SOCIAL_HUB) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Social Hub")
            HubSocialScreen(mainViewModel = mainViewModel)
        },
        RouteConfiguration(ROUTE_CONTEXT_CONFIG) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Configurar Contexto")
            /*ContextSettingScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_CAMPAIGNS) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Campañas")
            /*CampaignsScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_CAMPAIGN_DETAIL) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Detalle de Campaña")
            /*CampaignDetailScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_CAMPAIGN_FORM) { _, _, _ ->
            /*CampaignFormScreen(
                mainViewModel = mainViewModel,
                onBack = { dashboardNavController.popBackStack() }
            )*/
        },
        RouteConfiguration(ROUTE_SCHEMES) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Esquemas")
            /*SchemesScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_SCHEME_DETAIL) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Detalle de Esquema")
            /*SchemeDetailScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_CONTENTS) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Contenidos")
            /*ContentsScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_CONTENT_DETAIL) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Vista Previa")
            /*ContentDetailScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_PROFILE) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Perfil y Recargas")
            /*ProfileScreen(mainViewModel = mainViewModel)*/
        },
        RouteConfiguration(ROUTE_PRIVACY) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Privacidad")
            PlaceholderScreen(
                mainViewModel = mainViewModel,
                title = "Privacidad y Términos",
                description = "Políticas de privacidad y tratamiento de datos de Meta Developer Platform.",
                icon = Icons.Outlined.Lock
            )
        },
        RouteConfiguration(ROUTE_HELP) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Ayuda y Soporte")
            PlaceholderScreen(
                mainViewModel = mainViewModel,
                title = "Ayuda y Soporte",
                description = "Tutoriales y centro de soporte técnico para la automatización con IA.",
                icon = Icons.AutoMirrored.Outlined.Help
            )
        },
        RouteConfiguration(ROUTE_SETTINGS) { _, _, _ ->
            mainViewModel.navigationViewModel.setTitle("Configuración Global")
            /*SettingsScreen(
                mainViewModel = mainViewModel,
                onBack = { dashboardNavController.popBackStack() }
            )*/
        }
    )
    mainViewModel.navigationViewModel.setMenuItems(topMenuItems)

    CompositionLocalProvider(
        LocalDashboardNavController provides dashboardNavController,
        LocalSnackbarHostState provides snackbarHostState
    ) {
        CustomNavigationDrawer(
            mainViewModel = mainViewModel,
            drawerState = drawerState,
            menuItems = drawerMenuItems,
            selectedRoute = currentRoute,
            onItemSelected = { route ->
                scope.launch { drawerState.close() }
                if (currentRoute != route) {
                    if (route == ROUTE_HOME) {
                        dashboardNavController.popBackStack(ROUTE_HOME, inclusive = false)
                    } else {
                        goRoute(
                            navController = dashboardNavController,
                            route = route,
                            popUpToRoute = ROUTE_HOME,
                            inclusive = false,
                            saveState = true
                        )
                    }
                }
            },
            onLogout = {
                scope.launch {
                    drawerState.close()
                    onLogout()
                }
            },
            content = {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    contentWindowInsets = WindowInsets.systemBars,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    topBar = {
                        TopBar(mainViewModel = mainViewModel)
                    }
                ) { paddingValues ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                    ) {

                        Navigation(
                            navController = dashboardNavController,
                            routeConfigurations = routeConfigurations,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun DashboardHomeContent(mainViewModel: MainViewModel) {
    val dashboardViewModel = mainViewModel.dashboardViewModel
    val loginViewModel = mainViewModel.loginViewModel
    val facebookViewModel = mainViewModel.facebookViewModel
    /*val contentsViewModel = mainViewModel.contentsViewModel*/

    val page by facebookViewModel.selectedPage.collectAsStateWithLifecycle()
    val pages by facebookViewModel.activePages.collectAsStateWithLifecycle()
    val user by loginViewModel.user.collectAsStateWithLifecycle()
    val uiState by dashboardViewModel.uiState.collectAsStateWithLifecycle()
    /*val contentsState by contentsViewModel.uiState.collectAsStateWithLifecycle()
    */
    val paddingBottom by mainViewModel.paddingBottom.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val tabs = listOf("Resumen", "Historial")
    val pagerState = rememberPagerState(pageCount = { tabs.size })

    /*    if (uiState.isLoading) {
            SplashLoadingScreen(message = uiState.syncMessage ?: "Preparando Dashboard...")
            return
        }*/

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                    )
                )
            )
    ) {
        // INDICADOR DE SINCRONIZACIÓN
        if (uiState.isSyncing) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(14.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = uiState.syncMessage ?: "Sincronizando datos con la nube...",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // TABS SUPERIORES
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                    color = MaterialTheme.colorScheme.primary
                )
            },
            divider = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                    text = { Text(title, style = MaterialTheme.typography.titleSmall) }
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.Top
        ) { pageIndex ->
            if (pageIndex == 0) {
                // PESTAÑA 1: RESUMEN
                DashboardOverview(
                    user = user,
                    page = page,
                    pages = pages,
                    uiState = uiState,
                    paddingBottom = paddingBottom,
                    mainViewModel = mainViewModel
                )
            } else {
                // PESTAÑA 2: HISTORIAL
                /*DashboardHistory(
                    contentsViewModel = contentsViewModel,
                    contentsState = contentsState,
                    paddingBottom = paddingBottom,
                    mainViewModel = mainViewModel
                )*/
            }
        }
    }
}

@Composable
fun DashboardOverview(
    user: UserFirestore?,
    page: SocialMediaFirestore?,
    pages: List<SocialMediaFirestore>,
    uiState: DashboardUiState,
    paddingBottom: Dp,
    mainViewModel: MainViewModel
) {
    val dashboardNavController = LocalDashboardNavController.current

    val tierName = remember(user?.subscriptionTier) {
        when (user?.subscriptionTier?.lowercase()) {
            "pro" -> "Plan PRO"
            "agency" -> "Plan AGENCY"
            else -> "FREEMIUM"
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = paddingBottom + 16.dp,
            start = 16.dp,
            end = 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header de bienvenida y créditos
        item {
            Card(
                modifier = Modifier.fillMaxWidth()
                    .clickable { dashboardNavController.navigate(ROUTE_PROFILE) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                        alpha = 0.5f
                    )
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "¡Hola, ${user?.name ?: "Usuario"}! 👋",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Membresía activa: $tierName",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "⚡ ${user?.credits ?: 0} Créditos",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Salud de Automatización (Métricas Precisas)
        item {
            Text(
                text = "Estado del Sistema",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        item {
            AutomationHealthCard(
                lastRun = uiState.lastVigilanteRun,
                pendingSchemes = uiState.pendingSchemesCount,
                pendingPosts = uiState.pendingPostsCount,
                onFixClick = { dashboardNavController.navigate(ROUTE_SETTINGS) }
            )
        }

        if (page != null) {
            item {
                PageProfileHeader(
                    page = page,
                    isContextConfigured = uiState.isContextConfigured,
                    onConfigureClick = { dashboardNavController.navigate(ROUTE_CONTEXT_CONFIG) }
                )
            }
        } else {
            item {
                NoPageSelectedCard(
                    pagesCount = pages.size,
                    onSelectClick = { dashboardNavController.navigate(ROUTE_SOCIAL_HUB) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                /*MetricCard(
                    modifier = Modifier.weight(1f).clickable { dashboardNavController.navigate(ROUTE_CAMPAIGNS) }, 
                    title = "Estrategias Activas", 
                    value = "${uiState.campaigns.count { it.status == CampaignStatus.ACTIVE }}",
                    icon = Icons.Default.AutoGraph, 
                    color = MaterialTheme.colorScheme.primary
                )*/
                MetricCard(
                    modifier = Modifier.weight(1f)
                        .clickable { dashboardNavController.navigate(ROUTE_CONTENTS) },
                    title = "Publicados Hoy",
                    value = "${uiState.publishedTodayCount}",
                    icon = Icons.Default.CheckCircle,
                    color = Color(0xFF4CAF50)
                )
            }
        }
    }
}

/*@Composable
fun DashboardHistory(
    contentsViewModel: ContentsViewModel,
    contentsState: ContentsUiState,
    paddingBottom: Dp,
    mainViewModel: MainViewModel
) {
    val dashboardNavController = LocalDashboardNavController.current
    val use12hFormat by mainViewModel.preferencesManager.use12hFormatFlow.collectAsState(initial = false)
    
    val publishedPosts = remember(contentsState.posts) {
        contentsState.posts.filter { it.status == ContentStatus.PUBLISHED }.sortedByDescending { it.updatedAt }
    }

    if (publishedPosts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                Spacer(Modifier.height(16.dp))
                Text("Sin actividad reciente", fontWeight = FontWeight.Bold, color = Color.Gray)
                Text("Los simulacros completados aparecerán aquí.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Publicaciones Finalizadas (${publishedPosts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(publishedPosts) { post ->
                PostFeedCard(
                    post = post,
                    onDelete = { contentsViewModel.deletePost(post) },
                    onApprove = {}, // No necesario en historial
                    showGallery = true,
                    use12hFormat = use12hFormat,
                    onClick = {
                        contentsViewModel.selectPost(post)
                        dashboardNavController.navigate(ROUTE_CONTENT_DETAIL)
                    }
                )
            }
        }
    }
}*/

@Composable
fun AutomationHealthCard(
    lastRun: String,
    pendingSchemes: Int,
    pendingPosts: Int,
    onFixClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Shield,
                    null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Vigilante de Producción",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    text = "Último: $lastRun",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HealthIndicator(
                    label = "Esquemas Pendientes",
                    count = pendingSchemes,
                    color = MaterialTheme.colorScheme.secondary
                )
                HealthIndicator(
                    label = "Contenidos por Aprobar",
                    count = pendingPosts,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
fun HealthIndicator(label: String, count: Int, color: Color) {
    Column {
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
    }
}

@Composable
fun MetricCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun NoPageSelectedCard(pagesCount: Int, onSelectClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                alpha = 0.2f
            )
        )
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Business,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = if (pagesCount > 0) "$pagesCount Páginas Vinculadas" else "Sin páginas vinculadas",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = if (pagesCount > 0) "Toca aquí para seleccionar una página y empezar." else "Toca aquí para vincular tu primera fanpage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun PageProfileHeader(
    page: SocialMediaFirestore,
    isContextConfigured: Boolean,
    onConfigureClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onConfigureClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        val profileImageUrl = remember(page.picture) {
            page.picture?.let { path ->
                if (path.startsWith("http")) path else "${Constants.WEB_SERVER_URL}$path"
            }
        }
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profileImageUrl,
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    modifier = Modifier.basicMarquee(),
                    text = page.name,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                page.category?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isContextConfigured) MaterialTheme.colorScheme.surface.copy(alpha = 0.3f) else MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = if (isContextConfigured) "🟢 Contexto IA Entrenado" else "🔴 Toca para entrenar IA",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isContextConfigured) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun PlaceholderScreen(
    mainViewModel: MainViewModel,
    title: String,
    description: String,
    icon: ImageVector
) {
    val paddingTop by mainViewModel.paddingTop.collectAsState()
    val paddingBottom by mainViewModel.paddingBottom.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(
                top = paddingTop + 16.dp,
                bottom = paddingBottom + 16.dp,
                start = 24.dp,
                end = 24.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(20.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.tertiaryContainer
            ) {
                Text(
                    text = "🚧 Próximamente en desarrollo",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
        }
    }
}

@Preview(showBackground = true, device = "spec:width=411dp,height=891dp")
@Composable
fun DashboardPreview() {
    val context = LocalContext.current
    val application = context.applicationContext as? Application ?: Application()
    val mainViewModel = getMockMainViewModel(application)
    SocialHubTheme() {
        DashboardScreen(mainViewModel = mainViewModel, onLogout = {})
    }
}
