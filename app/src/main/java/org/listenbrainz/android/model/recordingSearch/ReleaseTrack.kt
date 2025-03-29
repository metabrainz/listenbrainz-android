package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class ReleaseTrack(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("length")
    val length: Int? = null,
    @SerializedName("number")
    val number: String? = null,
    @SerializedName("title")
    val title: String? = null
)