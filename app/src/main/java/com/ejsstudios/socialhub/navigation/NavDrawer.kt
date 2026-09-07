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

package com.ejsstudios.socialhub.navigation

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.utils.Constants
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CAMPAIGNS
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CONTENTS
import com.ejsstudios.socialhub.utils.Constants.ROUTE_CONTEXT_CONFIG
import com.ejsstudios.socialhub.utils.Constants.ROUTE_HOME
import com.ejsstudios.socialhub.utils.Constants.ROUTE_PROFILE
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SCHEMES
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SOCIAL_HUB
import com.ejsstudios.socialhub.viewmodels.MainViewModel

@Composable
fun CustomNavigationDrawer(
    mainViewModel: MainViewModel,
    drawerState: DrawerState,
    menuItems: List<DrawerMenuItem>,
    selectedRoute: String?,
    onItemSelected: (String) -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val dashboardViewModel = mainViewModel.dashboardViewModel
    val facebookViewModel = mainViewModel.facebookViewModel
    val uiState by dashboardViewModel.uiState.collectAsState()
    val selectedPage by facebookViewModel.selectedPage.collectAsState()
    val context = LocalContext.current

    val visibleMenuItems = remember(
        selectedPage?.id,
        uiState.isContextConfigured,
        uiState.hasCampaigns,
        uiState.hasSchemes,
        menuItems
    ) {
        getDynamicMenuItems(
            selectedPage = selectedPage,
            isContextConfigured = uiState.isContextConfigured,
            hasCampaigns = uiState.hasCampaigns,
            hasSchemes = uiState.hasSchemes,
            allItems = menuItems
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                drawerShape = RoundedCornerShape(topEnd = 32.dp, bottomEnd = 32.dp),
                modifier = Modifier.width(320.dp)
            ) {
                DrawerHeader(
                    mainViewModel = mainViewModel
                )

                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    visibleMenuItems.forEach { item ->
                        val isSelected = selectedRoute == item.route
                        val isLocked = !item.enabled

                        NavigationDrawerItem(
                            label = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = item.title,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                            alpha = 0.5f
                                        ) else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (isLocked && item.route in listOf(
                                            ROUTE_CAMPAIGNS,
                                            ROUTE_SCHEMES,
                                            ROUTE_CONTENTS
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Bloqueado",
                                            modifier = Modifier.size(16.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                alpha = 0.5f
                                            )
                                        )
                                    }
                                }
                            },
                            selected = isSelected,
                            onClick = {
                                if (item.enabled) {
                                    onItemSelected(item.route)
                                } else {
                                    val contextMessage = when (item.route) {
                                        ROUTE_CAMPAIGNS -> "Debes configurar el Contexto de IA para esta página antes de crear campañas."
                                        ROUTE_SCHEMES -> "Debes crear al menos una Campaña antes de definir Esquemas."
                                        ROUTE_CONTENTS -> "Debes definir al menos un Esquema antes de generar Contenidos."
                                        else -> "Esta sección requiere seleccionar una página de Facebook."
                                    }
                                    Toast.makeText(context, contextMessage, Toast.LENGTH_LONG)
                                        .show()
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unSelectedIcon,
                                    contentDescription = item.title,
                                    tint = if (isLocked) MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                        alpha = 0.4f
                                    ) else if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.padding(vertical = 2.dp),
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.3f
                                ),
                                unselectedContainerColor = Color.Transparent,
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )

                NavigationDrawerItem(
                    label = { Text(text = "Cerrar Sesión", fontWeight = FontWeight.Bold) },
                    selected = false,
                    onClick = onLogout,
                    icon = {
                        Icon(
                            Icons.AutoMirrored.Outlined.Logout,
                            contentDescription = "Logout"
                        )
                    },
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = NavigationDrawerItemDefaults.colors(
                        unselectedTextColor = MaterialTheme.colorScheme.error,
                        unselectedIconColor = MaterialTheme.colorScheme.error
                    )
                )
            }
        },
        content = content
    )
}

@Composable
private fun DrawerHeader(
    mainViewModel: MainViewModel,
) {
    val loginViewModel = mainViewModel.loginViewModel
    val facebookViewModel = mainViewModel.facebookViewModel
    val dashboardNavController = Constants.LocalDashboardNavController.current
    val scope = rememberCoroutineScope()

    val user by loginViewModel.user.collectAsState()
    val pages by facebookViewModel.activePages.collectAsState()
    val selectedPage by facebookViewModel.selectedPage.collectAsState()
    var expanded by remember { mutableStateOf(false) }

    // URL absoluta para el perfil del usuario
    val profileImageUrl = remember(user?.picture) {
        user?.picture?.let { path ->
            if (path.startsWith("http")) path else "${Constants.WEB_SERVER_URL}$path"
        }
    }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer
        )
    )

    Box(modifier = Modifier.fillMaxWidth().height(280.dp).background(brush = gradient)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        dashboardNavController.navigate(ROUTE_PROFILE)
                    }
            ) {
                Surface(
                    modifier = Modifier.size(64.dp).clip(CircleShape),
                    color = Color.White.copy(alpha = 0.2f),
                    border = BorderStroke(2.dp, Color.White),
                ) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Profile",
                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user?.name ?: "Usuario Social",
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (pages.size == 1) "1 página vinculada" else "${pages.size} páginas vinculadas",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(
                                alpha = 0.85f
                            ), fontWeight = FontWeight.Medium
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val tierText = when (user?.subscriptionTier?.lowercase()) {
                "pro" -> "PLAN PRO"
                "agency" -> "PLAN AGENCY"
                else -> "FREEMIUM"
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(
                                if (tierText != "FREEMIUM") Color(0xFF4CAF50) else Color(
                                    0xFFFF9800
                                )
                            )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = tierText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }

                Text(
                    text = "⚡ ${user?.credits ?: 0} Créditos",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFFFFD700),
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable { expanded = true }
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = if (selectedPage != null) "${selectedPage?.name} - ${selectedPage?.platform}" else "Selecciona página (${pages.size})",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface).width(280.dp)
                ) {
                    pages.forEachIndexed { index, page ->
                        val isPageSelected = page.id == selectedPage?.id

                        val pageImageUrl = remember(page.picture) {
                            page.picture?.let { path ->
                                if (path.startsWith("http")) path else "${Constants.WEB_SERVER_URL}$path"
                            }
                        }

                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = "${page.name} - ${page.platform}",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isPageSelected) FontWeight.Bold else FontWeight.Normal),
                                        maxLines = 1, overflow = TextOverflow.Ellipsis
                                    )
                                    page.category?.let {
                                        Text(
                                            it,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            },
                            leadingIcon = {
                                Surface(
                                    modifier = Modifier.size(32.dp).clip(CircleShape),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                ) {
                                    if (pageImageUrl != null) {
                                        AsyncImage(
                                            model = pageImageUrl,
                                            contentDescription = "Page Icon",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Business,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }
                                }
                            },
                            trailingIcon = {
                                if (isPageSelected) Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            },
                            onClick = {
                                facebookViewModel.selectPage(page)
                                expanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        )
                        if (index < pages.size - 1) {
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getDynamicMenuItems(
    selectedPage: SocialMediaFirestore?,
    isContextConfigured: Boolean,
    hasCampaigns: Boolean,
    hasSchemes: Boolean,
    allItems: List<DrawerMenuItem>
): List<DrawerMenuItem> {
    val result = mutableListOf<DrawerMenuItem>()

    allItems.forEach { item ->
        val updatedItem = when (item.route) {
            ROUTE_HOME -> item.copy(enabled = true)
            ROUTE_SOCIAL_HUB -> item.copy(enabled = true)
            ROUTE_CONTEXT_CONFIG -> item.copy(enabled = selectedPage != null)
            ROUTE_CAMPAIGNS -> item.copy(enabled = selectedPage != null && isContextConfigured)
            ROUTE_SCHEMES -> item.copy(enabled = selectedPage != null && isContextConfigured && hasCampaigns)
            ROUTE_CONTENTS -> item.copy(enabled = selectedPage != null && isContextConfigured && hasCampaigns && hasSchemes)
            else -> item.copy(enabled = true)
        }
        result.add(updatedItem)
    }

    return result
}
