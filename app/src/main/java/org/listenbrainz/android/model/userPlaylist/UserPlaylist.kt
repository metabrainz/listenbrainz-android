package org.listenbrainz.android.model.userPlaylist


import com.google.gson.annotations.SerializedName

data class UserPlaylist(
    @SerializedName("annotation")
    val annotation: String? = null,
    @SerializedName("creator")
    val creator: String? = null,
    @SerializedName("date")
    val date: String? = null,
    @SerializedName("extension")
    val extension: Extension = Extension(),
    @SerializedName("identifier")
    val identifier: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("track")
    val track: List<Any> = listOf()
){
    // Get the MBID of the playlist
    fun getPlaylistMBID(): String? {
        // Regex to extract the MBID from the identifier
        val regex = """playlist/([a-f0-9\-]+)""".toRegex()
        val matchResult = identifier?.let { regex.find(it) }
        return matchResult?.groupValues?.get(1)
    }
}