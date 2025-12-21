package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReleaseEvent(
    @SerialName("area")
    val area: Area? = null,
    @SerialName("date")
    val date: String? = null
)