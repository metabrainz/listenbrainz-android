package org.listenbrainz.android.model.createdForYou


import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName("annotation")
    val `annotation`: String,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("date")
    val date: String,
    @SerializedName("extension")
    val extension: Extension,
    @SerializedName("identifier")
    val identifier: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("track")
    val track: List<Any>
)