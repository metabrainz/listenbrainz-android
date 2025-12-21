package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Track (
    @SerialName("creator") var creator: String = "",
    @SerialName("identifier") var identifier: String = "",
    @SerialName("title") var title: String = ""
)
