package org.listenbrainz.android.ui.screens.brainzplayer


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.screens.brainzplayer.navigation.Navigation
import org.listenbrainz.android.ui.screens.brainzplayer.overview.AlbumsOverViewScreen
import org.listenbrainz.android.ui.screens.brainzplayer.overview.ArtistsOverviewScreen
import org.listenbrainz.android.ui.screens.brainzplayer.overview.OverviewScreen
import org.listenbrainz.android.ui.screens.brainzplayer.overview.RecentPlaysScreen
import org.listenbrainz.android.ui.screens.brainzplayer.overview.SongsOverviewScreen
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.BPAlbumViewModel
import org.listenbrainz.android.viewmodel.BPArtistViewModel
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.PlaylistViewModel
import org.listenbrainz.android.viewmodel.SongViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainzPlayerScreen() {
    // View models
    val BPAlbumViewModel = hiltViewModel<BPAlbumViewModel>()
    val songsViewModel = hiltViewModel<SongViewModel>()
    val BPArtistViewModel = hiltViewModel<BPArtistViewModel>()
    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    
    // Data streams
    val albums = BPAlbumViewModel.albums.collectAsState(initial = listOf()).value     // TODO: Introduce initial values to avoid flicker.
    val songs = songsViewModel.songs.collectAsState(initial = listOf()).value
    val artists = BPArtistViewModel.artists.collectAsState(initial = listOf()).value
    val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
    val songsPlayedToday = brainzPlayerViewModel.songsPlayedToday.collectAsState(initial = listOf()).value
    val recentlyPlayed = brainzPlayerViewModel.recentlyPlayed.collectAsState(initial = mutableListOf()).value
    val topRecents = recentlyPlayed.take(5).toMutableList()
    val topArtists = artists.take(5).toMutableList()
    val topAlbums = albums.take(5).toMutableList()
    val albumSongsMap : MutableMap<Album,List<Song>> = mutableMapOf()

    for(i in 1..albums.size){
        val albumSongs : List<Song> = BPAlbumViewModel.getAllSongsOfAlbum(albums[i-1].albumId).collectAsState(
            initial = listOf()
        ).value
        albumSongsMap[albums[i-1]] = albumSongs
    }

    val songsPlayedThisWeek = brainzPlayerViewModel.songsPlayedThisWeek.collectAsState(initial = listOf()).value
    topRecents.add(Song())
    topArtists.add(Artist())
    topAlbums.add(Album())
    
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Navigation(albums = albums, previewAlbums = topAlbums, artists = artists, previewArtists = topArtists, playlists, songsPlayedToday, songsPlayedThisWeek ,topRecents ,songs, albumSongsMap)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
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
    albumSongsMap: MutableMap<Album,List<Song>>,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
) {

    val currentTab : MutableState<Int> = remember {mutableStateOf(0)}
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    val isPlaying = brainzPlayerViewModel.isPlaying
    Column {
        Row(
            modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .background(
                Brush.verticalGradient(
                    listOf(
                        ListenBrainzTheme.colorScheme.background,
                        Color.Transparent
                    )
                )
            )
        ) {
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
                    song ->
                    brainzPlayerViewModel.changePlayable(
                        listOf(song),
                        PlayableType.ALL_SONGS,
                        song.mediaID,
                        0,
                        0L
                    )
                    brainzPlayerViewModel.playOrToggleSong(song,true)
                },
                onAddToQueue = {
                    song ->
                    val currentSongs = brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                    currentSongs?.add(currentSongs.size, song)
                    brainzPlayerViewModel.appPreferences.currentPlayable = brainzPlayerViewModel.appPreferences.currentPlayable?.copy(songs = currentSongs ?: emptyList())
                    brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let {
                        brainzPlayerViewModel.changePlayable(
                            it,
                            PlayableType.ALL_SONGS,
                            brainzPlayerViewModel.appPreferences.currentPlayable?.id ?: 0,
                            brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID==currentlyPlayingSong.mediaID   } ?: 0,brainzPlayerViewModel.songCurrentPosition.value
                        )
                    }
                    brainzPlayerViewModel.queueChanged(
                        currentlyPlayingSong,
                        brainzPlayerViewModel.isPlaying.value
                    )
                },
                onPlayNext = {
                    song ->
                    val currentSongIndex =
                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID==currentlyPlayingSong.mediaID   }
                            ?.plus(1)
                    if (isPlaying.value && currentSongIndex != null) {
                        val currentSongs = brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                        currentSongs?.add(currentSongIndex, song)
                        brainzPlayerViewModel.appPreferences.currentPlayable = brainzPlayerViewModel.appPreferences.currentPlayable?.copy(songs = currentSongs ?: emptyList())
                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let {
                            brainzPlayerViewModel.changePlayable(
                                it,
                                PlayableType.ALL_SONGS,
                                brainzPlayerViewModel.appPreferences.currentPlayable?.id ?: 0,
                                brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID==currentlyPlayingSong.mediaID   } ?: 0,brainzPlayerViewModel.songCurrentPosition.value
                            )
                        }
                        brainzPlayerViewModel.queueChanged(
                            currentlyPlayingSong,
                            brainzPlayerViewModel.isPlaying.value
                        )
                    }
                    else{
                        // No song is playing, so start playing the selected song
                        brainzPlayerViewModel.changePlayable(
                            listOf(song),
                            PlayableType.SONG,
                            song.mediaID,
                            0,
                            0L
                        )
                        brainzPlayerViewModel.playOrToggleSong(song, true)
                    }

                },
                onAddToExistingPlaylist = {
                    song ->
                },
                onAddToNewPlaylist = {
                    song ->
                }
            )
            2 -> ArtistsOverviewScreen(
                artists = artists,
                onPlayClick = {
                    artist ->
                    brainzPlayerViewModel.changePlayable(
                        artist.songs,
                        PlayableType.ARTIST,
                        artist.id,
                        0,
                        0L
                    )
                    brainzPlayerViewModel.playOrToggleSong(artist.songs[0], true)
                },
                onPlayNext = {
                    artist ->
                    val currentSongIndex =
                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID==currentlyPlayingSong.mediaID   }
                            ?.plus(1)
                    if (isPlaying.value && currentSongIndex != null) {
                        val currentSongs = brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                        currentSongs?.addAll(currentSongIndex, artist.songs)
                        brainzPlayerViewModel.appPreferences.currentPlayable = brainzPlayerViewModel.appPreferences.currentPlayable?.copy(songs = currentSongs ?: emptyList())
                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let {
                            brainzPlayerViewModel.changePlayable(
                                it,
                                PlayableType.ALL_SONGS,
                                brainzPlayerViewModel.appPreferences.currentPlayable?.id ?: 0,
                                brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID==currentlyPlayingSong.mediaID   } ?: 0,brainzPlayerViewModel.songCurrentPosition.value
                            )
                        }
                        brainzPlayerViewModel.queueChanged(
                            currentlyPlayingSong,
                            brainzPlayerViewModel.isPlaying.value
                        )
                    }
                    else{
                        brainzPlayerViewModel.changePlayable(
                            artist.songs,
                            PlayableType.ARTIST,
                            artist.id,
                            0,
                            0L
                        )
                        brainzPlayerViewModel.playOrToggleSong(artist.songs[0], true)
                    }
                },
                onAddToQueue = {
                    artist ->
                    val currentSongs = brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                    currentSongs?.addAll(currentSongs.size, artist.songs)
                    brainzPlayerViewModel.appPreferences.currentPlayable = brainzPlayerViewModel.appPreferences.currentPlayable?.copy(songs = currentSongs ?: emptyList())
                    brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let {
                        brainzPlayerViewModel.changePlayable(
                            it,
                            PlayableType.ALL_SONGS,
                            brainzPlayerViewModel.appPreferences.currentPlayable?.id ?: 0,
                            brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID==currentlyPlayingSong.mediaID   } ?: 0,brainzPlayerViewModel.songCurrentPosition.value
                        )
                    }
                    brainzPlayerViewModel.queueChanged(
                        currentlyPlayingSong,
                        brainzPlayerViewModel.isPlaying.value
                    )
                },
                onAddToNewPlaylist = {
                    artist ->  
                },
                onAddToExistingPlaylist = {
                    artist ->  
                }
            )
            3 -> AlbumsOverViewScreen(albums = albums, onPlayIconClick = {
                album ->
                val albumSongs = albumSongsMap[album]!!
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