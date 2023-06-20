package org.listenbrainz.android.ui.screens.search

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.model.SearchUiState
import org.listenbrainz.android.viewmodel.SearchViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    showSearchScreen: Boolean,
    onDismiss: () -> Unit,
    viewModel: SearchViewModel = hiltViewModel(),
    context: Context = LocalContext.current
) {
    AnimatedVisibility(
        visible = showSearchScreen,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        
        val uiState = viewModel.uiState.collectAsState(initial = SearchUiState(emptyList(), "")).value
        
        SearchBar(
            query = uiState.query,
            onQueryChange = {
                viewModel.updateQueryFlow(it)
            },
            onSearch = {
                viewModel.updateQueryFlow(it)
            },
            active = showSearchScreen,
            onActiveChange = { isActive ->
                if (!isActive)
                    onDismiss()
            },
            placeholder = {
                Text(text = "Search users", color = MaterialTheme.colorScheme.onSurface.copy(0.5f))
            }
        ) {
            LazyColumn {
                items(uiState.result){
                    Text(text = it.username)
                }
            }
        }
    }
}