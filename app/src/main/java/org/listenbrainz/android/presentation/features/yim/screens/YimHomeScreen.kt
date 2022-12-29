package org.listenbrainz.android.presentation.features.yim.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme

@Composable
fun YimHomeScreen(
    viewModel: YimViewModel
){
    // TODO : Implement first page and compose navigation
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    YearInMusicTheme {
        YimHomeScreen(viewModel = viewModel())
    }
}
