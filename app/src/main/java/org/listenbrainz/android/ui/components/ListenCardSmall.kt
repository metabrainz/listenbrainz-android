package org.listenbrainz.android.ui.components
import org.listenbrainz.android.viewmodel.LikeState

import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlinx.coroutines.CoroutineScope
import org.listenbrainz.android.R
import org.listenbrainz.android.model.AdditionalInfo
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.ResponseError
import org.listenbrainz.android.model.TrackMetadata
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.feed.FeedListenArtist
import org.listenbrainz.android.ui.screens.feed.SocialDropdownDefault
import org.listenbrainz.android.ui.screens.feed.events.LikeDislikeButton
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.thenIf

/**Small configuration of listen card.
 * This composable has fixed height used from [ListenBrainzTheme.sizes].
 * @param enableTrailingContent True means card will reserve space for trailing content.
 * @param trailingContent **MUST** use the given modifier in its top level layout.
 * @param dropDown The dropdown we want to show when icon is clicked.
 * @param dropDownState State of the dropdown icon. Usually, in case of a lazy list, we would want to supply a
 * mutable state list or map with index/key rather than maintaining a state for each listen card. False means
 * dropdown should remain closed.
 * @author jasje*/
@Composable
private fun TimestampText(time: Long) {
    Text(
        text = FeedEventType.getTimeStringForFeed(time),
        color = ListenBrainzTheme.colorScheme.hint,
        fontSize = ListenBrainzTheme.textStyles.listenSubtitle.fontSize,
        maxLines = 1,
    )
}

@Composable
fun ListenCardSmall(
    modifier: Modifier = Modifier,
    trackName: String,
    artists: List<FeedListenArtist?>,
    coverArtUrl: String?,
    listenedAt: Long? = null,
    @DrawableRes errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text,
    onDropdownIconClick: () -> Unit = {},
    titleColor: Color = ListenBrainzTheme.colorScheme.listenText,
    subtitleColor: Color = titleColor.copy(alpha = 0.7f),
    goToArtistPage: (String) -> Unit,
    dropDown: @Composable (() -> Unit)? = null,
    trailingContent: @Composable ((modifier: Modifier) -> Unit)? = null,
    blurbContent: @Composable (ColumnScope.(modifier: Modifier) -> Unit)? = null,
    titleAndSubtitle: @Composable (modifier: Modifier) -> Unit = @Composable {
        TitleAndSubtitle(
            modifier = it.padding(end = 6.dp),
            title = trackName,
            artists = artists,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            goToArtistPage = goToArtistPage
        )
    },
    coverArt: @Composable (modifier: Modifier) -> Unit = @Composable {
        AlbumArt(
            coverArtUrl = coverArtUrl,
            modifier = it,
            errorAlbumArt = errorAlbumArt
        )
    },
    onLikeClick: (() -> Unit)? = null,
    onDislikeClick: (() -> Unit)? = null,
    likeState: LikeState = LikeState.NEUTRAL,
            preCoverArtContent: @Composable ((modifier: Modifier) -> Unit)? = null,
    isPlaying: Boolean = false,
    color: Color = if (isPlaying) {
        ListenBrainzTheme.colorScheme.level2
    } else {
        ListenBrainzTheme.colorScheme.level1
    },
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = true) { onClick() },
        shape = ListenBrainzTheme.shapes.listenCardSmall,
        shadowElevation = 4.dp,
        color = color
    ) {
        val showTrailingContent = trailingContent != null
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ListenBrainzTheme.sizes.listenCardHeight),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    preCoverArtContent?.let {
                        it(Modifier)
                    }

                    coverArt(Modifier.size(ListenBrainzTheme.sizes.listenCardHeight))
            
                    Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.coverArtAndTextGap))

                    titleAndSubtitle(Modifier.weight(1f))
                }
             }

                Row(
                    modifier = Modifier.weight(0.35f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End
                ) {
                    // Trailing content
                    if (showTrailingContent) {
                        val modifier = Modifier.thenIf(dropDown == null) {
                            padding(end = 6.dp)
                        }
                        trailingContent(modifier)
                    } else
                        listenedAt?.let { TimestampText(it) }

                    // Dropdown Icon
                    if (dropDown != null) {
                        Column(
                            modifier = Modifier
                                .weight(1f, fill = false)
                                .padding(horizontal = 8.dp),
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.Center
                        ) {
                            DropdownButton(
                                modifier = Modifier
                                    .thenIf(showTrailingContent && listenedAt != null) {
                                        padding(top = ListenBrainzTheme.paddings.insideButton)
                                    }
                                    .weight(1f, fill = false),
                                onDropdownIconClick = onDropdownIconClick
                            )

                            if (showTrailingContent) {
                                listenedAt?.TimestampText()
                            }
                        }
                        dropDown()
                    }
                }
            }
            
            blurbContent?.let {
                HorizontalDivider(color = ListenBrainzTheme.colorScheme.hint)

                blurbContent(Modifier.padding(ListenBrainzTheme.paddings.insideCard))
            }

            if (onLikeClick != null && onDislikeClick != null) {
                val currentLikeState = remember { mutableStateOf(likeState) }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LikeDislikeButton(
                        likeState = currentLikeState.value,
                        onTap = {
                            onLikeClick()
                            currentLikeState.value = LikeState.LIKED
                        },
                        onLongPress = {
                            onDislikeClick()
                            currentLikeState.value = LikeState.DISLIKED
                        }
                    )
                }
            }

        }
    }
}

@Composable
fun ListenCardSmall(
    modifier: Modifier = Modifier,
    trackName: String,
    artist: String,
    coverArtUrl: String?,
    listenedAt: Long? = null,
    @DrawableRes errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text,
    onDropdownIconClick: () -> Unit = {},
    dropDown: @Composable () -> Unit = {},
    trailingContent: @Composable (modifier: Modifier) -> Unit = {},
    blurbContent: @Composable (ColumnScope.(modifier: Modifier) -> Unit)? = null,
    isPlaying: Boolean = false,
    color: Color = if (isPlaying) {
        ListenBrainzTheme.colorScheme.level2
    } else {
        ListenBrainzTheme.colorScheme.level1
    },
    titleColor: Color = ListenBrainzTheme.colorScheme.listenText,
    subtitleColor: Color = titleColor.copy(alpha = 0.7f),
    goToArtistPage: (String) -> Unit,
    onClick: () -> Unit,
    onLikeClick: (() -> Unit)? = null,
    onDislikeClick: (() -> Unit)? = null,
    likeState: LikeState = LikeState.NEUTRAL
) {
    ListenCardSmall(
        modifier = modifier,
        trackName = trackName,
        artists = listOf(FeedListenArtist(artist, null, "")),
        coverArtUrl = coverArtUrl,
        listenedAt = listenedAt,
        errorAlbumArt = errorAlbumArt,
        onDropdownIconClick = onDropdownIconClick,
        dropDown = dropDown,
        trailingContent = trailingContent,
        blurbContent = blurbContent,
        isPlaying = isPlaying,
        color = color,
        titleColor = titleColor,
        subtitleColor = subtitleColor,
        goToArtistPage = goToArtistPage,
        onClick = onClick,
        onLikeClick = onLikeClick,
        onDislikeClick = onDislikeClick,
        likeState = likeState,
    )
}

@Composable
fun ListenCardSmallDefault(
    modifier: Modifier = Modifier,
    metadata: Metadata,
    coverArtUrl: String?,
    @DrawableRes errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text,
    trailingContent: @Composable ((modifier: Modifier) -> Unit)? = null,
    blurbContent: @Composable (ColumnScope.(modifier: Modifier) -> Unit)? = null,
    preCoverArtContent: @Composable ((modifier: Modifier) -> Unit)? = null,
    color: Color = ListenBrainzTheme.colorScheme.level1,
    titleColor: Color = ListenBrainzTheme.colorScheme.listenText,
    subtitleColor: Color = titleColor.copy(alpha = 0.7f),
    onDropdownError: suspend CoroutineScope.(error: ResponseError) -> Unit,
    onDropdownSuccess: suspend CoroutineScope.(message: String) -> Unit,
    onRemoveFromPlaylist: (() -> Unit)? = null,
    goToArtistPage: (String) -> Unit,
    onLikeClick: (() -> Unit)? = null,
    onDislikeClick: (() -> Unit)? = null,
    likeState: LikeState = LikeState.NEUTRAL
) {
    metadata.trackMetadata?.let {
        var isDropdownExpanded by remember { mutableStateOf(false) }

        ListenCardSmall(
            modifier = modifier,
            trackName = metadata.trackMetadata.trackName,
            artists = metadata.trackMetadata.mbidMapping?.artists ?: listOf(
                FeedListenArtist(metadata.trackMetadata.artistName , null, "")
            ),
            coverArtUrl = coverArtUrl,
            errorAlbumArt = errorAlbumArt,
            onDropdownIconClick = {
                isDropdownExpanded = !isDropdownExpanded
            },
            listenedAt = metadata.listenedAt ?: metadata.created,
            dropDown = {
                SocialDropdownDefault(
                    isExpanded = isDropdownExpanded,
                    metadata = metadata,
                    onError = onDropdownError,
                    onSuccess = onDropdownSuccess,
                    onRemoveFromPlaylist = onRemoveFromPlaylist,
                    onDropdownDismiss = { isDropdownExpanded = !isDropdownExpanded },
                )
            },
            preCoverArtContent = preCoverArtContent,
            trailingContent = trailingContent,
            blurbContent = blurbContent,
            color = color,
            titleColor = titleColor,
            subtitleColor = subtitleColor,
            goToArtistPage = goToArtistPage,
            onLikeClick = onLikeClick,
            onDislikeClick = onDislikeClick,
            likeState = likeState,
            )
    }
}

@Composable
private fun DropdownButton(modifier: Modifier = Modifier, onDropdownIconClick: () -> Unit) {
    Icon(
        painter = painterResource(id = R.drawable.ic_options),
        contentDescription = "",
        tint = ListenBrainzTheme.colorScheme.hint,
        modifier = modifier
            .clickable(onClick = onDropdownIconClick)
            .padding(horizontal = ListenBrainzTheme.paddings.insideButton)
    )
}

@Composable
private fun AlbumArt(
    coverArtUrl: String?,
    modifier: Modifier = Modifier,
    errorAlbumArt: Int = R.drawable.ic_coverartarchive_logo_no_text
) {
    // Use this for previews
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(coverArtUrl)
            .build(),
        fallback = painterResource(id = errorAlbumArt),
        error = painterResource(id = errorAlbumArt),
        modifier = modifier
            .clip(GenericShape { size, _ ->
                addRect(Rect(0f, 0f, size.width*0.95f, size.height))
            }),
        contentScale = ContentScale.Crop,
        placeholder = painterResource(id = errorAlbumArt),
        filterQuality = FilterQuality.Low,
        contentDescription = "Album Cover Art"
    )
}

/** [title] corresponds to release name and [artists] corresponds to all the artists as per
 * MB's credit system.
 * The [artists] list consists of artist names and join phrases used to join multiple artists together*/
@Composable
fun TitleAndSubtitle(
    modifier: Modifier = Modifier,
    title: String,
    artists: List<FeedListenArtist?>,
    alignment: Alignment.Horizontal = Alignment.Start,
    titleColor: Color = ListenBrainzTheme.colorScheme.listenText,
    subtitleColor: Color = titleColor.copy(alpha = 0.7f),
    goToArtistPage: (String) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = alignment
    ) {
        Text(
            text = title,
            style = ListenBrainzTheme.textStyles.listenTitle,
            color = titleColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row {
            artists.forEach { artist ->
                artist?.artistCreditName?.let {
                    fun Modifier.goToArtistPage() =
                        if(artist.artistMbid != null){
                            this.clickable {
                                goToArtistPage(artist.artistMbid)
                            }
                        } else
                            this

                    Text(
                        modifier = Modifier.goToArtistPage(),
                        text = artist.artistCreditName + (artist.joinPhrase ?: ""),
                        style = ListenBrainzTheme.textStyles.listenSubtitle,
                        color = subtitleColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun ListenCardSmallPreview() {
    ListenBrainzTheme {
        ListenCardSmallDefault(
            metadata = Metadata(
                listenedAt = System.currentTimeMillis() / 1000,
                trackMetadata = TrackMetadata(
                    trackName = "Title",
                    artistName = "Artist",
                    additionalInfo = AdditionalInfo(),
                    releaseName = "",
                    mbidMapping = null
                ),
            ),
            coverArtUrl = "",
            trailingContent = { modifier ->
                Column(modifier = modifier) {
                    TitleAndSubtitle(title = "Userrrrrrrrrrrrrr", goToArtistPage = {}, artists = listOf(FeedListenArtist("Artist", "", "")),)
                }
            },
            goToArtistPage = {},
            blurbContent = {
                Column(modifier = it) {
                    Text(text = "Blurb Content", color = ListenBrainzTheme.colorScheme.text)
                }
            },
            onDropdownSuccess = {},
            onDropdownError = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ListenCardSmallNoTrailingContentPreview() {
    ListenBrainzTheme {
        ListenCardSmallDefault(
            metadata = Metadata(
                listenedAt = System.currentTimeMillis() / 1000,
                trackMetadata = TrackMetadata(
                    trackName = "Title",
                    artistName = "Artist",
                    additionalInfo = AdditionalInfo(),
                    releaseName = "",
                    mbidMapping = null
                ),
            ),
            coverArtUrl = "",
            goToArtistPage = {},
            blurbContent = {
                Column(modifier = it) {
                    Text(text = "Blurb Content", color = ListenBrainzTheme.colorScheme.text)
                }
            },
            onDropdownSuccess = {},
            onDropdownError = {},
            onLikeClick = {}
        )
    }
}