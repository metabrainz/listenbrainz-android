package org.listenbrainz.android.ui.screens.brainzplayer.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onPlayNext : (Song) -> Unit,
    onAddToQueue : (Song) -> Unit,
    onAddToExistingPlaylist : (Song) -> Unit,
    onAddToNewPlaylist : (Song) -> Unit,
) {
    val songsStarting  = remember(songs.size) {
        val songsStarting = mutableMapOf<Char, MutableList<Song>>()
        for (i in 0..25) {
            songsStarting['A' + i] = mutableListOf()
        }

        for (i in 1..songs.size) {
            songsStarting[songs[i - 1].title[0]]?.add(songs[i-1])
        }
        songsStarting
    }

    val viewModel: BrainzPlayerViewModel = hiltViewModel()
    val currentlyPlayingSong = viewModel.currentlyPlayingSong.collectAsStateWithLifecycle().value.toSong

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        for (i in 0..25) {
            val startingLetter = ('A' + i)
            if (songsStarting[startingLetter]!!.isNotEmpty()) {
                Column(
                    modifier = Modifier.background(
                        brush = ListenBrainzTheme.colorScheme.gradientBrush
                    ).padding(top = 15.dp, bottom = 15.dp)
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
                    for (j in 1..songsStarting[startingLetter]!!.size) {
                        val song = songsStarting[startingLetter]!![j-1]
                        val coverArt = songsStarting[startingLetter]!![j - 1].albumArt

                        var showDropdown by remember { mutableStateOf(false) }

                        ListenCardSmall(
                            modifier = Modifier.padding(start = 10.dp, end = 10.dp),
                            trackName = songsStarting[startingLetter]!![j - 1].title,
                            artist = songsStarting[startingLetter]!![j - 1].artist,
                            coverArtUrl = coverArt,
                            errorAlbumArt = R.drawable.ic_erroralbumart,
                            onClick = { onPlayIconClick(song, songsStarting[startingLetter]!!) },
                            onDropdownIconClick = {
                                showDropdown = !showDropdown
                            },
                            goToArtistPage = { },
                            isPlaying = song.mediaID == currentlyPlayingSong.mediaID,
                            dropDown = {
                                BrainzPlayerDropDownMenu(
                                    onAddToNewPlaylist = {onAddToNewPlaylist(song)},
                                    onAddToExistingPlaylist = {onAddToExistingPlaylist(song)},
                                    onAddToQueue = {onAddToQueue(song)},
                                    onPlayNext = {onPlayNext(song)},
                                    expanded = showDropdown,
                                    onDismiss = { showDropdown = false },
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