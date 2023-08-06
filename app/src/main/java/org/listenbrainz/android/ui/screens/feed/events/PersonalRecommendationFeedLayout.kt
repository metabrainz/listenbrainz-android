package org.listenbrainz.android.ui.screens.feed.events

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Surface
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.listenbrainz.android.model.FeedEvent
import org.listenbrainz.android.model.FeedEventType
import org.listenbrainz.android.model.Metadata
import org.listenbrainz.android.ui.components.ListenCardSmall
import org.listenbrainz.android.ui.screens.feed.BaseFeedLayout
import org.listenbrainz.android.ui.theme.ListenBrainzTheme
import org.listenbrainz.android.util.Utils

@Composable
fun PersonalRecommendationFeedLayout(
    event: FeedEvent,
    parentUser: String,
    onDeleteOrHide: () -> Unit,
    onDropdownClick: () -> Unit,
    onClick: () -> Unit
) {
    BaseFeedLayout(
        eventType = FeedEventType.PERSONAL_RECORDING_RECOMMENDATION,
        event = event,
        parentUser = parentUser,
        onDeleteOrHide = onDeleteOrHide
    ) {
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
            enableBlurbContent = true,
            onClick = onClick,
            blurbContent = { modifier ->
                Column(modifier = modifier) {
                    
                    // Only show "Sent to:" text if user is the one who personally recommended.
                    if (FeedEventType.isUserSelf(event, parentUser)){
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            
                            Text(
                                text = "Sent to:",
                                style = ListenBrainzTheme.textStyles.feedBlurbContent,
                                color = ListenBrainzTheme.colorScheme.text
                            )
                            
                            LazyRow {
                                
                                items(items = event.metadata.usersList ?: emptyList()) { user ->
                                    Spacer(modifier = Modifier.width(6.dp))
                                    
                                    Card(
                                        shape = CircleShape,
                                        colors = CardDefaults.cardColors(
                                            containerColor = ListenBrainzTheme.colorScheme.lbSignature
                                        )
                                    ) {
                                        Text(
                                            modifier = Modifier.padding(ListenBrainzTheme.paddings.insideCard),
                                            text = user,
                                            style = ListenBrainzTheme.textStyles.feedBlurbContent,
                                            color = ListenBrainzTheme.colorScheme.onLbSignature
                                        )
                                    }
                                }
                            }
                        }
    
                        Spacer(modifier = modifier.height(4.dp))
                    }
                    
                    Text(
                        text = event.blurbContent,
                        style = ListenBrainzTheme.textStyles.feedBlurbContent,
                        color = ListenBrainzTheme.colorScheme.text
                    )
                }
            }
        )
        
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PersonalRecommendationFeedLayoutPreview() {
    ListenBrainzTheme {
        Surface(color = ListenBrainzTheme.colorScheme.background) {
            PersonalRecommendationFeedLayout(
                event = FeedEvent(
                    id = 0,
                    created = 0,
                    type = "like",
                    hidden = false,
                    metadata = Metadata(
                        blurbContent = "Good song.",
                        usersList = listOf("JasjeetTest", "akshaaatt")
                    ),
                    username = "Jasjeet"
                ),
                onDeleteOrHide = {},
                onDropdownClick = {},
                parentUser = "Jasjeet"
            ) {}
        }
    }
}
