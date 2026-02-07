package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class ListeningActivity(
    @SerialName("from_ts") val fromTs: Int? = null,
    @SerialName("listen_count") val listenCount: Int? = null,
    @SerialName("time_range") val timeRange: String? = null,
    @SerialName("to_ts") val toTs: Int? = null,
    @Transient var componentIndex: Int? = null,
    @Transient var color: Int? = null,
)