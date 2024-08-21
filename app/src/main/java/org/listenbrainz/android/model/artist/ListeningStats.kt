package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName

data class ListeningStats(
    @SerializedName("artist_mbid") val artistMbid: String? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("from_ts") val fromTs: Int? = null,
    @SerializedName("last_updated") val lastUpdated: Int? = null,
    val listeners: List<Listeners?>? = null,
    @SerializedName("stats_range") val statsRange: String? = null,
    @SerializedName("to_ts") val toTs: Int? = null,
    @SerializedName("total_listen_count") val totalListenCount: Int? = null,
    @SerializedName("total_user_count") val totalUserCount: Int? = null
)