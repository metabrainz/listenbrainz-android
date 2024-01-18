package org.listenbrainz.android.ui.screens.yim23.navigation

import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.listenbrainz.android.model.AppNavigationItem
import org.listenbrainz.android.model.yimdata.Yim23Screens
import org.listenbrainz.android.model.yimdata.YimScreens
import org.listenbrainz.android.ui.screens.profile.ProfileScreen
import org.listenbrainz.android.ui.screens.yim.*
import org.listenbrainz.android.ui.screens.yim.navigation.addYimScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23AlbumsListScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23ChartTitleScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23DiscoveriesListScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23DiscoveriesScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23DiscoveriesTitleScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23FriendsScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23HomeScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23LastScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23MissedSongsListScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23MissedSongsScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23MusicBuddiesScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23NewAlbumsFromTopArtistsScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23PlaylistTitleScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23StatsGraphScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23StatsHeatMapScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23StatsScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23StatsTitleScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23TopAlbumsScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23TopArtistsScreen
import org.listenbrainz.android.ui.screens.yim23.Yim23TopSongsScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.connectivityobserver.NetworkConnectivityViewModel
import org.listenbrainz.android.viewmodel.SocialViewModel
import org.listenbrainz.android.viewmodel.Yim23ViewModel
import org.listenbrainz.android.viewmodel.YimViewModel

// Transition Duration
private const val screenTransitionDuration = 900

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Yim23Navigation(
    yimViewModel: Yim23ViewModel,
    socialViewModel: SocialViewModel,
    activity: ComponentActivity,
    networkConnectivityViewModel: NetworkConnectivityViewModel,
) {
    val navController = rememberNavController()
    var scrollToTopState by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
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

        composable(route = AppNavigationItem.Profile.route){
            Surface (color = ListenBrainzTheme.colorScheme.background) {
                ProfileScreen(
                    onScrollToTop = { scrollToTop ->
                        scope.launch {
                            if (scrollToTopState){
                                scrollToTop()
                                scrollToTopState = false
                            }
                        }
                    },
                    scrollRequestState = false
                )
            }
        }


        addYimScreen( route = Yim23Screens.YimChartTitleScreen.name ){
            Yim23ChartTitleScreen(viewModel = yimViewModel, navController = navController)
        }

        addYimScreen( route = Yim23Screens.YimTopAlbumScreen.name ){
            Yim23TopAlbumsScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimAlbumsListScreen.name ){
            Yim23AlbumsListScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimTopSongsScreen.name ){
            Yim23TopSongsScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimTopArtistsScreen.name ){
            Yim23TopArtistsScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimStatsTitleScreen.name){
            Yim23StatsTitleScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimStatsScreen.name){
            Yim23StatsScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimStatsHeatMapScreen.name){
            Yim23StatsHeatMapScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimStatsGraphScreen.name){
        Yim23StatsGraphScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimPlaylistsTitleScreen.name){
            Yim23PlaylistTitleScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimDiscoveriesScreen.name){
            Yim23DiscoveriesScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimDiscoveriesListScreen.name){
            Yim23DiscoveriesListScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimMissedSongsScreen.name){
            Yim23MissedSongsScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimMissedSongsListScreen.name){
            Yim23MissedSongsListScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimDiscoverTitleScreen.name){
            Yim23DiscoveriesTitleScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimNewAlbumsFromTopArtistsScreen.name){
            Yim23NewAlbumsFromTopArtistsScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimMusicBuddiesScreen.name){
            Yim23MusicBuddiesScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimFriendsScreen.name){
            Yim23FriendsScreen(viewModel = yimViewModel, navController = navController)
        }
        addYimScreen( route = Yim23Screens.YimLastScreen.name){
            Yim23LastScreen(viewModel = yimViewModel, navController = navController)
        }
    }
}