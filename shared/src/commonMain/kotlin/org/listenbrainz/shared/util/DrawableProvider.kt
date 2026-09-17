package org.listenbrainz.shared.util

enum class DrawableResource {
    FEED_SEND,
    FEED_PIN,
    FEED_LOVE,
    FEED_LISTEN,
    FEED_FOLLOW,
    FEED_NOTIFICATION,
    FEED_REVIEW,
    FEED_UNKNOWN,
    IC_LISTENBRAINZ_LOGO_NO_TEXT
}


interface DrawableProvider{
    fun getDrawable(res: DrawableResource): Int
}