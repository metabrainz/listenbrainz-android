package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.screens.about.AboutScreen
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerScreen
import org.listenbrainz.android.ui.screens.explore.ExploreScreen
import org.listenbrainz.android.ui.screens.feed.FeedScreen
import org.listenbrainz.android.ui.screens.newsbrainz.NewsBrainzScreen
import org.listenbrainz.android.ui.screens.profile.ProfileScreen
import org.listenbrainz.android.ui.screens.settings.SettingsScreen

@Composable
fun AppNavigation(
    navController: NavController = rememberNavController(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
) {
    NavHost(
        navController = navController as NavHostController,
        modifier = Modifier.fillMaxSize(),
        startDestination = AppNavigationItem.Feed.route
    ){
        composable(route = AppNavigationItem.Feed.route){
            FeedScreen(scrollToTopState = scrollRequestState, onScrollToTop = onScrollToTop)
        }
        composable(route = AppNavigationItem.BrainzPlayer.route){
            BrainzPlayerScreen()
        }
        composable(route = AppNavigationItem.Explore.route){
            ExploreScreen()
        }
        composable(route = AppNavigationItem.Profile.route){
            ProfileScreen(
                onScrollToTop = onScrollToTop,
                scrollRequestState = scrollRequestState
            )
        }
        composable(route = AppNavigationItem.Settings.route){
            SettingsScreen(navController = navController)
        }

        composable(route = AppNavigationItem.About.route){
            AboutScreen()
        }
    }
}