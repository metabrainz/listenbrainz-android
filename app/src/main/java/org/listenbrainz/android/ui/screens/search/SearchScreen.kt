package org.listenbrainz.android.ui.screens.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.SocialError
import org.listenbrainz.android.model.User
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.SearchViewModel

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(searchBarState: SearchBarState, ) {
    AnimatedVisibility(
        visible = searchBarState.isActive,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        
        val viewModel: SearchViewModel = hiltViewModel()
        val uiState: SearchUiState by viewModel.uiState.collectAsState()
        
        SearchScreen(
            uiState = uiState,
            onDismiss = {
                searchBarState.deactivate()
                viewModel.clearUi()
            },
            onQueryChange = { query -> viewModel.updateQueryFlow(query) },
            onFollowClick = { user, currentFollowStatus ->
                viewModel.toggleFollowStatus(user, currentFollowStatus)
            },
            onClear = { viewModel.clearUi() },
            onErrorShown = { viewModel.clearErrorFlow() },
            showSearchScreen = searchBarState.isActive
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
    onFollowClick: suspend (User, Boolean) -> Flow<Boolean>,
    onClear: () -> Unit,
    keyboardController: SoftwareKeyboardController? = LocalSoftwareKeyboardController.current,
    onSearch: (String) -> Unit = {
        onQueryChange(it)
        keyboardController?.hide()
    },
    onErrorShown: () -> Unit,
    showSearchScreen: Boolean
) {
    
    SearchBar(
        query = uiState.query,
        onQueryChange = onQueryChange,
        onSearch = onSearch,
        active = showSearchScreen,
        onActiveChange = { isActive ->
            if (!isActive)
                onDismiss()
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onDismiss() },
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
        )
    ) {
        
        Column {
            
            LaunchedEffect(uiState.error){
                if (uiState.error != null){
                    delay(2000)
                    onErrorShown()
                }
            }
            
            AnimatedVisibility(
                visible = uiState.error != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ListenBrainzTheme.colorScheme.lbSignature),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = uiState.error?.toast() ?: "",
                        modifier = Modifier.padding(vertical = 2.dp),
                        color = ListenBrainzTheme.colorScheme.onLbSignature
                    )
                }
            }
    
            val scope = rememberCoroutineScope()
    
            LazyColumn(contentPadding = PaddingValues(ListenBrainzTheme.paddings.lazyListAdjacent)) {
                items(uiState.result, key = { it.username }) { user ->
            
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
                                isFollowed = user.isFollowed,
                                scope = scope
                            ) { currentFollowStatus ->
                                onFollowClick(user, currentFollowStatus)
                            }
                        }
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
fun SearchScreenPreview() {
    ListenBrainzTheme {
        SearchScreen(
            uiState = SearchUiState(
                query = "Jasjeet",
                result = listOf(User("Jasjeet"),
                    User("JasjeetTest"),
                    User("Jako")
                ),
                error = SocialError.USER_NOT_FOUND
            ),
            onDismiss = {},
            onQueryChange = {},
            onFollowClick = { _, _ -> flow { emit(true) }},
            onClear = {},
            onErrorShown = {},
            showSearchScreen = true
        )
    }
}