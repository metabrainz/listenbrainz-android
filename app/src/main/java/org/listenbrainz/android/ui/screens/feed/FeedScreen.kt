package org.listenbrainz.android.ui.screens.feed

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.viewmodel.FeedViewModel

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    FeedScreen()
}


@Composable
private fun FeedScreen() {

}

@Preview
@Composable
private fun FeedScreenPreview() {
    ListenBrainzTheme {
        FeedScreen()
    }
}