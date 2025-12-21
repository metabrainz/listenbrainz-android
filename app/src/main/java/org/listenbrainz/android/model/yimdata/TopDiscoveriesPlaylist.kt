package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopDiscoveriesPlaylist(
    @SerialName("identifier") var identifier: String = "",
    @SerialName("title") var title: String = "",
    @SerialName("track") var tracksList: ArrayList<Track> = arrayListOf()
)
