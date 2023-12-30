package org.listenbrainz.android.ui.screens.yim23.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.ui.screens.yim.*
import org.listenbrainz.android.ui.screens.yim.navigation.addYimScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23ChartTitleScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23HomeScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23TopAlbumsScreen
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.viewmodel.YimViewModel

// Transition Duration
private const val screenTransitionDuration = 900

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun Yim23Navigation(
    yimViewModel: Yim23ViewModel,
    activity: ComponentActivity,
    networkConnectivityViewModel: NetworkConnectivityViewModel,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        modifier = Modifier.fillMaxSize(),
        startDestination = Yim23Screens.YimLandingScreen.name
    ){
        // Add all Yim screens here

        composable(
            route = Yim23Screens.YimLandingScreen.name,
            enterTransition = { fadeIn() },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Up,
                    animationSpec = tween(screenTransitionDuration)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(screenTransitionDuration)
                )
            }
        ) {
            Yim23HomeScreen(viewModel = yimViewModel, networkConnectivityViewModel = networkConnectivityViewModel,navController = navController, activity = activity)
        }

        addYim23Screen( route = Yim23Screens.YimChartTitleScreen.name ){
            Yim23ChartTitleScreen(viewModel = yimViewModel, navController = navController)
        }

        addYim23Screen( route = Yim23Screens.YimTopAlbumScreen.name ){
            Yim23TopAlbumsScreen(viewModel = yimViewModel, navController = navController)
        }
    }
}

fun NavGraphBuilder.addYim23Screen(
    route : String,
    content : @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit),
){
    composable(
        route = route,
        enterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(screenTransitionDuration)
            )
        },
        exitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(screenTransitionDuration)
            )
        },
        popEnterTransition = {
            slideIntoContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(screenTransitionDuration)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                towards = AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(screenTransitionDuration)
            )
        },
        content = content
    )
}