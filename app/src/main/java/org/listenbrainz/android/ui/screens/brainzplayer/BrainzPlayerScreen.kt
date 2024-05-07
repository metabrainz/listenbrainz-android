package org.listenbrainz.android.ui.screens.brainzplayer


import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.*
import org.listenbrainz.android.ui.screens.brainzplayer.navigation.Navigation
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainzPlayerScreen() {
    // View models
    val albumViewModel = hiltViewModel<AlbumViewModel>()
    val songsViewModel = hiltViewModel<SongViewModel>()
    val artistViewModel = hiltViewModel<ArtistViewModel>()
    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    
    // Data streams
    val albums = albumViewModel.albums.collectAsState(initial = listOf()).value     // TODO: Introduce initial values to avoid flicker.
    val songs = songsViewModel.songs.collectAsState(initial = listOf()).value
    val artists = artistViewModel.artists.collectAsState(initial = listOf()).value
    val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
    val songsPlayedToday = brainzPlayerViewModel.songsPlayedToday.collectAsState(initial = listOf()).value
    val recentlyPlayed = brainzPlayerViewModel.recentlyPlayed.collectAsState(initial = mutableListOf()).value
    val topRecents = recentlyPlayed.take(5).toMutableList()
    val topArtists = artists.take(5).toMutableList()
    val topAlbums = albums.take(5).toMutableList()
    val albumSongsMap : MutableMap<Long,List<Song>> = mutableMapOf()
    for(i in 1..albums.size){
        val albumSongs : List<Song> = albumViewModel.getAllSongsOfAlbum(albums[i-1].albumId).collectAsState(
            initial = listOf()
        ).value
        albumSongsMap[albums[i-1].albumId] = albumSongs
    }
    val songsPlayedThisWeek = brainzPlayerViewModel.songsPlayedThisWeek.collectAsState(initial = listOf()).value
    topRecents.add(Song())
    topArtists.add(Artist())
    topAlbums.add(Album())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Navigation(albums = albums, previewAlbums = topAlbums, artists = artists, previewArtists = topArtists, playlists, songsPlayedToday, songsPlayedThisWeek ,topRecents ,songs, albumSongsMap)
    }
}


@Composable
fun BrainzPlayerHomeScreen(
    songs : List<Song>,
    albums: List<Album>,
    previewAlbums: List<Album>,
    artists: List<Artist>,
    previewArtists: List<Artist>,
    songsPlayedToday: List<Song>,
    songsPlayedThisWeek: List<Song>,
    recentlyPlayedSongs: List<Song>,
    albumSongsMap: MutableMap<Long,List<Song>>,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
) {

    val currentTab : MutableState<Int> = remember {mutableStateOf(0)}
    Column {
        Row(modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    listOf(
                        ListenBrainzTheme.colorScheme.background,
                        Color.Transparent
                    )
                )
            )) {
            Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.chipsHorizontal / 2))
            repeat(5) { position ->
                ElevatedSuggestionChip(
                    modifier = Modifier.padding(ListenBrainzTheme.paddings.chipsHorizontal),
                    colors = SuggestionChipDefaults.elevatedSuggestionChipColors(
                        if (position == currentTab.value) {
                            ListenBrainzTheme.colorScheme.chipSelected
                        } else {
                            ListenBrainzTheme.colorScheme.chipUnselected
                        }
                    ),
                    shape = ListenBrainzTheme.shapes.chips,
                    elevation = SuggestionChipDefaults.elevatedSuggestionChipElevation(elevation = 4.dp),
                    label = {
                        androidx.compose.material3.Text(
                            text = when (position) {
                                0 -> "Overview"
                                1 -> "Recent"
                                2 -> "Artists"
                                3 -> "Albums"
                                4 -> "Songs"
                                else -> "Overview"
                            },
                            style = ListenBrainzTheme.textStyles.chips,
                            color = ListenBrainzTheme.colorScheme.text,
                        )
                    },
                    onClick = {currentTab.value = position}
                )
            }
        }
        when (currentTab.value) {
            0 -> OverviewScreen(
                songsPlayedToday = songsPlayedToday,
                recentlyPlayedSongs = recentlyPlayedSongs,
                goToRecentScreen = {currentTab.value = 1},
                goToArtistScreen = {currentTab.value = 2},
                goToAlbumScreen = {currentTab.value = 3},
                brainzPlayerViewModel = brainzPlayerViewModel,
                artists = previewArtists,
                albums = previewAlbums
            )
            1 -> RecentPlaysScreen(
                songsPlayedToday = songsPlayedToday,
                songsPlayedThisWeek = songsPlayedThisWeek,
                onPlayIconClick = {
                    song, newPlayables ->
                    brainzPlayerViewModel.changePlayable(
                        newPlayables,
                        PlayableType.ALL_SONGS,
                        song.mediaID,
                        newPlayables.indexOf(song),
                        0L
                    )
                    brainzPlayerViewModel.playOrToggleSong(song,true)
                }
            )
            2 -> ArtistsOverviewScreen(
                artists = artists
            )
            3 -> AlbumsOverViewScreen(albums = albums, onPlayIconClick = {
                album ->
                val albumSongs = albumSongsMap[album.albumId]!!
                Log.v("pranav",album.albumId.toString())
                brainzPlayerViewModel.changePlayable(
                    albumSongs.sortedBy { it.trackNumber },
                    PlayableType.ALBUM,
                    album.albumId,
                    albumSongs
                        .sortedBy { it.trackNumber }
                        .indexOf (albumSongs[0]),
                    0L
                )
                brainzPlayerViewModel.playOrToggleSong(albumSongs[0],true)


            })
            4 -> SongsOverviewScreen(songs = songs, onPlayIconClick = {
                    song , newPlayables ->
                brainzPlayerViewModel.changePlayable(
                    newPlayables,
                    PlayableType.ALL_SONGS,
                    song.mediaID,
                    newPlayables.sortedBy { it.discNumber }.indexOf(song),
                    0L
                )
                brainzPlayerViewModel.playOrToggleSong(song,true)
            })
        }
    }
}