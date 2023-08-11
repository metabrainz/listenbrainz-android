package org.listenbrainz.android.ui.screens.brainzplayer.navigation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Playlist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.screens.brainzplayer.AlbumScreen
import org.listenbrainz.android.ui.screens.brainzplayer.ArtistScreen
import org.listenbrainz.android.ui.screens.brainzplayer.BrainzPlayerHomeScreen
import org.listenbrainz.android.ui.screens.brainzplayer.OnAlbumClickScreen
import org.listenbrainz.android.ui.screens.brainzplayer.OnArtistClickScreen
import org.listenbrainz.android.ui.screens.brainzplayer.OnPlaylistClickScreen
import org.listenbrainz.android.ui.screens.brainzplayer.PlaylistScreen
import org.listenbrainz.android.ui.screens.brainzplayer.SongScreen


@ExperimentalMaterial3Api
@Composable
fun Navigation(
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    recentlyPlayedSongs: Playlist,
    songs: List<Song>,
    navHostController: NavHostController = rememberNavController()
) {
    
    fun goTo(destination: BrainzPlayerNavigationItem) = navHostController.navigate(destination.route)
    
    NavHost(navController = navHostController, startDestination = BrainzPlayerNavigationItem.Home.route) {
        
        composable(route = BrainzPlayerNavigationItem.Home.route) {
            BrainzPlayerHomeScreen(
                songs = songs,
                albums = albums,
                artists = artists,
                playlists = playlists,
                recentlyPlayedSongs = recentlyPlayedSongs,
                navigateToSongsScreen = { goTo(BrainzPlayerNavigationItem.Songs) },
                navigateToArtist = { id -> navHostController.navigate("onArtistClick/$id")},
                navigateToAlbumsScreen = { goTo(BrainzPlayerNavigationItem.Albums) },
                navigateToArtistsScreen = { goTo(BrainzPlayerNavigationItem.Artists) },
                navigateToPlaylistsScreen = { goTo(BrainzPlayerNavigationItem.Playlists) },
                navigateToAlbum = { id -> navHostController.navigate("onAlbumClick/$id")},
                navigateToPlaylist = { id -> navHostController.navigate("onPlaylistClick/$id")}
            )
        }
        composable(route = BrainzPlayerNavigationItem.Songs.route) {
            SongScreen()
        }
        composable(route = BrainzPlayerNavigationItem.Artists.route) {
            ArtistScreen { id ->
                navHostController.navigate("onArtistClick/$id")
            }
        }
        composable(route = BrainzPlayerNavigationItem.Albums.route) {
            AlbumScreen { id -> navHostController.navigate("onAlbumClick/$id") }
        }
        composable(route = BrainzPlayerNavigationItem.Playlists.route) {
            PlaylistScreen { id -> navHostController.navigate("onPlaylistClick/$id") }
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
                OnArtistClickScreen(artistID = artistID) { id ->
                    navHostController.navigate("onAlbumClick/$id")
                }
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
