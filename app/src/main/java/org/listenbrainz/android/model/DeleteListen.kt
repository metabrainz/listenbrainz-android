package org.listenbrainz.android.model

import kotlinx.serialization.SerialName

data class DeleteListen(
    @SerialName("listened_at") val listenedAt: Long,
    @SerialName("recording_msid") val recordingMsid: String
)