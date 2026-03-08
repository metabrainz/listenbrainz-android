package org.listenbrainz.android.ui.screens.feed.events

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import org.listenbrainz.android.model.feed.FeedEvent
import org.listenbrainz.android.model.feed.FeedEventType
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
@Composable
fun ThanksFeedLayout(
    event: FeedEvent,
    parentUser: String,
    goToUserPage: (String) -> Unit,
    referencedEvent:FeedEvent? = null
) {

    val thanker = event.metadata.thankerUsername ?: event.username ?: "Someone"
    val thankee = event.metadata.thankeeUsername ?: "you"
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
                text = "$thanker thanked $thankee for recommending a track",
                color = ListenBrainzTheme.colorScheme.text
            )

            if (!message.isNullOrBlank()) {
                Text(
                    text = message,
                    style = ListenBrainzTheme.textStyles.feedBlurbContent,
                    color = ListenBrainzTheme.colorScheme.text
                )
            }
   Spacer(modifier = Modifier.height(8.dp))

            // Render original event card
            referencedEvent?.let { originalEvent ->

    val originalType = FeedEventType.resolveEvent(originalEvent)

    // Prevent recursive THANKS rendering
    if (originalType != FeedEventType.THANKS) {

        Spacer(modifier = Modifier.height(8.dp))

        originalType.Content(
            event = originalEvent,
            referencedEvent = null, // avoid deep nesting
            parentUser = parentUser,
            isHidden = false,
            onDeleteOrHide = {},
            onDropdownClick = {},
            dropDownState = null,
            index = 0,
            onOpenInMusicBrainz = {},
            onRecommend = {},
            onPersonallyRecommend = {},
            onReview = {},
            onPin = {},
            onClick = {},
            goToUserPage = goToUserPage,
            goToArtistPage = {}
     )
            }
        }
    }
}
}