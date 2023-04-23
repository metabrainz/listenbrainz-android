package org.listenbrainz.android.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ListenCard(listen: Listen, coverArtUrl: String, onItemClicked: (listen: Listen) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onItemClicked(listen) },
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            GlideImage(
                model = coverArtUrl,
                modifier = Modifier.size(80.dp, 80.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentDescription = null
            ){
                it.placeholder(R.drawable.ic_coverartarchive_logo_no_text).override(250)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                Text(
                    text = listen.track_metadata.track_name,
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = MaterialTheme.colors.surface,
                    fontWeight = FontWeight.Bold,
                    style = typography.subtitle1
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = buildString {
                        append(listen.track_metadata.artist_name)
                    },
                    modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                    color = MaterialTheme.colors.surface,
                    style = typography.caption
                )

                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = listen.track_metadata.release_name ?: "",
                        modifier = Modifier.padding(0.dp, 12.dp, 12.dp, 0.dp),
                        color = MaterialTheme.colors.surface,
                        style = typography.caption
                    )
                }
            }
/*  Love/Hate Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_heart_broken_24),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp, 16.dp),
                    tint = Color.Red
                )
                Icon(
                    painter = painterResource(id = R.drawable.ic_baseline_heart_broken_24),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp, 16.dp),
                    tint = Color.Red
                )
            }
*/
        }
    }
}

@Composable
@OptIn(ExperimentalGlideComposeApi::class)
fun ListenCardSmall(
    modifier: Modifier = Modifier,
    releaseName: String,
    artistName: String,
    coverArtUrl: String,
    /** Default is 75 as it consume less internet if images are being fetched from a URL.
     *
     *  Best is 200*/
    imageLoadSize: Int = 75,
    useSystemTheme: Boolean = false,    // TODO: remove this when YIM is removed
    @DrawableRes
    errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(enabled = true) { onClick() },
        shape = RoundedCornerShape(5.dp),
        shadowElevation = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            
            // Album cover art
            GlideImage(
                model = coverArtUrl,
                modifier = Modifier.size(60.dp),
                contentScale = ContentScale.Fit,
                contentDescription = "Album Cover Art"
            ) {
                it.placeholder(errorAlbumArt)
                    .override(imageLoadSize)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier) {
                androidx.compose.material3.Text(
                    text = releaseName,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                        .copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (onScreenUiModeIsDark() && useSystemTheme) Color.White else lb_purple,
                            lineHeight = 14.sp
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                androidx.compose.material3.Text(
                    text = artistName,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                        .copy(
                            fontWeight = FontWeight.Bold,
                            color = (if (onScreenUiModeIsDark() && useSystemTheme) Color.White else lb_purple).copy(alpha = 0.7f)
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
