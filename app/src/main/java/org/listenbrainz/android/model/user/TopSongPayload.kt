package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class TopSongPayload(
                                             val count: Int? = 0,
    @SerializedName("from_ts")               val fromTs: Int? = 0,
    @SerializedName("last_updated")          val lastUpdated: Int? = 0,
                                             val offset: Int? = 0,
                                             val range: String? = "",
                                             val recordings: List<Recording>? = listOf(),
    @SerializedName("to_ts")                 val toTs: Int? = 0,
    @SerializedName("total_recording_count") val totalRecordingCount: Int? = 0,
    @SerializedName("user_id")               val userId: String? = ""
)