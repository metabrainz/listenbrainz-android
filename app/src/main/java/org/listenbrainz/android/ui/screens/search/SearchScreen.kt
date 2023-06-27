package org.listenbrainz.android.ui.screens.search

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.model.User
import org.listenbrainz.android.ui.components.FollowButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.SearchViewModel

@Composable
fun SearchScreen(
    showSearchScreen: Boolean,
    onDismiss: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    AnimatedVisibility(
        visible = showSearchScreen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        
        val viewModel: SearchViewModel = hiltViewModel()
        val uiState: SearchUiState by viewModel.uiState.collectAsState()
        
        SearchScreen(
            uiState = uiState,
            onDismiss = onDismiss,
            onQueryChange = { query -> viewModel.updateQueryFlow(query) },
            onFollow = { user ->
                viewModel.toggleFollowStatus(user)
            },
            onClear = { viewModel.clearUi() },
            showError = { error ->
                snackbarHostState.showSnackbar(message = error.toast())
                viewModel.clearErrorFlow()
            },
            showSearchScreen = showSearchScreen
        )
        
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchScreen(
    uiState: SearchUiState,
    onDismiss: () -> Unit,
    onQueryChange: (String) -> Unit,
    /** Must return if the operation was successful.*/
    onFollow: suspend (User) -> Flow<Boolean>,
    onClear: () -> Unit,
    onSearch: (String) -> Unit = onQueryChange,
    showError: suspend (ResponseError) -> Unit,
    showSearchScreen: Boolean,
) {
    
    LaunchedEffect(uiState.error){
        if (uiState.error != null){
            showError(uiState.error)
        }
    }
    
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
                modifier = Modifier.clickable { onDismiss() },
                contentDescription = "Search users",
                tint = ListenBrainzTheme.colorScheme.hintText
            )
        },
        trailingIcon = {
            Icon(imageVector = Icons.Rounded.Cancel,
                modifier = Modifier.clickable { onClear() },
                contentDescription = "Close Search",
                tint = ListenBrainzTheme.colorScheme.hintText
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
                focusedLeadingIconColor = ListenBrainzTheme.colorScheme.text,
                focusedTrailingIconColor = ListenBrainzTheme.colorScheme.text,
                focusedTextColor = ListenBrainzTheme.colorScheme.text,
                unfocusedLeadingIconColor = Color.LightGray,
                unfocusedTrailingIconColor = Color.LightGray,
                cursorColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            )
        )
    ) {
    
        val scope = rememberCoroutineScope()
        
        LazyColumn(contentPadding = PaddingValues(ListenBrainzTheme.paddings.lazyListAdjacent)) {
            items(uiState.result, key = { it.username }){ user ->
                
                Column {
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .padding(ListenBrainzTheme.paddings.lazyListAdjacent)) {
                        Text(
                            text = user.username,
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .padding(start = 6.dp),
                            color = ListenBrainzTheme.colorScheme.text,
                            fontWeight = FontWeight.Bold,
                        )
                        FollowButton(
                            modifier = Modifier.align(Alignment.CenterEnd),
                            isFollowed = user.isFollowed,
                            scope = scope
                        ) {
                            onFollow(user)
                        }
                    }
                }
                
            }
        }
        
    }
}

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
                error = null),
            onDismiss = {},
            onQueryChange = {},
            onFollow = { flow { emit(true) }},
            onClear = {},
            showError = {},
            showSearchScreen = true
        )
    }
}