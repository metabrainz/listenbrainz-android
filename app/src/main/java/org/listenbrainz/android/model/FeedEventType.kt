package org.listenbrainz.android.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable

enum class FeedEventType (
    val type: String,
    @DrawableRes val icon: Int = 0,
    val isPlayable: Boolean,
    val content: @Composable (FeedEvent, () -> Unit) -> Unit
) {
    
    RECORDING_RECOMMENDATION(
        type = "recording_recommendation",
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    PERSONAL_RECORDING_RECOMMENDATION(
        type = "personal_recording_recommendation",
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    RECORDING_PIN(
        type = "recording_pin",
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    LIKE(
        type = "like",
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    LISTEN(
        type = "listen",
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    FOLLOW(
        type = "follow",
        isPlayable = false,
        content = { event, onClick ->
        
        }
    ),
    
    NOTIFICATION(
        type = "notification",
        isPlayable = false,
        content = { event, onClick ->
        
        }
    ),
    
    REVIEW (
        type = "critiquebrainz_review",
        isPlayable = true,
        content = { event, onClick ->
        
        }
    ),
    
    /** In case a new event is added in future that had not been published to the app. */
    UNKNOWN(
        type = "update_app",
        isPlayable = false,
        content = { event, onClick ->
        
        }
    );
    
}