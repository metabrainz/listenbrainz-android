package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class PlaylistTrack(
    @SerializedName("album")
    val album: String? = null,
    @SerializedName("creator")
    val creator: String? = null,
    @SerializedName("duration")
    val duration: Int? = null,
    @SerializedName("extension")
    val extension: TrackExtension = TrackExtension(),
    @SerializedName("identifier")
    val identifier: List<String> = listOf(),
    @SerializedName("title")
    val title: String? = null
)