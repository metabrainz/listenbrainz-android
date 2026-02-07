package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SimilarArtists (
    @SerialName("artists")
    val artists: List<SimilarArtist> = listOf(),
    @SerialName("topRecordingColor")
    val topRecordingColor : ReleaseColor? = null,
    @SerialName("topReleaseGroupColor")
    val topReleaseGroupColor : ReleaseColor? = null
)
