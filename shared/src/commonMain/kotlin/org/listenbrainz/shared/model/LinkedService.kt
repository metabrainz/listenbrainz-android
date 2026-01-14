package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable

@Serializable
enum class LinkedService(val code: String, val packageName: String? = null) {
    SPOTIFY("spotify", "com.spotify.music"),
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
