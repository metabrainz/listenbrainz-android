package org.listenbrainz.android.ui.screens.profile.createdforyou

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.R
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun PlaylistTitleCard(modifier: Modifier = Modifier,
                      alignment: Alignment = Alignment.TopStart,
                      cardBg: Int = R.drawable.playlist_card_bg1,
                      isSelected: Boolean = true
                      ){
    Box(
        modifier = modifier
            .clip(shape = ListenBrainzTheme.shapes.listenCardSmall)
//            .border(1.dp, ListenBrainzTheme.colorScheme.)
        ,
        contentAlignment = alignment
    ){
        Image(painter = painterResource(cardBg),
            contentDescription = "Playlist Card Background",)
    }
}

@Preview(showBackground = true)
@Composable
fun PlaylistTitleCardPreview(){
    PlaylistTitleCard(
        alignment = Alignment.Center
    )
}