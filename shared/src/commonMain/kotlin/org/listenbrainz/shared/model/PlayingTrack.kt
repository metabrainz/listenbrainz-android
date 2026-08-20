package org.listenbrainz.shared.model

import kotlinx.serialization.Serializable
import org.listenbrainz.shared.util.ListenSubmissionStateConstants.DEFAULT_DURATION

/** Track metadata class for Listen service.*/
@Serializable
data class PlayingTrack(
    var artist: String? = null,
    var title: String? = null,
    var releaseName: String? = null,
    var timestamp: Long = 0,
    var duration: Long = 0,
    var pkgName: String? = null,
    var playingNowSubmitted: Boolean = false,
    var submitted: Boolean = false,
) {
    val timestampSeconds: Long
        get() = timestamp / 1000

    val id: String
        get() = "$title - $artist - $pkgName"

    val isValid get() = title != null && artist != null && timestamp != 0L

    /** This means there's no track playing.*/
    fun isNothing(): Boolean = artist == null && title == null

    fun isSubmitted(): Boolean = submitted

    fun isDurationAbsent(): Boolean = duration <= 0L

    fun isDurationPresent(): Boolean = !isDurationAbsent()

    /** Similar means that the basic metadata matches. A song if replayed will be similar.*/
    fun isSimilarTo(other: Any): Boolean {
        return when (other) {
            is PlayingTrack ->  artist == other.artist
                    && title == other.title
                    && pkgName == other.pkgName
            else -> {
                this.toSimilarTo(other)
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
                        newTrack.timestamp - timestamp >= newTrack.duration
                    }
                    this.duration != 0L -> {
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
        val Nothing get() = PlayingTrack()

    }
}

expect fun PlayingTrack.toSimilarTo(other: Any): Boolean