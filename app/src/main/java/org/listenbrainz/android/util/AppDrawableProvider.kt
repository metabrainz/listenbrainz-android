package org.listenbrainz.android.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import org.listenbrainz.android.R
import org.listenbrainz.shared.util.DrawableResource

@Composable
fun DrawableResource.toDrawableRes(): Int{
    return remember(this){
        when(this){
            DrawableResource.FEED_SEND -> R.drawable.feed_send
            DrawableResource.FEED_PIN -> R.drawable.feed_pin
            DrawableResource.FEED_LOVE -> R.drawable.feed_love
            DrawableResource.FEED_LISTEN -> R.drawable.feed_listen
            DrawableResource.FEED_FOLLOW -> R.drawable.feed_follow
            DrawableResource.FEED_NOTIFICATION -> R.drawable.feed_notification
            DrawableResource.FEED_REVIEW -> R.drawable.feed_review
            DrawableResource.FEED_UNKNOWN -> R.drawable.feed_unknown
        }
    }
}

