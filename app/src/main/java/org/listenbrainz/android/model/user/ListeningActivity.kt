package org.listenbrainz.android.model.user

import com.google.gson.annotations.SerializedName

data class ListeningActivity(
    @SerializedName("from_ts")      val fromTs: Int? = null,
    @SerializedName("listen_count") val listenCount: Int? = null,
    @SerializedName("time_range")   val timeRange: String? = null,
    @SerializedName("to_ts")        val toTs: Int? = null,
                                    var componentIndex: Int? = null,
                                    var color: Int? = null,
)