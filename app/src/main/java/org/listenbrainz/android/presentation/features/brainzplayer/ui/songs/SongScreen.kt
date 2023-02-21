package org.listenbrainz.android.presentation.features.brainzplayer.ui.songs

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
import androidx.compose.material3.*
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
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.brainzplayer.PlayableType
import org.listenbrainz.android.presentation.features.brainzplayer.ui.BrainzPlayerViewModel
import org.listenbrainz.android.presentation.features.brainzplayer.ui.components.BPLibraryEmptyMessage
import org.listenbrainz.android.presentation.features.brainzplayer.ui.components.forwardingPainter
import org.listenbrainz.android.presentation.features.brainzplayer.ui.playlist.PlaylistViewModel


@OptIn(ExperimentalMaterialApi::class)
@ExperimentalMaterial3Api
@Composable
fun SongScreen() {
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    val songViewModel = hiltViewModel<SongViewModel>()
    val songs = songViewModel.songs.collectAsState(initial = listOf())
    val coroutineScope = rememberCoroutineScope()
    var songCardMoreOptionsDropMenuExpanded by rememberSaveable { mutableStateOf(-1) }
    var addToNewPlaylistState by remember { mutableStateOf(false) }
    var addToExistingPlaylistState by rememberSaveable { mutableStateOf(false) }
    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
    val playlistsCollectedFromChecklist =
        playlistViewModel.playlistsCollectedFromChecklist.collectAsState().value

    //Handling adding to new playlist dialog
    if (addToNewPlaylistState) {
        var text by rememberSaveable {
            mutableStateOf("")
        }
        AlertDialog(onDismissRequest = {
            addToNewPlaylistState = false
        },
            title = {
                Text(
                    text = "Add Playlist",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.Black
                )
            },
            text = {

                TextField(value = text, onValueChange = {
                    text = it
                },
                    label = {
                        Text(text = "Add Playlist Name")
                    })
            },
            confirmButton = {
                TextButton(onClick = {
                    coroutineScope.launch {
                        val song = songs.value[songCardMoreOptionsDropMenuExpanded]
                        playlistViewModel.addSongToNewPlaylist(song, text)
                    }
                    addToNewPlaylistState = false

                }) {
                    Text(
                        text = "Add",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    addToNewPlaylistState = false
                }
                ) {
                    Text(
                        text = "Cancel",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                }
            }
        )
    }

    if (addToExistingPlaylistState) {
        AlertDialog(
            onDismissRequest = { addToExistingPlaylistState = false },
            title = {
                Text(text = "Select Playlists")
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Start
                ) {
                    items(playlists.filter {
                        it.id != (-1).toLong()
                    }) { playlist ->
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            var checkedPlaylistIndices by remember {
                                mutableStateOf(
                                    false
                                )
                            }
                            Checkbox(checked = checkedPlaylistIndices,
                                onCheckedChange = {
                                    checkedPlaylistIndices = it
                                    playlistsCollectedFromChecklist.add(playlist)
                                })
                            Box(modifier = Modifier.fillParentMaxWidth(0.8f)) {
                                Text(text = playlist.title)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    for (playlist in playlistsCollectedFromChecklist) {
                        val song = songs.value[songCardMoreOptionsDropMenuExpanded]
                        coroutineScope.launch {
                            if (!playlist.items.contains(song)) playlistViewModel.addSongToPlaylist(
                                song,
                                playlist
                            )
                        }
                    }
                    playlistsCollectedFromChecklist.clear()

                    songCardMoreOptionsDropMenuExpanded = -1
                    addToExistingPlaylistState = false
                }) {
                    Text(text = "Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    songCardMoreOptionsDropMenuExpanded = -1
                    addToExistingPlaylistState = false
                }) {
                    Text(text = "Cancel")
                }
            }
        )
    }
    
    val refreshing by songViewModel.isRefreshing.collectAsState()
    val pullRefreshState = rememberPullRefreshState(
        refreshing = refreshing,
        onRefresh = { songViewModel.fetchSongsFromDevice(userRequestedRefresh = true) }
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(state = pullRefreshState)
    ) {
        if (songs.value.isEmpty()){
            BPLibraryEmptyMessage(modifier = Modifier.align(Alignment.Center))
        } else {
            LazyVerticalGrid(columns = GridCells.Fixed(2)) {
                items(songs.value.sortedBy { it.discNumber }) {
                    Box(modifier = Modifier
                        .padding(2.dp)
                        .height(240.dp)
                        .width(200.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable {
                            brainzPlayerViewModel.changePlayable(
                                songs.value.sortedBy { it.discNumber },
                                PlayableType.ALL_SONGS,
                                it.mediaID,
                                songs.value
                                    .sortedBy { it.discNumber }
                                    .indexOf(it)
                            )
                            brainzPlayerViewModel.playOrToggleSong(it, true)
                        }
                    ) {
                        DropdownMenu(
                            expanded = songCardMoreOptionsDropMenuExpanded == songs.value.indexOf(it),
                            onDismissRequest = {
                                songCardMoreOptionsDropMenuExpanded = -1
                                addToNewPlaylistState = false
                                addToExistingPlaylistState = false
                            }) {
                            DropdownMenuItem(
                                text = { Text(text = "Add to new playlist") },
                                onClick = { addToNewPlaylistState = true })
                            DropdownMenuItem(
                                text = { Text(text = "Add to existing playlist") },
                                onClick = { addToExistingPlaylistState = true })
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
                                        songCardMoreOptionsDropMenuExpanded = songs.value.indexOf(it)
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
        // End of songs list
        
        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = refreshing,
            state = pullRefreshState
        )
    }
    
}
