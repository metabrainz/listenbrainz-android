package org.listenbrainz.android.model

import com.google.gson.annotations.SerializedName

data class PinData(
    @SerializedName("pinned_recording") val pinnedRecording: PinnedRecording
)