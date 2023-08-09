package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils.getCoverArtUrl

@Composable
fun ListenLikeFeedLayout(
    event: FeedEvent,
    parentUser: String,
    onDeleteOrHide: () -> Unit,
    onDropdownClick: () -> Unit,
    onClick: () -> Unit
) {
    BaseFeedLayout(
        eventType = FeedEventType.LIKE,
        event = event,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide,
    ) {
        ListenCardSmall(
            releaseName = event.metadata.trackMetadata?.releaseName ?: "Unknown",
            artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
            coverArtUrl = getCoverArtUrl(
                caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caa_release_mbid,
                caaId = event.metadata.trackMetadata?.mbidMapping?.caa_id
            ),
            enableDropdownIcon = true,
            onDropdownIconClick = onDropdownClick,
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
                    eventType = "like",
                    hidden = false, metadata = Metadata(),
                    username = "Jasjeet"
                ),
                onDeleteOrHide = {},
                onDropdownClick = {},
                parentUser = "Jasjeet"
            ) {}
        }
    }
}