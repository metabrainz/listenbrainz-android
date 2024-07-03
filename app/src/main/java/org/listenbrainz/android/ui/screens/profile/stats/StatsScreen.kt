package org.listenbrainz.android.ui.screens.profile.stats

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.viewmodel.ProfileViewModel

@Composable
fun StatsScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    Text(text = uiState.listensTabUiState.listenCount.toString(), color = Color.White)
}