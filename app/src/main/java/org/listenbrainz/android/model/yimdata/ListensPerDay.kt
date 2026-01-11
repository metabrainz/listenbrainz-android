package org.listenbrainz.android.model.yimdata

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListensPerDay (
    @SerialName("from_ts") var fromTs: Int = 0,
    @SerialName("listen_count") var listenCount: Int = 0,
    @SerialName("time_range") var timeRange: String = "",
    @SerialName("to_ts") var toTs: Int = 0
)