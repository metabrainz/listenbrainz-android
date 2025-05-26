package org.listenbrainz.android.ui.screens.main

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.captionBar
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.BackdropValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.rememberBackdropScaffoldState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import org.listenbrainz.android.application.App
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.model.PermissionStatus
import org.listenbrainz.android.ui.components.DialogLB
import org.listenbrainz.android.ui.navigation.AdaptiveNavigationBar
import org.listenbrainz.android.ui.navigation.AppNavigation
import org.listenbrainz.android.ui.navigation.TopBar
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerBackDropScreen
import org.listenbrainz.android.ui.screens.search.BrainzPlayerSearchScreen
import org.listenbrainz.android.ui.screens.search.UserSearchScreen
import org.listenbrainz.android.ui.screens.search.rememberSearchBarState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.openAppSystemSettings
import org.listenbrainz.android.util.Utils.toPx
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.DashBoardViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.navigation.NavigationItem
import org.listenbrainz.android.ui.screens.onboarding.IntroductionScreens
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private lateinit var _dashBoardViewModel: DashBoardViewModel
    private val dashBoardViewModel get() = _dashBoardViewModel

    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        _dashBoardViewModel = ViewModelProvider(this)[DashBoardViewModel::class.java]

        dashBoardViewModel.setUiMode()
        dashBoardViewModel.beginOnboarding(this)
        dashBoardViewModel.updatePermissionPreference()

        setContent {
            ListenBrainzTheme {
                // TODO: Since this view-model will remain throughout the lifecycle of the app,
                //  we can have tasks which require such lifecycle access or longevity. We can get this view-model's
                //  instance anywhere when we initialize it as a hilt view-model.

                DisposableEffect(Unit) {
                    dashBoardViewModel.connectToSpotify()
                    onDispose {
                        dashBoardViewModel.disconnectSpotify()
                    }
                }

                val backStack =
                    rememberNavBackStack<NavigationItem>(NavigationItem.IntroductionScreen)
                NavDisplay(
                    backStack = backStack,
                    entryProvider = entryProvider {
                        entry<NavigationItem.IntroductionScreen>{
                            IntroductionScreens {
                                backStack.add(NavigationItem.HomeScreen)
                            }
                        }
                        entry<NavigationItem.HomeScreen>{
                            HomeScreen()
                        }
                    }
                )

            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            App.startListenService(appPreferences = dashBoardViewModel.appPreferences)
        }
    }
}