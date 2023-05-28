package org.listenbrainz.android.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.components.BackLayerContent
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerScreen
import org.listenbrainz.android.ui.screens.explore.ExploreScreen
import org.listenbrainz.android.ui.screens.profile.ProfileScreen

@Composable
fun AppNavigation(
    navController: NavController = rememberNavController(),
    activity: ComponentActivity,
    shouldScrollToTop: MutableState<Boolean>
) {
    NavHost(
        navController = navController as NavHostController,
        modifier = Modifier.fillMaxSize(),
        startDestination = AppNavigationItem.Home.route
    ){
        composable(route = AppNavigationItem.Home.route){
            BackLayerContent(activity = activity)
        }
        composable(route = AppNavigationItem.BrainzPlayer.route){
            BrainzPlayerScreen(appNavController = navController)
        }
        composable(route = AppNavigationItem.Explore.route){
            ExploreScreen()
        }
        composable(route = AppNavigationItem.Profile.route){
            ProfileScreen(shouldScrollToTop = shouldScrollToTop)
        }
    }
}