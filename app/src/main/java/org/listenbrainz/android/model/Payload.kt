package org.listenbrainz.android.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Payload(
    val count: Int = 0,
    @SerialName("latest_listen_ts") val latestListenTs: Int? = null,
    val listens: List<Listen> = emptyList(),
    @SerialName("user_id") val userId: String? = null
)