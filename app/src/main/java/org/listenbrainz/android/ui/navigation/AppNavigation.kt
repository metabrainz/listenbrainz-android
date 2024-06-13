package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerScreen
import org.listenbrainz.android.ui.screens.explore.ExploreScreen
import org.listenbrainz.android.ui.screens.feed.FeedScreen
import org.listenbrainz.android.ui.screens.profile.ProfileScreen
import org.listenbrainz.android.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavController = rememberNavController(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    snackbarState : SnackbarHostState
) {
    NavHost(
        navController = navController as NavHostController,
        modifier = Modifier.fillMaxSize(),
        startDestination = AppNavigationItem.Feed.route
    ){
        composable(route = AppNavigationItem.Feed.route){
            FeedScreen(scrollToTopState = scrollRequestState, onScrollToTop = onScrollToTop, goToUserPage = {username : String? ->
                if(username != null) {
                navController.navigate("${AppNavigationItem.Profile.route}/$username"){
                    // Avoid building large backstack
                    popUpTo(AppNavigationItem.Feed.route){
                        saveState = true
                    }
                    // Avoid copies
                    launchSingleTop = true
                    // Restore previous state
                    restoreState = true
                }
            } })
        }
        composable(route = AppNavigationItem.BrainzPlayer.route){
            BrainzPlayerScreen()
        }
        composable(route = AppNavigationItem.Explore.route){
            ExploreScreen()
        }
        composable(route = "${AppNavigationItem.Profile.route}/{username}", arguments = listOf(
            navArgument("username"){
                type = NavType.StringType
            }
        ))
        {
            val username = it.arguments?.getString("username")
            ProfileScreen(
                onScrollToTop = onScrollToTop,
                scrollRequestState = scrollRequestState,
                username = username,
                snackbarState = snackbarState
            )
        }
        composable(route = AppNavigationItem.Settings.route){
            SettingsScreen()
        }
    }
}