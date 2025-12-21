package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ArtistMap (
    @SerialName("artist_count") var artistCount: Int = 0,
    @SerialName("artists") var artists: ArrayList<Artists> = arrayListOf(),
    @SerialName("country") var country: String? = null,
    @SerialName("listen_count") var listenCount: Int = 0
)
