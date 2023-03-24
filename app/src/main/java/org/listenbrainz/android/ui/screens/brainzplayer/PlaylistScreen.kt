package org.listenbrainz.android.ui.screens.brainzplayer

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.drawscope.inset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.launch
import org.listenbrainz.android.R
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.Playlist
import org.listenbrainz.android.service.BrainzPlayerService
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.forwardingPainter
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.util.LBSharedPreferences
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel
import org.listenbrainz.android.viewmodel.PlaylistViewModel


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@ExperimentalMaterial3Api
@Composable
fun PlaylistScreen(
    navHostController: NavHostController,
    context: Context = LocalContext.current
) {
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    val currentSongIndex =
        BrainzPlayerService.playableSongs!!.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID } + 1
    var isFABDialogVisible by rememberSaveable {
        mutableStateOf(false)
    }
    var selectedPlaylistItemIndex by remember {
        mutableStateOf(-1)
    }
    var deletePlaylistState by remember {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    val playlists by playlistViewModel.playlists.collectAsState(initial = listOf())
    var renamePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                isFABDialogVisible = true
            }) {
                Icon(imageVector = Icons.Rounded.Add, contentDescription = "")
            }
        },
       // containerColor = colorResource(id = R.color.app_bg)
    ) {
        // Handling FAB button to add playlist
        if (isFABDialogVisible) {
            var text by rememberSaveable {
                mutableStateOf("")
            }
            AlertDialog(onDismissRequest = {
                isFABDialogVisible = false
            },
                title = {
                    Text(
                        text = "Add Playlist",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
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
                        if (text != "") {
                            coroutineScope.launch {
                                playlistViewModel.createPlaylist(text)
                            }
                            isFABDialogVisible = false
                        }
                    }) {
                        Text(
                            text = "Add",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        isFABDialogVisible = false
                    }
                    ) {
                        Text(
                            text = "Dismiss",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
        //Handling Playlist Rename Dialogs
        if (renamePlaylistDialog && selectedPlaylistItemIndex != -1){
            var renamePlaylistTitle by remember{
               mutableStateOf("")
            }
            AlertDialog(onDismissRequest = { renamePlaylistDialog = false },
                    title = {
                        Text(text = "Rename Playlist")
                    },
                text = {
                    TextField(value = renamePlaylistTitle, onValueChange = {
                        renamePlaylistTitle = it
                    },
                        label = {
                            Text(text = "Edit Playlist Name")
                        })
                },
                confirmButton = {
                    TextButton(onClick = {
                        coroutineScope.launch {
                            playlistViewModel.renamePlaylist(
                                playlists[selectedPlaylistItemIndex],
                                renamePlaylistTitle
                            )
                            renamePlaylistDialog = false
                            selectedPlaylistItemIndex = -1
                        }


                    }) {
                        Text(
                            text = "Rename",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        renamePlaylistDialog = false
                        selectedPlaylistItemIndex = -1

                    }) {
                        Text(
                            text = "Dismiss",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                )
        }
        // Handling Playlist delete dialog
        if (deletePlaylistState && selectedPlaylistItemIndex != -1) {
            AlertDialog(onDismissRequest = {
                deletePlaylistState = false
                selectedPlaylistItemIndex = -1
            },
                title = {
                    Text(
                        text = "Delete Playlist?",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    TextButton(onClick = {

                        val idx = selectedPlaylistItemIndex
                        coroutineScope.launch {

                            playlistViewModel.deletePlaylist(playlists[idx])
                        }
                        deletePlaylistState = false
                        selectedPlaylistItemIndex = -1


                    }) {
                        Text(
                            text = "Delete",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        deletePlaylistState = false
                        selectedPlaylistItemIndex = -1
                    }
                    ) {
                        Text(
                            text = "Dismiss",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }

        LazyVerticalGrid(columns = GridCells.Fixed(2)) {

            items(playlists.filter {
                it.id !=(-1).toLong()
            }) {
                Box(modifier = Modifier
                    .padding(2.dp)
                    .height(240.dp)
                    .width(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        navHostController.navigate("onPlaylistClick/${it.id}")
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
                                model = it.art,
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
                        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                            Text(
                                text = it.title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onSurface
                            )
                            Icon(
                                imageVector = Icons.Rounded.MoreVert,
                                tint = androidx.compose.material3.MaterialTheme.colorScheme.onSurface,
                                contentDescription = "",
                                modifier = Modifier.clickable {
                                    selectedPlaylistItemIndex = playlists.indexOf(it)

                                })
                            DropdownMenu(
                                expanded = selectedPlaylistItemIndex == playlists.indexOf(it),
                                onDismissRequest = {
                                    selectedPlaylistItemIndex = -1
                                    deletePlaylistState = false
                                }) {
                                DropdownMenuItem(text = {
                                    Text(text = "Play")
                                }, onClick = {
                                    if (it.items.isNotEmpty())
                                        brainzPlayerViewModel.playOrToggleSong(it.items[0],true)
                                    else
                                        Toast.makeText(context, "Playlist is empty!", Toast.LENGTH_SHORT).show()
                                })
                                DropdownMenuItem(text = {
                                    Text(text = "Rename")
                                }, onClick = {
                                    renamePlaylistDialog = true
                                    selectedPlaylistItemIndex = playlists.indexOf(it)
                                })
                                DropdownMenuItem(
                                    text = { Text(text = "Play Next") },
                                    onClick = {
                                        BrainzPlayerService.playableSongs.addAll(currentSongIndex, it.items)
                                        brainzPlayerViewModel.changePlayable(
                                            BrainzPlayerService.playableSongs,
                                            PlayableType.ALL_SONGS,
                                            LBSharedPreferences.currentPlayable?.id ?: 0,
                                            BrainzPlayerService.playableSongs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                                ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                        )
                                        brainzPlayerViewModel.queueChanged(
                                            currentlyPlayingSong,
                                            brainzPlayerViewModel.isPlaying.value
                                        )
                                        selectedPlaylistItemIndex = -1
                                    })
                                DropdownMenuItem(
                                    text = { Text(text = "Add to queue") },
                                    onClick = {
                                        BrainzPlayerService.playableSongs.addAll(
                                            BrainzPlayerService.playableSongs.size,
                                            it.items
                                        )
                                        brainzPlayerViewModel.changePlayable(
                                            BrainzPlayerService.playableSongs,
                                            PlayableType.ALL_SONGS,
                                            LBSharedPreferences.currentPlayable?.id ?: 0,
                                            BrainzPlayerService.playableSongs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                                ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                        )
                                        brainzPlayerViewModel.queueChanged(
                                            currentlyPlayingSong,
                                            brainzPlayerViewModel.isPlaying.value
                                        )
                                        selectedPlaylistItemIndex = -1
                                    })
                                if (it.id != (-1).toLong() && it.id != (0).toLong() && it.id != (1).toLong())
                                    DropdownMenuItem(
                                        text = { Text(text = "Delete Playlist") },
                                        onClick = {
                                            selectedPlaylistItemIndex = playlists.indexOf(it)
                                            deletePlaylistState = true
                                        }
                                    )
                            }
                        }
                    }
                }
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun OnPlaylistClickScreen(playlistID: Long) {
    val brainzPlayerViewModel = hiltViewModel<BrainzPlayerViewModel>()
    val playlistViewModel = hiltViewModel<PlaylistViewModel>()
    val selectedPlaylist =
        playlistViewModel.getPlaylistByID(playlistID).collectAsState(Playlist()).value
    var selectedPlaylistItemIndex by remember {
        mutableStateOf(-1)
    }
    val currentlyPlayingSong =
        brainzPlayerViewModel.currentlyPlayingSong.collectAsState().value.toSong
    val currentSongIndex =
        BrainzPlayerService.playableSongs!!.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID } + 1
    LazyColumn {
        items(selectedPlaylist.items) {
            BoxWithConstraints {
                val maxWidth =
                    (maxWidth - 60.dp).coerceAtMost(600.dp)
                Row(
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    val modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp).width(maxWidth)
                    ListenCardSmall(
                        modifier = modifier,
                        releaseName = it.title,
                        artistName = it.artist,
                        coverArtUrl = it.albumArt,
                        imageLoadSize = 200,
                        useSystemTheme = true,
                        errorAlbumArt = R.drawable.ic_erroralbumart
                    ) {
                        brainzPlayerViewModel.changePlayable(
                            selectedPlaylist.items.sortedBy { it.trackNumber },
                            PlayableType.ALBUM,
                            it.albumID,
                            selectedPlaylist.items
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
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "",
                            modifier = Modifier
                                .padding(10.dp)
                                .clickable {
                                    selectedPlaylistItemIndex = selectedPlaylist.items.indexOf(it)
                                }
                        )
                        DropdownMenu(
                            expanded = selectedPlaylistItemIndex == selectedPlaylist.items.indexOf(it),
                            onDismissRequest = {
                                selectedPlaylistItemIndex = -1
                            }) {
                            DropdownMenuItem(
                                text = { Text(text = "Play Next") },
                                onClick = {
                                    BrainzPlayerService.playableSongs.add(currentSongIndex, it)
                                    brainzPlayerViewModel.changePlayable(
                                        BrainzPlayerService.playableSongs,
                                        PlayableType.ALL_SONGS,
                                        LBSharedPreferences.currentPlayable?.id ?: 0,
                                        BrainzPlayerService.playableSongs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                            ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                    )
                                    brainzPlayerViewModel.queueChanged(
                                        currentlyPlayingSong,
                                        brainzPlayerViewModel.isPlaying.value
                                    )
                                    selectedPlaylistItemIndex = -1
                                })
                            DropdownMenuItem(
                                text = { Text(text = "Add to queue") },
                                onClick = {
                                    BrainzPlayerService.playableSongs.add(
                                        BrainzPlayerService.playableSongs.size,
                                        it
                                    )
                                    brainzPlayerViewModel.changePlayable(
                                        BrainzPlayerService.playableSongs,
                                        PlayableType.ALL_SONGS,
                                        LBSharedPreferences.currentPlayable?.id ?: 0,
                                        BrainzPlayerService.playableSongs.indexOfFirst { song -> song.mediaID == currentlyPlayingSong.mediaID }
                                            ?: 0, brainzPlayerViewModel.songCurrentPosition.value
                                    )
                                    brainzPlayerViewModel.queueChanged(
                                        currentlyPlayingSong,
                                        brainzPlayerViewModel.isPlaying.value
                                    )
                                    selectedPlaylistItemIndex = -1
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}