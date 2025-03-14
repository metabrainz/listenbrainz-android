package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class ReleaseTrack(
    @SerializedName("id")
    val id: String?,
    @SerializedName("length")
    val length: Int?,
    @SerializedName("number")
    val number: String?,
    @SerializedName("title")
    val title: String?
)