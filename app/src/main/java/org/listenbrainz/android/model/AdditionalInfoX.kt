package org.listenbrainz.android.model

data class AdditionalInfoX(
    val artist_mbids: List<String>,
    val discnumber: Int,
    val duration_ms: Int,
    val isrc: String,
    val listening_from: String,
    val origin_url: String,
    val recording_mbid: String,
    val recording_msid: String,
    val release_artist_name: String,
    val release_artist_names: List<String>,
    val release_group_mbid: String,
    val release_mbid: String,
    val spotify_album_artist_ids: List<String>,
    val spotify_album_id: String,
    val spotify_artist_ids: List<String>,
    val spotify_id: String,
    val tags: Any,
    val track_mbid: Any,
    val tracknumber: String,
    val work_mbids: Any,
    val youtube_id: Any
)