package org.listenbrainz.android.model.recordingSearch


import com.google.gson.annotations.SerializedName

data class RecordingSearchPayload(
    @SerializedName("count")
    val count: Int? = null,
    @SerializedName("created")
    val created: String? = null,
    @SerializedName("offset")
    val offset: Int? = null,
    @SerializedName("recordings")
    val recordings: List<RecordingData> = emptyList()
)