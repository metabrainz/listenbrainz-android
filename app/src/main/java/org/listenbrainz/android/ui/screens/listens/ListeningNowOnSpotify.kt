package org.listenbrainz.android.ui.screens.listens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import org.listenbrainz.android.model.ListenBitmap
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark
import org.listenbrainz.android.viewmodel.SocialViewModel

@Composable
fun ListeningNowOnSpotify(
    playerState: PlayerState?,
    bitmap: ListenBitmap
){
    val socialViewModel = hiltViewModel<SocialViewModel>()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ListenBrainzTheme.paddings.horizontal,
                vertical = 10.dp
            )
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = {
                val isPaused = playerState?.isPaused ?: false
                if (isPaused) {
                    socialViewModel.play()
                } else {
                    socialViewModel.pause()
                }
            }),
        elevation = 0.dp,
        backgroundColor = if (onScreenUiModeIsDark()) Color.Black else offWhite,
    ) {
        Column {
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Listening now on spotify",
                modifier = Modifier.padding(horizontal = 16.dp),
                color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.subtitle1,
                textAlign = TextAlign.Center,
            )
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
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
                        .size(80.dp)
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
                            color = ListenBrainzTheme.colorScheme.listenText,
                            fontWeight = FontWeight.Bold,
                            style = ListenBrainzTheme.textStyles.listenTitle,
                            maxLines = 1
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
        
                    if (playerState != null) {
                        Text(
                            text = playerState.track.artist.name,
                            color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                            style = ListenBrainzTheme.textStyles.listenSubtitle,
                            maxLines = 1
                        )
                    }
        
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    playerState?.track?.album?.name?.let { album ->
                        Text(
                            text = album,
                            color = ListenBrainzTheme.colorScheme.listenText.copy(alpha = 0.7f),
                            style = ListenBrainzTheme.textStyles.listenSubtitle,
                            maxLines = 1
                        )
                    }
                    
                }
            }
            
        }

    }
}

@Preview
@Composable
fun ListeningNowOnSpotifygPreview() {
    ListeningNowOnSpotify(
        playerState = null,
        bitmap = ListenBitmap()
    )
}
