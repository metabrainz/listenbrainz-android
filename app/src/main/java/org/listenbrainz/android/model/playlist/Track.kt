package org.listenbrainz.android.model.playlist


import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("album")
    val album: String,
    @SerializedName("creator")
    val creator: String,
    @SerializedName("duration")
    val duration: Int,
    @SerializedName("extension")
    val extension: TrackExtension,
    @SerializedName("identifier")
    val identifier: List<String>,
    @SerializedName("title")
    val title: String
)