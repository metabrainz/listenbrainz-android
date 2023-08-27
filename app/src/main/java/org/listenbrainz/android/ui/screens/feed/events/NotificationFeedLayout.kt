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

/**
 * @param isHidden should be a state.*/
@Composable
fun NotificationFeedLayout(
    event: FeedEvent,
    onDeleteOrHide: () -> Unit,
) {
    BaseFeedLayout(
        eventType = FeedEventType.NOTIFICATION,
        event = event,
        parentUser = "",
        onDeleteOrHide = onDeleteOrHide,
        content = {}
    )
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun NotificationFeedLayoutPreview() {
    ListenBrainzTheme {
        Surface(color = ListenBrainzTheme.colorScheme.background) {
            NotificationFeedLayout(
                event = FeedEvent(
                    id = 0,
                    created = 0,
                    type = "notification",
                    hidden = false,
                    metadata = Metadata(),
                    username = "Jasjeet"
                ),
                onDeleteOrHide = {}
            )
        }
    }
}