package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artist(
    @SerialName("artist_mbid") val artistMbid: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("listen_count") val listenCount: Int? = null,
)