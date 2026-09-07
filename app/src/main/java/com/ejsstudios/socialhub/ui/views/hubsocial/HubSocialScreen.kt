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
package com.ejsstudios.socialhub.ui.views.hubsocial

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Facebook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.ejsstudios.socialhub.LocalFacebookCallbackManager
import com.ejsstudios.socialhub.R
import com.ejsstudios.socialhub.firebase.db.model.SocialMediaFirestore
import com.ejsstudios.socialhub.firebase.db.model.SocialPlatform
import com.ejsstudios.socialhub.ui.views.custom.ScaledEnterExit
import com.ejsstudios.socialhub.ui.views.custom.ScreenHeader
import com.ejsstudios.socialhub.viewmodels.MainViewModel
import com.facebook.login.LoginManager

@Composable
fun HubSocialScreen(
    mainViewModel: MainViewModel
) {
    val hubViewModel = mainViewModel.hubSocialViewModel
    val facebookViewModel = hubViewModel.facebookViewModel
    val redditViewModel = hubViewModel.redditViewModel

    val fbUiState by facebookViewModel.uiState.collectAsStateWithLifecycle()
    val redditUiState by redditViewModel.uiState.collectAsStateWithLifecycle()
    val pages by hubViewModel.pages.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val callbackManager = LocalFacebookCallbackManager.current
    val uriHandler = LocalUriHandler.current

    val redditClientId = stringResource(R.string.reddit_client_id)
    val redditRedirectUri = stringResource(R.string.reddit_redirect_uri)

    DisposableEffect(callbackManager) {
        LoginManager.getInstance()
            .registerCallback(callbackManager, facebookViewModel.facebookCallback)
        onDispose { }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            ScreenHeader(
                title = "Social Hub",
                onBack = { /* Level 1 back handled via dashboard navController */ },
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Centro de Conexiones 🌐",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                item {
                    PlatformLinkingCard(
                        name = "Facebook",
                        icon = Icons.Default.Facebook,
                        iconColor = Color(0xFF1877F2),
                        pages = pages.filter { it.platform == SocialPlatform.FACEBOOK_PAGE.id || it.platform == SocialPlatform.FACEBOOK_GROUP.id },
                        onConnect = {
                            facebookViewModel.onLinkingStarted()
                            LoginManager.getInstance().logInWithReadPermissions(
                                context as ComponentActivity,
                                listOf(
                                    "public_profile",
                                    "email",
                                    "pages_manage_posts",
                                    "pages_read_engagement",
                                    "pages_show_list",
                                    "read_insights",
                                    "pages_read_user_content",
                                    "pages_manage_metadata"
                                )
                            )
                        },
                        onTogglePage = { pageId, active ->
                            hubViewModel.togglePage(
                                pageId,
                                active
                            )
                        }
                    )
                }

                item {
                    PlatformLinkingCard(
                        name = "Reddit",
                        icon = Icons.Default.CameraAlt,
                        iconColor = Color(0xFFFF4500),
                        pages = pages.filter { it.platform == SocialPlatform.REDDIT.id },
                        onConnect = {
                            val authUrl = "https://www.reddit.com/api/v1/authorize.compact" +
                                    "?client_id=$redditClientId" +
                                    "&response_type=code" +
                                    "&state=socialhub_state" +
                                    "&redirect_uri=$redditRedirectUri" +
                                    "&duration=permanent" +
                                    "&scope=identity,mysubreddits,submit"
                            uriHandler.openUri(authUrl)
                        },
                        onTogglePage = { pageId, active ->
                            hubViewModel.togglePage(pageId, active)
                        }
                    )
                }
                item {
                    PlatformLinkingCard(
                        name = "Instagram",
                        icon = Icons.Default.CameraAlt,
                        iconColor = Color(0xFFE4405F),
                        pages = emptyList(),
                        onConnect = { /* Proximamente */ },
                        onTogglePage = { _, _ -> }
                    )
                }
            }
        }

        // --- DIALOGO DE VINCULACIÓN EN CADENA (FACEBOOK) ---
        fbUiState.currentPageToLink?.let { page ->
            LinkPageDialog(
                page = page,
                otherOwnerId = fbUiState.otherOwnerId,
                onLink = { facebookViewModel.linkCurrentPage() },
                onSkip = { facebookViewModel.skipCurrentPage() }
            )
        }

        // --- DIALOGO DE VINCULACIÓN EN CADENA (REDDIT) ---
        redditUiState.currentSubredditToLink?.let { subreddit ->
            LinkPageDialog(
                page = subreddit,
                otherOwnerId = null, // Reddit no soporta chequeo de owner aun
                onLink = { redditViewModel.linkCurrentSubreddit() },
                onSkip = { redditViewModel.skipCurrentSubreddit() }
            )
        }

        if (fbUiState.isLoading || redditUiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
fun LinkPageDialog(
    page: SocialMediaFirestore,
    otherOwnerId: String?,
    onLink: () -> Unit,
    onSkip: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Vincular Página") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = page.picture,
                    contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(CircleShape).background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = page.name,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = page.category ?: "Sin categoría",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (otherOwnerId != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.errorContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "⚠️ Esta página ya está vinculada por otro usuario.",
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Text(
                        text = "¿Deseas vincular esta página a tu estrategia de IA?",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onLink) { Text("Vincular") }
        },
        dismissButton = {
            TextButton(onClick = onSkip) { Text("No vincular") }
        }
    )
}

@Composable
private fun PlatformLinkingCard(
    name: String,
    icon: ImageVector,
    iconColor: Color,
    pages: List<SocialMediaFirestore>,
    onConnect: () -> Unit,
    onTogglePage: (String, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val isLinked = pages.isNotEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = iconColor.copy(alpha = 0.1f),
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    if (isLinked) {
                        Text(
                            text = "Vinculado ✅",
                            color = Color(0xFF4CAF50),
                            style = MaterialTheme.typography.labelSmall
                        )
                    } else {
                        Text(
                            text = "No conectado",
                            color = Color.Gray,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }

                if (isLinked) {
                    IconButton(onClick = onConnect) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Actualizar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null
                        )
                    }
                } else {
                    Button(
                        onClick = onConnect,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = iconColor)
                    ) {
                        Text("Conectar", fontSize = 12.sp)
                    }
                }
            }

            ScaledEnterExit(visible = expanded && isLinked) {
                Column(modifier = Modifier.padding(bottom = 8.dp)) {
                    pages.forEach { page ->
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            thickness = 0.5.dp,
                            color = Color.LightGray.copy(alpha = 0.5f)
                        )
                        LinkedPageItem(
                            page = page,
                            onToggle = { active -> onTogglePage(page.id, active) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedPageItem(
    page: SocialMediaFirestore,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = page.picture,
            contentDescription = null,
            modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.LightGray),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = page.name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Text(
                text = page.category ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Switch(
            checked = page.isActive,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF4CAF50))
        )
    }
}
