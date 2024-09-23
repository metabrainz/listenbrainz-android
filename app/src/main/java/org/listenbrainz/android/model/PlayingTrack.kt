package org.listenbrainz.android.model

import android.media.MediaMetadata
import org.listenbrainz.android.util.ListenSubmissionState.Companion.DEFAULT_DURATION
import org.listenbrainz.android.util.ListenSubmissionState.Companion.extractArtist
import org.listenbrainz.android.util.ListenSubmissionState.Companion.extractDuration
import org.listenbrainz.android.util.ListenSubmissionState.Companion.extractReleaseName
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
    
    /** Determines if this track is a notification scrobbled track or not.*/
    fun isNotificationTrack(): Boolean = artist != null && title != null && duration == 0L
    
    fun isCallbackTrack(): Boolean = !isNotificationTrack()
    
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
    
    /** Similar means that the basic metadata matches. A song if replayed will be similar.*/
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
    
    /** Determines if *this* track is outdated in comparison to [newTrack].
     *
     * Covers case where a track being replayed is similar but is actually outdated.*/
    fun isOutdated(newTrack: PlayingTrack): Boolean {
        return when {
            this.isSimilarTo(newTrack) -> {
                // If track is similar.
                // If the difference in timestamps is greater than duration of the whole track, it
                // means our track is outdated for sure.
                when {
                    newTrack.duration != 0L -> {
                        // New track is callback track, duration data available.
                        newTrack.timestamp - timestamp >= newTrack.duration
                    }
                    this.duration != 0L -> {
                        // New track is callback track, duration data available.
                        newTrack.timestamp - timestamp >= this.duration
                    }
                    submitted -> true
                    else -> {
                        // New track and old track both are notification track. So, no duration
                        // available. We use default duration.
                        newTrack.timestamp - timestamp >= DEFAULT_DURATION
                    }
                }
            }
            else -> true
        }
    }
    
    companion object {
        fun MediaMetadata.toPlayingTrack(pkgName: String): PlayingTrack {
            return PlayingTrack(
                timestamp = System.currentTimeMillis(),
                artist = extractArtist(),
                title = extractTitle(),
                duration = extractDuration(),
                releaseName = extractReleaseName(),
                pkgName = pkgName,
                playingNowSubmitted = false
            )
        }
    }
}