package org.listenbrainz.android.ui.screens.brainzplayer.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Playlist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.screens.brainzplayer.*


@ExperimentalMaterial3Api
@Composable
fun Navigation(
    localNavHostController: NavHostController,
    appNavController: NavController,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    recentlyPlayedSongs: Playlist,
    songs: List<Song>
) {
    NavHost(navController = localNavHostController, startDestination = BrainzPlayerNavigationItem.Home.route) {
        
        composable(route = BrainzPlayerNavigationItem.Home.route) {
            HomeScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                playlists = playlists,
                recentlyPlayedSongs = recentlyPlayedSongs,
                navHostController = localNavHostController,
                appNavController = appNavController
            )
        }
        composable(route = BrainzPlayerNavigationItem.Songs.route) {
            SongScreen()
        }
        composable(route = BrainzPlayerNavigationItem.Artists.route) {
            ArtistScreen(localNavHostController)
        }
        composable(route = BrainzPlayerNavigationItem.Albums.route) {
            AlbumScreen(localNavHostController)
        }
        composable(route = BrainzPlayerNavigationItem.Playlists.route) {
            PlaylistScreen(localNavHostController)
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
                OnArtistClickScreen(artistID = artistID, localNavHostController)
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
