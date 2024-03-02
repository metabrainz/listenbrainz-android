package org.listenbrainz.android.ui.screens.brainzplayer


import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.ArrowForwardIos
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.listenbrainz.android.R
import org.listenbrainz.android.model.*
import org.listenbrainz.android.ui.components.forwardingPainter
import org.listenbrainz.android.ui.screens.brainzplayer.navigation.Navigation
import org.listenbrainz.android.viewmodel.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrainzPlayerScreen() {
    // View models
    val albumViewModel = hiltViewModel<AlbumViewModel>()
    val songsViewModel = hiltViewModel<SongViewModel>()
    val artistViewModel = hiltViewModel<ArtistViewModel>()
    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    
    // Data streams
    val albums = albumViewModel.albums.collectAsState(initial = listOf()).value     // TODO: Introduce initial values to avoid flicker.
    val songs = songsViewModel.songs.collectAsState(initial = listOf()).value
    val artists = artistViewModel.artists.collectAsState(initial = listOf()).value
    val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
    val recentlyPlayed = Playlist.recentlyPlayed
    
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Navigation(albums, artists, playlists, recentlyPlayed, songs)
    }
}


@Composable
fun BrainzPlayerHomeScreen(
    songs : List<Song>,
    albums: List<Album>,
    artists: List<Artist>,
    playlists: List<Playlist>,
    recentlyPlayedSongs: Playlist,
    brainzPlayerViewModel: BrainzPlayerViewModel = hiltViewModel(),
    navigateToSongsScreen: () -> Unit,
    navigateToArtistsScreen: () -> Unit,
    navigateToAlbumsScreen: () -> Unit,
    navigateToPlaylistsScreen: () -> Unit,
    navigateToArtist: (id: Long) -> Unit,
    navigateToAlbum: (id: Long) -> Unit,
    navigateToPlaylist: (id: Long) -> Unit
) {
    val searchTextState = remember {
        mutableStateOf(TextFieldValue(""))
    }

    Column(modifier = Modifier
        .padding(horizontal = 8.dp)
        .verticalScroll(rememberScrollState())
    ) {
        // Recently Played
        Text(
            text = "Recently Played",
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            textAlign = TextAlign.Start,
            color = MaterialTheme.colorScheme.onSurface
        )
        LazyRow(modifier = Modifier.height(200.dp)) {
            items(items = recentlyPlayedSongs.items) {
                BrainzPlayerActivityCards(icon = it.albumArt,
                    errorIcon = R.drawable.ic_artist,
                    title = it.title,
                    modifier = Modifier
                        .clickable {
                            brainzPlayerViewModel.changePlayable(recentlyPlayedSongs.items, PlayableType.ALL_SONGS, it.mediaID,recentlyPlayedSongs.items.sortedBy { it.discNumber }.indexOf(it),0L)
                            brainzPlayerViewModel.playOrToggleSong(it, true)
                        }
                )
            }
        }
        
        // Songs button
        Card(
            modifier = Modifier.padding(16.dp).clickable {
                navigateToSongsScreen()
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            elevation = 5.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Songs",
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
                    contentDescription = "Navigate to songs screen",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
        LazyRow(modifier = Modifier.height(200.dp)) {
            items(items = songs) { song ->
                BrainzPlayerActivityCards(icon = song.albumArt,
                    errorIcon = R.drawable.ic_artist,
                    title = song.title,
                    modifier = Modifier
                        .clickable {
                            brainzPlayerViewModel.changePlayable(
                                songs.sortedBy { it.discNumber },
                                PlayableType.ALL_SONGS,
                                song.mediaID,
                                songs
                                    .sortedBy { it.discNumber }
                                    .indexOf(song)
                            )
                            brainzPlayerViewModel.playOrToggleSong(song, true)
                        }
                )
            }
        }
        
        // Artists
        Card(
            modifier = Modifier.padding(16.dp).clickable {
                navigateToArtistsScreen()
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            elevation = 5.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Artists",
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
                    contentDescription = "Navigate to artists screen",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
        }
        LazyRow(modifier = Modifier.height(200.dp)) {
            items(items = artists) {
                BrainzPlayerActivityCards(icon = "",
                    errorIcon = R.drawable.ic_artist,
                    title = it.name,
                    modifier = Modifier
                        .clickable {
                            navigateToArtist(it.id)
                        }
                )
            }
        }
        
        
        // Albums
        Card(
            modifier = Modifier.padding(16.dp).clickable {
                navigateToAlbumsScreen()
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            elevation = 5.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Albums",
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
                    contentDescription = "Navigate to albums screen",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
        }
        LazyRow(modifier = Modifier.height(200.dp)) {
            items(albums) {
                BrainzPlayerActivityCards(it.albumArt,
                    R.drawable.ic_album,
                    title = it.title,
                    modifier = Modifier
                        .clickable {
                            navigateToAlbum(it.albumId)
                        }
                )
            }
        }
        
        
        // Playlists
        Card(
            modifier = Modifier.padding(16.dp).clickable {
                navigateToPlaylistsScreen()
            },
            shape = RoundedCornerShape(16.dp),
            backgroundColor = MaterialTheme.colorScheme.tertiaryContainer,
            elevation = 5.dp
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Playlists",
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 16.dp, start = 16.dp),
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Start,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Rounded.ArrowForwardIos,
                    modifier = Modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp, start = 8.dp),
                    contentDescription = "Navigate to playlists screen",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
        }
        LazyRow(modifier = Modifier.height(200.dp)) {
            items(playlists.filter {
                it.id != (-1).toLong()
            }) {
                BrainzPlayerActivityCards(
                    icon = "",
                    errorIcon = it.art,
                    title = it.title,
                    modifier = Modifier.clickable { navigateToPlaylist(it.id) }
                )
            }
        }
        
        
    }
}

@Composable
fun BrainzPlayerActivityCards(icon: String, errorIcon : Int, title: String, modifier : Modifier = Modifier) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .height(200.dp)
            .width(180.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable { },
        contentAlignment = Alignment.TopCenter
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = modifier
                    .padding(10.dp)
                    .clip(CircleShape)
                    .background(color = colorResource(id = R.color.bp_bottom_song_viewpager))
                    .size(150.dp)
            ) {
                AsyncImage(
                    modifier = modifier
                        .fillMaxSize()
                        .align(Alignment.TopCenter)
                        .clip(CircleShape),
                    model = icon,
                    contentDescription = "",
                    error = forwardingPainter(
                        painter = painterResource(id = errorIcon)
                    ) { info ->
                        inset(25f, 25f) {
                            with(info.painter) {
                                draw(size, info.alpha, info.colorFilter)
                            }
                        }
                    },
                    contentScale = ContentScale.Crop
                )
            }
            Text(
                text = title,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}