package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingSearchPayload(
    @SerializedName("count")
    val count: Int?,
    @SerializedName("created")
    val created: String?,
    @SerializedName("offset")
    val offset: Int?,
    @SerializedName("recordings")
    val recordings: List<RecordingData>?
)