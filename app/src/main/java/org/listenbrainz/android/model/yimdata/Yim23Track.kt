package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Yim23Track (
    @SerialName("creator") var creator: String = "",
    @SerialName("identifier") var identifier: String = "",
    @SerialName("extension") var extension: Yim23TrackExtension = Yim23TrackExtension(),
    @SerialName("title") var title: String = ""
)
