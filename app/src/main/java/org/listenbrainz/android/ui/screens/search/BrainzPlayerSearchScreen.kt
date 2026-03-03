package org.listenbrainz.android.ui.screens.search


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import org.koin.androidx.compose.koinViewModel
import org.listenbrainz.android.R
import org.listenbrainz.shared.model.PlayableType
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.model.search.SearchData
import org.listenbrainz.android.model.search.SearchUiState
import org.listenbrainz.android.ui.components.ListenCardSmallDefault
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.showToast
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun BrainzPlayerSearchScreen(
    viewModel: BrainzPlayerViewModel = koinViewModel(),
    deactivate: () -> Unit,
) {
    val context = LocalContext.current
    val brainzplayerQueryState by viewModel.searchQuery.collectAsState()
    val searchItems by viewModel.searchItems.collectAsState()

    var error by remember {
        mutableStateOf<ResponseError?>(null)
    }

    fun onDismiss() {
        viewModel.clearSearchResults()
        error = null
        deactivate()
    }

    SearchScreen(
        uiState = remember(searchItems, brainzplayerQueryState.text, error) {
            SearchUiState(
                query = brainzplayerQueryState.text,
                result = SearchData.Songs(searchItems),
                error = error
            )
        },
        onDismiss = ::onDismiss,
        queryValue = brainzplayerQueryState,
        onQueryChange = {
            viewModel.updateSearchQuery(it)
        },
        onClear = {
            viewModel.clearSearchResults()
        },
        onErrorShown = { error = null },
        placeholderText = "Search your music library"
    ) {
        LazyColumn {
            itemsIndexed(searchItems) { _, song ->
                ListenCardSmallDefault(
                    modifier = Modifier.padding(
                        horizontal = ListenBrainzTheme.paddings.horizontal,
                        vertical = ListenBrainzTheme.paddings.lazyListAdjacent
                    ),
                    metadata = song.toMetadata(),
                    coverArtUrl = song.albumArt,
                    errorAlbumArt = R.drawable.ic_erroralbumart,
                    goToArtistPage = {},
                    onDropdownSuccess = { context.showToast(it) },
                    onDropdownError = { error = it }
                ) {
                    viewModel.changePlayable(
                        listOf(song),
                        PlayableType.SONG,
                        song.mediaID,
                        0
                    )
                    viewModel.playOrToggleSong(song, true)
                    onDismiss()
                }
            }
        }
    }
}
