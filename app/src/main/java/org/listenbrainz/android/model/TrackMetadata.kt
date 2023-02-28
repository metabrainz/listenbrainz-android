package org.listenbrainz.android.model

data class TrackMetadata(
    val additional_info: AdditionalInfo?,
    val artist_name: String,
    val mbid_mapping: MbidMapping?,
    val release_name: String?,
    val track_name: String
)