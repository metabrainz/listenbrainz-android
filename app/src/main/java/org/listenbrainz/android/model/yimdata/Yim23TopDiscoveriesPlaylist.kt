package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Yim23TopDiscoveriesPlaylist (
    @SerialName("creator") var creator: String = "",
    @SerialName("identifier") var identifier: String = "",
    @SerialName("track") var tracks: List<Yim23Track> = listOf()
)
