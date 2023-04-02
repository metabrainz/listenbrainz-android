package org.listenbrainz.android.model

data class MbidMapping(
    val artist_mbids: List<String>,
    val caa_id: Long? = null,
    val caa_release_mbid: String? = null,
    val recording_mbid: String,
    val release_mbid: String? = null
)