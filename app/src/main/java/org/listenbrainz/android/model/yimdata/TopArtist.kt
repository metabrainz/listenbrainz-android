package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopArtist (
    @SerialName("artist_mbids") var artistMbids: ArrayList<String>? = arrayListOf(),  // Can be empty
    @SerialName("artist_name") var artistName: String? = null,
    @SerialName("listen_count") var listenCount: Int? = null
)