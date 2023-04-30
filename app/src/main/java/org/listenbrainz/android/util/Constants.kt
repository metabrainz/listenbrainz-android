package org.listenbrainz.android.util

object Constants {
    const val RECENTLY_PLAYED_KEY = "recently_played"
    const val SPOTIFY_PACKAGE_NAME = "com.spotify.music"
    const val YOUTUBE_MUSIC_PACKAGE_NAME = "com.google.android.apps.youtube.music"
    const val TAG = "ListenBrainz"
    const val FEEDBACK_EMAIL = "support@metabrainz.org"
    const val FEEDBACK_SUBJECT = "[LBAndroid] Feedback"
    const val SPOTIFY_REDIRECT_URI = "org.listenbrainz.android://callback"
    const val MUSICBRAINZ_AUTH_BASE_URL = "https://musicbrainz.org/oauth2/"
    const val LISTENBRAINZ_API_BASE_URL = "https://api.listenbrainz.org/"
    const val CLIENT_ID = "XqCukyOoCAH9PRrHpwiINlvJ1T-x4ffQ"
    const val CLIENT_SECRET = "CJrUcF_jyzj-MVCPhNXSwPIpu_eeb_Ye"
    const val OAUTH_REDIRECT_URI = "org.listenbrainz.android://oauth"

    object Strings {
        const val TIMESTAMP = "timestamp"
        const val PREFERENCE_LISTENING_BLACKLIST = "listening_blacklist"
        const val PREFERENCE_LISTENING_APPS = "listening_apps"
        const val PREFERENCE_LISTENING_ENABLED = "listening_enabled"
        const val PREFERENCE_LISTENING_SPOTIFY = "listening_spotify_enabled"
        const val PREFERENCE_SYSTEM_LANGUAGE = "use_english"
        const val PREFERENCE_SYSTEM_THEME = "app_theme"
        const val PREFERENCE_PERMS = "perms_code"
        const val PREFERENCE_ONBOARDING = "onboarding"
        const val PREFERENCE_ALBUMS_ON_DEVICE = "PREFERENCE_ALBUMS_ON_DEVICE"
        const val PREFERENCE_SONGS_ON_DEVICE = "PREFERENCE_SONGS_ON_DEVICE"
        const val USERNAME = "username"
        const val CURRENT_PLAYABLE = "CURRENT_PLAYABLE"
        const val MB_ACCESS_TOKEN = "mb_access_token"
        const val LB_ACCESS_TOKEN = "lb_access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val STATUS_LOGGED_IN = 1
        const val STATUS_LOGGED_OUT = 0
    }
}