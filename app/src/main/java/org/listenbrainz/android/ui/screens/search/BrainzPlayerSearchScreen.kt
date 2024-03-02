package org.listenbrainz.android.ui.screens.search


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.PlayableType
import org.listenbrainz.android.model.Song
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun BrainzPlayerSearchScreen(
    isActive: Boolean,
    viewModel: BrainzPlayerViewModel = hiltViewModel(),
    deactivate: () -> Unit,
    brainzplayerQueryState : MutableState<String>,
) {
    var searchItems by remember {
        mutableStateOf(mutableListOf<Song>())
    }

    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {

        SearchScreen(
            onDismiss = {
                deactivate()
            },
            onQueryChange = {
              newValue : String -> brainzplayerQueryState.value = newValue
              searchItems = viewModel.searchSongs(brainzplayerQueryState.value)!!.toMutableList()
            },
            queryState = brainzplayerQueryState,
            searchResult = searchItems,
            onClick = {
                song -> viewModel.changePlayable(listOf(song), PlayableType.SONG, song.mediaID,0)
                    viewModel.playOrToggleSong(song, true)
                deactivate()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onSearch: (String) -> Unit = {
        keyboardController?.hide()
    },
    focusRequester: FocusRequester = remember { FocusRequester() },
    window: WindowInfo = LocalWindowInfo.current,
    queryState : MutableState<String>,
    searchResult : MutableList<Song>,
    onClick: (song : Song) -> Unit
) {
    // Used for initial window focus.
    LaunchedEffect(window) {
        snapshotFlow { window.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused) {
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }

    SearchBar(
        modifier = Modifier.focusRequester(focusRequester),
        query = queryState.value,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = true,
        onActiveChange = { isActive ->
            if (!isActive)
                onDismiss()
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        keyboardController?.hide()
                        onDismiss()
                    },
                contentDescription = "Search",
                tint = ListenBrainzTheme.colorScheme.hint
            )
        },
        trailingIcon = {
            Icon(imageVector = Icons.Rounded.Cancel,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        queryState.value = ""
                        keyboardController?.show()
                    },
                contentDescription = "Close Search",
                tint = ListenBrainzTheme.colorScheme.hint
            )
        },
        placeholder = {
            Text(text = "Search local songs", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
        },
        colors = SearchBarDefaults.colors(
            containerColor = ListenBrainzTheme.colorScheme.background,
            dividerColor = ListenBrainzTheme.colorScheme.text,
            inputFieldColors = SearchBarDefaults.inputFieldColors(
                focusedPlaceholderColor = Color.Unspecified,
                focusedTextColor = ListenBrainzTheme.colorScheme.text,
                cursorColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            )
        ),
    ) {

        Column(
            modifier = Modifier
                .pointerInput(key1 = "Keyboard"){
                    // Tap to hide keyboard.
                    detectTapGestures {
                        keyboardController?.hide()
                    }
                }
        ) {
            SongList(searchResult = searchResult , onClick = {
                song -> onClick(song)
            })
        }
    }
}


@Composable
private fun SongList (searchResult: MutableList<Song> , onClick: (song : Song) -> Unit) {
    LazyColumn(contentPadding = PaddingValues(ListenBrainzTheme.paddings.lazyListAdjacent)) {
        itemsIndexed(searchResult) {_, song ->
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ListenBrainzTheme.paddings.lazyListAdjacent)
                ) {

                        TextButton(
                            { onClick(song) }
                        ){
                            Text(
                                text = song.title,
                                color = ListenBrainzTheme.colorScheme.text,
                                fontWeight = FontWeight.Normal
                            )
                        }
                }
            }
        }
    }
}