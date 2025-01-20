package org.listenbrainz.android.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.listenbrainz.android.R
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.BrainzPlayerExtensions.toSong
import org.listenbrainz.android.viewmodel.BrainzPlayerViewModel

@Composable
fun BrainzPlayerListenCard(
    modifier: Modifier = Modifier,
    title: String,
    subTitle: String,
    coverArtUrl: String?,
    @DrawableRes errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text,
    onDropdownIconClick: () -> Unit = {},
    dropDown: @Composable () -> Unit = {},
    dropDownState: Boolean = false,
    onPlayIconClick: () -> Unit,
    mediaId: Long? = null,
    viewModel: BrainzPlayerViewModel = hiltViewModel()
) {
    val currentlyPlayingSong = viewModel.currentlyPlayingSong.collectAsStateWithLifecycle().value.toSong
    val titleColor = if (currentlyPlayingSong.mediaID == mediaId) {
        Color(0xFFB94FE5)
    } else {
        ListenBrainzTheme.colorScheme.listenText
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlayIconClick() },
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        shadowElevation = 4.dp,
        color = ListenBrainzTheme.colorScheme.level1
    ) {
        Column {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ListenBrainzTheme.sizes.listenCardHeight),
                contentAlignment = Alignment.CenterStart
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {

                    AlbumArt(coverArtUrl, errorAlbumArt)

                    Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.coverArtAndTextGap))

                    TitleAndSubtitle(
                        modifier = Modifier.padding(end = 6.dp),
                        title = title,
                        goToArtistPage = {},
                        artists = listOf(FeedListenArtist(subTitle, null, "")),
                        titleColor = titleColor
                    )
                }
                Box(modifier = Modifier
                    .fillMaxWidth(0.275f)
                    .align(Alignment.CenterEnd)){
                    DropdownButton (modifier = Modifier.align(Alignment.CenterEnd), onDropdownIconClick = onDropdownIconClick)
                    if(dropDownState) dropDown()
                }
            }
        }
    }
}

@Composable
private fun DropdownButton(modifier: Modifier = Modifier, onDropdownIconClick: () -> Unit) {

    IconButton(
        modifier = modifier,
        onClick = onDropdownIconClick
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_options),
            contentDescription = "",
            tint = ListenBrainzTheme.colorScheme.hint,
            modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.insideCard)
        )
    }
}

@Composable
private fun AlbumArt(
    coverArtUrl: String?,
    errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text
) {
    // Use this for previews
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverArtUrl)
            .build(),
        fallback = painterResource(id = errorAlbumArt),
        error = painterResource(id = errorAlbumArt),
        modifier = Modifier
            .size(ListenBrainzTheme.sizes.listenCardHeight)
            .clip(GenericShape { size, _ ->
                addRect(Rect(0f, 0f, size.width*0.95f, size.height))
            }),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = errorAlbumArt),
        filterQuality = FilterQuality.Low,
        contentDescription = "Album Cover Art"
    )
}