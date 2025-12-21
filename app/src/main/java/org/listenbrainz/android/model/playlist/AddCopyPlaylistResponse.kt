package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AddCopyPlaylistResponse(
    @SerialName("playlist_mbid")
    val playlistMbid: String = "",
    @SerialName("status")
    val status: String = ""
)

@Serializable
data class EditPlaylistResponse(
    @SerialName("status")
    val status: String = ""
)