package org.listenbrainz.android.ui.screens.brainzplayer.overview

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.androidx.compose.koinViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.listenbrainz.android.R
import org.listenbrainz.android.application.App.Companion.context
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.components.BrainzPlayerDropDownMenu
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun SongsOverviewScreen(
    songs: List<Song>,
    onPlayIconClick: (Song, List<Song>) -> Unit,
    onPlayNext: (Song) -> Unit,
    onAddToQueue: (Song) -> Unit,
    onAddToExistingPlaylist: (Song) -> Unit,
    onAddToNewPlaylist: (Song) -> Unit,
) {
    val songsStarting = remember { mutableStateOf<Map<Char, List<Song>>>(emptyMap()) }

    LaunchedEffect(songs) {
        withContext(Dispatchers.Default) {
            val groupedSongs = mutableMapOf<Char, MutableList<Song>>()
            for (i in 0..25) {
                groupedSongs['A' + i] = mutableListOf()
            }

            songs.forEach { song ->
                val firstChar = song.title.firstOrNull()?.uppercaseChar() ?: '#'
                groupedSongs[firstChar]?.add(song)
            }

            songsStarting.value = groupedSongs
        }
    }

    val viewModel: BrainzPlayerViewModel = koinViewModel()
    val currentlyPlayingSong =
        viewModel.currentlyPlayingSong.collectAsStateWithLifecycle().value.toSong

    val areSongsLoaded by remember {
        derivedStateOf {
            songsStarting.value.isNotEmpty()
        }
    }

    AnimatedContent(
        modifier = Modifier.fillMaxSize(),
        targetState = areSongsLoaded,
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { areSongsLoaded ->
        if (areSongsLoaded) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
            ) {
                songsStarting.value.forEach { (startingLetter, songList) ->
                    if (songList.isNotEmpty()) {
                        item {
                            Column(
                                modifier = Modifier.padding(top = 15.dp, bottom = 15.dp)
                            ) {
                                Text(
                                    startingLetter.toString(),
                                    modifier = Modifier.padding(start = 10.dp, top = 10.dp, bottom = 5.dp),
                                    style = TextStyle(
                                        color = ListenBrainzTheme.colorScheme.lbSignature,
                                        fontSize = 20.sp,
                                        fontFamily = FontFamily(Font(R.font.roboto_bold)),
                                    )
                                )
                            }
                        }

                        items(songList) { song ->
                            val coverArt = song.albumArt
                            var showDropdown by remember { mutableStateOf(false) }
                            ListenCardSmall(
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                                trackName = song.title,
                                artist = song.artist,
                                coverArtUrl = coverArt,
                                errorAlbumArt = R.drawable.ic_erroralbumart,
                                onClick = {
                                    onPlayIconClick(song, songList)
                                },
                                onDropdownIconClick = {
                                    showDropdown = !showDropdown
                                },
                                goToArtistPage = { },
                                isPlaying = song.mediaID == currentlyPlayingSong.mediaID,
                                dropDown = {
                                    BrainzPlayerDropDownMenu(
                                        onAddToNewPlaylist = {
                                            onAddToNewPlaylist(song)
                                        },
                                        onAddToExistingPlaylist = {
                                            onAddToExistingPlaylist(song)
                                        },
                                        onAddToQueue = {
                                            onAddToQueue(song)
                                        },
                                        onPlayNext = {
                                            onPlayNext(song)
                                        },
                                        onShareAudio = {
                                            val uri = Uri.parse(song.uri)
                                            shareAudio(context,uri)
                                        },
                                        expanded = showDropdown,
                                        onDismiss = { showDropdown = false },
                                        showShareOption = true
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        } else {
            Box(
                modifier =  Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    modifier = Modifier.padding(12.dp),
                    text = "Loading songs...",
                    color = ListenBrainzTheme.colorScheme.text
                )
            }
        }
    }
}
fun shareAudio(context: Context, audioUri: Uri) {
    if (audioUri != Uri.EMPTY) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "audio/*"
            putExtra(Intent.EXTRA_STREAM, audioUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.applicationContext.startActivity(Intent.createChooser(shareIntent, "Share audio").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }
}

