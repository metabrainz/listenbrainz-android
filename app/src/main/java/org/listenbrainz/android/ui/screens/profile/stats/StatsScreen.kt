package org.listenbrainz.android.ui.screens.profile.stats

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import org.listenbrainz.android.ui.screens.profile.ProfileUiState
import org.listenbrainz.android.viewmodel.ProfileViewModel

@Composable
fun StatsScreen(
    viewModel: ProfileViewModel,
    uiState: ProfileUiState,
) {
    Text(text = uiState.listensTabUiState.listenCount.toString(), color = Color.White)
}