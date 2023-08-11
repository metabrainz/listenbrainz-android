package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils

@Composable
fun RecordingRecommendationFeedLayout(
    event: FeedEvent,
    parentUser: String,
    isHidden: Boolean,
    onDeleteOrHide: () -> Unit,
    onDropdownClick: () -> Unit,
    onClick: () -> Unit
) {
    BaseFeedLayout(
        eventType = FeedEventType.RECORDING_RECOMMENDATION,
        event = event,
        isHidden = isHidden,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide,) {
        ListenCardSmall(
            releaseName = event.metadata.trackMetadata?.releaseName ?: "Unknown",
            artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
            coverArtUrl = remember {
                Utils.getCoverArtUrl(
                    caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caa_release_mbid,
                    caaId = event.metadata.trackMetadata?.mbidMapping?.caa_id
                )
            },
            enableDropdownIcon = true,
            onDropdownIconClick = onDropdownClick,
            onClick = onClick
        )
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun RecordingRecommendationFeedCardPreview() {
    ListenBrainzTheme {
        Surface(color = ListenBrainzTheme.colorScheme.background) {
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
                isHidden = false
            ) {}
        }
    }
}
