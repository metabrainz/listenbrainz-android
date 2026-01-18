package org.listenbrainz.android.ui.screens.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.platform.WindowInfo
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.model.search.SearchType
import org.listenbrainz.android.model.search.SearchUiState
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: SearchUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    onClear: () -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onSearch: (String) -> Unit = {
        keyboardController?.hide()
    },
    placeholderText: String,
    onErrorShown: () -> Unit,
    onChangeSearchOption: (SearchType) -> Unit = {},
    isBrainzPlayerSearch: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    focusManager: FocusManager = LocalFocusManager.current,
    window: WindowInfo = LocalWindowInfo.current,
    content: @Composable () -> Unit
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

    val searchOptions = listOf(
        SearchType.USER,
        SearchType.PLAYLIST,
        SearchType.ARTIST,
        SearchType.ALBUM,
        SearchType.TRACK
    )

    SearchBar(
        modifier = Modifier.focusRequester(focusRequester),
        query = uiState.query,
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
                contentDescription = "Go Back",
                tint = ListenBrainzTheme.colorScheme.hint
            )
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Rounded.Cancel,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        onClear()
                        keyboardController?.show()
                    },
                contentDescription = "Close Search",
                tint = ListenBrainzTheme.colorScheme.hint
            )
        },
        placeholder = {
            Text(text = placeholderText, color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
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
                .pointerInput(key1 = "Keyboard") {
                    // Tap to hide keyboard.
                    detectTapGestures {
                        focusManager.clearFocus()
                        keyboardController?.hide()
                    }
                }
        ) {

            // Error bar for showing errors
            ErrorBar(uiState.error, onErrorShown)

            if (!isBrainzPlayerSearch) {
                Spacer(modifier = Modifier.padding(3.dp))
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(searchOptions) { option ->
                        val isSelected = option == uiState.selectedSearchType
                        Card(
                            modifier = Modifier
                                .clickable {
                                    onChangeSearchOption(option)
                                },
                            shape = ListenBrainzTheme.shapes.chips,
                            backgroundColor = if (isSelected){
                                ListenBrainzTheme.colorScheme.lbSignatureInverse
                            }
                            else{
                                ListenBrainzTheme.colorScheme.level1
                            },
                            elevation = 4.dp
                        ) {
                            Text(
                                text = option.title,
                                modifier = Modifier
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                color = if (isSelected){
                                    ListenBrainzTheme.colorScheme.listenText
                                }
                                else{
                                    ListenBrainzTheme.colorScheme.text
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.padding(3.dp))
                HorizontalDivider(
                    modifier = Modifier.fillMaxWidth(),
                    color = ListenBrainzTheme.colorScheme.text
                )
                Spacer(modifier = Modifier.padding(3.dp))
            }

            // Main Content
            content()
        }
    }
}