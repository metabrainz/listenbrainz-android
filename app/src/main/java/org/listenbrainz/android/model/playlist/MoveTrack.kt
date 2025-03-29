package org.listenbrainz.android.model.playlist

data class MoveTrack(
    val mbid: String,
    val from: Int,
    val to: Int,
    val count: Int
)
