package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingArtist(
    @SerializedName("aliases")
    val aliases: List<Aliase?>?,
    @SerializedName("disambiguation")
    val disambiguation: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("sort-name")
    val sortName: String?
)