package org.listenbrainz.android.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.screens.album.AlbumScreen
import org.listenbrainz.android.ui.screens.artist.ArtistScreen
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
    snackbarState: SnackbarHostState,
    goToUserProfile: () -> Unit,
    goToArtistPage: (String) -> Unit,
    goToAlbumPage: (String) -> Unit,
    goToUserPage: (String?) -> Unit,
) {
    NavHost(
        navController = navController as NavHostController,
        modifier = Modifier.fillMaxSize(),
        startDestination = AppNavigationItem.Feed.route
    ) {
        composable(route = AppNavigationItem.Feed.route) {
            FeedScreen(
                scrollToTopState = scrollRequestState,
                onScrollToTop = onScrollToTop,
                goToUserPage = { username: String? ->
                    if (username != null) {
                        navController.navigate("${AppNavigationItem.Profile.route}/$username") {
                            // Avoid building large backstack
                            popUpTo(AppNavigationItem.Feed.route) {
                                saveState = true
                            }
                            // Avoid copies
                            launchSingleTop = true
                            // Restore previous state
                            restoreState = true
                        }
                    }
                },
                goToArtistPage = goToArtistPage
            )
        }
        composable(route = AppNavigationItem.BrainzPlayer.route) {
            BrainzPlayerScreen()
        }
        composable(route = AppNavigationItem.Explore.route) {
            ExploreScreen()
        }
        composable(
            route = "${AppNavigationItem.Profile.route}/{username}",
            arguments = listOf(
                navArgument("username") {
                    type = NavType.StringType
                    nullable = true
                }
            )
        ) {
            val username = it.arguments?.getString("username")
            ProfileScreen(
                onScrollToTop = onScrollToTop,
                scrollRequestState = scrollRequestState,
                username = username,
                snackbarState = snackbarState,
                goToUserProfile = goToUserProfile,
                goToArtistPage = goToArtistPage,
                goToUserPage = goToUserPage
            )
        }
        composable(
            route = AppNavigationItem.Settings.route
        ) {
            SettingsScreen()
        }
        composable(
            route = "${AppNavigationItem.Artist.route}/{mbid}",
            arguments = listOf(
                navArgument("mbid") {
                    type = NavType.StringType
                }
            )
        ) {
            val artistMbid = it.arguments?.getString("mbid")
            if (artistMbid == null) {
                LaunchedEffect(Unit) {
                    snackbarState.showSnackbar("The artist page can't be loaded")
                }
            } else {
                ArtistScreen(
                    artistMbid = artistMbid,
                    goToArtistPage = goToArtistPage,
                    snackBarState = snackbarState,
                    goToUserPage = goToUserPage,
                    goToAlbumPage = goToAlbumPage
                )
            }
        }
        composable(
            route = "${AppNavigationItem.Album.route}/{mbid}",
            arguments = listOf(
                navArgument("mbid") {
                    type = NavType.StringType
                }
            )
        ) {
            val albumMbid = it.arguments?.getString("mbid")
            if (albumMbid == null) {
                LaunchedEffect(Unit) {
                    snackbarState.showSnackbar("The album page can't be loaded")
                }
            } else {
                AlbumScreen(albumMbid = albumMbid, snackBarState = snackbarState)
            }
        }
    }
}