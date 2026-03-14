package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.shared.model.feed.FeedListenArtist
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.util.PreviewSurface
import org.listenbrainz.android.util.Utils

@Composable
fun RecordingRecommendationFeedLayout(
    event: FeedEvent,
    parentUser: String,
    isHidden: Boolean,
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
        eventType = FeedEventType.RECORDING_RECOMMENDATION,
        event = event,
        isHidden = isHidden,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide, goToUserPage = goToUserPage) {
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
            goToArtistPage = goToArtistPage,
            onClick = onClick
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordingRecommendationFeedCardPreview() {
    PreviewSurface {
        RecordingRecommendationFeedLayout(
            event = FeedEvent(
                id = 0,
                created = 0,
                type = "like",
                hidden = false, metadata = Metadata(),
                username = "Jasjeet"
            ),
            onDeleteOrHide = {},
            onDropdownClick = {},
            parentUser = "Jasjeet",
            isHidden = false,
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
