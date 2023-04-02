package org.listenbrainz.android.model

data class AdditionalInfo(
    var artist_msid: String? = null,
    var artist_names: List<String> = listOf(),
    var discnumber: Int? = null,
    var duration_ms: Int? = null,
    var isrc: String? = null,
    var listening_from: String? = null,
    var recording_msid: String? = null,
    var release_artist_name: String? = null,
    var release_artist_names: List<String> = listOf(),
    var release_msid: String? = null,
    var release_mbid: String? = null,
    var spotify_album_artist_ids: List<String> = listOf(),
    var spotify_album_id: String? = null,
    var spotify_artist_ids: List<String> = listOf(),
    var spotify_id: String? = null,
    var tracknumber: Int? = null,
    
    // Used for listen submission body
    var media_player: String? = null,
    var submission_client: String? = null,
    var submission_client_version: String? = null
)