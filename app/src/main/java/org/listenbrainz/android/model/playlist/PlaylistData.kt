package org.listenbrainz.android.model.playlist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PlaylistData(
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
    val track: List<PlaylistTrack> = listOf(),
    val coverArt: String? = null
)