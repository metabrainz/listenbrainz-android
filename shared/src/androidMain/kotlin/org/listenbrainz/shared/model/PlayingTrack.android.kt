package org.listenbrainz.shared.model

import android.media.MediaMetadata
import org.listenbrainz.shared.util.extractArtist
import org.listenbrainz.shared.util.extractTitle

actual fun PlayingTrack.toSimilarTo(other: Any): Boolean {
    return when (other) {
        is MediaMetadata -> artist == other.extractArtist()
                && title == other.extractTitle()
        else -> {
            throw IllegalStateException(
                "${other.javaClass.simpleName} is not supported for use in this function."
            )
        }
    }
}