package org.listenbrainz.android.ui.components

import androidx.annotation.DrawableRes
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark

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
                Text(
                    text = releaseName,
                    style = MaterialTheme.typography.bodyLarge
                        .copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (onScreenUiModeIsDark() && useSystemTheme) Color.White else lb_purple,
                            lineHeight = 14.sp
                        ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = artistName,
                    style = MaterialTheme.typography.bodyMedium
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