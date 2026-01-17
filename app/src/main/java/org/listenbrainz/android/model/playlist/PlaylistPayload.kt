package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistPayload(
    @SerialName("playlist")
    val playlist: PlaylistData = PlaylistData()
)