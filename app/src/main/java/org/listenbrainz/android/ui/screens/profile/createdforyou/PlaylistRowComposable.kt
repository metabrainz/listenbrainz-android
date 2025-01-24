package org.listenbrainz.android.ui.screens.profile.createdforyou

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import org.listenbrainz.android.R
import org.listenbrainz.android.model.playlist.PlaylistArtist
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun PlaylistRowComposable(
    modifier: Modifier,
    trackName: String,
    artists: List<PlaylistArtist>,
    coverArtUrl: String? = null,
    @DrawableRes errorTrackImage: Int = R.drawable.ic_coverartarchive_logo_no_text,
    onDropdownIconClick: () -> Unit = {},
    dropDown: @Composable () -> Unit = {},
    isReorderButtonVisible: Boolean = true,
    color: Color = ListenBrainzTheme.colorScheme.level1,
    titleColor: Color = ListenBrainzTheme.colorScheme.listenText,
    subtitleColor: Color = titleColor.copy(alpha = 0.7f),
    goToArtistPage: (String) -> Unit,
    onClick: () -> Unit,
    onPlayClick: () -> Unit,
    onReorderClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .fillMaxWidth(),
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        shadowElevation = 4.dp,
        color = color,
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onClick() }
        ) {

            if (isReorderButtonVisible) {
                OrderButton {
                    onReorderClick()
                }
            }

            PlaylistArt(
                coverArtUrl = coverArtUrl,
                errorAlbumArt = errorTrackImage
            )

            Column(
                modifier = Modifier
                    .padding(
                        ListenBrainzTheme.paddings.insideCard
                    )
                    .weight(100f)
            ) {
                TitleAndSubtitlePlaylist(
                    title = trackName,
                    durationInSeconds = 0,
                    artists = artists,
                    titleColor = titleColor,
                    subtitleColor = subtitleColor,
                    goToArtistPage = goToArtistPage
                )
            }


            Buttons(
                onPlayClick = onPlayClick,
                onDropdownIconClick = onDropdownIconClick
            )
            dropDown()


        }
    }
}

/** [title] corresponds to release name and [artists] corresponds to all the artists as per
 * MB's credit system.
 * The [artists] list consists of artist names and join phrases used to join multiple artists together*/
@Composable
fun TitleAndSubtitlePlaylist(
    modifier: Modifier = Modifier,
    title: String,
    durationInSeconds: Int,
    artists: List<PlaylistArtist?>,
    alignment: Alignment.Horizontal = Alignment.Start,
    titleColor: Color = ListenBrainzTheme.colorScheme.listenText,
    subtitleColor: Color = titleColor.copy(alpha = 1.0f),
    durationColor: Color = titleColor.copy(alpha = 0.8f),
    goToArtistPage: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Row {
            Text(
                modifier = Modifier,
                text = title,
                style = ListenBrainzTheme.textStyles.listenTitle.copy(fontSize = 18.sp),
                color = titleColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .padding(bottom = 4.dp),
                text = formatSeconds(durationInSeconds),
                style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                color = durationColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row {
            artists.forEach { artist ->
                artist?.artistCreditName?.let {
                    fun Modifier.goToArtistPage() =
                        if (artist.artistMbid != null) {
                            this.clickable {
                                goToArtistPage(artist.artistMbid)
                            }
                        } else
                            this

                    Text(
                        modifier = Modifier
                            .goToArtistPage()
                            .padding(4.dp),
                        text = artist.artistCreditName + (artist.joinPhrase ?: ""),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Light),
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}


@Composable
private fun PlaylistArt(
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
                addRect(Rect(0f, 0f, size.width * 0.95f, size.height))
            }),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = errorAlbumArt),
        filterQuality = FilterQuality.Low,
        contentDescription = "Album Cover Art"
    )
}


@Composable
private fun Buttons(
    modifier: Modifier = Modifier,
    onPlayClick: () -> Unit,
    onDropdownIconClick: () -> Unit
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = onDropdownIconClick
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_options),
                contentDescription = "",
                tint = ListenBrainzTheme.colorScheme.hint,
                modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.insideCard)
            )
        }
        IconButton(
            modifier = modifier,
            onClick = onPlayClick
        ) {
            Icon(
                painter = painterResource(id = R.drawable.brainz_player_play_button),
                contentDescription = "",
                tint = ListenBrainzTheme.colorScheme.hint,
                modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.insideCard)
            )
        }
    }
}

@Composable
private fun OrderButton(
    modifier: Modifier = Modifier,
    onReorderClick: () -> Unit
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = { onReorderClick() }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_reorder),
                contentDescription = "Image for reordering",
                tint = ListenBrainzTheme.colorScheme.hint,
                modifier = Modifier.padding(horizontal = ListenBrainzTheme.paddings.insideCard)
            )
        }

    }
}


fun formatSeconds(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%02d:%02d", minutes, secs)
    }
}

@Preview
@Composable
fun PlaylistRowComposablePreview() {
    ListenBrainzTheme {
        PlaylistRowComposable(
            modifier = Modifier,
            trackName = "Track Name",
            artists = listOf(
                PlaylistArtist(
                    artistCreditName = "Artist Name",
                    artistMbid = "Artist MBID",
                    joinPhrase = " & "
                )
            ),
            errorTrackImage = R.drawable.ic_coverartarchive_logo_no_text,

            onDropdownIconClick = {},
            dropDown = {},
            color = ListenBrainzTheme.colorScheme.level1,
            titleColor = ListenBrainzTheme.colorScheme.listenText,
            subtitleColor = ListenBrainzTheme.colorScheme.listenText,
            goToArtistPage = {},
            onClick = {},
            onPlayClick = {
            },
            onReorderClick = {}
        )
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun PlaylistRowComposablePreviewDark() {
    ListenBrainzTheme {
        PlaylistRowComposable(
            modifier = Modifier,
            trackName = "Track Name",
            artists = listOf(
                PlaylistArtist(
                    artistCreditName = "Artist Name",
                    artistMbid = "Artist MBID",
                    joinPhrase = " & "
                )
            ),
            errorTrackImage = R.drawable.ic_coverartarchive_logo_no_text,

            onDropdownIconClick = {},
            dropDown = {},
            color = ListenBrainzTheme.colorScheme.level1,
            titleColor = ListenBrainzTheme.colorScheme.listenText,
            subtitleColor = ListenBrainzTheme.colorScheme.listenText,
            goToArtistPage = {},
            onClick = {},
            onPlayClick = {},
            onReorderClick = {}
        )
    }
}