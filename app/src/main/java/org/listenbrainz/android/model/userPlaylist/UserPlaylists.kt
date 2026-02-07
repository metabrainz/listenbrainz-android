package org.listenbrainz.android.model.userPlaylist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserPlaylists(
    @SerialName("playlist")
    val playlist: UserPlaylist = UserPlaylist()
){
    fun getPlaylistMBID(): String? {
        val url = playlist.identifier
        val regex = """playlist/([a-f0-9\-]+)""".toRegex()
        val matchResult = url?.let { regex.find(it) }
        return matchResult?.groupValues?.get(1)
    }
}