package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingArtist(
    @SerializedName("aliases")
    val aliases: List<Aliase?> = emptyList(),
    @SerializedName("disambiguation")
    val disambiguation: String? = null,
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("sort-name")
    val sortName: String? = null
)