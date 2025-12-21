package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingTag(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("name")
    val name: String? = null
)