package org.listenbrainz.android.ui.screens.dashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import com.google.accompanist.pager.ExperimentalPagerApi
import dagger.hilt.android.AndroidEntryPoint
import org.listenbrainz.android.ui.navigation.BottomNavigationBar
import org.listenbrainz.android.ui.components.DialogLB
import org.listenbrainz.android.ui.components.TopAppBar
import org.listenbrainz.android.ui.navigation.AppNavigation
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerBackDropScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.UserPreferences.PermissionStatus
import org.listenbrainz.android.viewmodel.DashBoardViewModel


@AndroidEntryPoint
class DashboardActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterialApi::class, ExperimentalPagerApi::class)
    @SuppressLint("UnusedMaterialScaffoldPaddingParameter")
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
                Scaffold(
                    topBar = { TopAppBar(activity = this) },
                    bottomBar = { BottomNavigationBar(navController = navController, activity = this, backdropScaffoldState = backdropScaffoldState) },
                    backgroundColor = MaterialTheme.colorScheme.background
                ) {
                    if (isGrantedPerms == PermissionStatus.GRANTED.name) {
                        BrainzPlayerBackDropScreen(
                            backdropScaffoldState = backdropScaffoldState
                        ) {
                            AppNavigation(
                                navController = navController,
                                activity = this
                            )
                        }
                    }
                }
            }
        }
    }
}
