package org.listenbrainz.android.ui.screens.brainzplayer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
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
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.ui.components.BPLibraryEmptyMessage
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.forwardingPainter
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.AlbumViewModel
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlbumScreen(navigateToAlbum: (id: Long) -> Unit) {
    val albumViewModel = hiltViewModel<AlbumViewModel>()
    val albums = albumViewModel.albums.collectAsState(listOf())
    val refreshing by albumViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { albumViewModel.fetchAlbumsFromDevice(userRequestedRefresh = true) }
    )

    // Content
    Box(modifier = Modifier
        .fillMaxSize()
        .pullRefresh(state = pullRefreshState)
    ) {
        if (albums.value.isEmpty()){
            BPLibraryEmptyMessage(modifier = Modifier.align(Alignment.Center))
        } else {
            AlbumsList(albums, navigateToAlbum)
        }
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState
        )
    }
}


@Composable
private fun AlbumsList(
    albums: State<List<Album>>,
    navigateToAlbum: (id: Long) -> Unit
) {
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    var albumCardMoreOptionsDropMenuExpanded by rememberSaveable { mutableStateOf(-1) }
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    val albumViewModel = hiltViewModel<AlbumViewModel>()

    val currentSongIndex =
        albumViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
            ?.plus(1)
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(albums.value) {
            val albumSongs = albumViewModel.getAllSongsOfAlbum(it.albumId).collectAsState(listOf()).value
            Box(modifier = Modifier
                .padding(2.dp)
                .height(240.dp)
                .width(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    navigateToAlbum(it.albumId)
                }
            ) {
                DropdownMenu(
                    expanded = albumCardMoreOptionsDropMenuExpanded == albums.value.indexOf(it),
                    onDismissRequest = {
                        albumCardMoreOptionsDropMenuExpanded = -1
                    }) {
                    DropdownMenuItem(
                        text = { Text(text = "Play Next") },
                        onClick = {
                            if (currentSongIndex != null) {
                                albumViewModel.appPreferences.currentPlayable?.songs?.toMutableList()?.addAll(currentSongIndex, albumSongs)
                            }
                            brainzPlayerViewModel.changePlayable(
                                albumViewModel.appPreferences.currentPlayable?.songs?.toMutableList() ?: mutableListOf(),
                                PlayableType.ALL_SONGS,
                                albumViewModel.appPreferences.currentPlayable?.id ?: 0,
                                albumViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                    ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                            )
                            brainzPlayerViewModel.queueChanged(
                                currentlyPlayingSong,
                                brainzPlayerViewModel.isPlaying.value
                            )
                            albumCardMoreOptionsDropMenuExpanded = -1
                        })
                    DropdownMenuItem(
                        text = { Text(text = "Add to queue") },
                        onClick = {
                            albumViewModel.appPreferences.currentPlayable?.songs?.size?.let { it1 ->
                                albumViewModel.appPreferences.currentPlayable?.songs?.toMutableList()?.addAll(
                                    it1,
                                    albumSongs
                                )
                            }
                            albumViewModel.appPreferences.currentPlayable?.songs?.let { it1 ->
                                brainzPlayerViewModel.changePlayable(
                                    it1,
                                    PlayableType.ALL_SONGS,
                                    albumViewModel.appPreferences.currentPlayable?.id ?: 0,
                                    albumViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
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
                            .size(150.dp)
                    ) {
                        AsyncImage(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.TopCenter)
                                .background(colorResource(id = R.color.bp_bottom_song_viewpager)),
                            model = it.albumArt,
                            contentDescription = "",
                            error = forwardingPainter(
                                painter = painterResource(id = R.drawable.ic_song)
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
                                albumCardMoreOptionsDropMenuExpanded = albums.value.indexOf(it)
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
                    Text(
                        text = it.artist,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
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
fun OnAlbumClickScreen(albumID: Long) {
    val albumViewModel = hiltViewModel<AlbumViewModel>()
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    val selectedAlbum =
        albumViewModel.getAlbumFromID(albumID).collectAsState(initial = Album()).value
    val albumSongs = albumViewModel.getAllSongsOfAlbum(albumID).collectAsState(listOf()).value
    var albumCardMoreOptionsDropMenuExpanded by rememberSaveable { mutableStateOf(-1) }
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    val currentSongIndex =
        albumViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
            ?.plus(1)
    LazyColumn {
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colorResource(id = R.color.app_bg))
            ) {
                AsyncImage(
                    model = selectedAlbum.albumArt,
                    contentDescription = "",
                    error = painterResource(
                        id = R.drawable.ic_erroralbumart
                    ),
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .size(300.dp)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(10.dp))
                )
            }
        }
        item {
            Column(
                Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = selectedAlbum.title,
                    color = colorResource(id = R.color.text),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = selectedAlbum.artist,
                    color = colorResource(id = R.color.text),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
        items(items = albumSongs.sortedBy { it.trackNumber }) {
            
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
                        albumCardMoreOptionsDropMenuExpanded = albumSongs.indexOf(it)
                    }
                ) {
                    brainzPlayerViewModel.changePlayable(
                        albumSongs.sortedBy { it.trackNumber },
                        PlayableType.ALBUM,
                        it.albumID,
                        albumSongs
                            .sortedBy { it.trackNumber }
                            .indexOf(it),
                        0L
                    )
                    brainzPlayerViewModel.playOrToggleSong(it, true)
                }
                androidx.compose.material3.Surface(
                    shape = RoundedCornerShape(5.dp),
                    shadowElevation = 5.dp
                ) {
                    DropdownMenu(
                        expanded = albumCardMoreOptionsDropMenuExpanded == albumSongs.indexOf(it),
                        onDismissRequest = {
                            albumCardMoreOptionsDropMenuExpanded = -1
                        }) {
                        DropdownMenuItem(
                            text = { Text(text = "Play Next") },
                            onClick = {
                                if (currentSongIndex != null) {
                                    albumViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                                        ?.add(currentSongIndex, it)
                                }
                                albumViewModel.appPreferences.currentPlayable?.songs?.let { it1 ->
                                    brainzPlayerViewModel.changePlayable(
                                        it1,
                                        PlayableType.ALL_SONGS,
                                        albumViewModel.appPreferences.currentPlayable?.id ?: 0,
                                        albumViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
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
                                albumViewModel.appPreferences.currentPlayable?.songs?.size?.let { it1 ->
                                    albumViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                                        ?.add(
                                            it1,
                                            it
                                        )
                                }
                                albumViewModel.appPreferences.currentPlayable?.songs?.toMutableList()
                                    ?.let { it1 ->
                                        brainzPlayerViewModel.changePlayable(
                                            it1,
                                            PlayableType.ALL_SONGS,
                                            albumViewModel.appPreferences.currentPlayable?.id ?: 0,
                                            albumViewModel.appPreferences.currentPlayable?.songs?.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                                ?: 0,
                                            brainzPlayerViewModel.songCurrentPosition.value
                                        )
                                    }
                                brainzPlayerViewModel.queueChanged(
                                    currentlyPlayingSong,
                                    brainzPlayerViewModel.isPlaying.value
                                )
                                albumCardMoreOptionsDropMenuExpanded = -1
                            }
                        )
                    }
                }
            }
        }
    }
}