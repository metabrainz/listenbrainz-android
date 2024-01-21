package org.listenbrainz.android.ui.screens.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LifecycleStartEffect
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.listenbrainz.android.application.App
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.service.ListenSubmissionService
import org.listenbrainz.android.ui.components.DialogLB
import org.listenbrainz.android.ui.navigation.AppNavigation
import org.listenbrainz.android.ui.navigation.BottomNavigationBar
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerBackDropScreen
import org.listenbrainz.android.ui.screens.search.SearchScreen
import org.listenbrainz.android.ui.screens.search.rememberSearchBarState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.isServiceRunning
import org.listenbrainz.android.util.Utils.openAppSystemSettings
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private lateinit var dashBoardViewModel: DashBoardViewModel

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        dashBoardViewModel = ViewModelProvider(this)[DashBoardViewModel::class.java]

        setContent {
            ListenBrainzTheme {
                // TODO: Since this view-model will remain throughout the lifecycle of the app,
                //  we can have tasks which require such lifecycle access or longevity. We can get this view-model's
                //  instance anywhere when we initialize it as a hilt view-model.
    
                dashBoardViewModel.setUiMode()
                dashBoardViewModel.beginOnboarding(this)
                dashBoardViewModel.updatePermissionPreference()
                
                LifecycleStartEffect {
                    dashBoardViewModel.connectToSpotify()
                    onStopOrDispose {
                        dashBoardViewModel.disconnectSpotify()
                    }
                }
                
                var isGrantedPerms: String? by remember {
                    mutableStateOf(null)
                }
                
                LaunchedEffect(Unit) {
                    isGrantedPerms = dashBoardViewModel.getPermissionsPreference()
                }

                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permission ->
                    val isGranted = permission.values.any { it }
                    when {
                        isGranted -> {
                            isGrantedPerms = PermissionStatus.GRANTED.name
                            dashBoardViewModel.setPermissionsPreference(PermissionStatus.GRANTED.name)
                        }
                        else -> {
                            isGrantedPerms = when(isGrantedPerms){
                                PermissionStatus.NOT_REQUESTED.name -> {
                                    PermissionStatus.DENIED_ONCE.name
                                }
                                PermissionStatus.DENIED_ONCE.name -> {
                                    PermissionStatus.DENIED_TWICE.name
                                }
                                else -> {PermissionStatus.DENIED_TWICE.name}
                            }
                            dashBoardViewModel.setPermissionsPreference(isGrantedPerms)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    if (isGrantedPerms == PermissionStatus.NOT_REQUESTED.name) {
                        launcher.launch(dashBoardViewModel.neededPermissions)
                    }
                }

                when(isGrantedPerms){
                    PermissionStatus.DENIED_ONCE.name -> {
                        DialogLB(
                            options = arrayOf("Grant"),
                            firstOptionListener = {
                                launcher.launch(dashBoardViewModel.neededPermissions)
                            },
                            title = "Permissions required",
                            description = "BrainzPlayer requires local storage permission to play local songs.",
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            onDismiss = {}
                        )
                    }
                    PermissionStatus.DENIED_TWICE.name -> {
                        DialogLB(
                            title = "Permissions required",
                            description = "Please grant storage permissions from settings for the app to function.",
                            options = arrayOf("Open Settings"),
                            firstOptionListener = {
                                openAppSystemSettings()
                            },
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            onDismiss = {}
                        )
                    }
                }
 
                val navController = rememberNavController()
                val backdropScaffoldState =
                    rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
                var scrollToTopState by remember { mutableStateOf(false) }
                val snackbarState = SnackbarHostState()
                val searchBarState = rememberSearchBarState()
                val scope = rememberCoroutineScope()
                
                Scaffold(
                    topBar = { TopBar(navController = navController, searchBarState = searchBarState) },
                    bottomBar = {
                        BottomNavigationBar(
                            navController = navController,
                            backdropScaffoldState = backdropScaffoldState,
                            scrollToTop = { scrollToTopState = true }
                        )
                    },
                    snackbarHost = {
                        SnackbarHost(hostState = snackbarState) { snackbarData ->
                            Snackbar(
                                snackbarData = snackbarData,
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                actionColor = MaterialTheme.colorScheme.inverseOnSurface,
                                dismissActionContentColor = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets.captionBar
                
                ) {
                    
                    if (isGrantedPerms == PermissionStatus.GRANTED.name) {
                        
                        BrainzPlayerBackDropScreen(
                            backdropScaffoldState = backdropScaffoldState,
                            paddingValues = it,
                        ) {
                            AppNavigation(
                                navController = navController,
                                scrollRequestState = scrollToTopState,
                                onScrollToTop = { scrollToTop ->
                                    scope.launch {
                                        if (scrollToTopState){
                                            scrollToTop()
                                            scrollToTopState = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
                
                SearchScreen(
                    isActive = searchBarState.isActive,
                    deactivate = {searchBarState.deactivate()}
                )
                
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch(Dispatchers.Main) {
            if (dashBoardViewModel.isNotificationListenerServiceAllowed()) {
                if (!isServiceRunning(ListenSubmissionService::class.java)) {
                    App.startListenService()
                }
            }
        }
    }
}
