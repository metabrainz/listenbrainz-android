package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.screens.feed.SocialDropdown
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.getCoverArtUrl

@Composable
fun ListenLikeFeedLayout(
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
    onReview: () -> Unit
) {
    BaseFeedLayout(
        eventType = FeedEventType.LIKE,
        event = event,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide,
    ) {
        ListenCardSmall(
            trackName = event.metadata.trackMetadata?.trackName ?: "Unknown",
            artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
            coverArtUrl = remember {
                getCoverArtUrl(
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
            onClick = onClick
        )
    }
}

@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun ListenLikeFeedLayoutPreview() {
    ListenBrainzTheme {
        Surface(color = ListenBrainzTheme.colorScheme.background) {
            ListenLikeFeedLayout(
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