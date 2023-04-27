package org.listenbrainz.android.ui.screens.listens

import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.spotify.protocol.types.PlayerState
import org.listenbrainz.android.ui.components.SeekBar
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun ProgressBar(playerState: PlayerState?) {
    val listenViewModel = hiltViewModel<ListensViewModel>()
    val progress by listenViewModel.progress.collectAsState(initial = 0f)
    Column {
        Box {
            SeekBar(
                modifier = Modifier
                    .height(10.dp)
                    .fillMaxWidth(0.98F)
                    .padding(0.dp),
                progress = progress,
                onValueChange = {//get the value of the seekbar
                    listenViewModel.seekTo(it, playerState)
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
            val song = playerState?.track
            var duration = "00:00"
            val songCurrentPosition by listenViewModel.songCurrentPosition.collectAsState()
            var currentPosition = "00:00"
            if ((song?.duration ?: 0) / (1000 * 60 * 60) > 0 && songCurrentPosition / (1000 * 60 * 60) > 0) {
                duration = String.format(
                    "%02d:%02d:%02d",
                    (song?.duration ?: 0) / (1000 * 60 * 60),
                    (song?.duration ?: 0) / (1000 * 60) % 60,
                    (song?.duration ?: 0) / 1000 % 60
                )
                currentPosition = String.format(
                    "%02d:%02d:%02d",
                    songCurrentPosition / (1000 * 60 * 60),
                    songCurrentPosition / (1000 * 60) % 60,
                    songCurrentPosition / 1000 % 60
                )
            } else {
                duration = String.format(
                    "%02d:%02d",
                    (song?.duration ?: 0) / (1000 * 60) % 60,
                    (song?.duration ?: 0) / 1000 % 60
                )
                currentPosition =
                    String.format("%02d:%02d", songCurrentPosition / (1000 * 60) % 60, songCurrentPosition / 1000 % 60)
            }
            Text(
                text = currentPosition,
                textAlign = TextAlign.Start,
                color = Color.White,
                modifier = Modifier.padding(end = 5.dp)
            )

            Text(
                text = duration,
                textAlign = TextAlign.Start,
                color = Color.White,
                modifier = Modifier.padding(start = 5.dp)
            )
        }
    }
}