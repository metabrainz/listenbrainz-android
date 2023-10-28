package org.listenbrainz.android.ui.screens.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.User
import org.listenbrainz.android.model.UserListUiState
import org.listenbrainz.android.ui.components.ErrorBar
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.SearchViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(
    isActive: Boolean,
    viewModel: SearchViewModel = hiltViewModel(),
    deactivate: () -> Unit
) {
    AnimatedVisibility(
        visible = isActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        val uiState: SearchUiState by viewModel.uiState.collectAsState()
        
        SearchScreen(
            uiState = uiState,
            onDismiss = {
                deactivate()
                viewModel.clearUi()
            },
            onQueryChange = { query -> viewModel.updateQueryFlow(query) },
            onFollowClick = { user, index ->
                viewModel.toggleFollowStatus(user, index)
            },
            onClear = { viewModel.clearUi() },
            onErrorShown = { viewModel.clearErrorFlow() },
        )
        
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    /** Must return if the operation was successful.*/
    onFollowClick: (User, Int) -> Unit,
    onClear: () -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onSearch: (String) -> Unit = {
        keyboardController?.hide()
    },
    onErrorShown: () -> Unit,
    focusRequester: FocusRequester = remember { FocusRequester() },
    window: WindowInfo = LocalWindowInfo.current
) {
    // Used for initial window focus.
    LaunchedEffect(window){
        snapshotFlow { window.isWindowFocused }.collect { isWindowFocused ->
            if (isWindowFocused){
                focusRequester.requestFocus()
                keyboardController?.show()
            }
        }
    }
    
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
                imageVector = Icons.Rounded.ArrowBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable {
                        keyboardController?.hide()
                        onDismiss()
                    },
                contentDescription = "Search users",
                tint = ListenBrainzTheme.colorScheme.hint
            )
        },
        trailingIcon = {
            Icon(imageVector = Icons.Rounded.Cancel,
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
            Text(text = "Search users", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
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
            
            // Error bar for showing errors
            ErrorBar(uiState.error, onErrorShown)
            
            // Main Content
            UserList(uiState, onFollowClick)
        }
    }
}

@Composable
private fun UserList(
    uiState: SearchUiState,
    onFollowClick: (User, Int) -> Unit
) {
    
    LazyColumn(contentPadding = PaddingValues(ListenBrainzTheme.paddings.lazyListAdjacent)) {
        itemsIndexed(uiState.result.userList) { index, user ->
            
            Column {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(ListenBrainzTheme.paddings.lazyListAdjacent)
                ) {
                    Row(
                        modifier = Modifier.align(Alignment.CenterStart),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Person,
                            contentDescription = "Profile",
                            tint = ListenBrainzTheme.colorScheme.hint
                        )
                        
                        Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.coverArtAndTextGap))
                        
                        Text(
                            text = user.username,
                            color = ListenBrainzTheme.colorScheme.text,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    FollowButton(
                        modifier = Modifier.align(Alignment.CenterEnd),
                        isFollowedState = uiState.result.isFollowedList[index]
                    ) {
                        onFollowClick(uiState.result.userList[index], index)
                    }
                }
            }
            
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
private fun SearchScreenPreview() {
    ListenBrainzTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "Jasjeet",
                result = UserListUiState(
                    listOf(User("Jasjeet"),
                        User("JasjeetTest"),
                        User("Jako")
                    ),
                    listOf(false, true, true)
                ) ,
                error = ResponseError.DOES_NOT_EXIST
            ),
            onDismiss = {},
            onQueryChange = {},
            onFollowClick = { _, _ -> flow { emit(true) }},
            onClear = {},
            onErrorShown = {}
        )
    }
}