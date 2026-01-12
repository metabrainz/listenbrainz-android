package org.listenbrainz.android.util

import kotlinx.serialization.Serializable
import org.listenbrainz.android.util.Constants.SPOTIFY_PACKAGE_NAME

object Constants {
    

    const val SPOTIFY_PACKAGE_NAME = "com.spotify.music"
    const val YOUTUBE_MUSIC_PACKAGE_NAME = "com.google.android.apps.youtube.music"
    const val TAG = "ListenBrainz"
    const val FEEDBACK_EMAIL = "support@metabrainz.org"
    const val FEEDBACK_SUBJECT = "[LBAndroid] Feedback"
    const val SPOTIFY_REDIRECT_URI = "org.listenbrainz.android://callback"
    const val LISTENBRAINZ_API_BASE_URL = "https://api.listenbrainz.org/1/"
    const val LISTENBRAINZ_BETA_API_BASE_URL = "https://beta-api.listenbrainz.org/1/"

    const val ABOUT_URL = "https://listenbrainz.org/about"
    const val LB_BASE_URL = "https://listenbrainz.org/"
    const val MB_BASE_URL = "https://musicbrainz.org/"
    const val CB_BASE_URL = "https://critiquebrainz.org/"

    const val GITHUB_API_BASE_URL = "https://api.github.com/repos/metabrainz/listenbrainz-android/"

    // App update constants
    const val VERSION_CHECK_DURATION = 1
    const val RE_PROMPT_USER_AFTER_DENIAL = 10

    object Strings {
        const val TIMESTAMP = "timestamp"


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

@Serializable
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