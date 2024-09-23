package org.listenbrainz.android.model.artist

import com.google.gson.annotations.SerializedName
import org.listenbrainz.android.model.feed.FeedListenArtist

data class ReleaseGroup(
    @SerializedName("artist_credit_name") val artistCreditName: String? = null,
    val artists: List<FeedListenArtist>? = listOf(),
    @SerializedName("caa_id") val caaId: Long? = null,
    @SerializedName("caa_release_mbid") val caaReleaseMbid: String? = null,
    val date: String? = null,
    val mbid: String? = null,
    val name: String? = null,
    @SerializedName("total_listen_count") val totalListenCount: Int? = null,
    @SerializedName("total_user_count") val totalUserCount: Int? = null,
    val type: String? = null
)