package org.listenbrainz.android.ui.components

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Playlist
import org.listenbrainz.android.ui.screens.brainzplayer.*

@OptIn(ExperimentalPagerApi::class)
@ExperimentalMaterial3Api
@Composable
fun Navigation(
    navHostController: NavHostController,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    recentlyPlayedSongs: Playlist,
    brainzPlayerActivity: BrainzPlayerActivity
) {
    NavHost(navController = navHostController, startDestination = BrainzNavigationItem.Home.route) {
        //BrainzPlayerActivity bottom navigation
        composable(route = BrainzNavigationItem.Home.route) {
            HomeScreen(albums = albums, artists, playlists, recentlyPlayedSongs, navHostController = navHostController,
                activity = brainzPlayerActivity
            )
        }
        composable(route = BrainzNavigationItem.Songs.route) {
            SongScreen()
        }
        composable(route = BrainzNavigationItem.Artists.route) {
            ArtistScreen(navHostController)
        }
        composable(route = BrainzNavigationItem.Albums.route) {
            AlbumScreen(navHostController)
        }
        composable(route = BrainzNavigationItem.Playlists.route) {
            PlaylistScreen(navHostController)
        }

        //BrainzPlayerActivity navigation on different screens
        composable(
            route = "onAlbumClick/{albumID}",
            arguments = listOf(navArgument("albumID") {
                type = NavType.LongType
            })
        ) { backStackEntry ->
            backStackEntry.arguments?.getLong("albumID")?.let { albumID ->
                OnAlbumClickScreen(albumID)
            }
        }
        composable(
            route = "onArtistClick/{artistID}",
            arguments = listOf(navArgument("artistID") {
                type = NavType.StringType
            })
        ) {
            it.arguments?.getString("artistID")?.let { artistID ->
                OnArtistClickScreen(artistID = artistID, navHostController)
            }
        }
        composable(
            route = "onPlaylistClick/{playlistID}",
            arguments = listOf(navArgument("playlistID") {
                type = NavType.LongType
            })
        ) {
            it.arguments?.getLong("playlistID")?.let { playlistID ->
                OnPlaylistClickScreen(playlistID = playlistID)
            }
        }
    }
}
