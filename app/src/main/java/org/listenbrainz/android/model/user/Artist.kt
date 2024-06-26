package org.listenbrainz.android.model.user

data class Artist(
    val artist_mbid: String,
    val artist_name: String,
    val listen_count: Int
)