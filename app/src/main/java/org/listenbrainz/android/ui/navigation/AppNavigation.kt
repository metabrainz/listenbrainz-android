package org.listenbrainz.android.ui.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.first
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.ui.screens.album.AlbumScreen
import org.listenbrainz.android.ui.screens.artist.ArtistScreen
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerScreen
import org.listenbrainz.android.ui.screens.explore.ExploreScreen
import org.listenbrainz.android.ui.screens.explore.HueSoundScreen
import org.listenbrainz.android.ui.screens.feed.FeedScreen
import org.listenbrainz.android.ui.screens.playlist.PlaylistDetailScreen
import org.listenbrainz.android.ui.screens.profile.LoginScreen
import org.listenbrainz.android.ui.screens.profile.ProfileScreen
import org.listenbrainz.android.ui.screens.settings.SettingsScreen
import org.listenbrainz.android.viewmodel.DashBoardViewModel

@Composable
fun AppNavigation(
    navController: NavController = rememberNavController(),
    scrollRequestState: Boolean,
    onScrollToTop: (suspend () -> Unit) -> Unit,
    dashBoardViewModel: DashBoardViewModel,
    snackbarState: SnackbarHostState,
) {
    fun NavOptionsBuilder.defaultNavOptions() {
        // Avoid building large backstack
        popUpTo(AppNavigationItem.Feed.route) {
            saveState = true
        }
        // Avoid copies
        launchSingleTop = true
        // Restore previous state
        restoreState = true
    }

    fun goToUserProfile(username: String) {
        navController.navigate("${AppNavigationItem.Profile.route}/${username}")
    }

    fun goToArtistPage(mbid: String) {
        navController.navigate("artist/$mbid") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goToAlbumPage(mbid: String) {
        navController.navigate("album/$mbid") {
            launchSingleTop = true
            restoreState = true
        }
    }

    fun goToPlaylist(mbid: String) {
        navController.navigate("${AppNavigationItem.PlaylistScreen.route}/$mbid") {
            launchSingleTop = true
            restoreState = true
        }
    }


    NavHost(
        navController = navController as NavHostController,
        modifier = Modifier.fillMaxSize(),
        startDestination = AppNavigationItem.Feed.route
    ) {
        appComposable(route = AppNavigationItem.Feed.route) {
            FeedScreen(
                scrollToTopState = scrollRequestState,
                onScrollToTop = onScrollToTop,
                goToUserPage = ::goToUserProfile,
                goToArtistPage = ::goToArtistPage
            )
        }
        appComposable(route = AppNavigationItem.BrainzPlayer.route) {
            BrainzPlayerScreen()
        }
        appComposable(route = AppNavigationItem.Explore.route) {
            ExploreScreen(goToHueSoundScreen = { navController.navigate(AppNavigationItem.HueSound.route) })
        }
        appComposable(
            route = AppNavigationItem.Profile.route
        ) {
            val viewModel = hiltViewModel<DashBoardViewModel>()
            LoginScreen {
                val username = viewModel.usernameFlow.first()
                if (username.isNotBlank()) {
                    goToUserProfile(username)
                }
            }
        }
        appComposable(
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
                goToUserProfile = ::goToUserProfile,
                goToArtistPage = ::goToArtistPage,
                goToPlaylist = ::goToPlaylist
            )
        }
        appComposable(
            route = AppNavigationItem.Settings.route
        ) {
            SettingsScreen(
                dashBoardViewModel = dashBoardViewModel
            )
        }
        appComposable(
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
                    goToArtistPage = ::goToArtistPage,
                    snackBarState = snackbarState,
                    goToUserPage = ::goToUserProfile,
                    goToAlbumPage = ::goToAlbumPage
                )
            }
        }
        appComposable(
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
        appComposable(
            route = "${AppNavigationItem.PlaylistScreen.route}/{mbid}",
            arguments = listOf(
                navArgument("mbid") {
                    type = NavType.StringType
                }
            )
        ){ backStackTrace ->
            val playlistMbid = backStackTrace.arguments?.getString("mbid")
            if (playlistMbid == null){
                LaunchedEffect(Unit){
                    snackbarState.showSnackbar("The playlist page can't be loaded")
                }
            } else {
                 PlaylistDetailScreen(
                    playlistMBID = playlistMbid,
                    snackbarState = snackbarState,
                    goToArtistPage = ::goToArtistPage,
                     goToUserPage = ::goToUserProfile
                 )
            }
        }

        appComposable(route = AppNavigationItem.HueSound.route) {
            HueSoundScreen()
        }
    }
}

fun NavGraphBuilder.appComposable(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable (AnimatedContentScope.(NavBackStackEntry) -> Unit)
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            fadeIn(tween(200))
        },
        exitTransition = {
            fadeOut(tween(200))
        },
        popExitTransition = {
            fadeOut(tween(200))
        },
        popEnterTransition = {
            fadeIn(tween(200))
        },
        content = content
    )
}