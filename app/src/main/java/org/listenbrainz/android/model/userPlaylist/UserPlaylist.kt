package org.listenbrainz.android.model.userPlaylist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class UserPlaylist(
    @SerialName("annotation")
    val annotation: String? = null,
    @SerialName("creator")
    val creator: String? = null,
    @SerialName("date")
    val date: String? = null,
    @SerialName("extension")
    val extension: Extension = Extension(),
    @SerialName("identifier")
    val identifier: String? = null,
    @SerialName("title")
    val title: String? = null,
    @SerialName("track")
    val track: List<JsonElement> = listOf(),
    val coverArt: String? = null
){
    // Get the MBID of the playlist
    fun getPlaylistMBID(): String? {
        // Regex to extract the MBID from the identifier
        val regex = """playlist/([a-f0-9\-]+)""".toRegex()
        val matchResult = identifier?.let { regex.find(it) }
        return matchResult?.groupValues?.get(1)
    }
}