package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Extension(
    @SerialName("https://musicbrainz.org/doc/jspf#playlist")
    val playlistExtensionData: PlaylistExtensionData = PlaylistExtensionData()
)