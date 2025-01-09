package org.listenbrainz.android.ui.screens.brainzplayer.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Artist
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.ui.components.BrainzPlayerDropDownMenu
import org.listenbrainz.android.ui.components.BrainzPlayerListenCard
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun ArtistsOverviewScreen(
    artists: List<Artist>,
    onPlayClick : (Artist) -> Unit,
    onPlayNext : (Artist) -> Unit,
    onAddToQueue : (Artist) -> Unit,
    onAddToExistingPlaylist : (Artist) -> Unit,
    onAddToNewPlaylist : (Artist) -> Unit,
    onSongClick: (Song) -> Unit
) {
    val artistsStarting: MutableMap<Char, MutableList<Artist>> = mutableMapOf()
    var dropdownState by remember {
        mutableStateOf(Pair(-1,-1))
    }
    var expandedArtist by remember { mutableStateOf<Artist?>(null) }

    for (i in 0..25) {
        artistsStarting['A' + i] = mutableListOf()
    }

    for (i in 1..artists.size) {
        artistsStarting[artists[i - 1].name[0]]?.add(artists[i - 1])
    }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        for (i in 0..25) {
            val startingLetter: Char = ('A' + i)
            if (artistsStarting[startingLetter]!!.size > 0) {
                Column(
                    modifier = Modifier
                        .background(
                            brush = ListenBrainzTheme.colorScheme.gradientBrush
                        )
                        .padding(top = 15.dp, bottom = 15.dp)
                ) {
                    Text(
                        startingLetter.toString(),
                        modifier = Modifier.padding(start = 10.dp, top = 15.dp, bottom = 15.dp),
                        style = TextStyle(
                            color = ListenBrainzTheme.colorScheme.lbSignature,
                            fontSize = 20.sp,
                            fontFamily = FontFamily(Font(R.font.roboto_bold)),
                        )
                    )
                    for (j in 1..artistsStarting[startingLetter]!!.size) {
                        var coverArt: String? = null
                        val artist = artistsStarting[startingLetter]!![j - 1]
                        if (artist.albums.isNotEmpty())
                            coverArt = artist.albums[0].albumArt
                        Column {
                            BrainzPlayerListenCard(
                                title = artist.name,
                                subTitle = when (artist.songs.size) {
                                    1 -> "1 track"
                                    else -> "${artist.songs.size} tracks"
                                },
                                coverArtUrl = coverArt,
                                errorAlbumArt = R.drawable.ic_artist,
                                onPlayIconClick = {
                                    onPlayClick(artist)
                                },
                                modifier = Modifier
                                    .padding(start = 10.dp, end = 10.dp)
                                    .clickable {
                                        expandedArtist =
                                            if (expandedArtist == artist) null else artist
                                    },
                                dropDown = {
                                    BrainzPlayerDropDownMenu(
                                        onAddToNewPlaylist = { onAddToNewPlaylist(artist) },
                                        onAddToExistingPlaylist = { onAddToExistingPlaylist(artist) },
                                        onAddToQueue = { onAddToQueue(artist) },
                                        onPlayNext = { onPlayNext(artist) },
                                        expanded = dropdownState == Pair(i, j - 1),
                                        onDismiss = { dropdownState = Pair(-1, -1) }
                                    )
                                },
                                onDropdownIconClick = { dropdownState = Pair(i, j - 1) },
                                dropDownState = dropdownState == Pair(i, j - 1)
                            )
                            if (expandedArtist == artist) {
                                Row(modifier = Modifier.fillMaxWidth()) {
                                    Spacer(modifier = Modifier.weight(0.5f))
                                    Column(
                                        modifier = Modifier
                                            .weight(3.5f)
                                            .padding(horizontal = ListenBrainzTheme.paddings.horizontal)
                                    ) {
                                        artist.songs.forEach { song ->
                                            ListenCardSmall(
                                                modifier = Modifier
                                                    .padding(vertical = 6.dp)
                                                    .fillMaxWidth(),
                                                trackName = song.title,
                                                artists = listOf(FeedListenArtist(song.artist, null, "")),
                                                coverArtUrl = song.albumArt,
                                                errorAlbumArt = R.drawable.ic_erroralbumart,
                                                goToArtistPage = {}
                                            ) {
                                                onSongClick(song)
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}