package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme

@Composable
fun FollowFeedLayout(
    event: FeedEvent,
    parentUser: String,
) {
    BaseFeedLayout(
        eventType = FeedEventType.FOLLOW,
        event = event,
        parentUser = parentUser,
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
            FollowFeedLayout(
                event = FeedEvent(
                    id = 0,
                    created = 0,
                    type = "like",
                    hidden = false,
                    metadata = Metadata(
                        user0 = "Jasjeet",
                        user1 = "JasjeetTest"
                    ),
                    username = "Jasjeet"
                ),
                parentUser = "Jasjeet"
            )
        }
    }
}