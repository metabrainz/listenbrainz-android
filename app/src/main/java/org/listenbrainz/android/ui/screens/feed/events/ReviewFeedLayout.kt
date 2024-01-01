package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import org.listenbrainz.android.R
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils

@Composable
fun ReviewFeedLayout(
    event: FeedEvent,
    parentUser: String,
    onDeleteOrHide: () -> Unit,
    onDropdownClick: () -> Unit,
    onClick: () -> Unit,
    dropdownState: Int?,
    index: Int,
    onOpenInMusicBrainz: () -> Unit,
    onPin: () -> Unit,
    onRecommend: () -> Unit,
    onPersonallyRecommend: () -> Unit,
    onReview: () -> Unit,
    uriHandler: UriHandler = LocalUriHandler.current
) {
    BaseFeedLayout(
        eventType = FeedEventType.REVIEW,
        event = event,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide
    ) {
        
        ListenCardSmall(
            trackName = event.metadata.entityName ?: "Unknown",
            artistName = event.metadata.trackMetadata?.artistName ?: "",
            coverArtUrl = remember {
                Utils.getCoverArtUrl(
                    caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                    caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                )
            },
            enableDropdownIcon = true,
            onDropdownIconClick = onDropdownClick,
            dropDown = {
                SocialDropdown(
                    isExpanded = dropdownState == index,
                    metadata = event.metadata,
                    onDismiss = onDropdownClick,
                    onOpenInMusicBrainz = onOpenInMusicBrainz,
                    onPin = onPin,
                    onRecommend = onRecommend,
                    onPersonallyRecommend = onPersonallyRecommend,
                    onReview = onReview
                )
            },
            enableBlurbContent = true,
            blurbContent = { modifier ->
                val iconSize = 24.dp
                
                Box(modifier = modifier.fillMaxWidth()) {
                    
                    Column(modifier = Modifier.padding(end = iconSize)) {
                        event.metadata.rating?.toFloat()?.let { rating ->
                            Row(
                                modifier = Modifier,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Rating: ",
                                    color = ListenBrainzTheme.colorScheme.text,
                                    style = ListenBrainzTheme.textStyles.feedBlurbContentTitle
                                )
                                RatingBar(
                                    value = rating,
                                    size = 16.dp,
                                    style = RatingBarStyle.Fill(
                                        inActiveColor = Color.Transparent,
                                        activeColor = ListenBrainzTheme.colorScheme.golden
                                    ),
                                    spaceBetween = 1.5.dp,
                                    onValueChange = {},
                                    onRatingChanged = {}
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                        
                        event.blurbContent?.let {
                            Text(
                                text = it,
                                style = ListenBrainzTheme.textStyles.feedBlurbContent,
                                color = ListenBrainzTheme.colorScheme.text
                            )
                        }
                        
                    }
    
                    Icon(
                        modifier = Modifier
                            .size(iconSize)
                            .padding(4.dp)
                            .align(Alignment.TopEnd)
                            .clickable {
                                uriHandler.openUri("https://critiquebrainz.org/review/${event.metadata.reviewMbid}") },
                        painter = painterResource(id = R.drawable.ic_redirect),
                        tint = ListenBrainzTheme.colorScheme.lbSignature,
                        contentDescription = "Go to CritiqueBrainz website"
                    )
                    
                }
            },
            onClick = onClick
        )
        
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ReviewFeedLayoutPreview() {
    ListenBrainzTheme {
        Surface(color = ListenBrainzTheme.colorScheme.background) {
            ReviewFeedLayout(
                event = FeedEvent(
                    id = 0,
                    created = 0,
                    type = "like",
                    hidden = false,
                    metadata = Metadata(
                        rating = 3,
                        blurbContent = "Good song.",
                        entityType = "track"
                    ),
                    username = "JasjeetTest"
                ),
                onDeleteOrHide = {},
                onDropdownClick = {},
                parentUser = "Jasjeet",
                onClick = {},
                dropdownState = null,
                index = 0,
                onOpenInMusicBrainz = {},
                onPin = {},
                onRecommend = {},
                onPersonallyRecommend = {},
                onReview = {}
            )
        }
    }
}