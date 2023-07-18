package org.listenbrainz.android.model

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable

enum class FeedEventType (
    val type: String,
    @DrawableRes val icon: Int = 0,
    val isPlayable: Boolean,
    val content: @Composable () -> Unit
) {
    
    RECORDING_RECOMMENDATION(
        type = "recording_recommendation",
        isPlayable = true,
        content = {
        
        }
    ),
    
    PERSONAL_RECORDING_RECOMMENDATION(
        type = "personal_recording_recommendation",
        isPlayable = true,
        content = {
        
        }
    ),
    
    RECORDING_PIN(
        type = "recording_pin",
        isPlayable = true,
        content = {
        
        }
    ),
    
    LIKE(
        type = "like",
        isPlayable = true,
        content = {
        
        }
    ),
    
    LISTEN(
        type = "listen",
        isPlayable = true,
        content = {
        
        }
    ),
    
    FOLLOW(
        type = "follow",
        isPlayable = false,
        content = {
        
        }
    ),
    
    NOTIFICATION(
        type = "notification",
        isPlayable = false,
        content = {
        
        }
    ),
    
    REVIEW (
        type = "critiquebrainz_review",
        isPlayable = true,
        content = {
        
        }
    ),
    
    /** In case a new event is added in future that had not been published to the app. */
    UNKNOWN(
        type = "update_app",
        isPlayable = false,
        content = {
        
        }
    );
    
}