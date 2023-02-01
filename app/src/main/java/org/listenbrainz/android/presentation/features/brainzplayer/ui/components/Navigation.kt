package org.listenbrainz.android.presentation.features.brainzplayer.ui.components


import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.google.accompanist.pager.ExperimentalPagerApi
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.Artist
import org.listenbrainz.android.data.sources.brainzplayer.Playlist
import org.listenbrainz.android.data.sources.brainzplayer.Song
import org.listenbrainz.android.presentation.features.brainzplayer.ui.BrainzPlayerActivity
import org.listenbrainz.android.presentation.features.brainzplayer.ui.HomeScreen
import org.listenbrainz.android.presentation.features.brainzplayer.ui.album.AlbumScreen
import org.listenbrainz.android.presentation.features.brainzplayer.ui.album.OnAlbumClickScreen
import org.listenbrainz.android.presentation.features.brainzplayer.ui.artist.ArtistScreen
import org.listenbrainz.android.presentation.features.brainzplayer.ui.artist.OnArtistClickScreen
import org.listenbrainz.android.presentation.features.brainzplayer.ui.playlist.OnPlaylistClickScreen
import org.listenbrainz.android.presentation.features.brainzplayer.ui.playlist.PlaylistScreen
import org.listenbrainz.android.presentation.features.brainzplayer.ui.songs.SongScreen
import org.listenbrainz.android.presentation.features.navigation.BrainzNavigationItem

@OptIn(ExperimentalPagerApi::class)
@ExperimentalMaterial3Api
@Composable
fun Navigation(
    navHostController: NavHostController,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    recentlyPlayedSongs: List<Song>,
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
