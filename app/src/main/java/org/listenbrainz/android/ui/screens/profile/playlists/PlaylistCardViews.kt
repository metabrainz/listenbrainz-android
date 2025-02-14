package org.listenbrainz.android.ui.screens.profile.playlists

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun PlaylistGridViewCard(
    coverArtURL: String,
    title: String,
    trackCount: Int,
    updatedDate: String,
    @DrawableRes errorCoverArt: Int = R.drawable.playlist_card_bg1,
    onClickOptionsButton:()->Unit,
    onClickCard: ()->Unit
) {
    Card(
        onClick = onClickCard,
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        modifier = Modifier
            .padding(8.dp)
            .width(300.dp)
            .clip(ListenBrainzTheme.shapes.listenCardSmall),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Image (Coil for async loading)
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(coverArtURL)
                    .error(errorCoverArt)
                    .placeholder(errorCoverArt)
                    .build(),
                contentDescription = "Playlist Cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row {
                Column(
                    modifier = Modifier.padding(8.dp)
                        .weight(1.0f)
                ) {
                    Text(
                        text = title,
                        color = ListenBrainzTheme.colorScheme.listenText,
                        style = ListenBrainzTheme.textStyles.dialogTitleBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "$trackCount tracks",
                            fontSize = 14.sp,
                            color = ListenBrainzTheme.colorScheme.onBackground
                        )
                        Text(
                            text = " | Updated $updatedDate",
                            fontSize = 14.sp,
                            color = ListenBrainzTheme.colorScheme.onBackground.copy(alpha = 0.75f)
                        )
                    }
                }
                IconButton(
                    onClick = {
                        onClickOptionsButton()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_options),
                        contentDescription = "Options button"
                    )
                }
            }
        }
    }
}


@Composable
fun PlaylistListViewCard(){

}


@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlaylistGridViewCardPreview() {
    ListenBrainzTheme {
        PlaylistGridViewCard(
            coverArtURL = "https://via.placeholder.com/150",
            title = "Playlist Title",
            trackCount = 10,
            updatedDate = "2 days ago",
            onClickOptionsButton = {}
        ){}
    }
}
