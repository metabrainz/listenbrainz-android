package org.listenbrainz.android.ui.screens.album

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.components.LoadingAnimation
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.AlbumViewModel

@Composable
fun AlbumScreen(
    albumMbid: String,
    viewModel: AlbumViewModel = hiltViewModel(),
) {
    LaunchedEffect(Unit) {
        viewModel.fetchAlbumData(albumMbid)
    }
    val uiState by viewModel.uiState.collectAsState()
    AlbumScreen(
        uiState = uiState
    )
}

@Composable
private fun AlbumScreen(
    uiState: AlbumUiState
) {
    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = uiState.isLoading,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(initialAlpha = 0.4f),
            exit = fadeOut(animationSpec = tween(durationMillis = 250))
        ) {
            LoadingAnimation()
        }
        AnimatedVisibility(
            visible = !uiState.isLoading,
        ) {
            ListenBrainzTheme {
                LazyColumn {
                    item {
                        Text(uiState.name.toString(), color = ListenBrainzTheme.colorScheme.textColor)
                    }
                }
            }

        }
    }
}

