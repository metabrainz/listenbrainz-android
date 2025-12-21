package org.listenbrainz.android.model.recordingSearch

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingSearchPayload(
    @SerialName("count")
    val count: Int? = null,
    @SerialName("created")
    val created: String? = null,
    @SerialName("offset")
    val offset: Int? = null,
    @SerialName("recordings")
    val recordings: List<RecordingData> = emptyList()
)