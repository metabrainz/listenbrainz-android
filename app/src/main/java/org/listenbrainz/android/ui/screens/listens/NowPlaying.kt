package org.listenbrainz.android.ui.screens.listens

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.spotify.protocol.types.PlayerState
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.components.SeekBar
import org.listenbrainz.android.viewmodel.ListensViewModel

@Composable
fun NowPlaying(
    playerState: PlayerState?,
    bitmap: listenPoster
){
    val listenViewModel = hiltViewModel<ListensViewModel>()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .height(180.dp)
            .clickable(onClick = {
               val isPaused = playerState?.isPaused ?: false
                if (isPaused) {
                     listenViewModel.play()
                } else {
                     listenViewModel.pause()
                }
            }),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .padding(10.dp)
        ) {
            Text(
                text = "Now playing",
                modifier = Modifier.padding(4.dp),
                color = MaterialTheme.colors.surface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )
        }
        Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .padding(top = 30.dp)
        ) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = bitmap.bitmap)
                    .placeholder(R.drawable.ic_coverartarchive_logo_no_text)
                    .error(R.drawable.ic_coverartarchive_logo_no_text)
                    .build()
            )

            Image(
                modifier = Modifier
                    .size(80.dp, 80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                painter = painter,
                alignment = Alignment.CenterStart,
                contentDescription = "",
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                playerState?.track?.name?.let { track ->
                    Text(
                        text = track,
                        modifier = Modifier.padding(0.dp, 0.dp, 5.dp, 0.dp),
                        color = MaterialTheme.colors.surface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle1,
                        maxLines = 1
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = buildString {
                        append(playerState?.track?.artist?.name)
                    },
                    modifier = Modifier.padding(0.dp, 0.dp, 5.dp, 0.dp),
                    color = MaterialTheme.colors.surface,
                    style = MaterialTheme.typography.caption,
                    maxLines = 2
                )

                Row {
                    playerState?.track?.album?.name?.let { album ->
                        Text(
                            text = album,
                            modifier = Modifier.padding(0.dp, 6.dp, 12.dp, 0.dp),
                            color = MaterialTheme.colors.surface,
                            style = MaterialTheme.typography.caption,
                            maxLines = 2
                        )
                    }
                }
            }
        }
            Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Bottom,
        ) {
                ProgressBar(playerState = playerState)
          }
        }

    }
}

@Preview
@Composable
fun NowPlayingPreview() {
    NowPlaying(
        playerState = null,
        bitmap = listenPoster()
    )
}
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

data class listenPoster(
    val bitmap: Bitmap?=null,
    val id:String?=""
)
