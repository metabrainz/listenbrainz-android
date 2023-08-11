package org.listenbrainz.android.model

data class MbidMapping(
    val artist_mbids: List<String>,
    val artists: List<FeedListenArtist>? = null,
    val caa_id: Long? = null,
    val caa_release_mbid: String? = null,
    val recording_mbid: String? = null,
    val recording_name: String,
    val release_mbid: String? = null
)