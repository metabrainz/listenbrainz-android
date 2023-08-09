package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils

@Composable
fun ReviewFeedLayout(
    event: FeedEvent,
    parentUser: String,
    onDeleteOrHide: () -> Unit,
    onDropdownClick: () -> Unit,
    onClick: () -> Unit
) {
    BaseFeedLayout(
        eventType = FeedEventType.REVIEW,
        event = event,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide
    ) {
        
        ListenCardSmall(
            releaseName = event.metadata.trackMetadata?.releaseName ?: "Unknown",
            artistName = event.metadata.trackMetadata?.artistName ?: "Unknown",
            coverArtUrl = Utils.getCoverArtUrl(
                caaReleaseMbid = event.metadata.trackMetadata?.mbidMapping?.caa_release_mbid,
                caaId = event.metadata.trackMetadata?.mbidMapping?.caa_id
            ),
            enableDropdownIcon = true,
            onDropdownIconClick = onDropdownClick,
            enableBlurbContent = true,
            blurbContent = { modifier ->
                Column(modifier = modifier) {
                    Text(
                        text = event.blurbContent,
                        style = ListenBrainzTheme.textStyles.feedBlurbContent,
                        color = ListenBrainzTheme.colorScheme.text
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
                    eventType = "like",
                    hidden = false,
                    metadata = Metadata(
                        blurbContent = "Good song.",
                        entityType = "track"
                    ),
                    username = "JasjeetTest"
                ),
                onDeleteOrHide = {},
                onDropdownClick = {},
                parentUser = "Jasjeet"
            ) {}
        }
    }
}