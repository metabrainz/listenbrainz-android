package org.listenbrainz.android.model.user

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TopAlbumsPayload(
    val count: Int? = 0,
    @SerialName("from_ts") val fromTs: Int? = 0,
    @SerialName("last_updated") val lastUpdated: Int? = 0,
    @SerialName("offset") val offset: Int? = 0,
    val range: String? = "",
    val releases: List<Release>? = listOf(),
    @SerialName("to_ts") val toTs: Int? = 0,
    @SerialName("total_release_count") val totalReleaseCount: Int? = 0,
    @SerialName("user_id") val userId: String? = ""
)