package org.listenbrainz.android.model

data class Metadata(
    val blurb_content: String,
    val created: Int,
    val entity_id: String,
    val entity_name: String,
    val entity_type: String,
    val inserted_at: Int,
    val listened_at: Int,
    val listened_at_iso: Any,
    val message: String,
    val playing_now: Any,
    val rating: Int,
    val relationship_type: String,
    val review_mbid: String,
    val text: String,
    val track_metadata: TrackMetadata,
    val user_name: String,
    val user_name_0: String,
    val user_name_1: String
)