package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class SimilarArtists (
    @SerializedName("artists")
    val artists: List<SimilarArtist> = listOf(),
    @SerializedName("topRecordingColor")
    val topRecordingColor : ReleaseColor? = null,
    @SerializedName("topReleaseGroupColor")
    val topReleaseGroupColor : ReleaseColor? = null
)
