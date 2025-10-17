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
    const val ONBOARDING = "onboarding_lb_updated"
    const val ABOUT_URL = "https://listenbrainz.org/about"
    const val LB_BASE_URL = "https://listenbrainz.org/"
    const val MB_BASE_URL = "https://musicbrainz.org/"
    const val CB_BASE_URL = "https://critiquebrainz.org/"

    const val GITHUB_API_BASE_URL = " https://api.github.com/repos/metabrainz/listenbrainz-android/"

    // App update constants
    const val VERSION_CHECK_DURATION = 1
    const val RE_PROMPT_USER_AFTER_DENIAL = 10

    object Strings {
        const val TIMESTAMP = "timestamp"
        const val PREFERENCE_LISTENING_BLACKLIST = "listening_blacklist"
        const val PREFERENCE_LISTENING_WHITELIST = "listening_whitelist"
        const val PREFERENCE_SUBMIT_LISTENS = "submit_listens"
        const val PREFERENCE_LISTEN_NEW_PLAYERS = "listen_new_players"
        const val PREFERENCE_LISTENING_APPS = "listening_apps"
        const val PREFERENCE_SYSTEM_THEME = "app_theme"
        const val PREFERENCE_PERMS = "perms_code"
        const val PREFERENCE_LOGIN_CONSENT_SCREEN_CACHE = "login_consent_screen_cache"
        const val PREFERENCE_ALBUMS_ON_DEVICE = "PREFERENCE_ALBUMS_ON_DEVICE"
        const val PREFERENCE_SONGS_ON_DEVICE = "PREFERENCE_SONGS_ON_DEVICE"
        const val PREFERENCE_REQUESTED_PERMISSIONS = "requested_permissions"
        const val PREFERENCE_INSTALL_SOURCE = "install_source"
        const val PREFERENCE_APP_LAUNCH_COUNT = "app_launch_count"
        const val PREFERENCE_LAST_VERSION_CHECK_LAUNCH_COUNT = "last_version_check_launch_count"
        const val PREFERENCE_LAST_UPDATE_PROMPT_LAUNCH_COUNT = "last_update_prompt_launch_count"
        const val PREFERENCE_DOWNLOAD_ID = "download_id"
        const val CRASH_REPORT_ENABLED = "is_crash_reporting_enabled"
        const val LINKED_SERVICES = "LINKED_SERVICES"
        const val USERNAME = "username"
        const val CURRENT_PLAYABLE = "CURRENT_PLAYABLE"
        const val LB_ACCESS_TOKEN = "lb_access_token"
        const val REFRESH_TOKEN = "refresh_token"
        const val STATUS_LOGGED_IN = 1
        const val STATUS_LOGGED_OUT = 0
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