package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseTrack(
    @SerialName("id")
    val id: String? = null,
    @SerialName("length")
    val length: Int? = null,
    @SerialName("number")
    val number: String? = null,
    @SerialName("title")
    val title: String? = null
)