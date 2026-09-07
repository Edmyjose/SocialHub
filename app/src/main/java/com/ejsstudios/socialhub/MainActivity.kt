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

package com.ejsstudios.socialhub

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.widget.Toast
import android.widget.Toast.makeText
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.ejsstudios.socialhub.debug.Loggers
import com.ejsstudios.socialhub.error.ExceptionHandler
import com.ejsstudios.socialhub.firebase.RemoteConfigManager
import com.ejsstudios.socialhub.firebase.login.LoginScreen
import com.ejsstudios.socialhub.firebase.login.LoginViewModel
import com.ejsstudios.socialhub.managers.AppReviewHandler
import com.ejsstudios.socialhub.navigation.Navigation
import com.ejsstudios.socialhub.navigation.RouteConfiguration
import com.ejsstudios.socialhub.navigation.goRoute
import com.ejsstudios.socialhub.permission.PermissionManager
import com.ejsstudios.socialhub.ui.theme.SocialHubTheme
import com.ejsstudios.socialhub.ui.theme.splash.SplashLoadingScreen
import com.ejsstudios.socialhub.ui.views.dashboard.DashboardScreen
import com.ejsstudios.socialhub.utils.Constants.FCM_CHANNEL_ID
import com.ejsstudios.socialhub.utils.Constants.LocalRootNavController
import com.ejsstudios.socialhub.utils.Constants.ROUTE_DASHBOARD
import com.ejsstudios.socialhub.utils.Constants.ROUTE_HOME
import com.ejsstudios.socialhub.utils.Constants.ROUTE_LOGIN
import com.ejsstudios.socialhub.utils.Constants.ROUTE_SPLASH
import com.ejsstudios.socialhub.utils.Constants.splashScreenOn
import com.ejsstudios.socialhub.viewmodels.MainViewModel
import com.ejsstudios.socialhub.viewmodels.MainViewModelFactory
import com.facebook.CallbackManager
import java.security.MessageDigest

val LocalFacebookCallbackManager =
    staticCompositionLocalOf<CallbackManager> { error("No CallbackManager provided") }

class MainActivity : ComponentActivity() {

    private lateinit var mainViewModel: MainViewModel
    private lateinit var permissionManager: PermissionManager
    private lateinit var backPressedCallback: OnBackPressedCallback
    private var backPressedTime: Long = 0
    private val callbackManager = CallbackManager.Factory.create()
    private lateinit var rootNavController: NavHostController


    private fun setupBackPressedCallback(context: Context) {
        val toast: Toast = makeText(
            context,
            context.getString(R.string.lbl_press_again_to_exit),
            Toast.LENGTH_SHORT
        )
        backPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val currentDestination = rootNavController.currentBackStackEntry?.destination?.route

                // Si estamos en el Dashboard o Login, manejamos la salida
                if (currentDestination == ROUTE_HOME || currentDestination == ROUTE_DASHBOARD || currentDestination == ROUTE_LOGIN) {
                    if (backPressedTime + 2000 > System.currentTimeMillis()) {
                        toast.cancel()
                        finish()
                    } else {
                        toast.show()
                        backPressedTime = System.currentTimeMillis()
                    }
                } else {
                    // Para otras rutas raíz, comportamiento normal
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }
        onBackPressedDispatcher.addCallback(this, backPressedCallback)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val context = this

        val factory = MainViewModelFactory(application)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]

        permissionManager = PermissionManager(this)
        permissionManager.checkAndRequestPermissions()
        val fcmChannel =
            NotificationChannel(
                FCM_CHANNEL_ID,
                "FCM_Channel",
                NotificationManager.IMPORTANCE_HIGH
            )
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(fcmChannel)

        RemoteConfigManager.initializeRemoteConfig(context, lifecycleScope = lifecycleScope)
        setupBackPressedCallback(this)

        // Manejar callback de Reddit si la app se inicia desde el deep link
        intent?.data?.let { uri ->
            if (uri.scheme == "socialhub" && uri.host == "reddit-auth") {
                val code = uri.getQueryParameter("code")
                if (code != null) {
                    mainViewModel.redditViewModel.startRedditLinking(code)
                }
            }
        }

        // Sincronización con el estado de carga del LoginViewModel para la Splash Screen
        splashScreen.setKeepOnScreenCondition {
            splashScreenOn
        }

        // Facebook Key Hash Generator
        try {
            @Suppress("DEPRECATION")
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val signatures = info.signatures
            if (signatures != null) {
                for (signature in signatures) {
                    val md = MessageDigest.getInstance("SHA")
                    md.update(signature.toByteArray())
                    Loggers.log(
                        "e",
                        "MainActivity",
                        "FacebookKeyHash KeyHash: " + Base64.encodeToString(
                            md.digest(),
                            Base64.DEFAULT
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Loggers.log("e", "MainActivity", "FacebookKeyHash Error $e")
        }

        setContent {
            val currentTheme by mainViewModel.currentTheme.collectAsState()

            SocialHubTheme(appTheme = currentTheme) {
                rootNavController = rememberNavController()
                CompositionLocalProvider(
                    LocalRootNavController provides rootNavController,
                    LocalFacebookCallbackManager provides callbackManager
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AppNavigation(
                            mainViewModel = mainViewModel
                        )
                    }
                }
                /*// Manejar navegación desde notificaciones
                intent.getStringExtra("navigate_to")?.let { route ->
                    LaunchedEffect(Unit) { navController.navigate(route) }
                }*/
            }
        }
        val appReviewHandler =
            AppReviewHandler(this, mainViewModel.preferencesManager, lifecycleScope)
        appReviewHandler.trackAppLaunch()
        appReviewHandler.showReviewFlowIfNeeded()
    }

    @Deprecated("Deprecated in Java")
    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intent.data?.let { uri ->
            if (uri.scheme == "socialhub" && uri.host == "reddit-auth") {
                val code = uri.getQueryParameter("code")
                if (code != null) {
                    mainViewModel.redditViewModel.startRedditLinking(code)
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun AppNavigation(
    mainViewModel: MainViewModel, modifier: Modifier = Modifier,
) {
    val rootNavController: NavHostController = LocalRootNavController.current
    val loginViewModel: LoginViewModel = mainViewModel.loginViewModel
    val uiState by loginViewModel.uiState.collectAsStateWithLifecycle()
    var isSuccessful by remember { mutableStateOf<Boolean?>(null) }


    // Verificación inicial de token con un pequeño delay artificial para la splash si se desea

    // Verificación inicial de token con un pequeño delay artificial para la splash si se desea
    LaunchedEffect(Unit) {
        // Permitimos que Compose se asiente un momento antes de quitar el splash de sistema
        try {
            loginViewModel.checkActiveSession { isLoading, isLoginSuccessful, loadingMessage ->
                splashScreenOn = isLoading
                if (isLoginSuccessful == true) {
                    isSuccessful = true
                    splashScreenOn = false
                    // NAVEGAR AL DASHBOARD Y LIMPIAR EL LOGIN DE FORMA EXPLÍCITA
                    goRoute(
                        navController = rootNavController,
                        route = ROUTE_HOME
                    )
                } else if (isLoginSuccessful == false) {
                    isSuccessful = false
                    splashScreenOn = false
                    if (rootNavController.currentDestination?.route != ROUTE_LOGIN) {
                        goRoute(
                            navController = rootNavController,
                            route = ROUTE_LOGIN
                        )
                    }
                }
            }
        } catch (e: Exception) {
            ExceptionHandler.handleCaughtException(e)
        }
    }

    // Redirección automática si la sesión cambia (Login, Logout o Deletion)
    LaunchedEffect(uiState.isLoginSuccessful) {
        if (uiState.isLoginSuccessful == true) {
            isSuccessful = true
            splashScreenOn = false
            // Navegar al Dashboard al hacer login exitoso
            if (rootNavController.currentDestination?.route != ROUTE_HOME) {
                goRoute(
                    navController = rootNavController,
                    route = ROUTE_HOME
                )
            }
        } else if (uiState.isLoginSuccessful == false) {
            isSuccessful = false
            if (rootNavController.currentDestination?.route != ROUTE_LOGIN &&
                rootNavController.currentDestination?.route != ROUTE_SPLASH
            ) {
                goRoute(
                    navController = rootNavController,
                    route = ROUTE_LOGIN
                )
            }
        }
    }

    val routeConfigurations = listOf(
        RouteConfiguration(ROUTE_SPLASH) { _, _, _ ->
            SplashLoadingScreen(message = uiState.loadingMessage)
        },
        RouteConfiguration(ROUTE_LOGIN) { _, _, _ ->
            LoginScreen(
                viewModel = loginViewModel
            )
        },
        RouteConfiguration(ROUTE_HOME) { _, _, _ ->
            DashboardScreen(
                mainViewModel = mainViewModel,
                onLogout = {
                    // Limpiamos la sesión de Facebook y volvemos al Login
                    loginViewModel.signOut()
                }
            )
        },
    )
    Navigation(
        navController = rootNavController,
        routeConfigurations = routeConfigurations,
        startDestination = if (isSuccessful == true) ROUTE_HOME else if (isSuccessful == false) ROUTE_LOGIN else ROUTE_SPLASH,
        modifier = modifier.fillMaxSize()
    )
}

/*@Composable
fun DashboardScreen(
    mainViewModel: MainViewModel,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {

    Scaffold(
        modifier = modifier.fillMaxSize()
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Dashboard", style = MaterialTheme.typography.headlineMedium)
        }
    }

}*/

