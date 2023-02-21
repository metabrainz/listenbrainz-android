package org.listenbrainz.android.presentation.features.brainzplayer.ui.album

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.brainzplayer.Album
import org.listenbrainz.android.data.sources.brainzplayer.PlayableType
import org.listenbrainz.android.presentation.features.brainzplayer.ui.BrainzPlayerViewModel
import org.listenbrainz.android.presentation.features.brainzplayer.ui.components.BPLibraryEmptyMessage
import org.listenbrainz.android.presentation.features.brainzplayer.ui.components.forwardingPainter


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AlbumScreen(navHostController: NavHostController) {
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
            AlbumsList(albums, navHostController)
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
    navHostController: NavHostController
) {
    LazyVerticalGrid(columns = GridCells.Fixed(2)) {
        items(albums.value) {
            Box(modifier = Modifier
                .padding(2.dp)
                .height(240.dp)
                .width(200.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable {
                    navHostController.navigate("onAlbumClick/${it.albumId}")
                }
            ) {
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
                    }
                    Text(
                        text = it.title,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colorResource(
                            id = R.color.white
                        )
                    )
                    Text(
                        text = it.artist,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = colorResource(
                            id = R.color.white
                        )
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
                    color = colorResource(id = R.color.white),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = selectedAlbum.artist,
                    color = colorResource(id = R.color.white),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
        items(items = albumSongs.sortedBy { it.trackNumber }) {
            Card(
                modifier = Modifier
                    .padding(10.dp)
                    .fillMaxWidth(0.98f)
                    .clickable {
                        brainzPlayerViewModel.changePlayable(
                            albumSongs.sortedBy { it.trackNumber },
                            PlayableType.ALBUM,
                            it.albumID,
                            albumSongs
                                .sortedBy { it.trackNumber }
                                .indexOf(it)
                        )
                        brainzPlayerViewModel.playOrToggleSong(it)
                    }
                ,
                backgroundColor = MaterialTheme.colors.onSurface
            ) {
                Spacer(modifier = Modifier.height(50.dp))
                Row(horizontalArrangement = Arrangement.Start) {
                    AsyncImage(
                        model = it.albumArt,
                        contentDescription = "",
                        error = painterResource(
                            id = R.drawable.ic_erroralbumart
                        ),
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.size(70.dp)
                    )
                    Column(Modifier.padding(start = 10.dp)) {
                        androidx.compose.material.Text(
                            text = it.title,
                            color = Color.White
                        )
                        androidx.compose.material.Text(
                            text = it.artist,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}