package org.listenbrainz.android.ui.navigation

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.ui.components.BackLayerContent
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerScreen
import org.listenbrainz.android.ui.screens.listens.ListensScreen
import org.listenbrainz.android.ui.screens.login.ProfileScreen

@Composable
fun AppNavigation(
    navController: NavController = rememberNavController(),
    activity: ComponentActivity
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
        composable(route = AppNavigationItem.Listens.route){
            ListensScreen(navController = navController)
        }
        composable(route = AppNavigationItem.Profile.route){
            ProfileScreen()
        }
    }
}