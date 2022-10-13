package org.listenbrainz.android.data.sources.api.entities

data class RecordingItem(
    val artist_credit_arg: String,
    val artist_credit_id: Int,
    val artist_credit_name: String,
    val artist_mbids: String,
    val index: Int,
    val match_type: Int,
    val recording_arg: String,
    val recording_mbid: String,
    val recording_name: String,
    val release_mbid: String,
    val release_name: String,
    val year: Int
)