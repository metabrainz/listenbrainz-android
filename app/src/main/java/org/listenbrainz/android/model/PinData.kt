package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PinData(
    @SerialName("pinned_recording") val pinnedRecording: PinnedRecording = PinnedRecording()
)