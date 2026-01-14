package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.util.PreviewSurface

@Composable
fun UnknownFeedLayout(
    event: FeedEvent
) {
    BaseFeedLayout(
        eventType = FeedEventType.UNKNOWN,
        event = event,
        parentUser = "",
        onDeleteOrHide = {},
        content = {},
        goToUserPage = {}
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun FollowFeedLayoutPreview() {
    PreviewSurface {
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