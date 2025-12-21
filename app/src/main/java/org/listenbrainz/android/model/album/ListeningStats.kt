package org.listenbrainz.android.model.album

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.listenbrainz.android.model.artist.Listeners

@Serializable
data class ListeningStats(
    @SerialName("artist_mbids") val artistMbids: List<String?>? = null,
    @SerialName("artist_name") val artistName: String? = null,
    @SerialName("caa_id") val caaId: Long? = null,
    @SerialName("caa_release_mbid") val caaReleaseMbid: String? = null,
    @SerialName("from_ts") val fromTs: Int? = null,
    @SerialName("last_updated") val lastUpdated: Int? = null,
    val listeners: List<Listeners?>? = null,
    @SerialName("release_group_mbid") val releaseGroupMbid: String? = null,
    @SerialName("release_group_name") val releaseGroupName: String? = null,
    @SerialName("stats_range") val statsRange: String? = null,
    @SerialName("to_ts") val toTs: Int? = null,
    @SerialName("total_listen_count") val totalListenCount: Int? = null,
    @SerialName("total_user_count") val totalUserCount: Int? = null
)