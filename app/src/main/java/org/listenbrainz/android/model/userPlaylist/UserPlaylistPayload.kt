package org.listenbrainz.android.model.userPlaylist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPlaylistPayload(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("offset")
    val offset: Int? = null,
    @SerialName("playlist_count")
    val playlistCount: Int? = null,
    @SerialName("playlists")
    val playlists: List<UserPlaylists> = listOf()
)