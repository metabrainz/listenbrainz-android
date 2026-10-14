package org.listenbrainz.android.util

import org.listenbrainz.android.R
import org.listenbrainz.shared.util.DrawableProvider
import org.listenbrainz.shared.util.DrawableResource

class AppDrawableProvider (): DrawableProvider {
    override fun getDrawable(res: DrawableResource): Int {
        return when (res) {
            DrawableResource.FEED_SEND -> R.drawable.feed_send
            DrawableResource.FEED_PIN -> R.drawable.feed_pin
            DrawableResource.FEED_LOVE -> R.drawable.feed_love
            DrawableResource.FEED_LISTEN -> R.drawable.feed_listen
            DrawableResource.FEED_FOLLOW -> R.drawable.feed_follow
            DrawableResource.FEED_NOTIFICATION -> R.drawable.feed_notification
            DrawableResource.FEED_REVIEW -> R.drawable.feed_review
            DrawableResource.FEED_UNKNOWN -> R.drawable.feed_unknown
            DrawableResource.IC_LISTENBRAINZ_LOGO_NO_TEXT -> R.drawable.ic_listenbrainz_logo_no_text
        }
    }
}

