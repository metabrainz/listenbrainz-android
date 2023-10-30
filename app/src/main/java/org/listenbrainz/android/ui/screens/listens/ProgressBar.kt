package org.listenbrainz.android.ui.screens.listens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import org.listenbrainz.android.ui.components.SeekBar
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun ProgressBar() {
    val listenViewModel = hiltViewModel<ListensViewModel>()
    val uiState by listenViewModel.uiState.collectAsState()
    var duration: String by remember {
        mutableStateOf("")
    }
    var currentPosition: String by remember {
        mutableStateOf("")
    }
    
    // TODO: Simplify this.
    LaunchedEffect(
        uiState.listeningNowUiState.songDuration,
        uiState.listeningNowUiState.playerState?.track
    ){
        val song = uiState.listeningNowUiState.playerState?.track
        if ((song?.duration ?: 0) / (1000 * 60 * 60) > 0 && uiState.listeningNowUiState.songCurrentPosition / (1000 * 60 * 60) > 0) {
            duration = String.format(
                "%02d:%02d:%02d",
                (song?.duration ?: 0) / (1000 * 60 * 60),
                (song?.duration ?: 0) / (1000 * 60) % 60,
                (song?.duration ?: 0) / 1000 % 60
            )
            currentPosition = String.format(
                "%02d:%02d:%02d",
                uiState.listeningNowUiState.songCurrentPosition / (1000 * 60 * 60),
                uiState.listeningNowUiState.songCurrentPosition / (1000 * 60) % 60,
                uiState.listeningNowUiState.songCurrentPosition / 1000 % 60
            )
        } else {
            duration = String.format(
                "%02d:%02d",
                (song?.duration ?: 0) / (1000 * 60) % 60,
                (song?.duration ?: 0) / 1000 % 60
            )
            currentPosition =
                String.format("%02d:%02d", uiState.listeningNowUiState.songCurrentPosition / (1000 * 60) % 60, uiState.listeningNowUiState.songCurrentPosition / 1000 % 60)
        }
    }
    
    Column {
        Box {
            SeekBar(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(0.98F)
                    .padding(0.dp),
                progress = uiState.listeningNowUiState.progress,
                onValueChange = {//get the value of the seekbar
                    //listenViewModel.seekTo(it, playerState)
                },
                onValueChanged = { }
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth(0.98F)
                .padding(start = 10.dp, top = 0.dp, end = 10.dp)
        ) {
            
            Text(
                text = currentPosition,
                textAlign = TextAlign.Start,
                color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                modifier = Modifier.padding(end = 5.dp)
            )

            Text(
                text = duration,
                textAlign = TextAlign.Start,
                color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}