package org.listenbrainz.android.model.album

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.artist.Listeners

data class ListeningStats(
    @SerializedName("artist_mbids") val artistMbids: List<String?>? = null,
    @SerializedName("artist_name") val artistName: String? = null,
    @SerializedName("caa_id") val caaId: Long? = null,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = null,
    @SerializedName("from_ts") val fromTs: Int? = null,
    @SerializedName("last_updated") val lastUpdated: Int? = null,
    val listeners: List<Listeners?>? = null,
    @SerializedName("release_group_mbid") val releaseGroupMbid: String? = null,
    @SerializedName("release_group_name")  val releaseGroupName: String? = null,
    @SerializedName("stats_range") val statsRange: String? = null,
    @SerializedName("to_ts") val toTs: Int? = null,
    @SerializedName("total_listen_count") val totalListenCount: Int? = null,
    @SerializedName("total_user_count") val totalUserCount: Int? = null
)