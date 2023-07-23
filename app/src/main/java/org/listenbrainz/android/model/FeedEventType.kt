package org.listenbrainz.android.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import org.listenbrainz.android.R

enum class FeedEventType (
    val type: String,
    @DrawableRes val icon: Int,
    val isPlayable: Boolean,
    val content: @Composable (FeedEvent, () -> Unit) -> Unit
) {
    
    RECORDING_RECOMMENDATION(
        type = "recording_recommendation",
        icon = R.drawable.feed_send,
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    PERSONAL_RECORDING_RECOMMENDATION(
        type = "personal_recording_recommendation",
        icon = R.drawable.feed_send,
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    RECORDING_PIN(
        type = "recording_pin",
        icon = R.drawable.feed_pin,
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    LIKE(
        type = "like",
        icon = R.drawable.feed_love,
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    LISTEN(
        type = "listen",
        icon = R.drawable.feed_listen,
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    FOLLOW(
        type = "follow",
        icon = R.drawable.feed_follow,
        isPlayable = false,
        content = { event, onClick ->
        
        }
    ),
    
    NOTIFICATION(
        type = "notification",
        icon = R.drawable.feed_notification,
        isPlayable = false,
        content = { event, onClick ->
        
        }
    ),
    
    REVIEW (
        type = "critiquebrainz_review",
        icon = R.drawable.feed_review,
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    /** In case a new event is added in future that had not been published to the app. */
    UNKNOWN(
        type = "update_app",
        icon = R.drawable.feed_unknown,
        isPlayable = false,
        content = { event, onClick ->
        
        }
    );
    
}