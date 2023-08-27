package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun UnknownFeedLayout(
    event: FeedEvent
) {
    BaseFeedLayout(
        eventType = FeedEventType.UNKNOWN,
        event = event,
        parentUser = "",
        onDeleteOrHide = {},
        content = {}
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FollowFeedLayoutPreview() {
    ListenBrainzTheme {
        Surface(color = ListenBrainzTheme.colorScheme.background) {
            UnknownFeedLayout(
                event = FeedEvent(
                    id = 0,
                    created = 0,
                    type = "unknown",
                    hidden = false,
                    metadata = Metadata(),
                    username = "Jasjeet"
                )
            )
        }
    }
}