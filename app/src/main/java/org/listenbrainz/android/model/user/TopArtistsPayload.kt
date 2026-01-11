package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopArtistsPayload(
    val artists: List<Artist> = emptyList(),
    val count: Int = 0,
    @SerialName("from_ts") val fromTs: Int = 0,
    @SerialName("last_updated") val lastUpdated: Int = 0,
    val offset: Int = 0,
    val range: String = "",
    @SerialName("to_ts") val toTs: Int = 0,
    @SerialName("total_artist_count") val totalArtistCount: Int = 0,
    @SerialName("user_id") val userId: String = ""
)