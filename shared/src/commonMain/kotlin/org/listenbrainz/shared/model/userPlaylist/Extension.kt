package org.listenbrainz.shared.model.userPlaylist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Extension(
    @SerialName("https://musicbrainz.org/doc/jspf#playlist")
    val createdForYouExtensionData: UserPlaylistExtensionData = UserPlaylistExtensionData()
)