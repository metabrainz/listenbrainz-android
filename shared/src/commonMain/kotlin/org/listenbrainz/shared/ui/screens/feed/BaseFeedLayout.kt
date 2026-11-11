package org.listenbrainz.shared.ui.screens.feed

import kotlin.time.Clock
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import org.listenbrainz.shared.resources.Res
import org.listenbrainz.shared.resources.ic_hide
import org.listenbrainz.shared.resources.ic_unhide
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.listenbrainz.shared.model.Metadata
import org.listenbrainz.shared.model.feed.FeedEvent
import org.listenbrainz.shared.model.feed.FeedEventType
import org.listenbrainz.shared.model.feed.FeedEventType.Companion.getTimeStringForFeed
import org.listenbrainz.shared.model.feed.FeedEventType.Companion.isActionDelete
import org.listenbrainz.shared.ui.theme.ListenBrainzTheme
import org.listenbrainz.shared.util.PreviewSurface

private val EventIconSize = 19.dp

@Composable
fun BaseFeedLayout(
    modifier: Modifier = Modifier,
    eventType: FeedEventType,
    event: FeedEvent,
    parentUser: String,
    onDeleteOrHide: () -> Unit,
    isHidden: Boolean = event.hidden == true,
    goToUserPage: (String) -> Unit,
    content: @Composable () -> Unit,
) {
    val lineColor = ListenBrainzTheme.colorScheme.hint
    val lineTopGap = ListenBrainzTheme.paddings.insideCard

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = ListenBrainzTheme.paddings.horizontal,
                top = ListenBrainzTheme.paddings.vertical,
                end = ListenBrainzTheme.paddings.horizontal
            )
            .drawBehind {
                val x = EventIconSize.toPx() / 2
                drawLine(
                    color = lineColor,
                    start = Offset(x, (EventIconSize + lineTopGap).toPx()),
                    end = Offset(x, size.height),
                    strokeWidth = 5f,
                    cap = StrokeCap.Round
                )
            },
    ) {
        EventIcon(eventType)

        Spacer(modifier = Modifier.width(ListenBrainzTheme.paddings.insideCard))

        Column(modifier = Modifier.weight(1f)) {
            // Tagline
            AnimatedContent(
                targetState = isHidden,
                label = "tagline"
            ) { isHidden ->
                if (!isHidden){
                    eventType.Tagline(
                        event = event,
                        parentUser = parentUser,
                        goToUserPage = goToUserPage,
                        linkStyle = SpanStyle(
                            color = ListenBrainzTheme.colorScheme.lbSignature,
                            fontWeight = FontWeight.Bold
                        ),
                        normalStyle = SpanStyle(
                            color = ListenBrainzTheme.colorScheme.text,
                            fontWeight = FontWeight.Light
                        )
                    )
                } else {
                    Text(
                        text = "This event is hidden.",
                        color = ListenBrainzTheme.colorScheme.text,
                        fontWeight = FontWeight.Light
                    )
                }
            }

            Spacer(modifier = Modifier.height(ListenBrainzTheme.paddings.insideCard))

            // Main Content
            AnimatedVisibility(
                visible = !isHidden,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                content()
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .height(24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Date(
                    event = event,
                    eventType = eventType,
                    parentUser = parentUser,
                    onActionClick = onDeleteOrHide,
                    isHidden = isHidden
                )
            }
        }
    }
}

@Composable
fun Date(
    modifier: Modifier = Modifier,
    event: FeedEvent,
    parentUser: String,
    eventType: FeedEventType,
    isHidden: Boolean = false,
    height: Dp = 24.dp,
    onActionClick: () -> Unit = {}
) {
    
    val timeString = remember(event.created) {
        getTimeStringForFeed(event.created.toLong())
    }
    
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
    
        // Date
        Text(
            text = timeString,
            color = ListenBrainzTheme.colorScheme.hint,
            fontSize = 12.sp
        )
    
        // Delete or hide button
        if (eventType.isDeletable || eventType.isHideable) {
            Icon(
                modifier = Modifier
                    .size(height)
                    .padding(start = 4.dp)
                    .clickable {
                        onActionClick()
                    },
                // TODO: USE CUSTOM ICONS HERE.
                imageVector = if (isActionDelete(event, eventType, parentUser))
                        Icons.Rounded.Delete
                    else if (isHidden)
                        vectorResource(Res.drawable.ic_unhide)
                    else
                        vectorResource(Res.drawable.ic_hide),
                tint = ListenBrainzTheme.colorScheme.lbSignature,
                contentDescription = if (isActionDelete(
                        event,
                        eventType,
                        parentUser
                    )
                ) "Delete this event." else "Hide this event."
            )
        }
    }
    
}


@Composable
private fun EventIcon(eventType: FeedEventType) {
    Image(
        modifier = Modifier.size(EventIconSize),
        painter = painterResource(eventType.icon),
        contentDescription = eventType.name
    )
}


@Preview
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun BaseFeedLayoutPreview() {
    PreviewSurface {
        val event = FeedEventType.RECORDING_PIN
        BaseFeedLayout(
            eventType = event,
            parentUser = "Jasjeet",
            event = FeedEvent(
                id = 0,
                created = Clock.System.now().toEpochMilliseconds(),
                type = "like",
                hidden = false, metadata = Metadata(user1 = "JasjeetTest"),
                username = "Jasjeet"
            ),
            goToUserPage = {},
            onDeleteOrHide = {},
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = CardDefaults.cardColors(containerColor = ListenBrainzTheme.colorScheme.level1),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(20.dp),
                    text = "Content",
                    color = ListenBrainzTheme.colorScheme.text
                )
            }
        }
    }
}