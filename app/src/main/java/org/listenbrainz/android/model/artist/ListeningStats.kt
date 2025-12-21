package org.listenbrainz.android.model.artist

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ListeningStats(
    @SerialName("artist_mbid") val artistMbid: String? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("from_ts") val fromTs: Int? = null,
    @SerialName("last_updated") val lastUpdated: Int? = null,
    val listeners: List<Listeners?>? = null,
    @SerialName("stats_range") val statsRange: String? = null,
    @SerialName("to_ts") val toTs: Int? = null,
    @SerialName("total_listen_count") val totalListenCount: Int? = null,
    @SerialName("total_user_count") val totalUserCount: Int? = null
)