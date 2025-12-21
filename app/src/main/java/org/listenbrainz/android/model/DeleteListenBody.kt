package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

class DeleteListenBody(
    @SerializedName("listened_at")
    val listenedAt: Long,

    @SerializedName("recording_msid")
    val recordingMsid: String
)