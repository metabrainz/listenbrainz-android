package org.listenbrainz.android.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple


@Composable
fun YimListenCard(
    releaseName: String,
    artistName: String,
    coverArtUrl: String?,
    onClick: () -> Unit = {}
) {
    val context = LocalContext.current
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = true) {
                onClick()
            },
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 5.dp,
        color = Color.White
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Album cover art
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(coverArtUrl)
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_erroralbumart),
                error = painterResource(R.drawable.ic_erroralbumart),
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit,
                contentDescription = "Album Cover Art"
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier) {
                Text(
                    text = releaseName,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artistName,
                    style = TextStyle(
                        fontFamily = FontFamily(Font(R.font.roboto_regular)),
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun YimListenCardPreview() {
    ListenBrainzTheme {
        YimListenCard(releaseName = "Release name", artistName = "Artist", coverArtUrl = "")
    }
}