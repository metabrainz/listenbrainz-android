package org.listenbrainz.android.ui.screens.yim.navigation

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
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.ui.screens.yim.*
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel
import org.listenbrainz.android.viewmodel.YimViewModel

// Transition Duration
private const val screenTransitionDuration = 900

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun YimNavigation(
    yimViewModel: YimViewModel,
    activity: ComponentActivity,
    networkConnectivityViewModel: NetworkConnectivityViewModel,
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        modifier = Modifier.fillMaxSize(),
        startDestination = YimScreens.YimHomeScreen.name
    ){
        // Add all Yim screens here
        
        composable(
            route = YimScreens.YimHomeScreen.name,
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
            YimHomeScreen(viewModel = yimViewModel, networkConnectivityViewModel = networkConnectivityViewModel,navController = navController, activity = activity)
        }
        
        addYimScreen( route = YimScreens.YimTopAlbumsScreen.name ){
            YimTopAlbumsScreen(yimViewModel = yimViewModel, navController = navController)
        }
        
        addYimScreen( route = YimScreens.YimChartsScreen.name ){
            YimChartsScreen(viewModel = yimViewModel, navController = navController)
        }
        
        addYimScreen( route = YimScreens.YimStatisticsScreen.name ){
            YimStatisticsScreen(yimViewModel = yimViewModel, navController = navController)
        }
        
        addYimScreen( route = YimScreens.YimRecommendedPlaylistsScreen.name ){
            YimRecommendedPlaylistsScreen(viewModel = yimViewModel, navController = navController)
        }
        
        addYimScreen( route = YimScreens.YimDiscoverScreen.name ){
            YimDiscoverScreen(yimViewModel = yimViewModel, navController = navController)
        }
        
        addYimScreen( route = YimScreens.YimEndgameScreen.name ){
            YimEndgameScreen(activity = activity)
        }
    }
}

fun NavGraphBuilder.addYimScreen(
    route : String,
    content : @Composable (AnimatedVisibilityScope.(NavBackStackEntry) -> Unit)
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