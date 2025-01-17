package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class Playlist(
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
    val track: List<Track> = listOf()
)