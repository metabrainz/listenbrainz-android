package org.listenbrainz.android.ui.screens.brainzplayer.overview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Album
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import org.listenbrainz.android.ui.components.BrainzPlayerDropDownMenu

@Composable
fun AlbumsOverViewScreen(
    albums: List<Album>,
    onPlayIconClick: (Album) -> Unit,
    onAddToNewPlaylist: ((Album) -> Unit)?,
    onPlayNext: ((Album) -> Unit)?,
    onAddToQueue: ((Album) -> Unit)?,
    onAddToExistingPlaylist: ((Album) -> Unit)?,
) {

    val albumSections = remember(albums) {
        albums
            .groupBy {
                it.title.firstOrNull()
                    ?.uppercaseChar()
                    ?.takeIf { c -> c.isLetter() } ?: '#'
            }
            .toSortedMap(compareBy<Char> { it == '#' }.thenBy { it })
            .entries
            .mapIndexed { sectionIndex, entry ->
                Triple(sectionIndex, entry.key, entry.value)
            }
    }
    val dropdownState: MutableState<Pair<Int, Int>> =
        remember { mutableStateOf(Pair(-1, -1)) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ListenBrainzTheme.colorScheme.gradientBrush)
    ) {

        albumSections.forEach { (sectionChosen, startingLetter, albumList) ->
            item {
                Text(
                    text = startingLetter.toString(),
                    modifier = Modifier.padding(start = 10.dp, top = 15.dp, bottom = 10.dp),
                    style = TextStyle(
                        color = ListenBrainzTheme.colorScheme.lbSignature,
                        fontSize = 20.sp,
                        fontFamily = FontFamily(Font(R.font.roboto_bold))
                    )
                )
            }
            itemsIndexed(albumList) { index, album ->
                ListenCardSmall(
                    trackName = album.title,
                    artist = album.artist,
                    coverArtUrl = album.albumArt,
                    errorAlbumArt = R.drawable.ic_erroralbumart,
                    modifier = Modifier.padding(horizontal = 10.dp),
                    onClick = { onPlayIconClick(album)},
                    goToArtistPage = { onPlayIconClick(album) },

                    onDropdownIconClick = {
                        dropdownState.value = Pair(sectionChosen, index)
                    },

                    dropDown = {
                        BrainzPlayerDropDownMenu(
                            expanded = dropdownState.value == Pair(sectionChosen, index),
                            onDismiss = { dropdownState.value = Pair(-1, -1) },
                            onAddToNewPlaylist =  onAddToNewPlaylist?.let { action->
                                { action(album) }
                            },
                            onAddToExistingPlaylist = onAddToExistingPlaylist?.let { action->
                                 {action(album)}
                            },
                            onPlayNext = onPlayNext?.let { action->
                                {action(album)}
                            },
                            onAddToQueue = onAddToQueue?.let {action->
                                {action(album)}
                            } ,
                            showShareOption = false
                        )
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
