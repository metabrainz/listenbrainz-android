package org.listenbrainz.android.data.sources.api.entities.listens

import org.listenbrainz.android.data.sources.api.entities.CoverArt

data class Listen(
    val inserted_at: String,
    val listened_at: Int,
    val recording_msid: String,
    val track_metadata: TrackMetadata,
    val user_name: String,
    var coverArt: CoverArt? = null
)