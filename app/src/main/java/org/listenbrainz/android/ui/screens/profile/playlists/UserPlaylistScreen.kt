package org.listenbrainz.android.ui.screens.profile.playlists

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.UserViewModel

@Composable
fun UserPlaylistScreen(
    snackbarState: SnackbarHostState,
    userViewModel: UserViewModel
) {
    val uiState by userViewModel.uiState.collectAsState()
    val context = LocalContext.current

    UserPlaylistScreenBase(
        uiState = uiState
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun UserPlaylistScreenBase(
    uiState: ProfileUiState
) {
    val userPlaylistsData = uiState.playlistTabUIState.userPlaylists.collectAsLazyPagingItems()
    val collabPlaylistsData = uiState.playlistTabUIState.collabPlaylists.collectAsLazyPagingItems()

    val isCurrentScreenCollab by remember { mutableStateOf(false) }
    val isRefreshing = remember(
        isCurrentScreenCollab,
        userPlaylistsData.loadState.refresh,
        collabPlaylistsData.loadState.refresh
    ) {
        if (isCurrentScreenCollab) {
            collabPlaylistsData.loadState.refresh is LoadState.Loading
        } else {
            userPlaylistsData.loadState.refresh is LoadState.Loading
        }
    }

    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            if(isCurrentScreenCollab) {
                collabPlaylistsData.refresh()
            } else {
                userPlaylistsData.refresh()
            }
        }
    )

    Box(
        modifier = Modifier.fillMaxSize()
            .pullRefresh(pullRefreshState)
    ) {
        LazyColumn {
            items(userPlaylistsData.itemCount) { index ->
                val playlist = userPlaylistsData[index]
                if (playlist != null) {
                    Text(text = playlist.title.toString())
                }
            }
        }

        PullRefreshIndicator(
            modifier = Modifier.align(Alignment.TopCenter),
            refreshing = isRefreshing,
            contentColor = ListenBrainzTheme.colorScheme.lbSignatureInverse,
            backgroundColor = ListenBrainzTheme.colorScheme.level1,
            state = pullRefreshState
        )
    }
}