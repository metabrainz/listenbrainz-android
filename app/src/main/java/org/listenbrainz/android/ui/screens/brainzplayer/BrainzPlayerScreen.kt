package org.listenbrainz.android.ui.screens.brainzplayer


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
    val songsPlayedThisWeek = brainzPlayerViewModel.songsPlayedThisWeek.collectAsState(initial = listOf()).value
    topRecents.add(Song())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Navigation(albums, artists, playlists, songsPlayedToday, songsPlayedThisWeek ,topRecents ,songs)
    }
}


@Composable
fun BrainzPlayerHomeScreen(
    songs : List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    songsPlayedToday: List<Song>,
    songsPlayedThisWeek: List<Song>,
    recentlyPlayedSongs: List<Song>,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
    navigateToSongsScreen: () -> Unit,
    navigateToArtistsScreen: () -> Unit,
    navigateToAlbumsScreen: () -> Unit,
    navigateToPlaylistsScreen: () -> Unit,
    navigateToArtist: (id: Long) -> Unit,
    navigateToAlbum: (id: Long) -> Unit,
    navigateToPlaylist: (id: Long) -> Unit
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
                brainzPlayerViewModel = brainzPlayerViewModel,
                artists = artists,
                albums = albums
            )
            1 -> RecentPlaysScreen(
                songsPlayedToday = songsPlayedToday,
                songsPlayedThisWeek = songsPlayedThisWeek
            )
            2 -> ArtistScreen(navigateToArtistScreen = {id -> navigateToArtistsScreen()})
        }
    }


//    Column(modifier = Modifier
//        .padding(horizontal = 8.dp)
//        .verticalScroll(rememberScrollState())
//    ) {
//        // Recently Played
////        Text(
////            text = "Recently Played",
////            modifier = Modifier
////                .fillMaxWidth()
////                .padding(16.dp),
////            fontWeight = FontWeight.Bold,
////            fontSize = 24.sp,
////            textAlign = TextAlign.Start,
////            color = MaterialTheme.colorScheme.onSurface
////        )
//        LazyRow(modifier = Modifier.height(200.dp)) {
//            items(items = recentlyPlayedSongs.items) {
//                BrainzPlayerActivityCards(icon = it.albumArt,
//                    errorIcon = R.drawable.ic_artist,
//                    title = it.title,
//                    modifier = Modifier
//                        .clickable {
//                            brainzPlayerViewModel.changePlayable(recentlyPlayedSongs.items, PlayableType.ALL_SONGS, it.mediaID,recentlyPlayedSongs.items.sortedBy { it.discNumber }.indexOf(it),0L)
//                            brainzPlayerViewModel.playOrToggleSong(it, true)
//                        }
//                )
//            }
//        }
//
//        // Songs button
//        Card(
//            modifier = Modifier
//                .padding(16.dp)
//                .clickable {
//                    navigateToSongsScreen()
//                },
//            shape = RoundedCornerShape(16.dp),
//            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
//            elevation = 5.dp
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = "Songs",
//                    modifier = Modifier
//                        .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 24.sp,
//                    textAlign = TextAlign.Start,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Icon(
//                    imageVector = Icons.Rounded.ArrowForwardIos,
//                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
//                    contentDescription = "Navigate to songs screen",
//                    tint = MaterialTheme.colorScheme.onSurface
//                )
//            }
//        }
//        LazyRow(modifier = Modifier.height(200.dp)) {
//            items(items = songs) { song ->
//                BrainzPlayerActivityCards(icon = song.albumArt,
//                    errorIcon = R.drawable.ic_artist,
//                    title = song.title,
//                    modifier = Modifier
//                        .clickable {
//                            brainzPlayerViewModel.changePlayable(
//                                songs.sortedBy { it.discNumber },
//                                PlayableType.ALL_SONGS,
//                                song.mediaID,
//                                songs
//                                    .sortedBy { it.discNumber }
//                                    .indexOf(song)
//                            )
//                            brainzPlayerViewModel.playOrToggleSong(song, true)
//                        }
//                )
//            }
//        }
//
//        // Artists
//        Card(
//            modifier = Modifier
//                .padding(16.dp)
//                .clickable {
//                    navigateToArtistsScreen()
//                },
//            shape = RoundedCornerShape(16.dp),
//            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
//            elevation = 5.dp
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = "Artists",
//                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 24.sp,
//                    textAlign = TextAlign.Start,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Icon(
//                    imageVector = Icons.Rounded.ArrowForwardIos,
//                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
//                    contentDescription = "Navigate to artists screen",
//                    tint = MaterialTheme.colorScheme.onSurface
//                )
//            }
//
//        }
//        LazyRow(modifier = Modifier.height(200.dp)) {
//            items(items = artists) {
//                BrainzPlayerActivityCards(icon = "",
//                    errorIcon = R.drawable.ic_artist,
//                    title = it.name,
//                    modifier = Modifier
//                        .clickable {
//                            navigateToArtist(it.id)
//                        }
//                )
//            }
//        }
//
//
//        // Albums
//        Card(
//            modifier = Modifier
//                .padding(16.dp)
//                .clickable {
//                    navigateToAlbumsScreen()
//                },
//            shape = RoundedCornerShape(16.dp),
//            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
//            elevation = 5.dp
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = "Albums",
//                    modifier = Modifier
//                        .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 24.sp,
//                    textAlign = TextAlign.Start,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Icon(
//                    imageVector = Icons.Rounded.ArrowForwardIos,
//                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
//                    contentDescription = "Navigate to albums screen",
//                    tint = MaterialTheme.colorScheme.onSurface
//                )
//            }
//
//        }
//        LazyRow(modifier = Modifier.height(200.dp)) {
//            items(albums) {
//                BrainzPlayerActivityCards(it.albumArt,
//                    R.drawable.ic_album,
//                    title = it.title,
//                    modifier = Modifier
//                        .clickable {
//                            navigateToAlbum(it.albumId)
//                        }
//                )
//            }
//        }
//
//
//        // Playlists
//        Card(
//            modifier = Modifier
//                .padding(16.dp)
//                .clickable {
//                    navigateToPlaylistsScreen()
//                },
//            shape = RoundedCornerShape(16.dp),
//            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
//            elevation = 5.dp
//        ) {
//            Row(verticalAlignment = Alignment.CenterVertically) {
//                Text(
//                    text = "Playlists",
//                    modifier = Modifier
//                        .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
//                    fontWeight = FontWeight.Bold,
//                    fontSize = 24.sp,
//                    textAlign = TextAlign.Start,
//                    color = MaterialTheme.colorScheme.onSurface
//                )
//                Icon(
//                    imageVector = Icons.Rounded.ArrowForwardIos,
//                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
//                    contentDescription = "Navigate to playlists screen",
//                    tint = MaterialTheme.colorScheme.onSurface
//                )
//            }
//
//        }
//        LazyRow(modifier = Modifier.height(200.dp)) {
//            items(playlists.filter {
//                it.id != (-1).toLong()
//            }) {
//                BrainzPlayerActivityCards(
//                    icon = "",
//                    errorIcon = it.art,
//                    title = it.title,
//                    modifier = Modifier.clickable { navigateToPlaylist(it.id) }
//                )
//            }
//        }
//
//
//    }
}

