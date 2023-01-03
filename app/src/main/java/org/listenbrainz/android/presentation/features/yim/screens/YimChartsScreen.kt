package org.listenbrainz.android.presentation.features.yim.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import org.listenbrainz.android.presentation.features.yim.YimViewModel
import org.listenbrainz.android.presentation.features.yim.ui.theme.YearInMusicTheme

@Composable
fun YimChartsScreen(
    viewModel: YimViewModel,
    navController: NavController
){
    YearInMusicTheme(redTheme = true) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
        
        }
    }
}