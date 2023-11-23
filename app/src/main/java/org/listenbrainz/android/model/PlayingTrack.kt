package org.listenbrainz.android.model

import android.media.MediaMetadata
import android.media.session.PlaybackState
import org.listenbrainz.android.util.ListenSubmissionState.Companion.extractArtist
import org.listenbrainz.android.util.ListenSubmissionState.Companion.extractTitle

/** Track metadata class for Listen Scrobble service.*/
data class PlayingTrack(
    var artist: String? = null,
    var title: String? = null,
    var releaseName: String? = null,
    var timestamp: Long = 0,
    var duration: Long = 0,
    var pkgName: String? = null,
    var playingNowSubmitted: Boolean = false,
    var submitted: Boolean = false,
    var playbackState: Int? = null
) {
    val timestampSeconds: Long
        get() = timestamp / 1000
    
    /** This means there's no track playing.*/
    fun isNothing(): Boolean = artist == null && title == null
    
    fun isSubmitted(): Boolean = submitted
    
    fun isPlaying(): Boolean = playbackState == PlaybackState.STATE_PLAYING
    
    /** Determines if this track is a notification scrobbled track or not.*/
    fun isNotificationTrack(): Boolean = artist != null && title != null && duration == 0L
    
    fun isCallbackTrack(): Boolean = !isNotificationTrack()
    
    fun hasNecessaryMetadata(): Boolean =
        artist != null
            && title != null
            && timestamp != 0L
            && duration != 0L
            && pkgName != null
    
    fun reset() {
        artist = null
        title = null
        releaseName = null
        timestamp = 0
        duration = 0
        pkgName = null
        playingNowSubmitted = false
        submitted = false
    }
    
    
    fun isSimilarTo(other: Any): Boolean {
        return when (other) {
            is PlayingTrack ->  artist == other.artist
                    && title == other.title
                    && pkgName == other.pkgName
            is MediaMetadata -> artist == other.extractArtist()
                    && title == other.extractTitle()
            else -> {
                throw IllegalStateException(
                    "${other.javaClass.simpleName} is not supported for use in this function."
                )
            }
        }
    }
}