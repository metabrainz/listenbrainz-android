package org.listenbrainz.android.ui.screens.listens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Listen
import org.listenbrainz.android.ui.theme.lb_purple
import org.listenbrainz.android.ui.theme.offWhite
import org.listenbrainz.android.ui.theme.onScreenUiModeIsDark

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun ListeningNowCard(listen: Listen, coverArtUrl: String, onItemClicked: (listen: Listen) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onItemClicked(listen) },
        elevation = 0.dp,
        backgroundColor = if (onScreenUiModeIsDark()) Color.Black else offWhite,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Listening now",
                    modifier = Modifier.padding(4.dp),
                    color = if (onScreenUiModeIsDark()) Color.White else Color.Black,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.subtitle1,
                    textAlign = TextAlign.Center,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                GlideImage(
                    model = coverArtUrl,
                    modifier = Modifier
                        .size(80.dp, 80.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentDescription = null
                ) {
                    it.placeholder(R.drawable.ic_coverartarchive_logo_no_text).override(250)
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.align(Alignment.CenterVertically)) {
                    Text(
                        text = listen.track_metadata.track_name,
                        modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                        color = if (onScreenUiModeIsDark()) Color.White else lb_purple,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.subtitle1
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = buildString {
                            append(listen.track_metadata.artist_name)
                        },
                        modifier = Modifier.padding(0.dp, 0.dp, 12.dp, 0.dp),
                        color = if (onScreenUiModeIsDark()) Color.White else lb_purple.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.caption
                    )

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = listen.track_metadata.release_name ?: "",
                            modifier = Modifier.padding(0.dp, 12.dp, 12.dp, 0.dp),
                            color = if (onScreenUiModeIsDark()) Color.White else lb_purple.copy(
                                alpha = 0.7f
                            ),
                            style = MaterialTheme.typography.caption
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
}
