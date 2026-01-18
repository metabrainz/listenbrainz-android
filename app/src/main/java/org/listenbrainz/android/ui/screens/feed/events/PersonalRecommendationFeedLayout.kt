package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.shared.model.feed.FeedListenArtist
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.components.dialogs.UserTag
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.PreviewSurface
import org.listenbrainz.android.util.Utils

@Composable
fun PersonalRecommendationFeedLayout(
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
    goToUserPage: (String) -> Unit,
    goToArtistPage: (String) -> Unit,
) {
    BaseFeedLayout(
        eventType = FeedEventType.PERSONAL_RECORDING_RECOMMENDATION,
        event = event,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide,
        goToUserPage = goToUserPage
    ) {
        ListenCardSmall(
            trackName = event.metadata.trackMetadata?.trackName ?: "Unknown",
            artists = event.metadata.trackMetadata?.mbidMapping?.artists ?: listOf(FeedListenArtist(event.metadata.trackMetadata?.artistName ?: "" , null, "")),
            coverArtUrl = remember {
                Utils.getCoverArtUrl(
                    caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caaReleaseMbid,
                    caaId = event.metadata.trackMetadata?.mbidMapping?.caaId
                )
            },
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
            blurbContent = { modifier ->
                Column(modifier = modifier) {

                    // Only show "Sent to:" text if user is the one who personally recommended.
                    if (FeedEventType.isUserSelf(event, parentUser)){

                        Row(
                            modifier = Modifier.padding(bottom = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = "Sent to:",
                                style = ListenBrainzTheme.textStyles.feedBlurbContent,
                                color = ListenBrainzTheme.colorScheme.text
                            )

                            LazyRow {

                                items(items = event.metadata.usersList ?: emptyList()) { user ->
                                    Spacer(modifier = Modifier.width(6.dp))

                                    UserTag(user)
                                }
                            }
                        }
                    }

                    event.blurbContent?.let {
                        Text(
                            text = it,
                            style = ListenBrainzTheme.textStyles.feedBlurbContent,
                            color = ListenBrainzTheme.colorScheme.text
                        )
                    }
                }
            },
            goToArtistPage = goToArtistPage,
            onClick = onClick
        )
        
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PersonalRecommendationFeedLayoutPreview() {
    PreviewSurface {
        PersonalRecommendationFeedLayout(
            event = FeedEvent(
                id = 0,
                created = 0,
                type = "like",
                hidden = false,
                metadata = Metadata(
                    blurbContent = "Good song.",
                    usersList = listOf("JasjeetTest", "akshaaatt")
                ),
                username = "Jasjeet"
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
            onReview = {},
            goToUserPage = {},
            goToArtistPage = {}
        )
    }
}
