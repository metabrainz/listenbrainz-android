package org.listenbrainz.android.presentation.features.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import org.listenbrainz.android.R
import org.listenbrainz.android.data.sources.api.entities.CoverArt
import org.listenbrainz.android.data.sources.api.entities.listens.Listen

@Composable
fun ListenCard(listen: Listen, coverArt: CoverArt?, onItemClicked: (listen: Listen) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = { onItemClicked(listen) }),
        elevation = 0.dp,
        backgroundColor = MaterialTheme.colors.onSurface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val painter = rememberAsyncImagePainter(
                ImageRequest.Builder(LocalContext.current)
                    .data(data = coverArt?.images?.get(0)?.thumbnails?.large)
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
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.End,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_baseline_heart_broken_24),
//                    contentDescription = null,
//                    modifier = Modifier.size(16.dp, 16.dp),
//                    tint = Color.Red
//                )
//
//                Icon(
//                    painter = painterResource(id = R.drawable.ic_baseline_heart_broken_24),
//                    contentDescription = null,
//                    modifier = Modifier.size(16.dp, 16.dp),
//                    tint = Color.Red
//                )
//            }
        }
    }
}
