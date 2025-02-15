package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class CreatedForYouPlaylists(
    @SerializedName("playlist")
    val playlist: CreatedForYouPlaylist = CreatedForYouPlaylist()
){
    fun getPlaylistMBID(): String? {
        val url = playlist.identifier
        val regex = """playlist/([a-f0-9\-]+)""".toRegex()
        val matchResult = url?.let { regex.find(it) }
        return matchResult?.groupValues?.get(1)
    }
}