package org.listenbrainz.android.util

import org.listenbrainz.android.util.Constants.SPOTIFY_PACKAGE_NAME

object Constants {
    
    const val RECENTLY_PLAYED_KEY = "recently_played"
    const val SPOTIFY_PACKAGE_NAME = "com.spotify.music"
    const val YOUTUBE_MUSIC_PACKAGE_NAME = "com.google.android.apps.youtube.music"
    const val TAG = "ListenBrainz"
    const val FEEDBACK_EMAIL = "support@metabrainz.org"
    const val FEEDBACK_SUBJECT = "[LBAndroid] Feedback"
    const val SPOTIFY_REDIRECT_URI = "org.listenbrainz.android://callback"
    const val LISTENBRAINZ_API_BASE_URL = "https://api.listenbrainz.org/1/"
    const val LISTENBRAINZ_BETA_API_BASE_URL = "https://beta-api.listenbrainz.org/1/"
    const val ONBOARDING = "onboarding_lb"
    const val ABOUT_URL = "https://listenbrainz.org/about"

    object Strings {
        const val TIMESTAMP = "timestamp"
        const val PREFERENCE_LISTENING_BLACKLIST = "listening_blacklist"
        const val PREFERENCE_LISTENING_WHITELIST = "listening_whitelist"
        const val PREFERENCE_SUBMIT_LISTENS = "submit_listens"
        const val PREFERENCE_LISTEN_NEW_PLAYERS = "listen_new_players"
        const val PREFERENCE_LISTENING_APPS = "listening_apps"
        const val PREFERENCE_SYSTEM_THEME = "app_theme"
        const val PREFERENCE_PERMS = "perms_code"
        const val PREFERENCE_ALBUMS_ON_DEVICE = "PREFERENCE_ALBUMS_ON_DEVICE"
        const val PREFERENCE_SONGS_ON_DEVICE = "PREFERENCE_SONGS_ON_DEVICE"
        const val LINKED_SERVICES = "LINKED_SERVICES"
        const val USERNAME = "username"
        const val CURRENT_PLAYABLE = "CURRENT_PLAYABLE"
        const val LB_ACCESS_TOKEN = "lb_access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val STATUS_LOGGED_IN = 1
        const val STATUS_LOGGED_OUT = 0

        const val CHANNEL_NOTI_SCROBBLING = "noti_scrobbling"
        const val CHANNEL_NOTI_SCR_ERR = "noti_scrobble_errors"
        const val CHANNEL_NOTI_NEW_APP = "noti_new_app"
        const val CHANNEL_NOTI_PENDING = "noti_pending_scrobbles"
        const val CHANNEL_ID = "listen_scrobble_channel"

        const val CHANNEL_PIXEL_NP =
            "com.google.intelligence.sense.ambientmusic.MusicNotificationChannel"
        const val PACKAGE_PIXEL_NP = "com.google.intelligence.sense"
        const val PACKAGE_PIXEL_NP_R = "com.google.android.as"
        const val PACKAGE_PIXEL_NP_AMM = "com.kieronquinn.app.pixelambientmusic"
        const val PACKAGE_SHAZAM = "com.shazam.android"
    }
    
}

enum class LinkedService(val code: String, val packageName: String? = null) {
    
    SPOTIFY("spotify", SPOTIFY_PACKAGE_NAME),
    CRITIQUEBRAINZ("critiquebrainz"),
    MUSICBRAINZ("musicbrainz"),
    UNKNOWN("");
    
    companion object {
        fun String.toLinkedService(): LinkedService {
            return when (this[0]) {
                's' -> SPOTIFY
                'c' -> CRITIQUEBRAINZ
                'm' -> MUSICBRAINZ
                else -> UNKNOWN
            }
        }
    }
    
}