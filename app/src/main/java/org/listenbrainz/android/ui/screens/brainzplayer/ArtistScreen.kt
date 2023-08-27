package org.listenbrainz.android.ui.screens.brainzplayer

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.ui.components.BPLibraryEmptyMessage
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.forwardingPainter
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.AlbumViewModel
import org.listenbrainz.android.viewmodel.ArtistViewModel
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArtistScreen(navigateToArtistScreen: (id: Long) -> Unit) {
    val artistViewModel = hiltViewModel<ArtistViewModel>()
    val artists = artistViewModel.artists.collectAsState(initial = listOf())

    val refreshing by artistViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { artistViewModel.fetchArtistsFromDevice(userRequestedRefresh = true) }
    )
    
    // Content
    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(state = pullRefreshState),
        contentAlignment = Alignment.Center
    ) {
        if (refreshing) {
            Text(text = "Preparing your artists.", color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface)
        } else if (artists.value.isEmpty()){
            BPLibraryEmptyMessage()
        } else {
            ArtistsScreen(artists = artists, navigateToArtistScreen = navigateToArtistScreen)
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState
        )
    }
    
}

@Composable
private fun ArtistsScreen(
    artists: State<List<Artist>>,
    navigateToArtistScreen: (id: Long) -> Unit
) {
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    var artistCardMoreOptionsDropMenuExpanded by rememberSaveable { mutableStateOf(-1) }
    val currentSongIndex =
        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID==currentlyPlayingSong.mediaID   }
            ?.plus(1)
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(artists.value) {artist ->
            Box(modifier = Modifier
                .padding(2.dp)
                .height(220.dp)
                .width(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    navigateToArtistScreen(artist.id)
                }
            ) {
                DropdownMenu(
                    expanded = artistCardMoreOptionsDropMenuExpanded == artists.value.indexOf(artist),
                    onDismissRequest = {
                        artistCardMoreOptionsDropMenuExpanded = -1
                    }) {
                    DropdownMenuItem(
                        text = { Text(text = "Play Next") },
                        onClick = {
                            if (currentSongIndex != null) {
                                brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()?.addAll(currentSongIndex, artist.songs)
                            }
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
                            artistCardMoreOptionsDropMenuExpanded = -1
                        })
                    DropdownMenuItem(
                        text = { Text(text = "Add to queue") },
                        onClick = {
                            brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.size?.let {
                                brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()?.addAll(
                                    it,artist.songs)
                            }
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
                            artistCardMoreOptionsDropMenuExpanded = -1
                        })
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .padding(10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .size(150.dp)
                    ) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.TopCenter)
                                .background(colorResource(id = R.color.bp_bottom_song_viewpager)),
                            imageVector = Icons.Default.Person,
                            colorFilter = ColorFilter.tint(colorResource(id = R.color.gray)),
                            contentDescription = "",
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier
                            .size(50.dp)
                            .padding(5.dp)
                            .clip(CircleShape)
                            .background(Color.LightGray)
                            .clickable {
                                artistCardMoreOptionsDropMenuExpanded = artists.value.indexOf(artist)
                            }
                            .align(Alignment.BottomEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = Icons.Rounded.Add, "")
                        }
                    }
                    Text(
                        text = artist.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
fun OnArtistClickScreen(artistID: String, navigateToAlbum: (id: Long) -> Unit) {
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    val artistViewModel = hiltViewModel<ArtistViewModel>()
    val albumViewModel = hiltViewModel<AlbumViewModel>()
    val artist = artistViewModel.getArtistByID(artistID).collectAsState(initial = Artist()).value
    val artistAlbums =
        artistViewModel.getAllAlbumsOfArtist(artist).collectAsState(initial = listOf()).value.distinctBy { it.albumId }
    val artistSongs =
        artistViewModel.getAllSongsOfArtist(artist).collectAsState(initial = listOf()).value.distinctBy { it.mediaID }
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    var artistCardMoreOptionsDropMenuExpanded by rememberSaveable { mutableStateOf(-1) }
    var albumCardMoreOptionsDropMenuExpanded by rememberSaveable { mutableStateOf(-1) }
    val currentSongIndex =
        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
            ?.plus(1)

    LazyColumn {
        item {
            Text(
                text = "Albums by the Artist",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        }
        item {
            LazyRow {
                items(items = artistAlbums) {
                    val albumSongs = albumViewModel.getAllSongsOfAlbum(it.albumId).collectAsState(listOf()).value
                    Box(
                        modifier = Modifier
                            .height(240.dp)
                            .width(200.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .clickable {
                                navigateToAlbum(it.albumId)
                            },
                        contentAlignment = Alignment.TopCenter
                    ) {
                        DropdownMenu(
                            expanded = albumCardMoreOptionsDropMenuExpanded == artistAlbums.indexOf(it),
                            onDismissRequest = {
                                albumCardMoreOptionsDropMenuExpanded = -1
                            }) {
                            DropdownMenuItem(
                                text = { Text(text = "Play Next") },
                                onClick = {
                                    if (currentSongIndex != null) {
                                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()?.addAll(currentSongIndex, albumSongs)
                                    }
                                    brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let { it1 ->
                                        brainzPlayerViewModel.changePlayable(
                                            it1,
                                            PlayableType.ALL_SONGS,
                                            brainzPlayerViewModel.appPreferences.currentPlayable?.id ?: 0,
                                            brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                                ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                        )
                                    }
                                    brainzPlayerViewModel.queueChanged(
                                        currentlyPlayingSong,
                                        brainzPlayerViewModel.isPlaying.value
                                    )
                                    albumCardMoreOptionsDropMenuExpanded = -1
                                })
                            DropdownMenuItem(
                                text = { Text(text = "Add to queue") },
                                onClick = {
                                    brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.size?.let { it1 ->
                                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()?.addAll(
                                            it1,
                                            albumSongs
                                        )
                                    }
                                    brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let { it1 ->
                                        brainzPlayerViewModel.changePlayable(
                                            it1,
                                            PlayableType.ALL_SONGS,
                                            brainzPlayerViewModel.appPreferences.currentPlayable?.id ?: 0,
                                            brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                                ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                        )
                                    }
                                    brainzPlayerViewModel.queueChanged(
                                        currentlyPlayingSong,
                                        brainzPlayerViewModel.isPlaying.value
                                    )
                                    albumCardMoreOptionsDropMenuExpanded = -1
                                })
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(color = colorResource(id = R.color.bp_bottom_song_viewpager))
                                    .size(150.dp)
                            ) {
                                AsyncImage(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .align(Alignment.TopCenter)
                                        .clip(RoundedCornerShape(10.dp)),
                                    model = it.albumArt,
                                    contentDescription = "",
                                    error = forwardingPainter(
                                        painter = painterResource(id = R.drawable.ic_album)
                                    ) { info ->
                                        inset(25f, 25f) {
                                            with(info.painter) {
                                                draw(size, info.alpha, info.colorFilter)
                                            }
                                        }
                                    },
                                    contentScale = ContentScale.Crop
                                )
                                Box(modifier = Modifier
                                    .size(50.dp)
                                    .padding(5.dp)
                                    .clip(CircleShape)
                                    .background(Color.LightGray)
                                    .clickable {
                                        albumCardMoreOptionsDropMenuExpanded = artistAlbums.indexOf(it)
                                    }
                                    .align(Alignment.BottomEnd),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Rounded.Add, "")
                                }
                            }
                            Text(
                                text = it.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Songs by the Artist",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
            )
        }
        items(artistSongs) {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
    
                ListenCardSmall(
                    modifier = Modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    trackName = it.title,
                    artistName = it.artist,
                    coverArtUrl = it.albumArt,
                    errorAlbumArt = R.drawable.ic_erroralbumart,
                    enableDropdownIcon = true,
                    onDropdownIconClick = {
                        artistCardMoreOptionsDropMenuExpanded = artistSongs.indexOf(it)
                    }
                ) {
                    brainzPlayerViewModel.changePlayable(
                        artistSongs,
                        PlayableType.ARTIST,
                        it.artistId,
                        artistSongs.indexOf(it),
                        0L
                    )
                    brainzPlayerViewModel.playOrToggleSong(it, true)
                }
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(5.dp),
                    shadowElevation = 5.dp
                ) {
                    DropdownMenu(
                        expanded = artistCardMoreOptionsDropMenuExpanded == artistSongs.indexOf(it),
                        onDismissRequest = {
                            artistCardMoreOptionsDropMenuExpanded = -1
                        }) {
                        DropdownMenuItem(
                            text = { Text(text = "Play Next") },
                            onClick = {
                                if (currentSongIndex != null) {
                                    brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                                        ?.add(currentSongIndex, it)
                                }
                                brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let { it1 ->
                                    brainzPlayerViewModel.changePlayable(
                                        it1,
                                        PlayableType.ALL_SONGS,
                                        brainzPlayerViewModel.appPreferences.currentPlayable?.id
                                            ?: 0,
                                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                            ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                    )
                                }
                                brainzPlayerViewModel.queueChanged(
                                    currentlyPlayingSong,
                                    brainzPlayerViewModel.isPlaying.value
                                )
                                artistCardMoreOptionsDropMenuExpanded = -1
                            })
                        DropdownMenuItem(
                            text = { Text(text = "Add to queue") },
                            onClick = {
                                brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.size?.let { it1 ->
                                    brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                                        ?.add(
                                            it1,
                                            it
                                        )
                                }
                                brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.let { it1 ->
                                    brainzPlayerViewModel.changePlayable(
                                        it1,
                                        PlayableType.ALL_SONGS,
                                        brainzPlayerViewModel.appPreferences.currentPlayable?.id
                                            ?: 0,
                                        brainzPlayerViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                            ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                    )
                                }
                                brainzPlayerViewModel.queueChanged(
                                    currentlyPlayingSong,
                                    brainzPlayerViewModel.isPlaying.value
                                )
                                artistCardMoreOptionsDropMenuExpanded = -1
                            }
                        )
                    }
                }
            }

        }
    }
}