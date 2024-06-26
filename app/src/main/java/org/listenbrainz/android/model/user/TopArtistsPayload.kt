package org.listenbrainz.android.model.user

data class TopArtistsPayload(
    val artists: List<Artist>,
    val count: Int,
    val from_ts: Int,
    val last_updated: Int,
    val offset: Int,
    val range: String,
    val to_ts: Int,
    val total_artist_count: Int,
    val user_id: String
)