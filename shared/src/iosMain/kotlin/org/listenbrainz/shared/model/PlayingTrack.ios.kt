package org.listenbrainz.shared.model

actual fun PlayingTrack.toSimilarTo(other: Any): Boolean {
    throw IllegalStateException("Unsupported type for similarity comparison.")
}