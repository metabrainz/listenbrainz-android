package org.listenbrainz.android.ui.screens.dashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.application.App
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.repository.preferences.AppPreferences
import org.listenbrainz.android.ui.components.DialogLB
import org.listenbrainz.android.ui.components.TopBar
import org.listenbrainz.android.ui.navigation.AppNavigation
import org.listenbrainz.android.ui.navigation.BottomNavigationBar
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerBackDropScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import javax.inject.Inject

@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {
    @Inject
    lateinit var appPreferences: AppPreferences

    @OptIn(ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        setContent {
            ListenBrainzTheme {
                // TODO: Since this view-model will remain throughout the lifecycle of the app,
                //  we can have tasks which require such lifecycle access or longevity. We can get this view-model's
                //  instance anywhere when we initialize it as a hilt view-model.
                val dashBoardViewModel : DashBoardViewModel by viewModels()
    
                dashBoardViewModel.setUiMode()
                dashBoardViewModel.beginOnboarding(this)
                dashBoardViewModel.updatePermissionPreference()
                
                var isGrantedPerms by remember {
                    mutableStateOf(dashBoardViewModel.appPreferences.permissionsPreference)
                }

                val launcher = rememberLauncherForActivityResult(
                    contract =
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { permission ->
                    val isGranted = permission.values.any { it }
                    when {
                        isGranted -> {
                            isGrantedPerms = PermissionStatus.GRANTED.name
                            dashBoardViewModel.appPreferences.permissionsPreference  = PermissionStatus.GRANTED.name
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
                            dashBoardViewModel.appPreferences.permissionsPreference = isGrantedPerms
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
                            dismissOnBackPress = false,
                            dismissOnClickOutside = false,
                            onDismiss = {}
                        )
                    }
                }
 
                val navController = rememberNavController()
                val backdropScaffoldState =
                    rememberBackdropScaffoldState(initialValue = BackdropValue.Revealed)
                val shouldScrollToTop = remember { mutableStateOf(false) }

                Scaffold(
                    topBar = { TopBar(activity = this, navController = navController) },
                    bottomBar = {
                        BottomNavigationBar(
                            navController = navController,
                            backdropScaffoldState = backdropScaffoldState,
                            shouldScrollToTop = shouldScrollToTop
                        )
                    },
                    backgroundColor = MaterialTheme.colorScheme.background
                ) {
                    if (isGrantedPerms == PermissionStatus.GRANTED.name) {
                        BrainzPlayerBackDropScreen(
                            backdropScaffoldState = backdropScaffoldState,
                            paddingValues = it
                        ) {
                            AppNavigation(
                                navController = navController,
                                activity = this,
                                shouldScrollToTop = shouldScrollToTop
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(appPreferences.isNotificationServiceAllowed && !appPreferences.lbAccessToken.isNullOrEmpty()) {
            App.startListenService()
        }
    }
}
