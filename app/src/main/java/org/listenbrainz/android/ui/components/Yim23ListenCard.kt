package org.listenbrainz.android.ui.components

import androidx.compose.foundation.Image
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.util.Utils


@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun Yim23ListenCard(
    releaseName: String,
    artistName: String,
    caaId : String,
    caaReleaseMbid : String ,
    onClick: () -> Unit = {}
) {
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
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Album cover art
            if(caaId != "" && caaReleaseMbid != ""){
                GlideImage(
                    model = Utils.getCoverArtUrl(
                        caaReleaseMbid = caaReleaseMbid,
                        caaId = caaId.toLong(),
                        size = 250,
                    ),
                    modifier = Modifier.size(60.dp),
                    contentScale = ContentScale.Fit,
                    contentDescription = "Album Cover Art"
                ) {
                    it.placeholder(R.drawable.ic_erroralbumart)
                        .override(75)
                }
            }
            else{
                Image(painter = painterResource(id = R.drawable.ic_erroralbumart) , contentDescription = "")
            }


            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier) {
                Text(
                    text = releaseName,
                    style = MaterialTheme.typography.bodyMedium
                        .copy(
                            fontWeight = FontWeight.Bold,
                            color = lb_purple,
                            fontSize = 14.sp
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.bodySmall
                        .copy(
                            fontWeight = FontWeight.Bold,
                            color = lb_purple.copy(alpha = 0.7f)
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
        // Doesn't work due to glide
        YimListenCard(releaseName = "Release name", artistName = "Artist", coverArtUrl = "")
    }
}