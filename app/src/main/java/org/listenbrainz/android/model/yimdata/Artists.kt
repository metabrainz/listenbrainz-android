package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Artists (
    @SerialName("artist_mbid") var artistMbid: String = "",
    @SerialName("artist_name") var artistName: String = "",
    @SerialName("listen_count") var listenCount: Int = 0
)