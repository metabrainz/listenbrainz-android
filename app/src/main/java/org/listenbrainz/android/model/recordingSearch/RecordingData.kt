package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingData(
    @SerializedName("artist-credit")
    val artistCredit: List<ArtistCredit>?,
    @SerializedName("first-release-date")
    val firstReleaseDate: String?,
    @SerializedName("id")
    val id: String?,
    @SerializedName("length")
    val length: Int?,
    @SerializedName("releases")
    val releases: List<RecordingRelease>?,
    @SerializedName("score")
    val score: Int?,
    @SerializedName("tags")
    val tags: List<RecordingTag>?,
    @SerializedName("title")
    val title: String?,
    @SerializedName("video")
    val video: Any?
)