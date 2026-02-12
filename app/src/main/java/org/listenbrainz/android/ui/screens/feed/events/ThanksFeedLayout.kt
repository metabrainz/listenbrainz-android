package org.listenbrainz.android.ui.screens.feed.events

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
@Composable
fun ThanksFeedLayout(
    event: FeedEvent,
    parentUser: String,
    goToUserPage: (String) -> Unit
) {

    val thanker = event.metadata.thankerUsername ?: event.username ?: "Someone"
    val thankee = event.metadata.thankeeUsername ?: "a user"
    val message = event.metadata.blurbContent

    BaseFeedLayout(
        eventType = FeedEventType.THANKS,
        event = event,
        parentUser = parentUser,
        onDeleteOrHide = {},
        goToUserPage = goToUserPage,
    ) {
        Column {

            Text(
                text = "$thanker thanked $thankee",
                color = ListenBrainzTheme.colorScheme.text
            )

            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    style = ListenBrainzTheme.textStyles.feedBlurbContent,
                    color = ListenBrainzTheme.colorScheme.text
                )
            }
        }
    }
}
