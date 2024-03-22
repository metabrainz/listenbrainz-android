package org.listenbrainz.android.ui.screens.brainzplayer


import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedSuggestionChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.*
import org.listenbrainz.android.ui.components.forwardingPainter
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
    val topRecents = recentlyPlayed.subList(0, minOf(recentlyPlayed.size , 5)).toMutableList()
    val topArtists = artists.subList(0, minOf(artists.size,5)).toMutableList()
    val topAlbums = albums.subList(0, minOf(songs.size,5)).toMutableList()
    val songsPlayedThisWeek = brainzPlayerViewModel.songsPlayedThisWeek.collectAsState(initial = listOf()).value
    topRecents.add(Song())
    topArtists.add(Artist())
    topAlbums.add(Album())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Navigation(albums = albums, previewAlbums = topAlbums, artists = artists, previewArtists = topArtists, playlists, songsPlayedToday, songsPlayedThisWeek ,topRecents ,songs)
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
                songsPlayedThisWeek = songsPlayedThisWeek
            )
            2 -> ArtistsOverviewScreen(
                artists = artists
            )
            3 -> AlbumsOverViewScreen(albums = albums)
            4 -> SongsOverviewScreen(songs = songs)
        }
    }
}

