package org.listenbrainz.android.data.sources.api.entities.listens

data class AdditionalInfo(
    val artist_msid: String,
    val artist_names: List<String>,
    val discnumber: Int,
    val duration_ms: Int,
    val isrc: String,
    val listening_from: String,
    val recording_msid: String,
    val release_artist_name: String,
    val release_artist_names: List<String>,
    val release_msid: String,
    val release_mbid: String? = null,
    val spotify_album_artist_ids: List<String>,
    val spotify_album_id: String,
    val spotify_artist_ids: List<String>,
    val spotify_id: String?,
    val tracknumber: Int
)