package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Yim23TrackExtension (
    @SerialName("https://musicbrainz.org/doc/jspf#track") var extensionData: Yim23TrackExtensionData = Yim23TrackExtensionData(),
)
