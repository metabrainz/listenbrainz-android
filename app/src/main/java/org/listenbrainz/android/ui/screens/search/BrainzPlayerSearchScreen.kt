package org.listenbrainz.android.ui.screens.search


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.R
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun BrainzPlayerSearchScreen(
    isActive: Boolean,
    viewModel: BrainzPlayerViewModel = hiltViewModel(),
    deactivate: () -> Unit,
) {
    var brainzplayerQueryState by remember {
        mutableStateOf("")
    }

    val searchItems = remember {
        mutableStateListOf<Song>()
    }

    fun onDismiss() {
        searchItems.clear()
        brainzplayerQueryState = ""
        deactivate()
    }

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        SearchScreen(
            uiState = remember(searchItems, brainzplayerQueryState) {
                SearchUiState(
                    query = brainzplayerQueryState,
                    result = searchItems,
                    error = null
                )
            },
            onDismiss = ::onDismiss,
            onQueryChange = { newValue  ->
                brainzplayerQueryState = newValue
                searchItems.clear()
                searchItems.addAll(viewModel.searchSongs(brainzplayerQueryState) ?: emptyList())
            },
            onClear = searchItems::clear,
            onErrorShown = {}
        ) {
            SongList(
                searchResult = searchItems,
                onClick = { song ->
                    viewModel.changePlayable(listOf(song), PlayableType.SONG, song.mediaID, 0)
                    viewModel.playOrToggleSong(song, true)
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun SongList (searchResult: MutableList<Song> , onClick: (song : Song) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(ListenBrainzTheme.paddings.lazyListAdjacent)) {
        itemsIndexed(searchResult) { _, song ->
            ListenCardSmallDefault(
                modifier = Modifier.padding(
                    horizontal = ListenBrainzTheme.paddings.horizontal,
                    vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                ),
                metadata = song.toMetadata(),
                coverArtUrl = song.albumArt,
                errorAlbumArt = R.drawable.ic_erroralbumart,
                goToArtistPage = {},
                onDropdownSuccess = {},
                onDropdownError = {}
            ) {
                onClick(song)
            }
        }
    }
}