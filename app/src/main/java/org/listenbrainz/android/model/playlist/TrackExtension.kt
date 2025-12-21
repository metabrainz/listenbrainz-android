package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TrackExtension(
    @SerialName("https://musicbrainz.org/doc/jspf#track")
    val trackExtensionData: TrackExtensionData = TrackExtensionData()
)