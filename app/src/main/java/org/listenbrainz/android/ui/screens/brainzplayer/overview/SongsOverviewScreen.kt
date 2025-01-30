package org.listenbrainz.android.ui.screens.brainzplayer.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Artist
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

            withContext(Dispatchers.Main) {
                songsStarting.value = groupedSongs
            }
        }
    }

    val viewModel: BrainzPlayerViewModel = hiltViewModel()
    val currentlyPlayingSong = viewModel.currentlyPlayingSong.collectAsStateWithLifecycle().value.toSong

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = ListenBrainzTheme.colorScheme.gradientBrush)
    ) {
        songsStarting.value.forEach {
            (startingLetter, songList) ->
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

                        songList.forEachIndexed { _, song ->
                            val coverArt = song.albumArt
                            var showDropdown by remember { mutableStateOf(false) }

                            ListenCardSmall(
                                modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                                trackName = song.title,
                                artist = song.artist,
                                coverArtUrl = coverArt,
                                errorAlbumArt = R.drawable.ic_erroralbumart,
                                onClick = {
                                    CoroutineScope(Dispatchers.Default).launch {
                                        onPlayIconClick(song, songList)
                                    }
                                },
                                onDropdownIconClick = {
                                    showDropdown = !showDropdown
                                },
                                goToArtistPage = { },
                                isPlaying = song.mediaID == currentlyPlayingSong.mediaID,
                                dropDown = {
                                    BrainzPlayerDropDownMenu(
                                        onAddToNewPlaylist = {
                                            CoroutineScope(Dispatchers.Default).launch {
                                                onAddToNewPlaylist(song)
                                            }
                                        },
                                        onAddToExistingPlaylist = {
                                            CoroutineScope(Dispatchers.Default).launch {
                                                onAddToExistingPlaylist(song)
                                            }
                                        },
                                        onAddToQueue = {
                                            CoroutineScope(Dispatchers.Default).launch {
                                                onAddToQueue(song)
                                            }
                                        },
                                        onPlayNext = {
                                            CoroutineScope(Dispatchers.Default).launch {
                                                onPlayNext(song)
                                            }
                                        },
                                        expanded = showDropdown,
                                        onDismiss = { showDropdown = false }
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                }
            }
        }
    }
}
